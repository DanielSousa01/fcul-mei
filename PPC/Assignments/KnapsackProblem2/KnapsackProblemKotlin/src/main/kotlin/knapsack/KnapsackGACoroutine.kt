package knapsack

import Individual
import KnapsackGA
import KnapsackGA.Companion.N_GENERATIONS
import KnapsackGA.Companion.POP_SIZE
import KnapsackGA.Companion.PROB_MUTATION
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import java.util.Random
import java.util.concurrent.ThreadLocalRandom

// TODO: CHUNKED VERSION
class KnapsackGACoroutine(override val silent: Boolean = false) : KnapsackGA {
    private var population: Array<Individual> = Array(POP_SIZE) { Individual.createRandom(Random()) }

    override fun run(): Individual = runBlocking {
        for (generation in 0 until N_GENERATIONS) {
            // Step1 - Calculate Fitness
            coroutineScope {
                population.map { individual ->
                    async {
                        individual.measureFitness()
                    }
                }.awaitAll()
            }

            // Step2 - Print the best individual so far.
            val best = bestOfPopulation()
            if (!silent) {
                println("${this::class.simpleName}: Best at generation $generation is $best with ${best.fitness}")
            }

            // Step3 - Find parents to mate (cross-over)
            val newPopulation = Array(POP_SIZE) { best }

            coroutineScope {
                (1 until POP_SIZE).map { i ->
                    async {
                        val r = ThreadLocalRandom.current()
                        // We select two parents, using a tournament.
                        val parent1 = tournament(r, population)
                        val parent2 = tournament(r, population)

                        newPopulation[i] = parent1.crossoverWith(parent2, r)
                    }
                }.awaitAll()
            }

            // Step4 - Mutate
            coroutineScope {
                (1 until POP_SIZE).map { i ->
                    async {
                        val r = ThreadLocalRandom.current()

                        if (r.nextDouble() < PROB_MUTATION) {
                            newPopulation[i].mutate(r)
                        }
                    }
                }.awaitAll()
            }

            population = newPopulation
        }

        return@runBlocking population.first()
    }

    private fun bestOfPopulation(): Individual {
        return population.maxByOrNull { it.fitness } ?: population[0]
    }
}
