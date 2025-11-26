package knapsack.channel

import Individual
import Individual.Companion.deepCopy
import KnapsackGA
import KnapsackGA.Companion.N_GENERATIONS
import KnapsackGA.Companion.POP_SIZE
import KnapsackGA.Companion.PROB_MUTATION
import KnapsackGA.Companion.tournament
import knapsack.channel.Messages.ProcessCrossoverIndividuals
import knapsack.channel.Messages.ProcessIndividuals
import knapsack.channel.Messages.ProcessedCrossoverIndividuals
import knapsack.channel.Messages.ProcessedIndividuals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.compareTo
import kotlin.div

class KnapsackGAChannel(
    override val silent: Boolean = false,
    private val nWorkers: Int = Runtime.getRuntime().availableProcessors(),
    private val chunkSize: Int = 10
) : KnapsackGA {
    private var population: Array<Individual> = Array(POP_SIZE) { Individual.createRandom(Random()) }

    override fun run(): Individual = runBlocking(Dispatchers.Default) {
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

        return@runBlocking population.first()
    }

    private suspend fun calculateFitness() = coroutineScope {
        val messagePoolSize = (POP_SIZE + chunkSize - 1) / chunkSize
        val workChannel = Channel<ProcessIndividuals>(messagePoolSize)
        val resultChannel = Channel<ProcessedIndividuals>(messagePoolSize)

        repeat(nWorkers) {
            launch {
                for (message in workChannel) {
                    val subPopulation = message.population
                    for (individual in subPopulation) {
                        individual.measureFitness()
                    }
                    resultChannel.send(ProcessedIndividuals(message.idx, subPopulation))
                }
            }
        }

        launch {
            val currentPop = population.deepCopy()

            for (idx in 0 until messagePoolSize) {
                val start = idx * chunkSize
                val end = minOf((idx + 1) * chunkSize, POP_SIZE)
                val chunk = currentPop.sliceArray(start until end)

                workChannel.send(ProcessIndividuals(idx, chunk))
            }
            workChannel.close()
        }

        repeat(messagePoolSize) {
            val result = resultChannel.receive()
            val startIdx = result.idx * chunkSize

            for (i in result.population.indices) {
                population[startIdx + i] = result.population[i]
            }
        }
        resultChannel.close()
    }

    private fun bestOfPopulation(): Individual {
        /*
		 * Returns the best individual of the population.
		 */
        return population.maxByOrNull { it.fitness } ?: population[0]
    }

    private suspend fun crossoverPopulation(best: Individual): Array<Individual> = coroutineScope {
        val toProcess = POP_SIZE - 1
        val messagePoolSize = (toProcess + chunkSize - 1) / chunkSize
        val workChannel = Channel<ProcessCrossoverIndividuals>(messagePoolSize)
        val resultChannel = Channel<ProcessedCrossoverIndividuals>(messagePoolSize)

        val newPopulation = Array(POP_SIZE) { best }

        repeat(nWorkers) {
            launch {
                for (message in workChannel) {
                    val population = message.population
                    val chunkSize = message.chunkSize

                    val newSubPopulation = Array(chunkSize) { Individual() }

                    val r = ThreadLocalRandom.current()

                    for (idx in 0 until chunkSize) {
                        // We select two parents, using a tournament.
                        val parent1 = tournament(r, population)
                        val parent2 = tournament(r, population)

                        newSubPopulation[idx] = parent1.crossoverWith(parent2, r)
                    }

                    resultChannel.send(ProcessedCrossoverIndividuals(message.idx, chunkSize, newSubPopulation))
                }
            }
        }

        launch {
            val currentPop = population.deepCopy()

            for (idx in 0 until messagePoolSize) {
                val start = idx * chunkSize
                val remaining = toProcess - start
                val chunkSize = minOf(chunkSize, remaining)
                if (chunkSize <= 0) continue
                workChannel.send(
                    ProcessCrossoverIndividuals(
                        idx,
                        chunkSize,
                        currentPop
                    )
                )
            }
            workChannel.close()
        }

        repeat(messagePoolSize) {
            val result = resultChannel.receive()
            val startIdx = 1 + result.idx * result.chunkSize

            for (i in result.newPopulation.indices) {
                newPopulation[startIdx + i] = result.newPopulation[i]
            }
        }
        resultChannel.close()

        return@coroutineScope newPopulation
    }

    private suspend fun mutatePopulation(newPopulation: Array<Individual>) = coroutineScope {
        val toProcess = POP_SIZE - 1
        val messagePoolSize = (toProcess + chunkSize - 1) / chunkSize
        val workChannel = Channel<ProcessedIndividuals>(messagePoolSize)
        val resultChannel = Channel<ProcessedIndividuals>(messagePoolSize)

        repeat(nWorkers) {
            launch {
                for (message in workChannel) {
                    val subPopulation = message.population
                    val r = ThreadLocalRandom.current()
                    for (individual in subPopulation) {
                        if (r.nextDouble() < PROB_MUTATION) {
                            individual.mutate(r)
                        }
                    }
                    resultChannel.send(ProcessedIndividuals(message.idx, subPopulation))
                }
            }
        }

        launch {
            val currentPop = newPopulation.deepCopy()

            for (idx in 0 until messagePoolSize) {
                val startIdx = 1 + idx * chunkSize
                val endIdx = minOf((idx + 1) * chunkSize, POP_SIZE)
                if (startIdx >= endIdx) continue

                val chunk = currentPop.sliceArray(startIdx until endIdx)

                workChannel.send(ProcessedIndividuals(idx, chunk))
            }
            workChannel.close()
        }

        repeat(messagePoolSize) {
            val result = resultChannel.receive()
            val startIdx = 1 + result.idx * chunkSize

            for (i in result.population.indices) {
                newPopulation[startIdx + i] = result.population[i]
            }
        }
        resultChannel.close()

    }

}
