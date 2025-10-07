package knapsack

import java.util.*

class KnapsackGA {
    private val r = Random()

    private var population = arrayOfNulls<Individual>(POP_SIZE)

    init {
        populateInitialPopulationRandomly()
    }

    private fun populateInitialPopulationRandomly() {
        /* Creates a new population, made of random individuals */
        for (i in 0 until POP_SIZE) {
            population[i] = Individual.createRandom(r)
        }
    }

    fun run() {
        for (generation in 0 until N_GENERATIONS) {
            // Step1 - Calculate Fitness

            for (i in 0 until POP_SIZE) {
                population[i]!!.measureFitness()
            }

            // Step2 - Print the best individual so far.
            val best = bestOfPopulation()
            println(
                "Best at generation " + generation + " is " + best + " with "
                        + best!!.fitness
            )

            // Step3 - Find parents to mate (cross-over)
            val newPopulation = arrayOfNulls<Individual>(POP_SIZE)
            newPopulation[0] = best // The best individual remains

            for (i in 1 until POP_SIZE) {
                // We select two parents, using a tournament.
                val parent1 = tournament(TOURNAMENT_SIZE, r)
                val parent2 = tournament(TOURNAMENT_SIZE, r)

                newPopulation[i] = parent1!!.crossoverWith(parent2!!, r)
            }

            // Step4 - Mutate
            for (i in 1 until POP_SIZE) {
                if (r.nextDouble() < PROB_MUTATION) {
                    newPopulation[i]!!.mutate(r)
                }
            }
            population = newPopulation
        }
    }

    private fun tournament(tournamentSize: Int, r: Random): Individual? {
        /*
		 * In each tournament, we select tournamentSize individuals at random, and we
		 * keep the best of those.
		 */
        var best = population[r.nextInt(POP_SIZE)]
        for (i in 0 until tournamentSize) {
            val other = population[r.nextInt(POP_SIZE)]
            if (other!!.fitness > best!!.fitness) {
                best = other
            }
        }
        return best
    }

    private fun bestOfPopulation(): Individual? {
        /*
		 * Returns the best individual of the population.
		 */
        var best = population[0]
        for (other in population) {
            if (other!!.fitness > best!!.fitness) {
                best = other
            }
        }
        return best
    }

    companion object {
        private const val N_GENERATIONS = 500
        private const val POP_SIZE = 100000
        private const val PROB_MUTATION = 0.5
        private const val TOURNAMENT_SIZE = 3
    }
}