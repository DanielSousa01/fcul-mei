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
	silent := true

	knapsack.InitiateProblem()

	knapsackGASequential := sequential.NewKnapsackGASequential()
	knapsackGAGoroutine := goroutine.NewKnapsackGAGoroutine(500)
	knapsackGAChannel := channel.NewKnapsackGAChannel(16, 500)
	knapsackGAActor := actor.NewKnapsackGAActor(16, 500)

	//warm up
	println("Warming up...")
	for i := 1; i <= 3; i++ {
		println("Warmup iteration ", i)
		knapsackGASequential.Run(silent)
		knapsackGAGoroutine.Run(silent)
		knapsackGAChannel.Run(silent)
		knapsackGAActor.Run(silent)
		println("Iteration ", i, " done")
	}
	println("Warmup comlete.")

	sequentialTimes := make([]time.Duration, 5)
	goroutineTimes := make([]time.Duration, 5)
	channelTimes := make([]time.Duration, 5)
	actorTimes := make([]time.Duration, 5)

	for i := 0; i < 5; i++ {
		println(i+1, ": Running Sequential GA...")
		sequentialTimeStart := time.Now()
		knapsackGASequential.Run(silent)
		sequentialTimeEnd := time.Now()
		sequentialTimes[i] = sequentialTimeEnd.Sub(sequentialTimeStart)

		println(i+1, ": Running Goroutine GA...")
		goroutineTimeStart := time.Now()
		knapsackGAGoroutine.Run(silent)
		goroutineTimeEnd := time.Now()
		goroutineTimes[i] = goroutineTimeEnd.Sub(goroutineTimeStart)

		println(i+1, ": Running Channel GA...")
		channelTimeStart := time.Now()
		knapsackGAChannel.Run(silent)
		channelTimeEnd := time.Now()
		channelTimes[i] = channelTimeEnd.Sub(channelTimeStart)

		println(i+1, ": Running Actor GA...")
		actorTimeStart := time.Now()
		knapsackGAActor.Run(silent)
		actorTimeEnd := time.Now()
		actorTimes[i] = actorTimeEnd.Sub(actorTimeStart)
	}

	sequentialTimeAvg := avgMillis(sequentialTimes)
	goroutineTimeAvg := avgMillis(goroutineTimes)
	channelTimeAvg := avgMillis(channelTimes)
	actorTimeAvg := avgMillis(actorTimes)

	println("Average Times (ms):")
	println("Sequential: ", sequentialTimeAvg)
	println("Goroutine:  ", goroutineTimeAvg)
	println("Channel:    ", channelTimeAvg)
	println("Actor:      ", actorTimeAvg)
}

func avgMillis(durs []time.Duration) float64 {
	if len(durs) == 0 {
		return 0
	}
	var sum time.Duration
	for _, d := range durs {
		sum += d
	}
	return float64(sum) / float64(time.Millisecond) / float64(len(durs))
}
