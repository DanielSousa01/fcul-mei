package main

import (
	"KnapsackProblemGo/knapsack"
	"KnapsackProblemGo/knapsack/actor"
	"KnapsackProblemGo/knapsack/channel"
	"KnapsackProblemGo/knapsack/goroutine"
	"KnapsackProblemGo/knapsack/sequential"
	"time"
)

func main() {
	//silent := false

	knapsack.InitiateProblem()

	knapsackGASequential := sequential.NewKnapsackGASequential()
	knapsackGAGoroutine := goroutine.NewKnapsackGAGoroutine(500)
	knapsackGAChannel := channel.NewKnapsackGAChannel(16, 500)
	knapsackGAActor := actor.NewKnapsackGAActor(16, 500)

	sequentialTimeStart := time.Now()
	knapsackGASequential.Run(false)
	sequentialTimeEnd := time.Now()
	sequentialDuration := sequentialTimeEnd.Sub(sequentialTimeStart)

	println()

	goroutineTimeStart := time.Now()
	knapsackGAGoroutine.Run(false)
	goroutineTimeEnd := time.Now()
	goroutineDuration := goroutineTimeEnd.Sub(goroutineTimeStart)

	println()

	channelTimeStart := time.Now()
	knapsackGAChannel.Run(false)
	channelTimeEnd := time.Now()
	channelDuration := channelTimeEnd.Sub(channelTimeStart)

	println()

	actorTimeStart := time.Now()
	knapsackGAActor.Run(false)
	actorTimeEnd := time.Now()
	actorDuration := actorTimeEnd.Sub(actorTimeStart)

	println()

	println("Sequential Execution Time:", sequentialDuration.Milliseconds(), "ms")
	println("Goroutine Execution Time:", goroutineDuration.Milliseconds(), "ms")
	println("Channel Execution Time:", channelDuration.Milliseconds(), "ms")
	println("Actor Execution Time:", actorDuration.Milliseconds(), "ms")
}
