import knapsack.channel.KnapsackGAChannel
import knapsack.coroutine.KnapsackGACoroutine
import knapsack.sequential.KnapsackGASequential

fun main() {
    val silent = true
    val knapsackGASequential = KnapsackGASequential(silent = silent)
    val knapsackGACoroutine = KnapsackGACoroutine(silent = silent, chunkSize = 500)
    val knapsackGAChannel = KnapsackGAChannel(silent = silent, chunkSize = 500)
    val knapsackGAActor = knapsack.actor.KnapsackGAActor(silent = silent, chunkSize = 500)

//  warmup
    println("Warming up...")
    for (i in 1..3) {
        println("Warmup iteration $i")
        knapsackGASequential.run()
        knapsackGACoroutine.run()
        knapsackGAChannel.run()
        knapsackGAActor.run()
    }
    println("Warmup complete.")

    val sequentialTimes = mutableListOf<Long>()
    val coroutineTimes = mutableListOf<Long>()
    val channelTimes = mutableListOf<Long>()
    val actorTimes = mutableListOf<Long>()

    for (i in 1..5) {
        println("$i: Running Knapsack GA Sequential")
        val sequentialTimeStart = System.currentTimeMillis()
        knapsackGASequential.run()
        val sequentialTimeEnd = System.currentTimeMillis()
        sequentialTimes.add(sequentialTimeEnd - sequentialTimeStart)

        println("$i: Running Knapsack GA Coroutine")
        val coroutineTimeStart = System.currentTimeMillis()
        knapsackGACoroutine.run()
        val coroutineTimeEnd = System.currentTimeMillis()
        coroutineTimes.add(coroutineTimeEnd - coroutineTimeStart)

        println("$i: Running Knapsack GA Channel")
        val channelTimeStart = System.currentTimeMillis()
        knapsackGAChannel.run()
        val channelTimeEnd = System.currentTimeMillis()
        channelTimes.add(channelTimeEnd - channelTimeStart)

        println("$i: Running Knapsack GA Actor")
        val actorTimeStart = System.currentTimeMillis()
        knapsackGAActor.run()
        val actorTimeEnd = System.currentTimeMillis()
        actorTimes.add(actorTimeEnd - actorTimeStart)
    }

    val sequenceTimeAvg = sequentialTimes.average().toLong()
    val coroutineTimeAvg = coroutineTimes.average().toLong()
    val channelTimeAvg = channelTimes.average().toLong()
    val actorTimeAvg = actorTimes.average().toLong()

    println("Average Sequential Time: $sequenceTimeAvg ms")
    println("Average Coroutine Time: $coroutineTimeAvg ms")
    println("Average Channel Time: $channelTimeAvg ms")
    println("Average Actor Time: $actorTimeAvg ms")
}
