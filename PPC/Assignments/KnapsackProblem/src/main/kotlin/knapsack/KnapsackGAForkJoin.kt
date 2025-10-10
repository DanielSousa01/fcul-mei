package knapsack

import Individual
import knapsack.KnapsackGA.Companion.N_GENERATIONS
import knapsack.KnapsackGA.Companion.POP_SIZE
import knapsack.KnapsackGA.Companion.PROB_MUTATION
import knapsack.KnapsackGA.Companion.THRESHOLD
import java.util.*
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask.invokeAll
import java.util.concurrent.RecursiveAction
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicReference

class KnapsackGAForkJoin(override val silent: Boolean = false) : KnapsackGA {
    private var population: Array<Individual> = Array(POP_SIZE)
    { Individual.createRandom(ThreadLocalRandom.current()) }

    private val maxThreads = Runtime.getRuntime().availableProcessors()
    private lateinit var forkJoinPool: ForkJoinPool

    override fun run(): Individual {
        forkJoinPool = ForkJoinPool(maxThreads)
        try {
            for (generation in 0 until N_GENERATIONS) {
                // Step1 - Calculate Fitness
                calculateFitness()

                // Step2 - Print the best individual so far.
                val best = bestOfPopulation()
                if (!silent)
                    println("${this::class.simpleName}: Best at generation $generation is $best with ${best.fitness}")

                // Step3 - Find parents to mate (cross-over)
                val newPopulation = calculateBestPopulation(best)

                // Step4 - Mutate
                mutate(newPopulation)
            }

            return population.first()
        } finally {
            forkJoinPool.shutdown()
        }
    }

    private fun calculateFitness() {
        val action = object : RecursiveAction() {
            override fun compute() {
                computeRange(0, POP_SIZE) { start, end ->
                    for (i in start until end) {
                        population[i].measureFitness()
                    }
                }
            }
        }
        forkJoinPool.invoke(action)
    }

    private fun bestOfPopulation(): Individual {
        val best: AtomicReference<Individual> = AtomicReference(population[0])

        val action = object : RecursiveAction() {
            override fun compute() {
                computeRange(0, POP_SIZE) { start, end ->
                    var localBest = best.get()
                    for (i in start until end) {
                        val other = population[i]
                        if (other.fitness > localBest.fitness) {
                            localBest = other
                        }
                    }

                    var currentBest = best.get()
                    while (localBest.fitness > currentBest.fitness) {
                        if (best.compareAndSet(currentBest, localBest)) {
                            break
                        }
                        currentBest = best.get()
                    }
                }
            }
        }

        forkJoinPool.invoke(action)
        return best.get()
    }

    private fun calculateBestPopulation(best: Individual): Array<Individual> {
        val newPopulation = Array(POP_SIZE) { best }

        val action = object : RecursiveAction() {
            override fun compute() {
                computeRange(1, POP_SIZE) { start, end ->
                    val r = ThreadLocalRandom.current()
                    for (i in start until end) {
                        val parent1 = tournament(r, population)
                        val parent2 = tournament(r, population)

                        newPopulation[i] = parent1.crossoverWith(parent2, r)
                    }
                }
            }
        }
        forkJoinPool.invoke(action)

        return newPopulation
    }

    private fun mutate(newPopulation: Array<Individual>) {
        val action = object : RecursiveAction() {
            override fun compute() {
                computeRange(1, POP_SIZE) { start, end ->
                    val r = ThreadLocalRandom.current()
                    for (i in start until end) {
                        if (r.nextDouble() < PROB_MUTATION) {
                            newPopulation[i].mutate(r)
                        }
                    }
                }
            }
        }

        forkJoinPool.invoke(action)

        population = newPopulation
    }

    private fun computeRange(start: Int, end: Int, action: (Int, Int) -> Unit) {
        if (end - start <= THRESHOLD) {
            action(start, end)
        } else {
            val mid = (start + end) / 2
            val left = object : RecursiveAction() {
                override fun compute() {
                    computeRange(start, mid, action)
                }
            }
            val right = object : RecursiveAction() {
                override fun compute() {
                    computeRange(mid, end, action)
                }
            }
            invokeAll(left, right)
        }
    }

}