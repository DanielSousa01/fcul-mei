package actors

import (
	"KnapsackProblemGo/knapsack"
	"github.com/asynkron/protoactor-go/actor"
)

type BestOfActor struct{}

type BestOfRequest struct {
	Individuals []*knapsack.Individual
}

type BestOfResponse struct {
	BestIndividual *knapsack.Individual
}

func NewBestOfActor() actor.Actor {
	return &BestOfActor{}
}

func (a *BestOfActor) Receive(context actor.Context) {
	switch msg := context.Message().(type) {
	case *BestOfRequest:
		a.handleRequest(msg, context)
	}
}

func (a *BestOfActor) handleRequest(request *BestOfRequest, context actor.Context) {
	individuals := request.Individuals
	var best *knapsack.Individual
	for _, ind := range individuals {
		if best == nil || ind.Fitness > best.Fitness {
			best = ind
		}
	}
	context.Respond(&BestOfResponse{BestIndividual: best})
}
