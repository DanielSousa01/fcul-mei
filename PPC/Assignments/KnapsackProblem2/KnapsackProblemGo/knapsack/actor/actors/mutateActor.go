package actors

import (
	"KnapsackProblemGo/knapsack"
	"math/rand"
	"time"

	"github.com/asynkron/protoactor-go/actor"
)

type MutateActor struct{}

type MutateRequest struct {
	Chunk    []*knapsack.Individual
	ChunkIdx int
}

type MutateResponse struct {
	Chunk    []*knapsack.Individual
	ChunkIdx int
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
	chunk := request.Chunk
	idx := request.ChunkIdx

	r := rand.New(rand.NewSource(time.Now().UnixNano() + int64(idx)))
	for _, i := range chunk {
		if r.Float64() < knapsack.ProbMutation {
			i.Mutate(r)
		}
	}

	context.Respond(&MutateResponse{
		Chunk:    chunk,
		ChunkIdx: idx,
	})
}
