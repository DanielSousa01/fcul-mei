package benchmark

import Coin
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(3)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
open class CoinPar30Benchmark {

    val coinsSize = 30

    @Param("3", "7", "15")
    var threshold = 0

    private val coins: IntArray = Coin.createRandomCoinSet(coinsSize)
    private lateinit var coin : Coin

    @Setup(Level.Trial)
    fun setup() {
        coin = Coin(threshold)
    }

    @Benchmark
    fun benchmark(bl: Blackhole) {
        val result = coin.par(coins, 0, 0)
        bl.consume(result)
    }
}