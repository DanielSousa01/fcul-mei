package knapsack

import Individual

sealed interface KnapsackGA {
    val silent: Boolean
    fun run(): Individual

    companion object {
        const val N_GENERATIONS = 500
        const val POP_SIZE = 100000
        const val PROB_MUTATION = 0.5
        const val TOURNAMENT_SIZE = 3
        const val THRESHOLD = 1000
    }
}