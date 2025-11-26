package actors

import (
	"KnapsackProblemGo/knapsack"
	"math/rand"

	"github.com/asynkron/protoactor-go/actor"
)

type CrossoverActor struct{}

type CrossoverRequest struct {
	Population    []*knapsack.Individual
	NewIndividual func(int, *knapsack.Individual)
	StartIdx      int
	EndIdx        int
}

type CrossoverResponse struct{}

func NewCrossoverActor() actor.Actor {
	return &CrossoverActor{}
}

func (a *CrossoverActor) Receive(context actor.Context) {
	switch msg := context.Message().(type) {
	case *CrossoverRequest:
		a.handleRequest(msg, context)
	}
}

func (a *CrossoverActor) handleRequest(request *CrossoverRequest, context actor.Context) {
	population := request.Population
	newIndividual := request.NewIndividual
	startIdx := request.StartIdx
	endIdx := request.EndIdx

	r := rand.New(rand.NewSource(rand.Int63()))

	for i := startIdx; i < endIdx; i++ {
		parent1 := knapsack.Tournament(r, population)
		parent2 := knapsack.Tournament(r, population)

		newIndividual(i, parent1.CrossoverWith(parent2, r))
	}

	context.Respond(&CrossoverResponse{})
}
