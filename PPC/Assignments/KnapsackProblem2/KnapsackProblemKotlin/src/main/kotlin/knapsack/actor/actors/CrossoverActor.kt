package knapsack.actor.actors

import Individual
import KnapsackGA.Companion.tournament
import akka.actor.AbstractActor
import java.util.concurrent.ThreadLocalRandom

class CrossoverActor : AbstractActor() {
    data class Request(
        val population: Array<Individual>,
        val chunkSize: Int,
        val chunkIdx: Int
    )

    data class Response(val newChunk: Array<Individual>, val chunkIdx: Int)

    override fun createReceive(): Receive {
        return receiveBuilder()
            .match(Request::class.java) { request ->
                val population = request.population
                val chunkSize = request.chunkSize
                val newChunk = Array(chunkSize) { Individual() }

                val r = ThreadLocalRandom.current()
                for (i in 0 until chunkSize) {
                    val parent1 = tournament(r, population)
                    val parent2 = tournament(r, population)

                    newChunk[i] = parent1.crossoverWith(parent2, r)
                }

                sender.tell(Response(newChunk, request.chunkIdx), self)
            }
            .build()
    }
}
