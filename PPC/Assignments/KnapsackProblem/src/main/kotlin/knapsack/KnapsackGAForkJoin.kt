package knapsack

import Individual
import knapsack.KnapsackGA.Companion.N_GENERATIONS
import knapsack.KnapsackGA.Companion.POP_SIZE
import knapsack.KnapsackGA.Companion.PROB_MUTATION
import knapsack.KnapsackGA.Companion.THRESHOLD
import knapsack.KnapsackGA.Companion.TOURNAMENT_SIZE
import java.util.*
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask.invokeAll
import java.util.concurrent.RecursiveAction
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicReference

class KnapsackGAForkJoin(override val silent: Boolean = false) : KnapsackGA {
    private val r = Random()
    private var population = arrayOfNulls<Individual>(POP_SIZE)

    private val maxThreads = Runtime.getRuntime().availableProcessors()
    private val forkJoinPool = ForkJoinPool(maxThreads)

    init {
        populateInitialPopulationRandomly()
    }

    private fun populateInitialPopulationRandomly() {
        val action = object : RecursiveAction() {
            override fun compute() {
                computeRange(0, POP_SIZE) { start, end ->
                    val localRandom = ThreadLocalRandom.current()
                    for (i in start until end) {
                        population[i] = Individual.createRandom(localRandom)
                    }
                }

            }
        }

        forkJoinPool.invoke(action)
    }

    override fun run(): Individual {
        for (generation in 0 until N_GENERATIONS) {
            // Step1 - Calculate Fitness
            calculateFitness()

            // Step2 - Print the best individual so far.
            val best = bestOfPopulation()
            if (!silent)
                println("Best at generation $generation is $best with ${best.fitness}")

            // Step3 - Find parents to mate (cross-over)
            val newPopulation = calculateBestPopulation(best)

            // Step4 - Mutate
            mutate(newPopulation)
        }
        
        return population.first()!!
    }

    private fun calculateFitness() {
        val action = object : RecursiveAction() {
            override fun compute() {
                computeRange(0, POP_SIZE) { start, end ->
                    for (i in start until end) {
                        population[i]!!.measureFitness()
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
                        if (other!!.fitness > localBest.fitness) {
                            localBest = other
                        }
                    }
                    // TODO: Check if this synchronization is good or should use compareAndSet with a loop
                    // Update the global best if the local best is better
                    synchronized(best) {
                        if (localBest.fitness > best.get().fitness) {
                            best.set(localBest)
                        }
                    }
                }
            }
        }

        forkJoinPool.invoke(action)

        return best.get()
    }

    private fun calculateBestPopulation(best: Individual): Array<Individual?> {
        val newPopulation = arrayOfNulls<Individual?>(POP_SIZE)
        newPopulation[0] = best // The best individual remains

        val action = object : RecursiveAction() {
            override fun compute() {
                computeRange(1, POP_SIZE) { start, end ->
                    for (i in start until end) {
                        // We select two parents, using a tournament.
                        val parent1 = tournament(r)
                        val parent2 = tournament(r)

                        newPopulation[i] = parent1.crossoverWith(parent2, r)
                    }
                }
            }
        }
        forkJoinPool.invoke(action)

        return newPopulation
    }

    private fun tournament(r: Random): Individual {
        /*
		 * In each tournament, we select tournamentSize individuals at random, and we
		 * keep the best of those.
		 */
        var best = population[r.nextInt(POP_SIZE)]

        val action = object : RecursiveAction() {
            override fun compute() {
                computeRange(0, TOURNAMENT_SIZE) { start, end ->
                    for (i in start until end) {
                        val other = population[r.nextInt(POP_SIZE)]
                        if (other!!.fitness > best!!.fitness) {
                            best = other
                        }
                    }
                }
            }
        }
        forkJoinPool.invoke(action)

        return best!!
    }

    private fun mutate(newPopulation: Array<Individual?>) {
        val action = object : RecursiveAction() {
            override fun compute() {
                computeRange(1, POP_SIZE) { start, end ->
                    for (i in start until end) {
                        if (r.nextDouble() < PROB_MUTATION) {
                            newPopulation[i]!!.mutate(r)
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