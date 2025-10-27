import kotlin.system.exitProcess

fun main() {
    val nCores = Runtime.getRuntime().availableProcessors()
    val coin = Coin(15)

    val repeats = 40
    for (i in 0 until repeats) {
        val coins = Coin.createRandomCoinSet(30)

        println("--- Repeat ${i + 1} ---")

        val seqInitialTime = System.nanoTime()
        val rs = coin.seq(coins, 0, 0)
        val seqEndTime = System.nanoTime() - seqInitialTime
        println("$nCores;Sequential;$seqEndTime")

        val parInitialTime = System.nanoTime()
        val rp = coin.par(coins, 0, 0)
        val parEndTime = System.nanoTime() - parInitialTime
        println("$nCores;ParallelUnsorted;$parEndTime")

        if (rp != rs) {
            println("Wrong Result!")
            exitProcess(-1)
        }
    }
}
