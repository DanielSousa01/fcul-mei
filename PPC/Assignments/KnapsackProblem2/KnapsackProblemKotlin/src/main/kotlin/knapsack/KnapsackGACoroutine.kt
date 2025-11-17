package knapsack

import Individual
import KnapsackGA
import KnapsackGA.Companion.POP_SIZE
import java.util.Random

class KnapsackGACoroutine(override val silent: Boolean = false): KnapsackGA {
    private val population: Array<Individual> = Array(POP_SIZE)
    { Individual.createRandom(Random()) }

    override fun run(): Individual {
        TODO("Not yet implemented")
    }
}