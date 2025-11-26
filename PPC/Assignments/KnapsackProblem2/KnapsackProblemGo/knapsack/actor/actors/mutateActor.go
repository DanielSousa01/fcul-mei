package actors

import (
	"github.com/asynkron/protoactor-go/actor"
)

type MutateActor struct{}

type MutateRequest struct {
	Mutate   func(int)
	StartIdx int
	EndIdx   int
}

type MutateResponse struct {
	Total int
}

func NewMutateActor() actor.Actor {
	return &MutateActor{}
}

func (a *MutateActor) Receive(context actor.Context) {
	switch msg := context.Message().(type) {
	case *MutateRequest:
		a.handleRequest(msg, context)
	}
}

func (a *MutateActor) handleRequest(request *MutateRequest, context actor.Context) {
	mutate := request.Mutate
	startIdx := request.StartIdx
	endIdx := request.EndIdx

	for i := startIdx; i < endIdx; i++ {
		mutate(i)
	}

	context.Respond(&MutateResponse{Total: endIdx - startIdx})
}
