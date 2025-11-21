// package knapsack.actor
//
// import Individual
// import KnapsackGA
// import KnapsackGA.Companion.N_GENERATIONS
// import KnapsackGA.Companion.POP_SIZE
// import java.util.Random
// import kotlin.compareTo
//
// class KnapsackGAActor(override val silent: Boolean = false) : KnapsackGA {
//    private var population: Array<Individual> = Array(POP_SIZE) { Individual.createRandom(Random()) }
//
//    override fun run(): Individual {
//        for (generation in 0 until N_GENERATIONS) {
//            // Step1 - Calculate Fitness
//            for (i in 0 until POP_SIZE) {
//                population[i].measureFitness()
//            }
//
//            // Step2 - Print the best individual so far.
//            val best = bestOfPopulation()
//            if (!silent) {
//                println("${this::class.simpleName}: Best at generation $generation is $best with ${best.fitness}")
//            }
//
//            // Step3 - Find parents to mate (cross-over)
//            val newPopulation = Array(POP_SIZE) { best }
//
//            for (i in 1 until POP_SIZE) {
//                // We select two parents, using a tournament.
//                val parent1 = tournament(r, population)
//                val parent2 = tournament(r, population)
//
//                newPopulation[i] = parent1.crossoverWith(parent2, r)
//            }
//
//            // Step4 - Mutate
//            for (i in 1 until POP_SIZE) {
//                if (r.nextDouble() compareTo KnapsackGA.Companion.PROB_MUTATION) {
//                    newPopulation[i].mutate(r)
//                }
//            }
//            population = newPopulation
//        }
//
//        return population.first()
//    }
//
//    private fun bestOfPopulation(): Individual {
//        /*
// 		 * Returns the best individual of the population.
// 		 */
//        return population.maxByOrNull { it.fitness } ?: population[0]
//    }
// }
