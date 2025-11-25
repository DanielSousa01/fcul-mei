package knapsack.actor.actors

import akka.actor.AbstractActor

class MutateActor : AbstractActor() {
    data class Request(
        val mutate: (Int) -> Unit,
        val startIdx: Int,
        val endIdx: Int
    )

    data class Response(val total: Int)

    override fun createReceive(): Receive {
        return receiveBuilder()
            .match(Request::class.java) { request ->
                val mutate = request.mutate
                val startIdx = request.startIdx
                val endIdx = request.endIdx

                for (i in startIdx until endIdx) {
                    mutate(i)
                }

                sender.tell(Response(endIdx - startIdx), self)
            }
            .build()
    }
}
