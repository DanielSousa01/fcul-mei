package actor

import (
	"KnapsackProblemGo/knapsack"
	"KnapsackProblemGo/knapsack/actor/actors"
	"fmt"
	"math/rand"
	"time"

	"github.com/asynkron/protoactor-go/actor"
)

type KnapsackGAActor struct {
	chunkSize       int
	population      []*knapsack.Individual
	fitnessActors   []*actor.PID
	bestOfActor     *actor.PID
	crossoverActors []*actor.PID
	mutateActors    []*actor.PID
	numWorkers      int
	system          *actor.ActorSystem
}

func NewKnapsackGAActor(numWorkers int, chunkSize int) *KnapsackGAActor {
	system := actor.NewActorSystem()

	ga := &KnapsackGAActor{
		chunkSize:       chunkSize,
		population:      make([]*knapsack.Individual, knapsack.PopSize),
		fitnessActors:   make([]*actor.PID, numWorkers),
		crossoverActors: make([]*actor.PID, numWorkers),
		mutateActors:    make([]*actor.PID, numWorkers),
		numWorkers:      numWorkers,
		system:          system,
	}

	for i := 0; i < numWorkers; i++ {
		fitnessProps := actor.PropsFromProducer(func() actor.Actor {
			return actors.NewFitnessActor()
		})
		ga.fitnessActors[i] = system.Root.Spawn(fitnessProps)

		mutateProps := actor.PropsFromProducer(func() actor.Actor {
			return actors.NewMutateActor()
		})
		ga.mutateActors[i] = system.Root.Spawn(mutateProps)

		crossoverProps := actor.PropsFromProducer(func() actor.Actor {
			return actors.NewCrossoverActor()
		})
		ga.crossoverActors[i] = system.Root.Spawn(crossoverProps)
	}

	ga.populateInitialPopulationRandomly()
	return ga
}

func (ga *KnapsackGAActor) populateInitialPopulationRandomly() {
	r := rand.New(rand.NewSource(time.Now().UnixNano()))
	for i := 0; i < knapsack.PopSize; i++ {
		ga.population[i] = knapsack.NewIndividualRandom(r)
	}
}

func (ga *KnapsackGAActor) Run(silent bool) *knapsack.Individual {
	for g := 0; g < knapsack.NGenerations; g++ {
		// Step 1: Calcular Fitness em paralelo
		ga.parallelFitness()

		// Step 2: Melhor indivíduo
		best := ga.getBestIndividual()
		if !silent {
			fmt.Printf("KnapsackGAActor: Best at generation %d has fitness %d\n", g, best.Fitness)
		}

		// Step 3: Crossover
		newPopulation := ga.crossover(best)

		// Step 4: Mutação paralela
		ga.parallelMutation(newPopulation)

		ga.population = newPopulation
	}

	return ga.population[0]
}

func (ga *KnapsackGAActor) parallelFitness() {
	type futureResult struct {
		future interface {
			Result() (interface{}, error)
		}
		startIdx int
	}

	var futures []futureResult
	actorIdx := 0

	for startIdx := 0; startIdx < knapsack.PopSize; startIdx += ga.chunkSize {
		endIdx := startIdx + ga.chunkSize
		if endIdx > knapsack.PopSize {
			endIdx = knapsack.PopSize
		}

		chunk := knapsack.DeepCopy(ga.population[startIdx:endIdx])

		request := &actors.FitnessRequest{
			Chunk:    chunk,
			ChunkIdx: actorIdx,
		}

		future := ga.system.Root.RequestFuture(ga.fitnessActors[actorIdx], request, -1)
		futures = append(futures, futureResult{
			future:   future,
			startIdx: startIdx,
		})

		actorIdx = (actorIdx + 1) % ga.numWorkers
	}

	for _, f := range futures {
		result, err := f.future.Result()
		if err == nil {
			response := result.(*actors.FitnessResponse)
			for i, ind := range response.Chunk {
				ga.population[f.startIdx+i] = ind
			}
		}
	}
}

func (ga *KnapsackGAActor) getBestIndividual() *knapsack.Individual {
	best := ga.population[0]
	for _, other := range ga.population {
		if other.Fitness > best.Fitness {
			best = other
		}
	}
	return best
}

func (ga *KnapsackGAActor) crossover(best *knapsack.Individual) []*knapsack.Individual {
	type futureResult struct {
		future interface {
			Result() (interface{}, error)
		}
		startIdx  int
		chunkSize int
	}

	newPopulation := make([]*knapsack.Individual, knapsack.PopSize)
	newPopulation[0] = best

	var futures []futureResult
	actorIdx := 0
	popCopy := knapsack.DeepCopy(ga.population)

	for startIdx := 1; startIdx < knapsack.PopSize; startIdx += ga.chunkSize {
		endIdx := startIdx + ga.chunkSize
		if endIdx > knapsack.PopSize {
			endIdx = knapsack.PopSize
		}

		chunkSize := endIdx - startIdx

		request := &actors.CrossoverRequest{
			Population: popCopy,
			ChunkSize:  chunkSize,
			ChunkIdx:   actorIdx,
		}

		future := ga.system.Root.RequestFuture(ga.crossoverActors[actorIdx], request, -1)
		futures = append(futures, futureResult{
			future:    future,
			startIdx:  startIdx,
			chunkSize: chunkSize,
		})

		actorIdx = (actorIdx + 1) % ga.numWorkers
	}

	for _, f := range futures {
		result, err := f.future.Result()
		if err == nil {
			response := result.(*actors.CrossoverResponse)
			for i, ind := range response.NewChunk {
				newPopulation[f.startIdx+i] = ind
			}
		}
	}

	return newPopulation
}

func (ga *KnapsackGAActor) parallelMutation(population []*knapsack.Individual) {
	type futureResult struct {
		future interface {
			Result() (interface{}, error)
		}
		startIdx int
	}

	var futures []futureResult
	actorIdx := 0

	for startIdx := 1; startIdx < knapsack.PopSize; startIdx += ga.chunkSize {
		endIdx := startIdx + ga.chunkSize
		if endIdx > knapsack.PopSize {
			endIdx = knapsack.PopSize
		}

		chunk := knapsack.DeepCopy(population[startIdx:endIdx])

		request := &actors.MutateRequest{
			Chunk:    chunk,
			ChunkIdx: actorIdx,
		}

		future := ga.system.Root.RequestFuture(ga.mutateActors[actorIdx], request, -1)
		futures = append(futures, futureResult{
			future:   future,
			startIdx: startIdx,
		})

		actorIdx = (actorIdx + 1) % ga.numWorkers
	}

	for _, f := range futures {
		result, err := f.future.Result()
		if err == nil {
			response := result.(*actors.MutateResponse)
			for i, ind := range response.Chunk {
				population[f.startIdx+i] = ind
			}
		}
	}
}
