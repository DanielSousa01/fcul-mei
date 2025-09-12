/**
 *  Multiplying matrices
 *  Write a program that receives two matrices of compatible sizes ([MxN] and [NxO])
 *  and returns a new matrix resulting from the multiplication of the other two.
 *      - Write a sequential version of the program.
 *      - Write a parallel version of the program using a fixed number of threads
 *      - Find the ideal chunk size for your machine
*/

object MultiplyMatrices {

    /**
     * Multiplies two matrices sequentially.
     * @param matrix1 The first matrix (MxN).
     * @param matrix2 The second matrix (NxO).
     * @return The resulting matrix (MxO) after multiplication.
     * @throws IllegalArgumentException if the matrices have incompatible sizes.
     */
    fun sequential(matrix1: List<List<Int>>, matrix2: List<List<Int>>): List<List<Int>> {
        val rowSize1 = matrix1.size
        val colSize1 = matrix1[0].size

        val rowSize2 = matrix2.size
        val colSize2 = matrix2[0].size

        if (colSize1 != rowSize2) {
            throw IllegalArgumentException("Incompatible matrix sizes")
        }

        val result = List(rowSize1) { MutableList(colSize2) { 0 } }

        matrix1.forEachIndexed { i, row ->
            for (j in 0 until colSize2) {
                row.forEachIndexed { k, value ->
                    result[i][j] += value * matrix2[k][j]
                }
            }
        }

        return result
    }

    /**
     * Multiplies two matrices in parallel using multiple threads.
     * Each row of the resulting matrix is computed in a separate thread.
     * @param matrix1 The first matrix (MxN).
     * @param matrix2 The second matrix (NxO).
     * @return The resulting matrix (MxO) after multiplication.
     * @throws IllegalArgumentException if the matrices have incompatible sizes.
     */
    fun parallel(matrix1: List<List<Int>>, matrix2: List<List<Int>>): List<List<Int>> {
        val rowSize1 = matrix1.size
        val colSize1 = matrix1[0].size

        val rowSize2 = matrix2.size
        val colSize2 = matrix2[0].size

        if (colSize1 != rowSize2) {
            throw IllegalArgumentException("Incompatible matrix sizes")
        }

        val result = List(rowSize1) { MutableList(colSize2) { 0 } }
        val threads = mutableListOf<Thread>()

        matrix1.forEachIndexed { i, row ->
            val thread = Thread {
                for (j in 0 until colSize2) {
                    row.forEachIndexed { k, value ->
                        result[i][j] += value * matrix2[k][j]
                    }
                }
            }
            threads.add(thread)
            thread.start()
        }

        threads.forEach { it.join() }
        return result
    }

    /**
     * Multiplies two matrices in parallel using multiple threads with a specified chunk size.
     * Each thread computes a chunk of rows of the resulting matrix.
     * @param matrix1 The first matrix (MxN).
     * @param matrix2 The second matrix (NxO).
     * @param chunkSize The number of rows each thread will compute.
     * @return The resulting matrix (MxO) after multiplication.
     * @throws IllegalArgumentException if the matrices have incompatible sizes.
     */
    fun parallelWithChunkSize(matrix1: List<List<Int>>, matrix2: List<List<Int>>, chunkSize: Int): List<List<Int>> {
        val rowSize1 = matrix1.size
        val colSize1 = matrix1[0].size
        val rowSize2 = matrix2.size
        val colSize2 = matrix2[0].size

        if (colSize1 != rowSize2) {
            throw IllegalArgumentException("Incompatible matrix sizes")
        }

        val result = List(rowSize1) { MutableList(colSize2) { 0 } }
        val threads = mutableListOf<Thread>()

        for (startRow in 0 until rowSize1 step chunkSize) {
            val endRow = (startRow + chunkSize).coerceAtMost(rowSize1)

            val thread = Thread {
                for (i in startRow until endRow) {
                    for (j in 0 until colSize2) {
                        for (k in 0 until colSize1) {
                            result[i][j] += matrix1[i][k] * matrix2[k][j]
                        }
                    }
                }
            }

            threads.add(thread)
            thread.start()
        }

        threads.forEach { it.join() }
        return result
    }

    /**
     * Determines the optimal chunk size for parallel matrix multiplication based on the machine's capabilities.
     * It tests various chunk sizes and measures the execution time to find the best one.
     * @param matrix1 The first matrix (MxN).
     * @param matrix2 The second matrix (NxO).
     * @return The optimal chunk size for parallel processing.
     */
    fun getOptimalChunkSize(matrix1: List<List<Int>>, matrix2: List<List<Int>>): Int {
        val numberOfCores = Runtime.getRuntime().availableProcessors()
        val matrix1Rows = matrix1.size

        val chunckSizes = listOf(1, 2, 4, 8, 16, matrix1Rows / numberOfCores, matrix1Rows / (numberOfCores * 2))
            .filter { it in 1..matrix1Rows }
            .distinct()

        var bestChunkSize = 1
        var bestTime = Long.MAX_VALUE

        for (chunkSize in chunckSizes) {
            val times = mutableListOf<Long>()

            repeat(5) {
                val startTime = System.nanoTime()
                parallelWithChunkSize(matrix1, matrix2, chunkSize)
                val endTime = System.nanoTime()
                times.add(endTime - startTime)
            }

            val averageTime = times.average().toLong()

            if (averageTime < bestTime) {
                bestChunkSize = chunkSize
            }
        }

        return bestChunkSize
    }
}

