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

    @Param("sequential", "forkjoin")
    lateinit var implementation: String

    private lateinit var knapsackGATarget: KnapsackGA

    @Setup(Level.Trial)
    fun setup() {
        System.setOut(PrintStream(ByteArrayOutputStream()))
        knapsackGATarget = when (implementation) {
            "sequential" -> KnapsackGASequential(silent = true)
            "forkjoin" -> KnapsackGAForkJoin(silent = true)
            else -> throw IllegalArgumentException("Unknown implementation: $implementation")
        }
    }


    @Benchmark
    fun benchmarkKnapsackGA(bh: Blackhole) {
        val result = knapsackGATarget.run()
        bh.consume(result)
    }

//    @Benchmark
//    @Fork(jvmArgs = ["-XX:+UseParallelGC"])
//    fun benchmarkKnapsackGAWithParallelGC(bh: Blackhole) {
//        val result = knapsackGATarget.run()
//        bh.consume(result)
//    }
//
//    @Benchmark
//    @Fork(jvmArgs = ["-XX:+UseG1GC"])
//    fun benchmarkKnapsackGAWithG1GC(bh: Blackhole) {
//        val result = knapsackGATarget.run()
//        bh.consume(result)
//    }
}