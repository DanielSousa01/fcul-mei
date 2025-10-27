import kotlin.system.exitProcess

fun main() {
    val nCores = Runtime.getRuntime().availableProcessors()
    val coin = Coin(10)

    val coins = Coin.createRandomCoinSet(20)
    println("Coins size: ${coins.size}")

    val repeats = 40
    for (i in 0 until repeats) {
        val seqInitialTime = System.nanoTime()
        val rs = coin.seq(coins, 0, 0)
        val seqEndTime = System.nanoTime() - seqInitialTime
        println("$nCores;Sequential;$seqEndTime")

        val parInitialTime = System.nanoTime()
        val rp = coin.parSorted(coins, 0, 0)
        val parEndTime = System.nanoTime() - parInitialTime
        println("$nCores;Parallel;$parEndTime")

        if (rp != rs) {
            println("Wrong Result!")
            exitProcess(-1)
        }
    }
}
