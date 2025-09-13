import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertTrue
import kotlin.math.abs

class ValueOfPIMonteCarloTests {

    @Nested
    inner class SequentialTests {

        @Test
        fun `should throw IllegalArgumentException when totalDarts is zero`() {
            assertThrows<IllegalArgumentException> {
                ValueOfPIMonteCarlo.sequential(0)
            }
        }

        @Test
        fun `should throw IllegalArgumentException when totalDarts is negative`() {
            assertThrows<IllegalArgumentException> {
                ValueOfPIMonteCarlo.sequential(-100)
            }
        }

        @Test
        fun `should return reasonable approximation of PI with small number of darts`() {
            val result = ValueOfPIMonteCarlo.sequential(1000)
            assertTrue(result > 0, "Result should be positive")
            assertTrue(result < 10, "Result should be reasonable upper bound")
        }

        @Test
        fun `should return better approximation of PI with larger number of darts`() {
            val result = ValueOfPIMonteCarlo.sequential(100000)
            val expectedPI = Math.PI
            val error = abs(result - expectedPI)
            assertTrue(error < 0.5, "Error should be less than 0.5 with 100k darts")
        }

        @Test
        fun `should return value between 2 and 5 for reasonable input`() {
            val result = ValueOfPIMonteCarlo.sequential(10000)
            assertTrue(result >= 2.0, "PI approximation should be at least 2.0")
            assertTrue(result <= 5.0, "PI approximation should be at most 5.0")
        }

        @Test
        fun `should work with minimum valid input`() {
            val result = ValueOfPIMonteCarlo.sequential(1)
            assertTrue(result == 0.0 || result == 4.0, "With 1 dart, result should be either 0 or 4")
        }
    }

    @Nested
    inner class ParallelFixedThreadsTests {

        @Test
        fun `should throw IllegalArgumentException when totalDarts is zero`() {
            assertThrows<IllegalArgumentException> {
                ValueOfPIMonteCarlo.parallel(0)
            }
        }

        @Test
        fun `should throw IllegalArgumentException when totalDarts is negative`() {
            assertThrows<IllegalArgumentException> {
                ValueOfPIMonteCarlo.parallel(-100)
            }
        }

        @Test
        fun `should return reasonable approximation of PI with small number of darts`() {
            val result = ValueOfPIMonteCarlo.parallel(1000)
            assertTrue(result > 0, "Result should be positive")
            assertTrue(result < 10, "Result should be reasonable upper bound")
        }

        @Test
        fun `should return better approximation of PI with larger number of darts`() {
            val result = ValueOfPIMonteCarlo.parallel(100000)
            val expectedPI = Math.PI
            val error = abs(result - expectedPI)
            assertTrue(error < 0.5, "Error should be less than 0.5 with 100k darts")
        }

        @Test
        fun `should return value between 2 and 5 for reasonable input`() {
            val result = ValueOfPIMonteCarlo.parallel(10000)
            assertTrue(result >= 2.0, "PI approximation should be at least 2.0")
            assertTrue(result <= 5.0, "PI approximation should be at most 5.0")
        }

        @Test
        fun `should work with minimum valid input`() {
            val result = ValueOfPIMonteCarlo.parallel(1)
            assertTrue(result == 0.0 || result == 4.0, "With 1 dart, result should be either 0 or 4")
        }

        @Test
        fun `should handle case where totalDarts is less than number of threads`() {
            val result = ValueOfPIMonteCarlo.parallel(2)
            assertTrue(result >= 0.0, "Result should be non-negative")
            assertTrue(result <= 4.0, "Result should not exceed 4.0")
        }

        @Test
        fun `parallel and sequential should produce similar results with large sample`() {
            val totalDarts = 500000
            val sequentialResult = ValueOfPIMonteCarlo.sequential(totalDarts)
            val parallelResult = ValueOfPIMonteCarlo.parallel(totalDarts)

            val difference = abs(sequentialResult - parallelResult)
            assertTrue(difference < 0.2, "Sequential and parallel results should be similar")
        }

        @Test
        fun `should work correctly with number of darts equal to number of threads`() {
            val result = ValueOfPIMonteCarlo.parallel(4)
            assertTrue(result >= 0.0, "Result should be non-negative")
            assertTrue(result <= 4.0, "Result should not exceed 4.0")
        }

        @Test
        fun `should work correctly with number of darts slightly larger than number of threads`() {
            val result = ValueOfPIMonteCarlo.parallel(5)
            assertTrue(result >= 0.0, "Result should be non-negative")
            assertTrue(result <= 4.0, "Result should not exceed 4.0")
        }
    }

