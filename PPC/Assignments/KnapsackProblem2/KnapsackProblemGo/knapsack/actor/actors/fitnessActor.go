package actors

import (
	"github.com/asynkron/protoactor-go/actor"
)

type FitnessActor struct{}

type FitnessRequest struct {
	MeasureFitness func(int)
	StartIdx       int
	EndIdx         int
}

type FitnessResponse struct{}

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
	measureFitness := request.MeasureFitness
	startIdx := request.StartIdx
	endIdx := request.EndIdx

	for i := startIdx; i < endIdx; i++ {
		measureFitness(i)
	}

	context.Respond(&FitnessResponse{})
}
