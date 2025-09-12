/**
 * Gathering trapezoids together
 * Write a program that estimates the integral of a given function f, using the trapezoid rule.
 *
 * Your function should receive:
 *      - The function (You can use f x = x * (x-1), but it should work for any function)
 *      - The lower bound of the integral (0.0)
 *      - The upper bound of the integral (1.0)
 *      - The resolution (10^-7)
 *
 * The result should be a float approximating the integral of that function, 0.1(6) in that example.
 *      - Write a sequential version of the program.
 *      - Write a parallel version of the program using a fixed number of threads
 *      - Find the ideal chunk size for your machine
 *
 */
object GatheringTrapezoids {

    /**
     * Estimates the integral of a given function f using the trapezoid rule sequentially.
     *
     * @param f The function to integrate.
     * @param lowerBound The lower bound of the integral.
     * @param upperBound The upper bound of the integral.
     * @param resolution The desired resolution for the approximation.
     * @return The estimated integral of the function f from lowerBound to upperBound.
     * @throws IllegalArgumentException if lowerBound is not less than upperBound or if resolution is not positive.
     */
    fun sequential(
        f: (Double) -> Double,
        lowerBound: Double,
        upperBound: Double,
        resolution: Double
    ): Double {
        validateParameters(lowerBound, upperBound, resolution)

        var trapezoidNum = 1
        var trapezoidWidth = (upperBound - lowerBound) / trapezoidNum

        var prev = 0.5 * (f(lowerBound) + f(upperBound)) * trapezoidWidth

        while (true) {
            trapezoidNum *= 2
            trapezoidWidth = (upperBound - lowerBound) / trapezoidNum

            var sum = 0.0

            for (i in 1 until trapezoidNum step 2) {
                val x = lowerBound + i * trapezoidWidth
                sum += f(x)
            }

            val current = 0.5 * prev + sum * trapezoidWidth
            if (kotlin.math.abs(current - prev) < resolution) return current

            prev = current
        }
    }

    /**
     * Estimates the integral of a given function f using the trapezoid rule in parallel with a fixed number of threads.
     *
     * @param f The function to integrate.
     * @param lowerBound The lower bound of the integral.
     * @param upperBound The upper bound of the integral.
     * @param resolution The desired resolution for the approximation.
     * @return The estimated integral of the function f from lowerBound to upperBound.
     * @throws IllegalArgumentException if lowerBound is not less than upperBound or if resolution is not positive.
     */
    fun parallel(
        f: (Double) -> Double,
        lowerBound: Double,
        upperBound: Double,
        resolution: Double,
    ): Double {
        validateParameters(lowerBound, upperBound, resolution)

        val numberOfThreads = 4

        var trapezoidNum = 1
        var trapezoidWidth = (upperBound - lowerBound) / trapezoidNum
        var prev = 0.5 * (f(lowerBound) + f(upperBound)) * trapezoidWidth

        while (true) {
            trapezoidNum *= 2
            trapezoidWidth = (upperBound - lowerBound) / trapezoidNum

            val oddPoints = (1 until trapezoidNum step 2).toList()

            val chunkSize = oddPoints.size / numberOfThreads + 1
            val threads = mutableListOf<Thread>()
            val results = DoubleArray(numberOfThreads) { 0.0 }

            for (threadIndex in 0 until numberOfThreads) {
                val start = threadIndex * chunkSize
                val end = minOf((threadIndex + 1) * chunkSize, oddPoints.size)

                if (start < oddPoints.size) {
                    val thread = Thread {
                        var sum = 0.0
                        for (i in start until end) {
                            val oddPoint = oddPoints[i]
                            val x = lowerBound + oddPoint * trapezoidWidth
                            sum += f(x)
                        }
                        results[threadIndex] = sum
                    }
                    threads.add(thread)
                    thread.start()
                }
            }

            threads.forEach { it.join() }
            val totalSum = results.sum()
            val current = 0.5 * prev + totalSum * trapezoidWidth

            if (kotlin.math.abs(current - prev) < resolution) return current

            prev = current
        }
    }

