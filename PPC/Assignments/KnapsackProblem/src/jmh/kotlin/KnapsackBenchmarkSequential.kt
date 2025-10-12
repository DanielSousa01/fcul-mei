package benchmark

import knapsack.KnapsackGA
import knapsack.KnapsackGASequential
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(3)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
open class KnapsackBenchmarkSequential {

    private lateinit var knapsackGATarget: KnapsackGA

    @Setup(Level.Trial)
    fun setup() {
        knapsackGATarget = KnapsackGASequential(silent = true)
    }

    @Benchmark
    fun benchmarkKnapsackGA(bh: Blackhole) {
        val result = knapsackGATarget.run()
        bh.consume(result)
    }

}