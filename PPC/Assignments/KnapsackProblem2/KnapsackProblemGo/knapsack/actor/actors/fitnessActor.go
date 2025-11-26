package actors

import (
	"KnapsackProblemGo/knapsack"

	"github.com/asynkron/protoactor-go/actor"
)

type FitnessActor struct{}

type FitnessRequest struct {
	Chunk    []*knapsack.Individual
	ChunkIdx int
}

type FitnessResponse struct {
	Chunk    []*knapsack.Individual
	ChunkIdx int
}

func NewFitnessActor() actor.Actor {
	return &FitnessActor{}
}

func (a *FitnessActor) Receive(context actor.Context) {
	switch msg := context.Message().(type) {
	case *FitnessRequest:
		a.handleRequest(msg, context)
	}
}

func (a *FitnessActor) handleRequest(request *FitnessRequest, context actor.Context) {
	chunk := request.Chunk

	for _, i := range chunk {
		i.MeasureFitness()
	}

	context.Respond(&FitnessResponse{
		Chunk:    chunk,
		ChunkIdx: request.ChunkIdx,
	})
}
