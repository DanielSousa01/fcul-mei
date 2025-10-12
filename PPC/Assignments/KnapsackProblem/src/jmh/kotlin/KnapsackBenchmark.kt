package benchmark

import knapsack.KnapsackGA
import knapsack.KnapsackGAForkJoin
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
open class KnapsackBenchmark {

    @Param(
        "sequential",
        "forkjoin1k16Threads", "forkjoin5k16Threads", "forkjoin10k16Threads",
        "scattergather16Threads", "masterworker16Threads",
        "forkjoin1k8Threads", "forkjoin5k8Threads", "forkjoin10k8Threads",
        "scattergather8Threads", "masterworker8Threads",
        "forkjoin1k4Threads", "forkjoin5k4Threads", "forkjoin10k4Threads",
        "scattergather4Threads", "masterworker4Threads"
    )
    lateinit var implementation: String

    private lateinit var knapsackGATarget: KnapsackGA

    @Setup(Level.Trial)
    fun setup() {
        knapsackGATarget = when (implementation) {
            "sequential" -> KnapsackGASequential(silent = true)
            "forkjoin1k16Threads" -> KnapsackGAForkJoin(silent = true)
            "forkjoin5k16Threads" -> KnapsackGAForkJoin(silent = true, threshold = 5000)
            "forkjoin10k16Threads" -> KnapsackGAForkJoin(silent = true, threshold = 10000)
            "scattergather16Threads" -> knapsack.KnapsackGAScatterGather(silent = true)
            "masterworker16Threads" -> knapsack.KnapsackGAMasterWorker(silent = true)
            "forkjoin1k8Threads" -> KnapsackGAForkJoin(silent = true, maxThreads = 8)
            "forkjoin5k8Threads" -> KnapsackGAForkJoin(silent = true, maxThreads = 8, threshold = 5000)
            "forkjoin10k8Threads" -> KnapsackGAForkJoin(silent = true, maxThreads = 8, threshold = 10000)
            "scattergather8Threads" -> knapsack.KnapsackGAScatterGather(silent = true, maxThreads = 8)
            "masterworker8Threads" -> knapsack.KnapsackGAMasterWorker(silent = true, maxThreads = 8)
            "forkjoin1k4Threads" -> KnapsackGAForkJoin(silent = true, maxThreads = 4)
            "forkjoin5k4Threads" -> KnapsackGAForkJoin(silent = true, maxThreads = 4, threshold = 5000)
            "forkjoin10k4Threads" -> KnapsackGAForkJoin(silent = true, maxThreads = 4, threshold = 10000)
            "scattergather4Threads" -> knapsack.KnapsackGAScatterGather(silent = true, maxThreads = 4)
            "masterworker4Threads" -> knapsack.KnapsackGAMasterWorker(silent = true, maxThreads = 4)
            else -> throw IllegalArgumentException("Unknown implementation: $implementation")
        }
    }


    @Benchmark
    fun benchmarkKnapsackGA16Threads(bh: Blackhole) {
        val result = knapsackGATarget.run()
        bh.consume(result)
    }

}