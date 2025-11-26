package knapsack.actor.actors

import Individual
import akka.actor.AbstractActor

class FitnessActor : AbstractActor() {

    data class Request(
        val chunk: Array<Individual>,
        val chunkIdx: Int
    )

    data class Response(val chunk: Array<Individual>, val chunkIdx: Int)

    override fun createReceive(): Receive {
        return receiveBuilder()
            .match(Request::class.java) { request ->
                val chunk = request.chunk

                for (individual in chunk) {
                    individual.measureFitness()
                }

                sender.tell(Response(chunk, request.chunkIdx), self)
            }
            .build()
    }
}
