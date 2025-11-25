package knapsack.actor.actors

import Individual
import KnapsackGA.Companion.N_GENERATIONS
import KnapsackGA.Companion.POP_SIZE
import KnapsackGA.Companion.PROB_MUTATION
import akka.actor.AbstractActor
import akka.actor.ActorRef
import akka.actor.Props
import akka.routing.RoundRobinPool
import java.util.*
import java.util.concurrent.ThreadLocalRandom

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
    private val bestOfPool: ActorRef = context.actorOf(
        RoundRobinPool(poolSize)
            .props(Props.create(BestOfActor::class.java)),
        "bestOfWorkerPool"
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
        for (startIdx in 0 until POP_SIZE step chunkSize) {
            val endIdx = minOf(startIdx + chunkSize, POP_SIZE)

            val message = FitnessActor.Request(
                { idx: Int -> population[idx].measureFitness() },
                startIdx,
                endIdx
            )
            fitnessPool.tell(message, self)
        }
    }

    private fun bestOfPopulation() {
        val message = BestOfActor.Request(population)
        bestOfPool.tell(message, self)
    }

    private fun crossoverPopulation() {
        responsesReceived = 0
        newPopulation = Array(POP_SIZE) { best }

        for (startIdx in 1 until POP_SIZE step chunkSize) {
            val endIdx = minOf(startIdx + chunkSize, POP_SIZE)

            val message = CrossoverActor.Request(
                population,
                { idx: Int, individual: Individual -> newPopulation[idx] = individual },
                startIdx,
                endIdx
            )
            crossoverPool.tell(message, self)
        }
    }

    private fun mutatePopulation() {
        responsesReceived = 0
        ThreadLocalRandom.current()

        for (startIdx in 1 until POP_SIZE step chunkSize) {
            val endIdx = minOf(startIdx + chunkSize, POP_SIZE)
            val r = ThreadLocalRandom.current()

            val message = MutateActor.Request(
                { idx: Int ->
                    if (r.nextDouble() < PROB_MUTATION) {
                        newPopulation[idx].mutate(r)
                    }
                },
                startIdx,
                endIdx
            )

            mutatePool.tell(message, self)
        }
    }

    override fun createReceive(): Receive {
        return receiveBuilder()
            .match(Start::class.java) {
                originalSender = sender
                calculateFitness()
            }
            .match(FitnessActor.Response::class.java) { msg ->
                responsesReceived += msg.total

                if (responsesReceived == POP_SIZE) {
                    bestOfPopulation()
                }
            }
            .match(BestOfActor.Response::class.java) { msg ->
                best = msg.best

                if (!silent) {
                    println("KnapsackGAActor: Best at generation $generation is $best with ${best.fitness}")
                }

                crossoverPopulation()
            }
            .match(CrossoverActor.Response::class.java) { msg ->
                responsesReceived += msg.total

                if (responsesReceived == POP_SIZE - 1) {
                    mutatePopulation()
                }
            }
            .match(MutateActor.Response::class.java) { msg ->
                responsesReceived += msg.total

                if (responsesReceived == POP_SIZE - 1) {
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
