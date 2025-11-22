package main

import (
	"KnapsackProblemGo/knapsack"
	"KnapsackProblemGo/knapsack/sequential"
	"time"
)

func main() {
	//silent := false

	knapsack.InitiateProblem()

	knapsackGA := sequential.NewKnapsackGA()

	sequentialTimeStart := time.Now()
	knapsackGA.Run(false)
	sequentialTimeEnd := time.Now()
	sequentialDuration := sequentialTimeEnd.Sub(sequentialTimeStart)

	println("Sequential Execution Time:", sequentialDuration.Milliseconds(), "ms")
}
