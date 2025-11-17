import java.util.Random

interface KnapsackGA {
    val silent: Boolean
    fun run(): Individual

    fun tournament(r: Random, population: Array<Individual>): Individual {
        /*
		 * In each tournament, we select tournamentSize individuals at random, and we
		 * keep the best of those.
		 */
        var best = population[r.nextInt(POP_SIZE)]
        for (i in 0 until TOURNAMENT_SIZE) {
            val other = population[r.nextInt(POP_SIZE)]
            if (other.fitness > best.fitness) {
                best = other
            }
        }
        return best
    }

    companion object {
        const val N_GENERATIONS = 500
        const val POP_SIZE = 100000
        const val PROB_MUTATION = 0.5
        const val TOURNAMENT_SIZE = 3
    }
}
