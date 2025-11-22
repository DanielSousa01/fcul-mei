package sequential

import (
	"KnapsackProblemGo/knapsack"
	"fmt"
	"math/rand"
)

type KnapsackGASequential struct {
	r          *rand.Rand
	population []*knapsack.Individual
}

func NewKnapsackGASequential() *KnapsackGASequential {
	ga := &KnapsackGASequential{
		r:          rand.New(rand.NewSource(rand.Int63())),
		population: make([]*knapsack.Individual, knapsack.PopSize),
	}

	ga.populateInitialPopulationRandomly()
	return ga
}

func (ga *KnapsackGASequential) populateInitialPopulationRandomly() {
	for i := 0; i < knapsack.PopSize; i++ {
		ga.population[i] = knapsack.NewIndividualRandom(ga.r)
	}
}

func (ga *KnapsackGASequential) Run(silent bool) *knapsack.Individual {
	for g := 0; g < knapsack.NGenerations; g++ {
		// Step1 - Calculate Fitness
		for i := 0; i < knapsack.PopSize; i++ {
			ga.population[i].MeasureFitness()
		}

		// Step2 - Print the best individual so far.
		best := ga.bestOfPopulation()
		if !silent {
			fmt.Printf("KnapsackGASequential: Best at generation %d has fitness %d\n", g, best.Fitness)
		}

		// Step3 - Find parents to mate (cross-over)
		newPopulation := make([]*knapsack.Individual, knapsack.PopSize)
		newPopulation[0] = best
		for i := 1; i < knapsack.PopSize; i++ {
			parent1 := knapsack.Tournament(ga.r, ga.population)
			parent2 := knapsack.Tournament(ga.r, ga.population)

			newPopulation[i] = parent1.CrossoverWith(parent2, ga.r)
		}

		// Step4 - Mutate
		for i := 1; i < knapsack.PopSize; i++ {
			if ga.r.Float64() < knapsack.ProbMutation {
				newPopulation[i].Mutate(ga.r)
			}
		}

		ga.population = newPopulation
	}

	return ga.population[0]
}

func (ga *KnapsackGASequential) bestOfPopulation() *knapsack.Individual {
	best := ga.population[0]
	for _, other := range ga.population {
		if other.Fitness > best.Fitness {
			best = other
		}
	}
	return best
}
