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
	messagePoolSize := (knapsack.PopSize + ga.chunkSize - 1) / ga.chunkSize
	workChannel := make(chan ProcessIndividuals, messagePoolSize)
	resultChannel := make(chan ProcessedIndividuals, messagePoolSize)

	var wg sync.WaitGroup
	for i := 0; i < ga.nWorkers; i++ {
		wg.Add(1)
		go func() {
			defer wg.Done()
			for message := range workChannel {
				chunk := message.population
				for _, ind := range chunk {
					ind.MeasureFitness()
				}
				resultChannel <- ProcessedIndividuals{
					idx:        message.idx,
					population: chunk,
				}
			}
		}()
	}

	go func() {
		wg.Wait()
		close(resultChannel)
	}()

	for i := 0; i < messagePoolSize; i++ {
		start := i * ga.chunkSize
		end := start + ga.chunkSize
		if end > knapsack.PopSize {
			end = knapsack.PopSize
		}
		chunk := knapsack.DeepCopy(ga.population[start:end])

		workChannel <- ProcessIndividuals{
			idx:        i,
			population: chunk,
		}
	}

	close(workChannel)

	for result := range resultChannel {
		start := result.idx * ga.chunkSize

		for j, ind := range result.population {
			ga.population[start+j] = ind
		}
	}
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
	toProcess := knapsack.PopSize - 1
	messagePoolSize := (toProcess + ga.chunkSize - 1) / ga.chunkSize
	workChannel := make(chan ProcessCrossoverIndividuals, messagePoolSize)
	resultChannel := make(chan ProcessedCrossoverIndividuals, messagePoolSize)

	newPopulation := make([]*knapsack.Individual, knapsack.PopSize)
	newPopulation[0] = best

	var wg sync.WaitGroup
	for i := 0; i < ga.nWorkers; i++ {
		wg.Add(1)
		go func() {
			defer wg.Done()
			for message := range workChannel {
				idx := message.idx
				population := message.population
				chunkSize := message.chunkSize

				newSubPopulation := make([]*knapsack.Individual, chunkSize)
				r := rand.New(rand.NewSource(time.Now().UnixNano() + int64(idx)))

				for j := 0; j < chunkSize; j++ {
					parent1 := knapsack.Tournament(r, population)
					parent2 := knapsack.Tournament(r, population)

					newSubPopulation[j] = parent1.CrossoverWith(parent2, r)
				}

				resultChannel <- ProcessedCrossoverIndividuals{
					idx:           message.idx,
					chunkSize:     chunkSize,
					newPopulation: newSubPopulation,
				}
			}
		}()
	}

	go func() {
		wg.Wait()
		close(resultChannel)
	}()

	currentPop := knapsack.DeepCopy(ga.population)

	for i := 0; i < messagePoolSize; i++ {
		start := i * ga.chunkSize
		remaining := toProcess - start
		chunkSize := ga.chunkSize
		if remaining < ga.chunkSize {
			chunkSize = remaining
		}
		if chunkSize > 0 {
			workChannel <- ProcessCrossoverIndividuals{
				idx:        i,
				population: currentPop,
				chunkSize:  chunkSize,
			}
		}
	}
	close(workChannel)

	for result := range resultChannel {
		start := result.idx*ga.chunkSize + 1

		for j, ind := range result.newPopulation {
			newPopulation[start+j] = ind
		}
	}

	return newPopulation
}

func (ga *KnapsackChannel) mutatePopulation(newPopulation []*knapsack.Individual) {
	toProcess := knapsack.PopSize - 1
	messagePoolSize := (toProcess + ga.chunkSize - 1) / ga.chunkSize
	workChannel := make(chan ProcessIndividuals, messagePoolSize)
	resultChannel := make(chan ProcessedIndividuals, messagePoolSize)

	var wg sync.WaitGroup
	for i := 0; i < ga.nWorkers; i++ {
		wg.Add(1)
		go func() {
			defer wg.Done()
			for message := range workChannel {
				idx := message.idx
				chunk := message.population
				r := rand.New(rand.NewSource(time.Now().UnixNano() + int64(idx)))
				for _, ind := range chunk {
					if r.Float64() < knapsack.ProbMutation {
						ind.Mutate(r)
					}
				}
				resultChannel <- ProcessedIndividuals{
					idx:        idx,
					population: chunk,
				}
			}
		}()
	}

	go func() {
		wg.Wait()
		close(resultChannel)
	}()

	for i := 0; i < messagePoolSize; i++ {
		start := i*ga.chunkSize + 1
		end := start + ga.chunkSize
		if end > knapsack.PopSize {
			end = knapsack.PopSize
		}

		chunk := knapsack.DeepCopy(newPopulation[start:end])
		workChannel <- ProcessIndividuals{
			idx:        i,
			population: chunk,
		}
	}
	close(workChannel)

	for result := range resultChannel {
		start := result.idx*ga.chunkSize + 1
		for j, ind := range result.population {
			newPopulation[start+j] = ind
		}
	}
}
