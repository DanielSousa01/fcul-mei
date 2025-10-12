package benchmark

import knapsack.KnapsackGA
import knapsack.KnapsackGAForkJoin
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(3)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
open class KnapsackBenchmarkParallel {

    @Param("forkjoin1k", "forkjoin5k", "forkjoin10k", "scattergather", "masterworker")
    lateinit var implementation: String

    @Param("4", "8", "16")
    var nThreads: Int = 0

    private lateinit var knapsackGATarget: KnapsackGA

    @Setup(Level.Trial)
    fun setup() {
        knapsackGATarget = when (implementation) {
            "forkjoin1k" -> KnapsackGAForkJoin(silent = true, maxThreads = nThreads)
            "forkjoin5k" -> KnapsackGAForkJoin(silent = true, maxThreads = nThreads, threshold = 5000)
            "forkjoin10k" -> KnapsackGAForkJoin(silent = true, maxThreads = nThreads, threshold = 10000)
            "scattergather" -> knapsack.KnapsackGAScatterGather(silent = true, maxThreads = nThreads)
            "masterworker" -> knapsack.KnapsackGAMasterWorker(silent = true, maxThreads = nThreads)
            else -> throw IllegalArgumentException("Unknown implementation: $implementation")
        }
    }

    @Benchmark
    fun benchmarkKnapsackGA(bh: Blackhole) {
        val result = knapsackGATarget.run()
        bh.consume(result)
    }

}