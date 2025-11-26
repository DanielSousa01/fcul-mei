package knapsack.actor.actors

import Individual
import KnapsackGA.Companion.PROB_MUTATION
import akka.actor.AbstractActor
import java.util.concurrent.ThreadLocalRandom

class MutateActor : AbstractActor() {
    data class Request(
        val chunk: Array<Individual>,
        val chunkIdx: Int
    )

    data class Response(val chunk: Array<Individual>, val chunkIdx: Int)

    override fun createReceive(): Receive {
        return receiveBuilder()
            .match(Request::class.java) { request ->
                val chunk = request.chunk
                val r = ThreadLocalRandom.current()

                for (individual in chunk) {
                    if (r.nextDouble() < PROB_MUTATION) {
                        individual.mutate(r)
                    }
                }

                sender.tell(Response(chunk, request.chunkIdx), self)
            }
            .build()
    }
}
