import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RecursiveTask
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max

class Coin(
    private val threshold: Int = 15
) {
    private val maxThreads: Int = Runtime.getRuntime().availableProcessors()
    private val threadPool: ForkJoinPool = ForkJoinPool(maxThreads)

    private val memory = ConcurrentHashMap<Pair<Int, Int>, Int>()
    private val bestSoFar = AtomicInteger(0)

    fun seq(coins: IntArray, index: Int, accumulator: Int): Int {
        if (index >= coins.size) {
            return if (accumulator < LIMIT) accumulator else -1
        }

        if (accumulator + coins[index] > LIMIT) {
            return -1
        }

        val a = seq(coins, index + 1, accumulator)
        val b = seq(coins, index + 1, accumulator + coins[index])

        return max(a, b)
    }

    fun par(coins: IntArray, index: Int, accumulator: Int): Int {
        memory.clear()
        bestSoFar.set(0)

        val task = CoinTask(coins, index, accumulator)
        return threadPool.invoke(task)
    }

    private inner class CoinTask(
        private val coins: IntArray,
        private val index: Int,
        private val accumulator: Int,
    ) : RecursiveTask<Int>() {
        private val key: Pair<Int, Int> = Pair(index, accumulator)

        override fun compute(): Int {
            memory[key]?.let {
                return it
            }

            if (index >= coins.size) {
                val result = if (accumulator < LIMIT) accumulator else -1

                if (result > bestSoFar.get()) {
                    bestSoFar.updateAndGet { currentBest -> max(currentBest, result) }
                }

                memory[key] = result
                return result
            }

            if (accumulator + coins[index] > LIMIT) {
                memory[key] = -1
                return -1
            }

            if (coins.size - index <= threshold) {
                val result = seq(coins, index, accumulator)
                memory[key] = result
                return result
            }

            return computeWork(coins, index, accumulator)
        }

        private fun computeWork(coins: IntArray, index: Int, accumulator: Int): Int {
            val leftTask = CoinTask(coins, index + 1, accumulator)
            val rightTask = CoinTask(coins, index + 1, accumulator + coins[index])

            leftTask.fork()
            val rightResult = rightTask.compute()
            val leftResult = leftTask.join()

            val result = max(leftResult, rightResult)
            memory[key] = result
            return result
        }
    }

    companion object {
        const val LIMIT: Int = 999

        fun createRandomCoinSet(N: Int): IntArray {
            val r = IntArray(N)
            for (i in 0 until N) {
                if (i % 10 == 0) {
                    r[i] = 400
                } else {
                    r[i] = 4
                }
            }
            return r
        }
    }
}