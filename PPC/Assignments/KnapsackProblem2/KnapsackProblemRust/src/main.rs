mod knapsack;

use std::time::Instant;
use knapsack::sequential::knapsack_ga_sequential::new_knapsack_ga_sequential;
use knapsack::channel::knapsack_ga_channel::new_knapsack_ga_channel;
use knapsack::coroutine::knapsack_ga_coroutine::new_knapsack_ga_coroutine;

fn main() {
    println!("Starting Knapsack Genetic Algorithm...");

    let mut ga_sequential = new_knapsack_ga_sequential();
    let mut ga_channel = new_knapsack_ga_channel(16, 500);
    let mut ga_coroutine = new_knapsack_ga_coroutine(500);

    let sequential_start = Instant::now();
    ga_sequential.run(false);
    let sequential_duration = sequential_start.elapsed();

    let channel_start = Instant::now();
    ga_channel.run(false);
    let channel_duration = channel_start.elapsed();
    
    let coroutine_start = Instant::now();
    ga_coroutine.run(false);
    let coroutine_duration = coroutine_start.elapsed();

    println!("Sequential Execution Time: {:?}", sequential_duration);
    println!("Channel-based Execution Time: {:?}", channel_duration);
    println!("Coroutine-based Execution Time: {:?}", coroutine_duration);
}
