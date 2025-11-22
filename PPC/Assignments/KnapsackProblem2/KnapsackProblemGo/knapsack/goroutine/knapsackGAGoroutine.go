package goroutine

import (
	"KnapsackProblemGo/knapsack"
	"fmt"
	"math/rand"
	"sync"
	"time"
)

type KnapsackGoroutine struct {
	population []*knapsack.Individual
	chunkSize  int
}

func NewKnapsackGAGoroutine(chunkSize int) *KnapsackGoroutine {
	ga := &KnapsackGoroutine{
		population: make([]*knapsack.Individual, knapsack.PopSize),
		chunkSize:  chunkSize,
	}

	ga.populateInitialPopulationRandomly()
	return ga
}

func (ga *KnapsackGoroutine) populateInitialPopulationRandomly() {
	r := rand.New(rand.NewSource(time.Now().UnixNano()))
	for i := 0; i < knapsack.PopSize; i++ {
		ga.population[i] = knapsack.NewIndividualRandom(r)
	}
}

func (ga *KnapsackGoroutine) Run(silent bool) *knapsack.Individual {
	for g := 0; g < knapsack.NGenerations; g++ {
		// Step1 - Calculate Fitness
		ga.calculateFitness()

		// Step2 - Print the best individual so far.
		best := ga.bestOfPopulation()
		if !silent {
			fmt.Printf("KnapsackGAGoroutine: Best at generation %d has fitness %d\n", g, best.Fitness)
		}

		// Step3 - Find parents to mate (cross-over)
		newPopulation := ga.crossoverPopulation(best)

		// Step4 - Mutate
		ga.mutatePopulation(newPopulation)

		ga.population = newPopulation
	}

	return ga.population[0]
}

func (ga *KnapsackGoroutine) calculateFitness() {
	ga.computeChunk(knapsack.PopSize, 0, func(i int) {
		ga.population[i].MeasureFitness()
	})
}

func (ga *KnapsackGoroutine) bestOfPopulation() *knapsack.Individual {
	best := ga.population[0]
	for _, other := range ga.population {
		if other.Fitness > best.Fitness {
			best = other
		}
	}
	return best
}

func (ga *KnapsackGoroutine) crossoverPopulation(best *knapsack.Individual) []*knapsack.Individual {
	newPopulation := make([]*knapsack.Individual, knapsack.PopSize)
	newPopulation[0] = best

	ga.computeChunk(knapsack.PopSize, 1, func(i int) {
		seed := time.Now().UnixNano() + int64(i)*1000000
		r := rand.New(rand.NewSource(seed))

		parent1 := knapsack.Tournament(r, ga.population)
		parent2 := knapsack.Tournament(r, ga.population)

		newPopulation[i] = parent1.CrossoverWith(parent2, r)
	})

	return newPopulation
}

func (ga *KnapsackGoroutine) mutatePopulation(newPopulation []*knapsack.Individual) {
	ga.computeChunk(knapsack.PopSize, 1, func(i int) {
		seed := time.Now().UnixNano() + int64(i)*1000000
		r := rand.New(rand.NewSource(seed))

		if r.Float64() < knapsack.ProbMutation {
			newPopulation[i].Mutate(r)
		}
	})
}

func (ga *KnapsackGoroutine) computeChunk(size int, startIDX int, chunkProcessor func(int)) {
	var wg sync.WaitGroup

	for idx := startIDX; idx < size; idx += ga.chunkSize {
		wg.Add(1)
		go func(start int) {
			defer wg.Done()
			end := start + ga.chunkSize
			if end > size {
				end = size
			}
			for i := start; i < end; i++ {
				chunkProcessor(i)
			}
		}(idx)
	}

	wg.Wait()
}
