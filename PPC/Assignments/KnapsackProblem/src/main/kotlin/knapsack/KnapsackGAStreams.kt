package knapsack

import Individual
import knapsack.KnapsackGA.Companion.N_GENERATIONS
import knapsack.KnapsackGA.Companion.POP_SIZE
import knapsack.KnapsackGA.Companion.PROB_MUTATION
import java.util.*
import java.util.stream.IntStream

class KnapsackGAStreams(override val silent: Boolean = false) : KnapsackGA {
    private val r = Random()
    private var population: Array<Individual> = Array(POP_SIZE) { Individual.createRandom(r) }

    override fun run(): Individual {
        for (generation in 0 until N_GENERATIONS) {
            // Step1 - Calculate Fitness
            calculateFitness()

            // Step2 - Print the best individual so far.
            val best = bestOfPopulation()
            if (!silent)
                println("${this::class.simpleName}: Best at generation $generation is $best with ${best.fitness}")

            // Step3 - Find parents to mate (cross-over)
            val newPopulation = calculateBestPopulation(best)

            // Step4 - Mutate
            mutate(newPopulation)
        }

        return population.first()
    }

    private fun calculateFitness() {
        IntStream.range(0, POP_SIZE)
            .parallel()
            .forEach { i ->
                population[i].measureFitness()
            }
    }

    private fun bestOfPopulation(): Individual =
        Arrays.stream(population)
            .parallel()
            .max(Comparator.comparingInt { it.fitness })
            .get()


    private fun calculateBestPopulation(best: Individual): Array<Individual> {
        val newPopulation = Array(POP_SIZE) { best }

        IntStream.range(1, POP_SIZE)
            .parallel()
            .forEach {
                val parent1 = tournament(r, population)
                val parent2 = tournament(r, population)
                newPopulation[it] = parent1.crossoverWith(parent2, r)
            }

        return newPopulation
    }

    private fun mutate(newPopulation: Array<Individual>) {
        IntStream.range(1, POP_SIZE)
            .parallel()
            .forEach {
                if (r.nextDouble() < PROB_MUTATION) {
                    population[it].mutate(r)
                }
            }

        population = newPopulation
    }
}