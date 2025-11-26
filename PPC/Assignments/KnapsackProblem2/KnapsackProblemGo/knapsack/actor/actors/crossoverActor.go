package actors

import (
	"KnapsackProblemGo/knapsack"
	"math/rand"
	"time"

	"github.com/asynkron/protoactor-go/actor"
)

type CrossoverActor struct{}

type CrossoverRequest struct {
	Population []*knapsack.Individual
	ChunkSize  int
	ChunkIdx   int
}

type CrossoverResponse struct {
	NewChunk []*knapsack.Individual
	ChunkIdx int
}

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
	chunkSize := request.ChunkSize
	idx := request.ChunkIdx
	newChunk := make([]*knapsack.Individual, chunkSize)

	r := rand.New(rand.NewSource(time.Now().UnixNano() + int64(idx)))

	for i := 0; i < chunkSize; i++ {
		parent1 := knapsack.Tournament(r, population)
		parent2 := knapsack.Tournament(r, population)

		newChunk[i] = parent1.CrossoverWith(parent2, r)
	}

	context.Respond(&CrossoverResponse{
		NewChunk: newChunk,
		ChunkIdx: idx,
	})
}
