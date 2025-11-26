mod knapsack;

use std::time::{Duration, Instant};
use knapsack::sequential::knapsack_ga_sequential::new_knapsack_ga_sequential;
use knapsack::channel::knapsack_ga_channel::new_knapsack_ga_channel;
use knapsack::coroutine::knapsack_ga_coroutine::new_knapsack_ga_coroutine;

fn main() {
    println!("Starting Knapsack Genetic Algorithm...");

    let mut ga_sequential = new_knapsack_ga_sequential();
    let mut ga_channel = new_knapsack_ga_channel(16, 500);
    let mut ga_coroutine = new_knapsack_ga_coroutine(500);

    println!("Warming up...");
    for i in 0..3 {
        println!("Warm-up iteration {:?}", i);
        ga_sequential.run(true);
        ga_channel.run(true);
        ga_coroutine.run(true);
    }
    println!("Warm-up complete.");

    let mut sequential_times: Vec<Duration> = Vec::new();
    let mut coroutine_times: Vec<Duration> = Vec::new();
    let mut channel_times: Vec<Duration> = Vec::new();

    for i in 0..5 {
        println!("Timed iteration {:?}", i);

        let start = Instant::now();
        ga_sequential.run(false);
        let duration = start.elapsed();
        sequential_times.push(duration);
        println!("Sequential iteration {:?} time: {:?}", i, duration);

        let start = Instant::now();
        ga_channel.run(false);
        let duration = start.elapsed();
        channel_times.push(duration);
        println!("Channel iteration {:?} time: {:?}", i, duration);

        let start = Instant::now();
        ga_coroutine.run(false);
        let duration = start.elapsed();
        coroutine_times.push(duration);
        println!("Coroutine iteration {:?} time: {:?}", i, duration);
    }

    let sequential_time_avg = sequential_times.iter().sum::<Duration>() / sequential_times.len() as u32;
    let channel_time_avg = channel_times.iter().sum::<Duration>() / channel_times.len() as u32;
    let coroutine_time_avg = coroutine_times.iter().sum::<Duration>() / coroutine_times.len() as u32;

    println!("Average Sequential Time: {:?}", sequential_time_avg);
    println!("Average Channel Time: {:?}", channel_time_avg);
    println!("Average Coroutine Time: {:?}", coroutine_time_avg);
}
