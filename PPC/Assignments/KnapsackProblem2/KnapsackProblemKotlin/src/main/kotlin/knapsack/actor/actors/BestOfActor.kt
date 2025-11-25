package knapsack.actor.actors

import Individual
import akka.actor.AbstractActor

class BestOfActor : AbstractActor() {

    data class Request(
        val population: Array<Individual>
    )

    data class Response(val best: Individual)

    override fun createReceive(): Receive {
        return receiveBuilder()
            .match(Request::class.java) { request ->
                val population = request.population

                val best = population.maxByOrNull { it.fitness } ?: population[0]

                sender.tell(Response(best), self)
            }
            .build()
    }
}
