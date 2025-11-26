package knapsack

import (
	"math/rand"
)

const GeneSize = 1000
const weightLimit = 300

var (
	values  = make([]int, GeneSize)
	weights = make([]int, GeneSize)
)

type Individual struct {
	SelectedItems []bool
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
	ind := &Individual{
		SelectedItems: make([]bool, GeneSize),
		Fitness:       0,
	}

	for i := 0; i < GeneSize; i++ {
		ind.SelectedItems[i] = r.Intn(2) == 1
	}
	return ind
}

func (ind *Individual) deepCopy() *Individual {
	clone := &Individual{
		SelectedItems: make([]bool, GeneSize),
		Fitness:       ind.Fitness,
	}
	copy(clone.SelectedItems, ind.SelectedItems)
	return clone
}

func DeepCopy(arrInd []*Individual) []*Individual {
	cloneArr := make([]*Individual, len(arrInd))
	for i, ind := range arrInd {
		cloneArr[i] = ind.deepCopy()
	}
	return cloneArr
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
	child := &Individual{
		SelectedItems: make([]bool, GeneSize),
		Fitness:       0,
	}
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
