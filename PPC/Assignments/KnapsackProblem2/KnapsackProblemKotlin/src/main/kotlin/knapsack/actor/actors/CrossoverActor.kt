package knapsack.actor.actors

import Individual
import KnapsackGA.Companion.tournament
import akka.actor.AbstractActor
import java.util.concurrent.ThreadLocalRandom

class CrossoverActor : AbstractActor() {
    data class Request(
        val population: Array<Individual>,
        val newIndividual: (Int, Individual) -> Unit,
        val startIdx: Int,
        val endIdx: Int
    )

    data class Response(val total: Int)

    override fun createReceive(): Receive {
        return receiveBuilder()
            .match(Request::class.java) { request ->
                val population = request.population
                val newIndividual = request.newIndividual
                val startIdx = request.startIdx
                val endIdx = request.endIdx

                val r = ThreadLocalRandom.current()
                for (i in startIdx until endIdx) {
                    val parent1 = tournament(r, population)
                    val parent2 = tournament(r, population)

                    newIndividual(i, parent1.crossoverWith(parent2, r))
                }

                sender.tell(Response(endIdx - startIdx), self)
            }
            .build()
    }
}
