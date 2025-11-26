package knapsack.channel

import Individual

sealed interface Messages {
    data class ProcessIndividuals(
        val idx: Int,
        val population: Array<Individual>
    ) : Messages

    data class ProcessedIndividuals(
        val idx: Int,
        val population: Array<Individual>
    ) : Messages

    data class ProcessCrossoverIndividuals(
        val idx: Int,
        val chunkSize: Int,
        val population: Array<Individual>,
    ) : Messages

    data class ProcessedCrossoverIndividuals(
        val idx: Int,
        val chunkSize: Int,
        val newPopulation: Array<Individual>,
    ) : Messages
}
