import knapsack.KnapsackGAForkJoin
import knapsack.KnapsackGASequential
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class KnapsackGATests {

    @Test
    fun testForkJoin() {
        val knapsackGAForkJoin = KnapsackGAForkJoin()
        val result = knapsackGAForkJoin.run()

        assertEquals(expectedResult.fitness, result.fitness)
    }

    companion object {
        lateinit var expectedResult: Individual

        @JvmStatic
        @BeforeAll
        fun setup() {
            val knapsackGA = KnapsackGASequential(silent = true)
            expectedResult = knapsackGA.run()
        }
    }
}