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

	props := actor.PropsFromProducer(func() actor.Actor {
		return actors.NewBestOfActor()
	})
	ga.bestOfActor = system.Root.Spawn(props)

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
	}

	var futures []futureResult
	actorIdx := 0

	for startIdx := 0; startIdx < knapsack.PopSize; startIdx += ga.chunkSize {
		endIdx := startIdx + ga.chunkSize
		if endIdx > knapsack.PopSize {
			endIdx = knapsack.PopSize
		}

		start := startIdx
		end := endIdx
		pop := ga.population

		request := &actors.FitnessRequest{
			MeasureFitness: func(idx int) {
				pop[idx].MeasureFitness()
			},
			StartIdx: start,
			EndIdx:   end,
		}

		future := ga.system.Root.RequestFuture(ga.fitnessActors[actorIdx], request, -1)
		futures = append(futures, futureResult{future: future})

		actorIdx = (actorIdx + 1) % ga.numWorkers
	}

	for _, f := range futures {
		_, _ = f.future.Result()
	}
}

func (ga *KnapsackGAActor) getBestIndividual() *knapsack.Individual {
	request := &actors.BestOfRequest{
		Individuals: ga.population,
	}

	future := ga.system.Root.RequestFuture(ga.bestOfActor, request, -1)
	result, _ := future.Result()
	response := result.(*actors.BestOfResponse)
	return response.BestIndividual
}

func (ga *KnapsackGAActor) crossover(best *knapsack.Individual) []*knapsack.Individual {
	type futureResult struct {
		future interface {
			Result() (interface{}, error)
		}
	}

	newPopulation := make([]*knapsack.Individual, knapsack.PopSize)
	newPopulation[0] = best

	var futures []futureResult
	actorIdx := 0

	for startIdx := 1; startIdx < knapsack.PopSize; startIdx += ga.chunkSize {
		endIdx := startIdx + ga.chunkSize
		if endIdx > knapsack.PopSize {
			endIdx = knapsack.PopSize
		}

		start := startIdx
		end := endIdx
		newPop := newPopulation
		pop := ga.population

		request := &actors.CrossoverRequest{
			Population: pop,
			NewIndividual: func(idx int, individual *knapsack.Individual) {
				newPop[idx] = individual
			},
			StartIdx: start,
			EndIdx:   end,
		}

		future := ga.system.Root.RequestFuture(ga.crossoverActors[actorIdx], request, -1)
		futures = append(futures, futureResult{future: future})

		actorIdx = (actorIdx + 1) % ga.numWorkers
	}

	for _, f := range futures {
		_, _ = f.future.Result()
	}

	return newPopulation
}

func (ga *KnapsackGAActor) parallelMutation(population []*knapsack.Individual) {
	type futureResult struct {
		future interface {
			Result() (interface{}, error)
		}
	}

	var futures []futureResult
	actorIdx := 0

	for startIdx := 1; startIdx < knapsack.PopSize; startIdx += ga.chunkSize {
		endIdx := startIdx + ga.chunkSize
		if endIdx > knapsack.PopSize {
			endIdx = knapsack.PopSize
		}

		start := startIdx
		end := endIdx
		pop := population
		seed := time.Now().UnixNano() + int64(startIdx)*1000000
		r := rand.New(rand.NewSource(seed))

		request := &actors.MutateRequest{
			Mutate: func(idx int) {
				if r.Float64() < knapsack.ProbMutation {
					pop[idx].Mutate(r)
				}
			},
			StartIdx: start,
			EndIdx:   end,
		}

		future := ga.system.Root.RequestFuture(ga.mutateActors[actorIdx], request, -1)
		futures = append(futures, futureResult{future: future})

		actorIdx = (actorIdx + 1) % ga.numWorkers
	}

	for _, f := range futures {
		_, _ = f.future.Result()
	}
}