    /**
     * Estimates the integral of a given function f using the trapezoid rule in parallel with a specified chunk size.
     *
     * @param f The function to integrate.
     * @param lowerBound The lower bound of the integral.
     * @param upperBound The upper bound of the integral.
     * @param resolution The desired resolution for the approximation.
     * @param chunkSize The number of odd points each thread will process.
     * @return The estimated integral of the function f from lowerBound to upperBound.
     * @throws IllegalArgumentException if lowerBound is not less than upperBound or if resolution is not positive.
     */
    fun parallel(
        f: (Double) -> Double,
        lowerBound: Double,
        upperBound: Double,
        resolution: Double,
        chunkSize: Int
    ): Double {
        validateParameters(lowerBound, upperBound, resolution)

        var trapezoidNum = 1
        var trapezoidWidth = (upperBound - lowerBound) / trapezoidNum
        var prev = 0.5 * (f(lowerBound) + f(upperBound)) * trapezoidWidth

        while (true) {
            trapezoidNum *= 2
            trapezoidWidth = (upperBound - lowerBound) / trapezoidNum

            val oddPoints = (1 until trapezoidNum step 2).toList()

            val threads = mutableListOf<Thread>()
            val numberOfThreads = (oddPoints.size + chunkSize - 1) / chunkSize
            val results = DoubleArray(numberOfThreads) { 0.0 }

            for (threadIndex in 0 until numberOfThreads) {
                val start = threadIndex * chunkSize
                val end = minOf((threadIndex + 1) * chunkSize, oddPoints.size)

                if (start < oddPoints.size) {
                    val thread = Thread {
                        var sum = 0.0
                        for (i in start until end) {
                            val oddPoint = oddPoints[i]
                            val x = lowerBound + oddPoint * trapezoidWidth
                            sum += f(x)
                        }
                        results[threadIndex] = sum
                    }
                    threads.add(thread)
                    thread.start()
                }
            }

            threads.forEach { it.join() }
            val totalSum = results.sum()
            val current = 0.5 * prev + totalSum * trapezoidWidth

            if (kotlin.math.abs(current - prev) < resolution) return current

            prev = current
        }
    }

    /**
     * Finds the optimal chunk size for parallel execution by testing different chunk sizes and measuring execution time.
     *
     * @param f The function to integrate.
     * @param lowerBound The lower bound of the integral.
     * @param upperBound The upper bound of the integral.
     * @param resolution The desired resolution for the approximation.
     * @param minChunkSize The minimum chunk size to test.
     * @param maxChunkSize The maximum chunk size to test.
     * @param step The step size for incrementing chunk sizes during testing.
     * @return The optimal chunk size that resulted in the fastest execution time.
     */
    fun getOptimalChunkSize(
        f: (Double) -> Double,
        lowerBound: Double,
        upperBound: Double,
        resolution: Double,
        minChunkSize: Int = 1,
        maxChunkSize: Int = 1000,
        step: Int = 10
    ): Int {
        var bestChunkSize = minChunkSize
        var bestTime = Long.MAX_VALUE

        for (chunkSize in minChunkSize..maxChunkSize step step) {
            val startTime = System.nanoTime()
            parallel(f, lowerBound, upperBound, resolution, chunkSize)
            val endTime = System.nanoTime()
            val duration = endTime - startTime

            if (duration < bestTime) {
                bestTime = duration
                bestChunkSize = chunkSize
            }
        }

        return bestChunkSize
    }

    /** Validates the input parameters for the integration functions.
     *
     * @param lowerBound The lower bound of the integral.
     * @param upperBound The upper bound of the integral.
     * @param resolution The desired resolution for the approximation.
     * @throws IllegalArgumentException if lowerBound is not less than upperBound or if resolution is not positive.
     */
    private fun validateParameters(lowerBound: Double, upperBound: Double, resolution: Double) {
        require(lowerBound < upperBound) { "Lower bound must be less than upper bound" }
        require(resolution > 0) { "Resolution must be positive" }
    }
}



