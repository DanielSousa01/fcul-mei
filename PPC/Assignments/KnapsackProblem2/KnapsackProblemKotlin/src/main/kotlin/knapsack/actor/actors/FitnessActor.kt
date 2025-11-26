package knapsack.actor.actors

import akka.actor.AbstractActor

class FitnessActor : AbstractActor() {

    data class Request(
        val measureFitness: (Int) -> Unit,
        val startIdx: Int,
        val endIdx: Int
    )

    data class Response(val total: Int)

    override fun createReceive(): Receive {
        return receiveBuilder()
            .match(Request::class.java) { request ->
                val measureFitness = request.measureFitness
                val startIdx = request.startIdx
                val endIdx = request.endIdx

                for (i in startIdx until endIdx) {
                    measureFitness(i)
                }

                sender.tell(Response(endIdx - startIdx), self)
            }
            .build()
    }
}
