package knapsack

import (
	"math/rand"
)

const (
	NGenerations   = 500
	PopSize        = 100000
	ProbMutation   = 0.5
	TournamentSize = 3
)

func Tournament(r *rand.Rand, population [PopSize]*Individual) *Individual {
	best := population[r.Intn(PopSize)]

	for i := 0; i < TournamentSize; i++ {
		other := population[r.Intn(PopSize)]
		if other.Fitness > best.Fitness {
			best = other
		}
	}
	return best
}
