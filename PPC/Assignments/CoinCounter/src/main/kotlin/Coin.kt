import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RecursiveTask
import kotlin.math.max

class Coin(
    private val threshold: Int = THRESHOLD
) {
    private val maxThreads: Int = Runtime.getRuntime().availableProcessors()
    private val threadPool: ForkJoinPool = ForkJoinPool(maxThreads)

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
        val task = CoinTask(coins, index, accumulator)
        return threadPool.invoke(task)
    }

    private inner class CoinTask(
        private val coins: IntArray,
        private val index: Int,
        private val accumulator: Int
    ) : RecursiveTask<Int>() {
        override fun compute(): Int {
            if (index >= coins.size) {
                return if (accumulator < LIMIT) accumulator else -1
            }

            if (accumulator + coins[index] > LIMIT) {
                return -1
            }

            if (coins.size - index <= threshold) {
                return seq(coins, index, accumulator)
            }

            return computeWork(coins, index, accumulator)
        }

        private fun computeWork(coins: IntArray, index: Int, accumulator: Int): Int {
            val leftTask = CoinTask(coins, index + 1, accumulator)
            val rightTask = CoinTask(coins, index + 1, accumulator + coins[index])

            leftTask.fork()
            val rightResult = rightTask.compute()
            val leftResult = leftTask.join()

            return max(leftResult, rightResult)
        }
    }

    companion object {
        private const val LIMIT: Int = 999
        const val THRESHOLD: Int = 15
    }
}