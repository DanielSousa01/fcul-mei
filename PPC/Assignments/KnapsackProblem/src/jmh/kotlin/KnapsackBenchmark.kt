package benchmark

import knapsack.KnapsackGA
import knapsack.KnapsackGAForkJoin
import knapsack.KnapsackGASequential
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(3)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
open class KnapsackBenchmark {

    @Param("sequential", "masterworker", "scattergather", "forkjoin1k", "forkjoin5k", "forkjoin10k", "streams")
    lateinit var implementation: String

    private lateinit var knapsackGATarget: KnapsackGA

    @Setup(Level.Trial)
    fun setup() {
        System.setOut(PrintStream(ByteArrayOutputStream()))
        knapsackGATarget = when (implementation) {
            "sequential" -> KnapsackGASequential(silent = true)
            "forkjoin1k" -> KnapsackGAForkJoin(silent = true)
            "forkjoin5k" -> KnapsackGAForkJoin(silent = true, threshold = 5000)
            "forkjoin10k" -> KnapsackGAForkJoin(silent = true, threshold = 10000)
            "scattergather" -> knapsack.KnapsackGAScatterGather(silent = true)
            "masterworker" -> knapsack.KnapsackGAMasterWorker(silent = true)
            "streams" -> knapsack.KnapsackGAStreams(silent = true)
            else -> throw IllegalArgumentException("Unknown implementation: $implementation")
        }
    }


    @Benchmark
    fun benchmarkKnapsackGA(bh: Blackhole) {
        val result = knapsackGATarget.run()
        bh.consume(result)
    }

}