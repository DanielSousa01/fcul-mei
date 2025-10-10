package knapsack

import Individual
import knapsack.KnapsackGA.Companion.N_GENERATIONS
import knapsack.KnapsackGA.Companion.POP_SIZE
import knapsack.KnapsackGA.Companion.PROB_MUTATION
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicReference

class KnapsackGAScatterGather(override val silent: Boolean = false) : KnapsackGA {
    private var population: Array<Individual> = Array(POP_SIZE)
    { Individual.createRandom(ThreadLocalRandom.current()) }

    private val maxThreads = Runtime.getRuntime().availableProcessors()
    private lateinit var threadPool: ExecutorService

    override fun run(): Individual {
        threadPool = Executors.newFixedThreadPool(maxThreads)
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
            threadPool.shutdown()
        }
    }

    private fun calculateFitness() {
        computeFutures(0, POP_SIZE) { start, end ->
            for (j in start until end) {
                population[j].measureFitness()
            }
        }
    }

    private fun bestOfPopulation(): Individual {
        val best: AtomicReference<Individual> = AtomicReference(population[0])

        computeFutures(0, POP_SIZE) { start, end ->
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

        return best.get()
    }

    private fun calculateBestPopulation(best: Individual): Array<Individual> {
        val newPopulation = Array(POP_SIZE) { best }

        computeFutures(1, POP_SIZE) { start, end ->
            val r = ThreadLocalRandom.current()
            for (i in start until end) {
                val parent1 = tournament(r, population)
                val parent2 = tournament(r, population)

                newPopulation[i] = parent1.crossoverWith(parent2, r)
            }
        }

        return newPopulation
    }

    private fun mutate(newPopulation: Array<Individual>) {
        computeFutures(1, POP_SIZE) { start, end ->
            val r = ThreadLocalRandom.current()
            for (i in start until end) {
                if (r.nextDouble() < PROB_MUTATION) {
                    newPopulation[i].mutate(r)
                }
            }
        }

        population = newPopulation
    }

    private fun computeFutures(startAt: Int, endAt: Int, action: (Int, Int) -> Unit) {
        val chunkSize = (endAt - startAt) / maxThreads
        val futures = mutableListOf<Future<*>>()

        for (i in 0 until maxThreads) {
            val start = startAt + i * chunkSize
            val end = if (i == maxThreads - 1) endAt else startAt + (i + 1) * chunkSize

            val future = threadPool.submit {
                action(start, end)
            }
            futures.add(future)
        }

        futures.forEach { it.get() }
    }
}