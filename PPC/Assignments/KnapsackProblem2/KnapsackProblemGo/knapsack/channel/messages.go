package channel

import "KnapsackProblemGo/knapsack"

type ProcessIndividuals struct {
	idx        int
	population []*knapsack.Individual
}

type ProcessedIndividuals struct {
	idx        int
	population []*knapsack.Individual
}

type ProcessCrossoverIndividuals struct {
	idx        int
	chunkSize  int
	population []*knapsack.Individual
}

type ProcessedCrossoverIndividuals struct {
	idx           int
	chunkSize     int
	newPopulation []*knapsack.Individual
}
