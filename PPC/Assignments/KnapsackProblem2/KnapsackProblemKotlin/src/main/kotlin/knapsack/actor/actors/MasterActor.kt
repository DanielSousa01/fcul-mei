package knapsack.actor.actors

import Individual
import Individual.Companion.deepCopy
import KnapsackGA.Companion.N_GENERATIONS
import KnapsackGA.Companion.POP_SIZE
import akka.actor.AbstractActor
import akka.actor.ActorRef
import akka.actor.Props
import akka.routing.RoundRobinPool
import java.util.*

class MasterActor(
    private val chunkSize: Int,
    private val silent: Boolean = false,
    poolSize: Int
) : AbstractActor() {
    private var population: Array<Individual> = Array(POP_SIZE) { Individual.createRandom(Random()) }
    private lateinit var originalSender: ActorRef

    private val fitnessPool: ActorRef = context.actorOf(
        RoundRobinPool(poolSize)
            .props(Props.create(FitnessActor::class.java)),
        "fitnessWorkerPool"
    )
    private val crossoverPool: ActorRef = context.actorOf(
        RoundRobinPool(poolSize)
            .props(Props.create(CrossoverActor::class.java)),
        "crossoverWorkerPool"
    )
    private val mutatePool: ActorRef = context.actorOf(
        RoundRobinPool(poolSize)
            .props(Props.create(MutateActor::class.java)),
        "mutateWorkerPool"
    )

    private var generation = 0
    private var responsesReceived = 0
    private var chunksExpected = 0
    private lateinit var best: Individual
    private lateinit var newPopulation: Array<Individual>

    private fun nextGeneration() {
        generation++

        if (generation >= N_GENERATIONS) {
            originalSender.tell(Finished(best), self)
            context.stop(self)
            return
        }

        calculateFitness()
    }

    fun calculateFitness() {
        responsesReceived = 0
        chunksExpected = 0

        for (startIdx in 0 until POP_SIZE step chunkSize) {
            val endIdx = minOf(startIdx + chunkSize, POP_SIZE)
            val chunk = population.sliceArray(startIdx until endIdx).deepCopy()

            val message = FitnessActor.Request(chunk, chunksExpected)
            fitnessPool.tell(message, self)
            chunksExpected++
        }
    }

    private fun bestOfPopulation() {
        best = population.maxByOrNull { it.fitness } ?: population[0]

        if (!silent) {
            println("KnapsackGAActor: Best at generation $generation is $best with ${best.fitness}")
        }
        crossoverPopulation()
    }

    private fun crossoverPopulation() {
        responsesReceived = 0
        chunksExpected = 0
        newPopulation = Array(POP_SIZE) { best }
        val populationSnapshot = population.deepCopy()

        for (startIdx in 1 until POP_SIZE step chunkSize) {

            val endIdx = minOf(startIdx + chunkSize, POP_SIZE)
            val localChunkSize = endIdx - startIdx

            val message = CrossoverActor.Request(
                populationSnapshot,
                localChunkSize,
                chunksExpected
            )
            crossoverPool.tell(message, self)
            chunksExpected++
        }
    }

    private fun mutatePopulation() {
        responsesReceived = 0
        chunksExpected = 0

        for (startIdx in 1 until POP_SIZE step chunkSize) {
            val endIdx = minOf(startIdx + chunkSize, POP_SIZE)
            val chunk = newPopulation.sliceArray(startIdx until endIdx).deepCopy()

            val message = MutateActor.Request(chunk, chunksExpected)
            mutatePool.tell(message, self)
            chunksExpected++
        }
    }

    override fun createReceive(): Receive {
        return receiveBuilder()
            .match(Start::class.java) {
                originalSender = sender
                calculateFitness()
            }
            .match(FitnessActor.Response::class.java) { msg ->
                val startIdx = msg.chunkIdx * chunkSize
                for (i in msg.chunk.indices) {
                    population[startIdx + i] = msg.chunk[i]
                }
                responsesReceived++

                if (responsesReceived == chunksExpected) {
                    bestOfPopulation()
                }
            }
            .match(CrossoverActor.Response::class.java) { msg ->
                val startIdx = 1 + msg.chunkIdx * chunkSize
                for (i in msg.newChunk.indices) {
                    newPopulation[startIdx + i] = msg.newChunk[i]
                }
                responsesReceived++

                if (responsesReceived == chunksExpected) {
                    mutatePopulation()
                }
            }
            .match(MutateActor.Response::class.java) { msg ->
                val startIdx = 1 + msg.chunkIdx * chunkSize
                for (i in msg.chunk.indices) {
                    newPopulation[startIdx + i] = msg.chunk[i]
                }
                responsesReceived++

                if (responsesReceived == chunksExpected) {
                    // Swap populations
                    population = newPopulation
                    nextGeneration()
                }
            }
            .build()
    }

    companion object {
        data class Start(val dummy: Boolean = true)
        data class Finished(val best: Individual)
    }
}
