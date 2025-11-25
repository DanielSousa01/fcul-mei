import knapsack.channel.KnapsackGAChannel
import knapsack.coroutine.KnapsackGACoroutine
import knapsack.sequential.KnapsackGASequential

fun main() {
    val silent = false
    val knapsackGASequential = KnapsackGASequential(silent = silent)
    val knapsackGACoroutine = KnapsackGACoroutine(silent = silent, chunkSize = 500)
    val knapsackGAChannel = KnapsackGAChannel(silent = silent, chunkSize = 500)
    val knapsackGAActor = knapsack.actor.KnapsackGAActor(silent = silent, chunkSize = 500)

//    val sequentialTimeStart = System.currentTimeMillis()
//    println("Running Knapsack GA Sequential")
//    knapsackGASequential.run()
//    val sequentialTimeEnd = System.currentTimeMillis()
//
//    val coroutineTimeStart = System.currentTimeMillis()
//    println("Running Knapsack GA Coroutine")
//    knapsackGACoroutine.run()
//    val coroutineTimeEnd = System.currentTimeMillis()
//
//    val channelTimeStart = System.currentTimeMillis()
//    println("Running Knapsack GA Channel")
//    knapsackGAChannel.run()
//    val channelTimeEnd = System.currentTimeMillis()

    val actorTimeStart = System.currentTimeMillis()
    println("Running Knapsack GA Actor")
    knapsackGAActor.run()
    val actorTimeEnd = System.currentTimeMillis()

//    println("Sequential Time: ${sequentialTimeEnd - sequentialTimeStart} ms")
//    println("Coroutine Time: ${coroutineTimeEnd - coroutineTimeStart} ms")
//    println("Channel Time: ${channelTimeEnd - channelTimeStart} ms")
    println("Actor Time: ${actorTimeEnd - actorTimeStart} ms")
}
