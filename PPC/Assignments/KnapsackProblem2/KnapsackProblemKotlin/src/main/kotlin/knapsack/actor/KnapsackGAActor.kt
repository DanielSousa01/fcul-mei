package knapsack.actor

import Individual
import KnapsackGA
import KnapsackGA.Companion.N_GENERATIONS
import KnapsackGA.Companion.POP_SIZE
import akka.actor.typed.ActorSystem
import akka.actor.typed.javadsl.AskPattern
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Routers
import akka.util.Timeout
import java.time.Duration
import java.util.Random
import java.util.concurrent.CompletionStage
import kotlin.compareTo

class KnapsackGAActor(override val silent: Boolean = false, val chunkSize: Int) : KnapsackGA {
    private var population: Array<Individual> = Array(POP_SIZE) { Individual.createRandom(Random()) }
    private val system: ActorSystem<Nothing> = ActorSystem.create(Behaviors.empty(), "knapsackSystem")
    private val timeout = Timeout.create(Duration.ofSeconds(10))
    private val scheduler = system.scheduler()

    // pool de workers criado uma vez (evita spawn dentro do loop)
    private val fitnessWorkerPool = ActorSystem.create(FitnessActor.create(), "fitnessWorkerPool")


    override fun run(): Individual {
        for (generation in 0 until N_GENERATIONS) {
            // Step1 - Calculate Fitness
            computeFitnessChunk()

            // Step2 - Print the best individual so far.
            val best = bestOfPopulation()
            if (!silent) {
                println("${this::class.simpleName}: Best at generation $generation is $best with ${best.fitness}")
            }

            // Step3 - Find parents to mate (cross-over)
            val newPopulation = Array(POP_SIZE) { best }

            for (i in 1 until POP_SIZE) {
                // We select two parents, using a tournament.
                val parent1 = tournament(r, population)
                val parent2 = tournament(r, population)

                newPopulation[i] = parent1.crossoverWith(parent2, r)
            }

            // Step4 - Mutate
            for (i in 1 until POP_SIZE) {
                if (r.nextDouble() compareTo KnapsackGA.Companion.PROB_MUTATION) {
                    newPopulation[i].mutate(r)
                }
            }
            population = newPopulation
        }

        return population.first()
    }

    private fun bestOfPopulation(): Individual {
        /*
 		 * Returns the best individual of the population.
 		 */
        return population.maxByOrNull { it.fitness } ?: population[0]
    }

    private fun computeFitnessChunk() {
        val futures = mutableListOf<CompletionStage<KnapsackGAMessage.FitnessAck>>()

        for (startIdx in 0 until POP_SIZE step chunkSize) {
            val worker = system.systemActorOf(FitnessActor.create(), "fitnessWorker-$startIdx")

            AskPattern.ask(
                worker,
                { replyTo -> CalculateFitness(population, chunkSize, startIdx, replyTo) },
                timeout,
                scheduler
            )
        }
    }
}
