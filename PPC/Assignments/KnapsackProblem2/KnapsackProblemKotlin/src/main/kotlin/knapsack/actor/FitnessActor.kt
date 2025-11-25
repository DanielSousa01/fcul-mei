package knapsack.actor

import KnapsackGA.Companion.POP_SIZE
import akka.actor.AbstractActor
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import knapsack.actor.KnapsackGAMessage.FitnessRequest

class FitnessActor : AbstractActor() {
    override fun createReceive(): AbstractActor.Receive? {
        return receiveBuilder()
            .match(FitnessRequest::class.java) { request ->
                val population = request.population
                val startIdx = request.startIdx
                val endIdx = request.endIdx

                for (i in startIdx until endIdx) {
                    population[i].measureFitness()
                }

                sender.tell(KnapsackGAMessage.FitnessResponse(endIdx - startIdx), self)
            }
            .build()
    }
}
