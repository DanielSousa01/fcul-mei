import java.util.*

/*
* This file is not supposed to be changed.
*/
class Individual {
    /*
        * This array corresponds to whether the object at a given index
        * is selected to be placed inside the knapsack.
        * The goal is to find the items that maximize the total value with
        * surpassing the weight limit.
        */
    var selectedItems: BooleanArray = BooleanArray(GENE_SIZE)
    var fitness: Int = 0

    fun deepCopy(): Individual {
        val copy = Individual()
        copy.selectedItems = selectedItems.copyOf()
        copy.fitness = fitness
        return copy
    }

    /*
	 * This method evaluates how good a solution the current individual is.
	 * Returns +totalValue if within the weight limit, otherwise returns
	 * -overlimit. The goal is to maximize the fitness.
	 */
    fun measureFitness() {
        var totalWeight = 0
        var totalValue = 0
        for (i in 0 until GENE_SIZE) {
            if (selectedItems[i]) {
                totalValue += VALUES[i]
                totalWeight += WEIGHTS[i]
            }
        }
        if (totalWeight > WEIGHT_LIMIT) {
            this.fitness = -(totalWeight - WEIGHT_LIMIT)
        } else {
            this.fitness = totalValue
        }
    }

    /*
	 * Generates a random point in the genotype (selected Items)
	 * Until that point, uses genes from dad (current)
	 * After that point, uses genes from mom (mate)
	 */
    fun crossoverWith(mate: Individual, r: Random): Individual {
        val child = Individual()
        val crossoverPoint = r.nextInt(GENE_SIZE)
        for (i in 0 until GENE_SIZE) {
            if (i < crossoverPoint) {
                child.selectedItems[i] = selectedItems[i]
            } else {
                child.selectedItems[i] = mate.selectedItems[i]
            }
        }
        return child
    }

    fun mutate(r: Random) {
        val mutationPoint = r.nextInt(GENE_SIZE)
        selectedItems[mutationPoint] = !selectedItems[mutationPoint]
    }

    companion object {
        // This is the definition of the problem
        const val GENE_SIZE: Int = 1000 // Number of possible items
        var VALUES: IntArray = IntArray(GENE_SIZE)
        var WEIGHTS: IntArray = IntArray(GENE_SIZE)
        var WEIGHT_LIMIT: Int = 300

        init {
            // This code initializes the problem.
            val r = Random(1L)
            for (i in 0 until GENE_SIZE) {
                VALUES[i] = r.nextInt(100)
                WEIGHTS[i] = r.nextInt(100)
            }
        }

        fun createRandom(r: Random): Individual {
            val ind = Individual()
            for (i in 0 until GENE_SIZE) {
                ind.selectedItems[i] = r.nextBoolean()
            }
            return ind
        }

        fun Array<Individual>.deepCopy(): Array<Individual> {
            return Array(this.size) { idx ->
                this[idx].deepCopy()
            }
        }
    }
}
