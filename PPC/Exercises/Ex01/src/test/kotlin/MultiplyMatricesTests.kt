import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MultiplyMatricesTests {

    @Nested
    inner class SequentialMultiplicationTests {
        @Test
        fun testSimpleMultiplication() {
            val matrix1 = listOf(
                listOf(1, 2, 3),
                listOf(4, 5, 6)
            )

            val matrix2 = listOf(
                listOf(7, 8),
                listOf(9, 10),
                listOf(11, 12)
            )

            val expected = listOf(
                listOf(58, 64),
                listOf(139, 154)
            )

            val result = MultiplyMatrices.sequential(matrix1, matrix2)
            assertEquals(expected, result)
        }

        @Test
        fun testIdentityMatrix() {
            val matrix = listOf(
                listOf(1, 2),
                listOf(3, 4)
            )
            val identity = listOf(
                listOf(1, 0),
                listOf(0, 1)
            )

            val result = MultiplyMatrices.sequential(matrix, identity)
            assertEquals(matrix, result)
        }

        @Test
        fun testIncompatibleSizes() {
            val matrix1 = listOf(
                listOf(1, 2)
            )
            val matrix2 = listOf(
                listOf(1, 2),
                listOf(3, 4),
                listOf(5, 6)
            )

            assertFailsWith<IllegalArgumentException> {
                MultiplyMatrices.sequential(matrix1, matrix2)
            }
        }
    }

    @Nested
    inner class ParallelMultiplicationTests {
        @Test
        fun testParallelSimpleMultiplication() {
            val matrix1 = listOf(
                listOf(1, 2, 3),
                listOf(4, 5, 6)
            )

            val matrix2 = listOf(
                listOf(7, 8),
                listOf(9, 10),
                listOf(11, 12)
            )

            val expected = listOf(
                listOf(58, 64),
                listOf(139, 154)
            )

            val result = MultiplyMatrices.parallel(matrix1, matrix2)
            assertEquals(expected, result)
        }

        @Test
        fun testParallelIdentityMatrix() {
            val matrix = listOf(
                listOf(1, 2),
                listOf(3, 4)
            )
            val identity = listOf(
                listOf(1, 0),
                listOf(0, 1)
            )

            val result = MultiplyMatrices.parallel(matrix, identity)
            assertEquals(matrix, result)
        }

        @Test
        fun testParallelIncompatibleSizes() {
            val matrix1 = listOf(
                listOf(1, 2)
            )
            val matrix2 = listOf(
                listOf(1, 2),
                listOf(3, 4),
                listOf(5, 6)
            )

            assertFailsWith<IllegalArgumentException> {
                MultiplyMatrices.parallel(matrix1, matrix2)
            }
        }

        @Test
        fun testParallelVsSequential() {
            val matrix1 = listOf(
                listOf(1, 2, 3, 4),
                listOf(5, 6, 7, 8),
                listOf(9, 10, 11, 12)
            )

            val matrix2 = listOf(
                listOf(1, 0, 1),
                listOf(0, 1, 0),
                listOf(1, 1, 1),
                listOf(0, 0, 1)
            )

            val sequentialResult = MultiplyMatrices.sequential(matrix1, matrix2)
            val parallelResult = MultiplyMatrices.parallel(matrix1, matrix2)

            assertEquals(sequentialResult, parallelResult)
        }

        @Test
        fun testParallelLargerMatrix() {
            val matrix1 = listOf(
                listOf(1, 2, 3),
                listOf(4, 5, 6),
                listOf(7, 8, 9),
                listOf(10, 11, 12)
            )

            val matrix2 = listOf(
                listOf(1, 0),
                listOf(0, 1),
                listOf(1, 1)
            )

            val expected = listOf(
                listOf(4, 5),
                listOf(10, 11),
                listOf(16, 17),
                listOf(22, 23)
            )

            val result = MultiplyMatrices.parallel(matrix1, matrix2)
            assertEquals(expected, result)
        }

    }

    @Nested
    inner class ParallelMultiplicationWithChunksTests {

        @Test
        fun testParallelWithChunkSizeSimpleMultiplication() {
            val matrix1 = listOf(
                listOf(1, 2, 3),
                listOf(4, 5, 6)
            )

            val matrix2 = listOf(
                listOf(7, 8),
                listOf(9, 10),
                listOf(11, 12)
            )

            val expected = listOf(
                listOf(58, 64),
                listOf(139, 154)
            )

            val result = MultiplyMatrices.parallel(matrix1, matrix2, 1)
            assertEquals(expected, result)
        }

        @Test
        fun testParallelWithChunkSizeIdentityMatrix() {
            val matrix = listOf(
                listOf(1, 2),
                listOf(3, 4)
            )
            val identity = listOf(
                listOf(1, 0),
                listOf(0, 1)
            )

            val result = MultiplyMatrices.parallel(matrix, identity, 2)
            assertEquals(matrix, result)
        }

        @Test
        fun testParallelWithChunkSizeIncompatibleSizes() {
            val matrix1 = listOf(
                listOf(1, 2)
            )
            val matrix2 = listOf(
                listOf(1, 2),
                listOf(3, 4),
                listOf(5, 6)
            )

            assertFailsWith<IllegalArgumentException> {
                MultiplyMatrices.parallel(matrix1, matrix2, 1)
            }
        }

        @Test
        fun testParallelWithChunkSizeVsSequential() {
            val matrix1 = listOf(
                listOf(1, 2, 3, 4),
                listOf(5, 6, 7, 8),
                listOf(9, 10, 11, 12),
                listOf(13, 14, 15, 16)
            )

            val matrix2 = listOf(
                listOf(1, 0, 1),
                listOf(0, 1, 0),
                listOf(1, 1, 1),
                listOf(0, 0, 1)
            )

            val sequentialResult = MultiplyMatrices.sequential(matrix1, matrix2)
            val chunkResult = MultiplyMatrices.parallel(matrix1, matrix2, 2)

            assertEquals(sequentialResult, chunkResult)
        }

        @Test
        fun testParallelWithChunkSizeDifferentChunkSizes() {
            val matrix1 = listOf(
                listOf(1, 2, 3),
                listOf(4, 5, 6),
                listOf(7, 8, 9),
                listOf(10, 11, 12)
            )

            val matrix2 = listOf(
                listOf(1, 0),
                listOf(0, 1),
                listOf(1, 1)
            )

            val expected = listOf(
                listOf(4, 5),
                listOf(10, 11),
                listOf(16, 17),
                listOf(22, 23)
            )

            val chunkSizes = listOf(1, 2, 4, 8)

            for (chunkSize in chunkSizes) {
                val result = MultiplyMatrices.parallel(matrix1, matrix2, chunkSize)
                assertEquals(expected, result, "Failed for chunk size $chunkSize")
            }
        }

        @Test
        fun testParallelWithChunkSizeLargerThanMatrix() {
            val matrix1 = listOf(
                listOf(1, 2),
                listOf(3, 4)
            )

            val matrix2 = listOf(
                listOf(5, 6),
                listOf(7, 8)
            )

            val expected = listOf(
                listOf(19, 22),
                listOf(43, 50)
            )

            // Chunk size maior que o número de linhas
            val result = MultiplyMatrices.parallel(matrix1, matrix2, 10)
            assertEquals(expected, result)
        }

        @Test
        fun testParallelWithChunkSizeSingleElement() {
            val matrix1 = listOf(listOf(5))
            val matrix2 = listOf(listOf(3))
            val expected = listOf(listOf(15))

            val result = MultiplyMatrices.parallel(matrix1, matrix2, 1)
            assertEquals(expected, result)
        }

        @Test
        fun testWithOptimalChunkSize() {
            val matrix1 = List(100) { List(100) { it } }
            val matrix2 = List(100) { List(100) { it } }

            val optimalChunkSize = MultiplyMatrices.getOptimalChunkSize(matrix1, matrix2)
            println("Optimal Chunk Size: $optimalChunkSize")

            assert(optimalChunkSize in 1..100)
        }

        @Test
        fun testLargeMatrixWithOptimalChunkSize() {
            val matrix1 = List(200) { List(200) { it } }
            val matrix2 = List(200) { List(200) { it } }

            val optimalChunkSize = MultiplyMatrices.getOptimalChunkSize(matrix1, matrix2)
            val result = MultiplyMatrices.parallel(matrix1, matrix2, optimalChunkSize)
            val expected = MultiplyMatrices.sequential(matrix1, matrix2)

            assertEquals(expected, result)
        }

    }


}

