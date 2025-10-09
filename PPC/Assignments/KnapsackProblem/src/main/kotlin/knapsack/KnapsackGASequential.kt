package knapsack

import Individual
import knapsack.KnapsackGA.Companion.N_GENERATIONS
import knapsack.KnapsackGA.Companion.POP_SIZE
import knapsack.KnapsackGA.Companion.PROB_MUTATION
import knapsack.KnapsackGA.Companion.TOURNAMENT_SIZE
import java.util.*

class KnapsackGASequential(override val silent: Boolean = false) : KnapsackGA {
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

    override fun run(): Individual {
        for (generation in 0 until N_GENERATIONS) {
            // Step1 - Calculate Fitness
            for (i in 0 until POP_SIZE) {
                population[i]!!.measureFitness()
            }

            // Step2 - Print the best individual so far.
            val best = bestOfPopulation()
            if (!silent)
                println("Best at generation $generation is $best with ${best.fitness}")

            // Step3 - Find parents to mate (cross-over)
            val newPopulation = arrayOfNulls<Individual>(POP_SIZE)
            newPopulation[0] = best // The best individual remains

            for (i in 1 until POP_SIZE) {
                // We select two parents, using a tournament.
                val parent1 = tournament(r)
                val parent2 = tournament(r)

                newPopulation[i] = parent1.crossoverWith(parent2, r)
            }

            // Step4 - Mutate
            for (i in 1 until POP_SIZE) {
                if (r.nextDouble() < PROB_MUTATION) {
                    newPopulation[i]!!.mutate(r)
                }
            }
            population = newPopulation
        }

        return population.first()!!
    }

    private fun tournament(r: Random): Individual {
        /*
		 * In each tournament, we select tournamentSize individuals at random, and we
		 * keep the best of those.
		 */
        var best = population[r.nextInt(POP_SIZE)]
        for (i in 0 until TOURNAMENT_SIZE) {
            val other = population[r.nextInt(POP_SIZE)]
            if (other!!.fitness > best!!.fitness) {
                best = other
            }
        }
        return best!!
    }

    private fun bestOfPopulation(): Individual {
        /*
		 * Returns the best individual of the population.
		 */
        var best = population[0]
        for (other in population) {
            if (other!!.fitness > best!!.fitness) {
                best = other
            }
        }
        return best!!
    }
}