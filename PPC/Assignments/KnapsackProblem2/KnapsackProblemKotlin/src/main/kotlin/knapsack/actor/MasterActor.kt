package knapsack.actor

import Individual
import KnapsackGA.Companion.POP_SIZE
import akka.actor.AbstractActor
import akka.actor.ActorRef
import akka.actor.Props
import akka.routing.RoundRobinPool
import knapsack.actor.KnapsackGAMessage.FitnessRequest
import knapsack.actor.KnapsackGAMessage.FitnessResponse

class MasterActor(
    private val population: List<Individual>,
    private val chunkSize: Int,
    private val poolSize: Int
) : AbstractActor() {
    private val fitnessPool: ActorRef = context.actorOf(
        RoundRobinPool(poolSize)
            .props(Props.create(FitnessActor::class.java)),
        "fitnessWorkerPool"
    )
    private val crossoverPool: ActorRef = context.actorOf(
        RoundRobinPool(poolSize)
            .props(Props.create(CrossoverActor::class.java)),
        "fitnessWorkerPool"
    )
    private val mutatePool: ActorRef = context.actorOf(
        RoundRobinPool(poolSize)
            .props(Props.create(MutateActor::class.java)),
        "fitnessWorkerPool"
    )

    private var generation = 0
    private var responsesReceived = 0
    private lateinit var best : Individual

    fun calculateFitness() {
        responsesReceived = 0
        for (startIdx in 0 until POP_SIZE step chunkSize) {
            val endIdx = minOf(startIdx + chunkSize, POP_SIZE)

            val message = FitnessRequest(
                population,
                startIdx,
                endIdx
            )
            fitnessPool.tell(message, self)
        }
    }

    private fun bestOfPopulation() {
        /*
 		 * Returns the best individual of the population.
 		 */
        best = population.maxByOrNull { it.fitness } ?: population[0]
        crossoverPopulation()
    }

    private fun crossoverPopulation() {

    }


    override fun createReceive(): Receive {
        receiveBuilder()
            .match(FitnessResponse::class.java) { msg ->
                responsesReceived += msg.total

                if (responsesReceived == POP_SIZE) {
                    bestOfPopulation()
                    println("KnapsackGAActor: Best at generation $generation is $best with ${best.fitness}")
                }
            }
            .build()
    }
}