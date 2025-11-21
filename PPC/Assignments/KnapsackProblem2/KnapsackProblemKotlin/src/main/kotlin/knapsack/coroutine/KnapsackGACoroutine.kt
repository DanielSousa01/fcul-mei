package knapsack.coroutine

import Individual
import KnapsackGA
import KnapsackGA.Companion.N_GENERATIONS
import KnapsackGA.Companion.POP_SIZE
import KnapsackGA.Companion.PROB_MUTATION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Random
import java.util.concurrent.ThreadLocalRandom

class KnapsackGACoroutine(
    override val silent: Boolean = false,
    val chunkSize: Int = 10
) : KnapsackGA {
    private var population: Array<Individual> = Array(POP_SIZE) { Individual.createRandom(Random()) }

    override fun run(): Individual {
        for (generation in 0 until N_GENERATIONS) {
            // Step1 - Calculate Fitness
            calculateFitness()

            // Step2 - Print the best individual so far.
            val best = bestOfPopulation()
            if (!silent) {
                println("${this::class.simpleName}: Best at generation $generation is $best with ${best.fitness}")
            }

            // Step3 - Find parents to mate (cross-over)
            val newPopulation = crossoverPopulation(best)

            // Step4 - Mutate
            mutatePopulation(newPopulation)

            population = newPopulation
        }

        return population.first()
    }

    private fun calculateFitness() {
        computeChunk(POP_SIZE) {
            population[it].measureFitness()
        }
    }

    private fun bestOfPopulation(): Individual {
        /*
		 * Returns the best individual of the population.
		 */
        return population.maxByOrNull { it.fitness } ?: population[0]
    }

    private fun crossoverPopulation(best: Individual): Array<Individual> {
        val newPopulation = Array(POP_SIZE) { best }

        computeChunk(POP_SIZE, 1) {
            val r = ThreadLocalRandom.current()
            // We select two parents, using a tournament.
            val parent1 = tournament(r, population)
            val parent2 = tournament(r, population)

            newPopulation[it] = parent1.crossoverWith(parent2, r)
        }

        return newPopulation
    }

    private fun mutatePopulation(newPopulation: Array<Individual>) {
        computeChunk(POP_SIZE, 1) {
            val r = ThreadLocalRandom.current()

            if (r.nextDouble() < PROB_MUTATION) {
                newPopulation[it].mutate(r)
            }
        }
    }

    private fun computeChunk(size: Int, startIdx: Int = 0, chunkProcessor: (Int) -> Unit) {
        runBlocking(Dispatchers.Default) {
            (startIdx until size step chunkSize).map { chunkStart ->
                launch {
                    val chunkEnd = minOf(chunkStart + chunkSize, size)
                    for (i in chunkStart until chunkEnd) {
                        chunkProcessor(i)
                    }
                }
            }.joinAll()
        }
    }
}
