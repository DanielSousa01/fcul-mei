import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Throwing darts in MonteCarlo to find pi.
 * Write a program that estimates the value of PI using a Monte Carlo simulation.
 *
 * Consider a circle with radius 1 centered at the origin of a square defined by the opposing vertices (-1,-1) and (1,1).
 *
 * By throwing randomly darts inside the square (following a uniform distribution),
 * it is possible to obtain the ratio of darts that fell inside the circle and the total number of darts.
 * From this ratio, you should derive pi.
 *
 *      - Write a sequential version of the program.
 *      - Write a parallel version of the program using a fixed number of threads
 *      - Find the ideal chunk size for your machine
 */
object ValueOfPIMonteCarlo {

    /**
     * Estimates the value of PI using a Monte Carlo simulation sequentially.
     *
     * @param totalDarts The total number of darts to throw.
     * @return The estimated value of PI.
     * @throws IllegalArgumentException if totalDarts is not positive.
     */
    fun sequential(totalDarts: Int): Double {
        require(totalDarts > 0) { "Total darts must be positive" }

        var insideCircle = 0

        repeat(totalDarts) {
            val x = Math.random() * 2 - 1
            val y = Math.random() * 2 - 1
            if (x * x + y * y <= 1) {
                insideCircle++
            }
        }

        return 4.0 * insideCircle / totalDarts
    }

    /**
     * Estimates the value of PI using a Monte Carlo simulation in parallel with a fixed number of threads.
     *
     * @param totalDarts The total number of darts to throw.
     * @return The estimated value of PI.
     * @throws IllegalArgumentException if totalDarts is not positive.
     */
    fun parallel(totalDarts: Int): Double {
        require(totalDarts > 0) { "Total darts must be positive" }

        var numThreads = 4
        if (totalDarts < numThreads) numThreads = totalDarts

        val threadPool = Executors.newFixedThreadPool(numThreads)

        val chunkSize = totalDarts / numThreads
        val insideCircleResults = MutableList(numThreads) { 0 }

        executeThreadPool(threadPool, totalDarts, numThreads, chunkSize, insideCircleResults) 

        val totalInsideCircle = insideCircleResults.sum()

        return 4.0 * totalInsideCircle / totalDarts
    }

    /**
     * Estimates the value of PI using a Monte Carlo simulation in parallel with a specified chunk size.
     *
     * @param totalDarts The total number of darts to throw.
     * @param chunkSize The number of darts each thread will process.
     * @return The estimated value of PI.
     * @throws IllegalArgumentException if totalDarts or chunkSize is not positive.
     */
    fun parallel(totalDarts: Int, chunkSize: Int): Double {
        require(totalDarts > 0) { "Total darts must be positive" }
        require(chunkSize > 0) { "Chunk size must be positive" }

        val numThreads = (totalDarts + chunkSize - 1) / chunkSize
        val threadPool = Executors.newFixedThreadPool(numThreads)

        val insideCircleResults = MutableList(numThreads) { 0 }

        executeThreadPool(threadPool, totalDarts, numThreads, chunkSize, insideCircleResults)

        val totalInsideCircle = insideCircleResults.sum()

        return 4.0 * totalInsideCircle / totalDarts
    }

    /**
     * Finds the optimal chunk size for the parallel Monte Carlo simulation by testing different chunk sizes.
     *
     * @param totalDarts The total number of darts to throw.
     * @param maxChunkSize The maximum chunk size to test.
     * @return The optimal chunk size that minimizes execution time.
     * @throws IllegalArgumentException if totalDarts or maxChunkSize is not positive.
     */
    fun getOptimalChunkSize(totalDarts: Int, maxChunkSize: Int = 10000): Int {
        require(totalDarts > 0) { "Total darts must be positive" }
        require(maxChunkSize > 0) { "Max chunk size must be positive" }

        var bestChunkSize = 1
        var bestTime = Long.MAX_VALUE

        for (chunkSize in 1..maxChunkSize step 100) {
            val startTime = System.nanoTime()
            parallel(totalDarts, chunkSize)
            val endTime = System.nanoTime()
            val duration = endTime - startTime

            if (duration < bestTime) {
                bestTime = duration
                bestChunkSize = chunkSize
            }
        }

        return bestChunkSize
    }

    private fun executeThreadPool(
        threadPool: ExecutorService,
        totalDarts: Int,
        numThreads: Int,
        chunkSize: Int,
        insideCircleResults: MutableList<Int>) {

        for (threadIndex in 0 until numThreads) {
            val start = threadIndex * chunkSize
            val end = minOf(start + chunkSize, totalDarts)

            threadPool.submit {
                var insideCircle = 0
                for (i in start until end) {
                    val x = Math.random() * 2 - 1
                    val y = Math.random() * 2 - 1

                    if (x * x + y * y <= 1) insideCircle++
                }

                insideCircleResults[threadIndex] = insideCircle
            }
        }

        threadPool.shutdown()
        threadPool.awaitTermination(Long.MAX_VALUE, java.util.concurrent.TimeUnit.NANOSECONDS)
    }
}