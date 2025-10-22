import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(3)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
open class CoinSeqBenchmark {

    @Param("30", "50", "100", "300", "500")
    var coinsSizer = 0

    private lateinit var coins: IntArray
    private val coin = Coin()

    @Setup(Level.Trial)
    fun setup() {
        coins = coin.createRandomCoinSet(coinsSizer)
    }

    @Benchmark
    fun benchmark(bh: Blackhole) {
        val result = coin.seq(coins, 0, 0)
        bh.consume(result)
    }
}