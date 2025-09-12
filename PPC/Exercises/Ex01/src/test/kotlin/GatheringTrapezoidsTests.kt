import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GatheringTrapezoidsTests {

    @Nested
    inner class SequentialGatheringTrapezoidsTestsTests {
        @Test
        fun testLinearFunction() {
            val f: (Double) -> Double = { x -> x }
            val result = GatheringTrapezoids.sequential(f, 0.0, 1.0, 1e-7)
            assertEquals(0.5, result, 1e-6)
        }

        @Test
        fun testQuadraticFunction() {
            val f: (Double) -> Double = { x -> x * (x - 1) }
            val result = GatheringTrapezoids.sequential(f, 0.0, 1.0, 1e-7)
            assertEquals(-1.0/6.0, result, 1e-6)
        }

        @Test
        fun testConstantFunction() {
            val f: (Double) -> Double = { 5.0 }
            val result = GatheringTrapezoids.sequential(f, 0.0, 2.0, 1e-7)
            assertEquals(10.0, result, 1e-6)
        }

        @Test
        fun testCubicFunction() {
            val f: (Double) -> Double = { x -> x * x * x }
            val result = GatheringTrapezoids.sequential(f, 0.0, 2.0, 1e-7)
            assertEquals(4.0, result, 1e-6)
        }

        @Test
        fun testNegativeInterval() {
            val f: (Double) -> Double = { x -> x * x }
            val result = GatheringTrapezoids.sequential(f, -1.0, 1.0, 1e-7)
            assertEquals(2.0/3.0, result, 1e-6)
        }

        @Test
        fun testInvalidBounds() {
            val f: (Double) -> Double = { x -> x }

            assertFailsWith<IllegalArgumentException> {
                GatheringTrapezoids.sequential(f, 1.0, 0.0, 1e-7)
            }
        }

        @Test
        fun testInvalidResolution() {
            val f: (Double) -> Double = { x -> x }

            assertFailsWith<IllegalArgumentException> {
                GatheringTrapezoids.sequential(f, 0.0, 1.0, -1e-7)
            }

            assertFailsWith<IllegalArgumentException> {
                GatheringTrapezoids.sequential(f, 0.0, 1.0, 0.0)
            }
        }

        @Test
        fun testSinFunction() {
            val f: (Double) -> Double = { x -> kotlin.math.sin(x) }
            val result = GatheringTrapezoids.sequential(f, 0.0, kotlin.math.PI, 1e-6)
            assertEquals(2.0, result, 1e-5)
        }

        @Test
        fun testHighResolution() {
            val f: (Double) -> Double = { x -> x * x }
            val result = GatheringTrapezoids.sequential(f, 0.0, 1.0, 1e-10)
            assertEquals(1.0/3.0, result, 1e-9)
        }

        @Test
        fun testLargeInterval() {
            val f: (Double) -> Double = { x -> x }
            val result = GatheringTrapezoids.sequential(f, 0.0, 10.0, 1e-6)
            assertEquals(50.0, result, 1e-5)
        }
    }

    @Nested
    inner class ParallelGatheringTrapezoidsTests {
        @Test
        fun testLinearFunction() {
            val f: (Double) -> Double = { x -> x }
            val result = GatheringTrapezoids.parallel(f, 0.0, 1.0, 1e-7)
            assertEquals(0.5, result, 1e-6)
        }

        @Test
        fun testQuadraticFunction() {
            val f: (Double) -> Double = { x -> x * (x - 1) }
            val result = GatheringTrapezoids.parallel(f, 0.0, 1.0, 1e-7)
            assertEquals(-1.0/6.0, result, 1e-6)
        }

        @Test
        fun testConstantFunction() {
            val f: (Double) -> Double = { 5.0 }
            val result = GatheringTrapezoids.parallel(f, 0.0, 2.0, 1e-7)
            assertEquals(10.0, result, 1e-6)
        }

        @Test
        fun testCubicFunction() {
            val f: (Double) -> Double = { x -> x * x * x }
            val result = GatheringTrapezoids.parallel(f, 0.0, 2.0, 1e-7)
            assertEquals(4.0, result, 1e-6)
        }

        @Test
        fun testNegativeInterval() {
            val f: (Double) -> Double = { x -> x * x }
            val result = GatheringTrapezoids.parallel(f, -1.0, 1.0, 1e-7)
            assertEquals(2.0/3.0, result, 1e-6)
        }

        @Test
        fun testInvalidBounds() {
            val f: (Double) -> Double = { x -> x }

            assertFailsWith<IllegalArgumentException> {
                GatheringTrapezoids.parallel(f, 1.0, 0.0, 1e-7)
            }
        }

        @Test
        fun testInvalidResolution() {
            val f: (Double) -> Double = { x -> x }

            assertFailsWith<IllegalArgumentException> {
                GatheringTrapezoids.parallel(f, 0.0, 1.0, -1e-7)
            }

            assertFailsWith<IllegalArgumentException> {
                GatheringTrapezoids.parallel(f, 0.0, 1.0, 0.0)
            }
        }

        @Test
        fun testSinFunction() {
            val f: (Double) -> Double = { x -> kotlin.math.sin(x) }
            val result = GatheringTrapezoids.parallel(f, 0.0, kotlin.math.PI, 1e-6)
            assertEquals(2.0, result, 1e-5)
        }

        @Test
        fun testHighResolution() {
            val f: (Double) -> Double = { x -> x * x }
            val result = GatheringTrapezoids.parallel(f, 0.0, 1.0, 1e-10)
            assertEquals(1.0/3.0, result, 1e-9)
        }

        @Test
        fun testLargeInterval() {
            val f: (Double) -> Double = { x -> x }
            val result = GatheringTrapezoids.parallel(f, 0.0, 10.0, 1e-6)
            assertEquals(50.0, result, 1e-5)
        }

        @Test
        fun testParallelVsSequentialConsistency() {
            val f: (Double) -> Double = { x -> x * x + 2 * x + 1 }
            val sequentialResult = GatheringTrapezoids.sequential(f, 0.0, 3.0, 1e-6)
            val parallelResult = GatheringTrapezoids.parallel(f, 0.0, 3.0, 1e-6)
            assertEquals(sequentialResult, parallelResult, 1e-6)
        }

        @Test
        fun testComplexFunction() {
            val f: (Double) -> Double = { x -> kotlin.math.exp(-x * x) }
            val result = GatheringTrapezoids.parallel(f, -2.0, 2.0, 1e-5)
            assertEquals(1.772, result, 1e-2)
        }
    }

    @Nested
    inner class ParallelWithChunkSizeTests {
        @Test
        fun testLinearFunctionWithSmallChunk() {
            val f: (Double) -> Double = { x -> x }
            val result = GatheringTrapezoids.parallel(f, 0.0, 1.0, 1e-7, 1)
            assertEquals(0.5, result, 1e-6)
        }

        @Test
        fun testLinearFunctionWithLargeChunk() {
            val f: (Double) -> Double = { x -> x }
            val result = GatheringTrapezoids.parallel(f, 0.0, 1.0, 1e-7, 100)
            assertEquals(0.5, result, 1e-6)
        }

        @Test
        fun testQuadraticFunctionWithChunk() {
            val f: (Double) -> Double = { x -> x * (x - 1) }
            val result = GatheringTrapezoids.parallel(f, 0.0, 1.0, 1e-7, 10)
            assertEquals(-1.0/6.0, result, 1e-6)
        }

        @Test
        fun testConstantFunctionWithChunk() {
            val f: (Double) -> Double = { 5.0 }
            val result = GatheringTrapezoids.parallel(f, 0.0, 2.0, 1e-7, 5)
            assertEquals(10.0, result, 1e-6)
        }

        @Test
        fun testCubicFunctionWithChunk() {
            val f: (Double) -> Double = { x -> x * x * x }
            val result = GatheringTrapezoids.parallel(f, 0.0, 2.0, 1e-7, 20)
            assertEquals(4.0, result, 1e-6)
        }

        @Test
        fun testNegativeIntervalWithChunk() {
            val f: (Double) -> Double = { x -> x * x }
            val result = GatheringTrapezoids.parallel(f, -1.0, 1.0, 1e-7, 15)
            assertEquals(2.0/3.0, result, 1e-6)
        }

        @Test
        fun testSinFunctionWithChunk() {
            val f: (Double) -> Double = { x -> kotlin.math.sin(x) }
            val result = GatheringTrapezoids.parallel(f, 0.0, kotlin.math.PI, 1e-6, 25)
            assertEquals(2.0, result, 1e-5)
        }

        @Test
        fun testChunkSizeOne() {
            val f: (Double) -> Double = { x -> x * x }
            val result = GatheringTrapezoids.parallel(f, 0.0, 1.0, 1e-6, 1)
            assertEquals(1.0/3.0, result, 1e-5)
        }

        @Test
        fun testVeryLargeChunkSize() {
            val f: (Double) -> Double = { x -> x }
            val result = GatheringTrapezoids.parallel(f, 0.0, 1.0, 1e-6, 10000)
            assertEquals(0.5, result, 1e-5)
        }

        @Test
        fun testChunkSizeConsistencyWithFixedThreads() {
            val f: (Double) -> Double = { x -> x * x + 2 * x + 1 }
            val fixedThreadsResult = GatheringTrapezoids.parallel(f, 0.0, 3.0, 1e-6)
            val chunkSizeResult = GatheringTrapezoids.parallel(f, 0.0, 3.0, 1e-6, 50)
            assertEquals(fixedThreadsResult, chunkSizeResult, 1e-6)
        }

        @Test
        fun testChunkSizeConsistencyWithSequential() {
            val f: (Double) -> Double = { x -> kotlin.math.sin(x) + x }
            val sequentialResult = GatheringTrapezoids.sequential(f, 0.0, 2.0, 1e-6)
            val chunkSizeResult = GatheringTrapezoids.parallel(f, 0.0, 2.0, 1e-6, 30)
            assertEquals(sequentialResult, chunkSizeResult, 1e-6)
        }

        @Test
        fun testDifferentChunkSizesProduceSameResult() {
            val f: (Double) -> Double = { x -> x * x * x }
            val result1 = GatheringTrapezoids.parallel(f, 0.0, 2.0, 1e-6, 5)
            val result2 = GatheringTrapezoids.parallel(f, 0.0, 2.0, 1e-6, 50)
            val result3 = GatheringTrapezoids.parallel(f, 0.0, 2.0, 1e-6, 500)

            assertEquals(result1, result2, 1e-6)
            assertEquals(result2, result3, 1e-6)
        }

        @Test
        fun testComplexFunctionWithChunk() {
            val f: (Double) -> Double = { x -> kotlin.math.exp(-x * x) }
            val result = GatheringTrapezoids.parallel(f, -2.0, 2.0, 1e-5, 40)
            assertEquals(1.772, result, 1e-2)
        }

        @Test
        fun testLargeIntervalWithSmallChunk() {
            val f: (Double) -> Double = { x -> x }
            val result = GatheringTrapezoids.parallel(f, 0.0, 10.0, 1e-6, 3)
            assertEquals(50.0, result, 1e-5)
        }

        @Test
        fun testLargeIntervalWithLargeChunk() {
            val f: (Double) -> Double = { x -> x }
            val chunkSize = GatheringTrapezoids.getOptimalChunkSize(f, 0.0, 10.0, 1e-6)

            println("Optimal chunk size for large interval test: $chunkSize")

            val result = GatheringTrapezoids.parallel(f, 0.0, 10.0, 1e-6, chunkSize)
            assertEquals(50.0, result, 1e-5)
        }
    }
}