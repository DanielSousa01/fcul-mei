package knapsack

import (
	"math/rand"
)

const GeneSize = 1000

var (
	values      [GeneSize]int
	weights     [GeneSize]int
	weightLimit = 300
)

type Individual struct {
	SelectedItems [GeneSize]bool
	Fitness       int
}

func InitiateProblem() {
	r := rand.New(rand.NewSource(1))
	for i := 0; i < GeneSize; i++ {
		values[i] = r.Intn(100)
		weights[i] = r.Intn(100)
	}
}

func NewIndividualRandom(r *rand.Rand) *Individual {
	ind := &Individual{}

	for i := 0; i < GeneSize; i++ {
		ind.SelectedItems[i] = r.Intn(2) == 1
	}
	return ind
}

func (ind *Individual) MeasureFitness() {
	totalWeight := 0
	totalValue := 0

	for i := 0; i < GeneSize; i++ {
		if ind.SelectedItems[i] {
			totalWeight += weights[i]
			totalValue += values[i]
		}
	}
	if totalWeight > weightLimit {
		ind.Fitness = -(totalWeight - weightLimit)
	} else {
		ind.Fitness = totalValue
	}
}

func (ind *Individual) CrossoverWith(mate *Individual, r *rand.Rand) *Individual {
	child := &Individual{}
	crossoverPoint := r.Intn(GeneSize)

	for i := 0; i < GeneSize; i++ {
		if i < crossoverPoint {
			child.SelectedItems[i] = ind.SelectedItems[i]
		} else {
			child.SelectedItems[i] = mate.SelectedItems[i]
		}
	}
	return child
}

func (ind *Individual) Mutate(r *rand.Rand) {
	mutationPoint := r.Intn(GeneSize)
	ind.SelectedItems[mutationPoint] = !ind.SelectedItems[mutationPoint]
}
