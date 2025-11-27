package knapsack.actor

import Individual
import KnapsackGA
import akka.actor.ActorSystem
import akka.pattern.Patterns
import akka.util.Timeout
import knapsack.actor.actors.MasterActor
import knapsack.actor.actors.MasterActor.Companion.Finished
import knapsack.actor.actors.MasterActor.Companion.Start
import scala.concurrent.Await
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration as ScalaDuration
import java.time.Duration as JavaDuration

class KnapsackGAActor(
    override val silent: Boolean = false,
    private val nWorkers: Int = Runtime.getRuntime().availableProcessors(),
    val chunkSize: Int
) : KnapsackGA {

    override fun run(): Individual {
        val system = ActorSystem.create("KnapsackSystem")
        val timeout = Timeout.create(JavaDuration.ofMinutes(5))

        val masterActor = system.actorOf(
            akka.actor.Props.create(
                MasterActor::class.java,
                chunkSize,
                silent,
                nWorkers
            ),
            "masterActor"
        )

        val future = Patterns.ask(masterActor, Start(), timeout)
        val result = Await.result(future, ScalaDuration.create(5, TimeUnit.MINUTES)) as Finished

        system.terminate()
        Await.ready(system.whenTerminated(), ScalaDuration.Inf())

        return result.best
    }
}