    @Nested
    inner class ParallelChunkSizeTests {

        @Test
        fun `should throw IllegalArgumentException when totalDarts is zero`() {
            assertThrows<IllegalArgumentException> {
                ValueOfPIMonteCarlo.parallel(0, 100)
            }
        }

        @Test
        fun `should throw IllegalArgumentException when totalDarts is negative`() {
            assertThrows<IllegalArgumentException> {
                ValueOfPIMonteCarlo.parallel(-100, 100)
            }
        }

        @Test
        fun `should throw IllegalArgumentException when chunkSize is zero`() {
            assertThrows<IllegalArgumentException> {
                ValueOfPIMonteCarlo.parallel(1000, 0)
            }
        }

        @Test
        fun `should throw IllegalArgumentException when chunkSize is negative`() {
            assertThrows<IllegalArgumentException> {
                ValueOfPIMonteCarlo.parallel(1000, -50)
            }
        }

        @Test
        fun `should return reasonable approximation of PI with small number of darts`() {
            val result = ValueOfPIMonteCarlo.parallel(1000, 100)
            assertTrue(result > 0, "Result should be positive")
            assertTrue(result < 10, "Result should be reasonable upper bound")
        }

        @Test
        fun `should return better approximation of PI with larger number of darts`() {
            val result = ValueOfPIMonteCarlo.parallel(100000, 1000)
            val expectedPI = Math.PI
            val error = abs(result - expectedPI)
            assertTrue(error < 0.5, "Error should be less than 0.5 with 100k darts")
        }

        @Test
        fun `should work with chunkSize equal to totalDarts`() {
            val result = ValueOfPIMonteCarlo.parallel(1000, 1000)
            assertTrue(result >= 0.0, "Result should be non-negative")
            assertTrue(result <= 4.0, "Result should not exceed 4.0")
        }

        @Test
        fun `should work with chunkSize larger than totalDarts`() {
            val result = ValueOfPIMonteCarlo.parallel(100, 500)
            assertTrue(result >= 0.0, "Result should be non-negative")
            assertTrue(result <= 4.0, "Result should not exceed 4.0")
        }

        @Test
        fun `should return value between 2 and 5 for reasonable input`() {
            val result = ValueOfPIMonteCarlo.parallel(10000, 500)
            assertTrue(result >= 2.0, "PI approximation should be at least 2.0")
            assertTrue(result <= 5.0, "PI approximation should be at most 5.0")
        }

        @Test
        fun `should work with small chunk size`() {
            val result = ValueOfPIMonteCarlo.parallel(1000, 10)
            assertTrue(result >= 0.0, "Result should be non-negative")
            assertTrue(result <= 4.0, "Result should not exceed 4.0")
        }

        @Test
        fun `should work with chunk size of 1`() {
            val result = ValueOfPIMonteCarlo.parallel(100, 1)
            assertTrue(result >= 0.0, "Result should be non-negative")
            assertTrue(result <= 4.0, "Result should not exceed 4.0")
        }

        @Test
        fun `should produce similar results with different chunk sizes`() {
            val totalDarts = 50000
            val result1 = ValueOfPIMonteCarlo.parallel(totalDarts, 100)
            val result2 = ValueOfPIMonteCarlo.parallel(totalDarts, 1000)

            val difference = abs(result1 - result2)
            assertTrue(difference < 0.3, "Results with different chunk sizes should be similar")
        }

        @Test
        fun `parallel and sequential should produce similar results with large sample and optimal chunk size`() {
            val totalDarts = 20000
            val sequentialResult = ValueOfPIMonteCarlo.sequential(totalDarts)

            val optimalChunkSize = ValueOfPIMonteCarlo.getOptimalChunkSize(totalDarts)
            println("Optimal chunk size for $totalDarts darts is $optimalChunkSize")

            val parallelResult = ValueOfPIMonteCarlo.parallel(totalDarts, optimalChunkSize)

            val difference = abs(sequentialResult - parallelResult)
            assertTrue(difference < 0.2, "Sequential and parallel results should be similar")
        }
    }

}