import knapsack.KnapsackGAMasterWorker
import knapsack.KnapsackGASequential

fun main() {
    val knapsackGASequential = KnapsackGASequential()
    val knapsackGAMasterWorker = KnapsackGAMasterWorker()
    val knapsackGAForkJoin = knapsack.KnapsackGAForkJoin()
    val knapsackGAScatterGather = knapsack.KnapsackGAScatterGather()

    println("Running Knapsack GA Sequential")
    knapsackGASequential.run()

    println("Running Knapsack GA Master Worker")
    knapsackGAMasterWorker.run()

    println("Running Knapsack GA Fork Join")
    knapsackGAForkJoin.run()

    println("Running Knapsack GA Scatter Gather")
    knapsackGAScatterGather.run()
}
