package channel

import (
	"KnapsackProblemGo/knapsack"
	"fmt"
	"math/rand"
	"sync"
	"time"
)

type KnapsackChannel struct {
	population []*knapsack.Individual
	nWorkers   int
	chunkSize  int
}

type ProcessChunk struct {
	startIdx int
	endIdx   int
}

func NewKnapsackGAChannel(nWorkers int, chunkSize int) *KnapsackChannel {
	ga := &KnapsackChannel{
		population: make([]*knapsack.Individual, knapsack.PopSize),
		nWorkers:   nWorkers,
		chunkSize:  chunkSize,
	}

	ga.populateInitialPopulationRandomly()
	return ga
}

func (ga *KnapsackChannel) populateInitialPopulationRandomly() {
	r := rand.New(rand.NewSource(time.Now().UnixNano()))
	for i := 0; i < knapsack.PopSize; i++ {
		ga.population[i] = knapsack.NewIndividualRandom(r)
	}
}

func (ga *KnapsackChannel) Run(silent bool) *knapsack.Individual {
	for g := 0; g < knapsack.NGenerations; g++ {
		// Step1 - Calculate Fitness
		ga.calculateFitness()

		// Step2 - Print the best individual so far.
		best := ga.bestOfPopulation()
		if !silent {
			fmt.Printf("KnapsackGAChannel: Best at generation %d has fitness %d\n", g, best.Fitness)
		}

		// Step3 - Find parents to mate (cross-over)
		newPopulation := ga.crossoverPopulation(best)

		// Step4 - Mutate
		ga.mutatePopulation(newPopulation)

		ga.population = newPopulation
	}

	return ga.population[0]
}

func (ga *KnapsackChannel) calculateFitness() {
	ga.computeChannel(knapsack.PopSize, 0, func(i int) {
		ga.population[i].MeasureFitness()
	})
}

func (ga *KnapsackChannel) bestOfPopulation() *knapsack.Individual {
	best := ga.population[0]
	for _, other := range ga.population {
		if other.Fitness > best.Fitness {
			best = other
		}
	}
	return best
}

func (ga *KnapsackChannel) crossoverPopulation(best *knapsack.Individual) []*knapsack.Individual {
	newPopulation := make([]*knapsack.Individual, knapsack.PopSize)
	newPopulation[0] = best

	ga.computeChannel(knapsack.PopSize, 1, func(i int) {
		seed := time.Now().UnixNano() + int64(i)*1000000
		r := rand.New(rand.NewSource(seed))

		parent1 := knapsack.Tournament(r, ga.population)
		parent2 := knapsack.Tournament(r, ga.population)

		newPopulation[i] = parent1.CrossoverWith(parent2, r)
	})

	return newPopulation
}

func (ga *KnapsackChannel) mutatePopulation(newPopulation []*knapsack.Individual) {
	ga.computeChannel(knapsack.PopSize, 1, func(i int) {
		seed := time.Now().UnixNano() + int64(i)*1000000
		r := rand.New(rand.NewSource(seed))

		if r.Float64() < knapsack.ProbMutation {
			newPopulation[i].Mutate(r)
		}
	})
}

func (ga *KnapsackChannel) computeChannel(size int, startIDX int, chunkProcessor func(int)) {
	var wg sync.WaitGroup
	workChannel := make(chan ProcessChunk, ga.nWorkers)

	for i := 0; i < ga.nWorkers; i++ {
		wg.Add(1)
		go func() {
			defer wg.Done()
			for idx := range workChannel {
				for j := idx.startIdx; j < idx.endIdx; j++ {
					chunkProcessor(j)

				}
			}
		}()
	}

	for i := startIDX; i < size; i += ga.chunkSize {
		end := i + ga.chunkSize
		if end > size {
			end = size
		}

		chunk := ProcessChunk{
			startIdx: i,
			endIdx:   end,
		}

		workChannel <- chunk
	}
	close(workChannel)

	wg.Wait()
}
