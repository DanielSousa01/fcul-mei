import knapsack.KnapsackGACoroutine
import knapsack.KnapsackGASequential

fun main() {
    val knapsackGASequential = KnapsackGASequential(silent = true)
    val knapsackGACoroutine = KnapsackGACoroutine(silent = true, chunkSize = 500)

    val coroutineTimeStart = System.currentTimeMillis()
    println("Running Knapsack GA Coroutine")
    knapsackGACoroutine.run()
    val coroutineTimeEnd = System.currentTimeMillis()

    val sequentialTimeStart = System.currentTimeMillis()
    println("Running Knapsack GA Sequential")
    knapsackGASequential.run()
    val sequentialTimeEnd = System.currentTimeMillis()

    println("Sequential Time: ${sequentialTimeEnd - sequentialTimeStart} ms")
    println("Coroutine Time: ${coroutineTimeEnd - coroutineTimeStart} ms")
}
