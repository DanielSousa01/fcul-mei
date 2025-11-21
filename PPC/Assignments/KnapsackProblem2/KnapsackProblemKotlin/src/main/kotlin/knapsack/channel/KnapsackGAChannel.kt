package knapsack.channel

import Individual
import KnapsackGA
import KnapsackGA.Companion.N_GENERATIONS
import KnapsackGA.Companion.POP_SIZE
import KnapsackGA.Companion.PROB_MUTATION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Random
import java.util.concurrent.ThreadLocalRandom

class KnapsackGAChannel(
    override val silent: Boolean = false,
    private val nWorkers: Int = Runtime.getRuntime().availableProcessors()
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
        compute(POP_SIZE) { idx ->
            population[idx].measureFitness()
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

        compute(POP_SIZE, 1) { idx ->
            val r = ThreadLocalRandom.current()
            // We select two parents, using a tournament.
            val parent1 = tournament(r, population)
            val parent2 = tournament(r, population)

            newPopulation[idx] = parent1.crossoverWith(parent2, r)
        }

        return newPopulation
    }

    private fun mutatePopulation(newPopulation: Array<Individual>) {
        compute(POP_SIZE, 1) { idx ->
            val r = ThreadLocalRandom.current()

            if (r.nextDouble() < PROB_MUTATION) {
                newPopulation[idx].mutate(r)
            }
        }
    }

    private fun compute(size: Int, startIdx: Int = 0, processor: (Int) -> Unit) {
        val workChannel = Channel<Int>(size)

        runBlocking(Dispatchers.Default) {
            launch {
                for (i in startIdx until size) {
                    workChannel.send(i)
                }
                workChannel.close()
            }

            List(nWorkers) {
                launch {
                    for (i in workChannel) {
                        processor(i)
                    }
                }
            }.joinAll()
        }
    }
}
