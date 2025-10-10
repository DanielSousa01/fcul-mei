package knapsack

import Individual
import Worker
import knapsack.KnapsackGA.Companion.N_GENERATIONS
import knapsack.KnapsackGA.Companion.POP_SIZE
import knapsack.KnapsackGA.Companion.PROB_MUTATION
import worker.Task
import worker.TaskType
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicReference

class KnapsackGAMasterWorker(override val silent: Boolean = false) : KnapsackGA {
    private val r = Random()
    private var population: Array<Individual> = Array(POP_SIZE) { Individual.createRandom(r) }

    private val taskQueue: BlockingQueue<Task> = LinkedBlockingQueue()
    private val numWorkers = Runtime.getRuntime().availableProcessors()
    private val chunksSize = POP_SIZE / numWorkers

    private val workers = ArrayList<Thread>(numWorkers)

    init {
        startWorkers()
    }

    private fun startWorkers() {
        repeat(numWorkers) {
            val worker = Thread(Worker(taskQueue))
            workers.add(worker)
            worker.start()
        }
    }

    override fun run(): Individual {
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

        stopWorkers()

        return population.first()
    }

    private fun calculateFitness() {
        val latch = CountDownLatch(numWorkers)
        for (i in 0 until numWorkers) {
            val start = i * chunksSize
            val end = if (i == numWorkers - 1) POP_SIZE else (i + 1) * chunksSize

            taskQueue.put(Task(TaskType.RUNNABLE) {
                for (j in start until end) {
                    population[j].measureFitness()
                }
                latch.countDown()
            })
        }

        latch.await()
    }

    private fun bestOfPopulation(): Individual {
        val best: AtomicReference<Individual> = AtomicReference(population[0])
        val latch = CountDownLatch(numWorkers)

        for (i in 0 until numWorkers) {
            val start = i * chunksSize
            val end = if (i == numWorkers - 1) POP_SIZE else (i + 1) * chunksSize

            taskQueue.put(Task(TaskType.RUNNABLE) {
                var localBest = best.get()
                for (j in start until end) {
                    val other = population[j]
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
                latch.countDown()
            })
        }

        latch.await()
        return best.get()
    }

    private fun calculateBestPopulation(best: Individual): Array<Individual> {
        val newPopulation = Array(POP_SIZE) { best }
        val latch = CountDownLatch(numWorkers)

        for (i in 0 until numWorkers) {
            val start = 1 + i * chunksSize
            val end = if (i == numWorkers - 1) POP_SIZE else 1 + (i + 1) * chunksSize

            taskQueue.put(Task(TaskType.RUNNABLE) {
                for (j in start until end) {
                    val parent1 = tournament(r, population)
                    val parent2 = tournament(r, population)

                    newPopulation[j] = parent1.crossoverWith(parent2, r)
                }
                latch.countDown()
            })
        }

        latch.await()
        return newPopulation
    }

    private fun mutate(newPopulation: Array<Individual>) {
        val latch = CountDownLatch(numWorkers)
        for (i in 0 until numWorkers) {
            val start = 1 + i * chunksSize
            val end = if (i == numWorkers - 1) POP_SIZE else 1 + (i + 1) * chunksSize

            taskQueue.put(Task(TaskType.RUNNABLE) {
                for (j in start until end) {
                    if (r.nextDouble() < PROB_MUTATION) {
                        newPopulation[j].mutate(r)
                    }
                }
                latch.countDown()
            })
        }

        latch.await()
        population = newPopulation
    }

    private fun stopWorkers() {
        repeat(numWorkers) {
            taskQueue.put(Task(TaskType.POISON_PILL))
        }

        for (worker in workers) {
            worker.join()
        }
    }

}