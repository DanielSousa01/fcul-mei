package knapsack.actor

import Individual

sealed class GAMessage {
    data class CalculateFitness(val individuals: List<Individual>)
    data class FitnessResult(val individuals: List<Individual>)
}
