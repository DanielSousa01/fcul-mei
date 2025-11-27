package knapsack.channel

import Individual
import Individual.Companion.deepCopy
import KnapsackGA
import KnapsackGA.Companion.N_GENERATIONS
import KnapsackGA.Companion.POP_SIZE
import KnapsackGA.Companion.PROB_MUTATION
import KnapsackGA.Companion.tournament
import knapsack.channel.Messages.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ThreadLocalRandom

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
                println("KnapsackGAChannel: Best at generation $generation is $best with ${best.fitness}")
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
        val countDownLatch = CountDownLatch(nWorkers)

        repeat(nWorkers) {
            launch {
                for (message in workChannel) {
                    val subPopulation = message.population
                    for (individual in subPopulation) {
                        individual.measureFitness()
                    }
                    resultChannel.send(ProcessedIndividuals(message.idx, subPopulation))
                }
                countDownLatch.countDown()
            }
        }

        launch {
            countDownLatch.await()
            resultChannel.close()
        }

        for (idx in 0 until messagePoolSize) {
            val start = idx * chunkSize
            val end = minOf((idx + 1) * chunkSize, POP_SIZE)
            val chunk = population.sliceArray(start until end).deepCopy()

            workChannel.send(ProcessIndividuals(idx, chunk))
        }
        workChannel.close()

        for (result in resultChannel) {
            val startIdx = result.idx * chunkSize

            for (i in result.population.indices) {
                population[startIdx + i] = result.population[i]
            }
        }
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
        val countDownLatch = CountDownLatch(nWorkers)

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
                countDownLatch.countDown()
            }
        }

        launch {
            countDownLatch.await()
            resultChannel.close()
        }

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

        for (result in resultChannel) {
            val startIdx = 1 + result.idx * result.chunkSize

            for (i in result.newPopulation.indices) {
                newPopulation[startIdx + i] = result.newPopulation[i]
            }
        }

        return@coroutineScope newPopulation
    }

    private suspend fun mutatePopulation(newPopulation: Array<Individual>) = coroutineScope {
        val toProcess = POP_SIZE - 1
        val messagePoolSize = (toProcess + chunkSize - 1) / chunkSize
        val workChannel = Channel<ProcessIndividuals>(messagePoolSize)
        val resultChannel = Channel<ProcessedIndividuals>(messagePoolSize)
        val countDownLatch = CountDownLatch(nWorkers)

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
                countDownLatch.countDown()
            }
        }

        launch {
            countDownLatch.await()
            resultChannel.close()
        }

        for (idx in 0 until messagePoolSize) {
            val startIdx = 1 + idx * chunkSize
            val endIdx = minOf((idx + 1) * chunkSize, POP_SIZE)
            if (startIdx >= endIdx) continue

            val chunk = newPopulation.sliceArray(startIdx until endIdx).deepCopy()

            workChannel.send(ProcessIndividuals(idx, chunk))
        }
        workChannel.close()

        for (result in resultChannel) {
            val startIdx = 1 + result.idx * chunkSize
            for (i in result.population.indices) {
                newPopulation[startIdx + i] = result.population[i]
            }
        }

    }

}
