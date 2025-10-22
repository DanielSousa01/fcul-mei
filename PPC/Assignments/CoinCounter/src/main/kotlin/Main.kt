import kotlin.system.exitProcess

fun main() {
    val nCores = Runtime.getRuntime().availableProcessors()
    val coin = Coin(nCores, 2)

    val coins = coin.createRandomCoinSet(30)

    val repeats = 40
    for (i in 0 until repeats) {
        val seqInitialTime = System.nanoTime()
        val rs = coin.seq(coins, 0, 0)
        val seqEndTime = System.nanoTime() - seqInitialTime
        println("$nCores;Sequential;$seqEndTime")

        val parInitialTime = System.nanoTime()
        val rp = coin.par(coins, 0, 0)
        val parEndTime = System.nanoTime() - parInitialTime
        println("$nCores;Parallel;$parEndTime")

        if (rp != rs) {
            println("Wrong Result!")
            exitProcess(-1)
        }
    }
}