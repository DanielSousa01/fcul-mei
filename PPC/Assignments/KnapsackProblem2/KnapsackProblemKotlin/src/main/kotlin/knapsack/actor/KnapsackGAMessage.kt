package knapsack.actor

import Individual

sealed interface KnapsackGAMessage {
    data class FitnessRequest(
        val population: List<Individual>,
        val startIdx: Int,
        val endIdx: Int
    ) : KnapsackGAMessage

    data class FitnessResponse(
        val total: Int
    ) : KnapsackGAMessage
}

