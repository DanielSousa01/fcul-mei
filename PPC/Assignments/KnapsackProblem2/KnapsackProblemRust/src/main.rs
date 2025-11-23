mod knapsack;

use std::time::Instant;
use knapsack::sequential::knapsack_ga_sequential::new_knapsack_ga_sequential;

fn main() {
    println!("Starting Knapsack Genetic Algorithm...");

    let mut ga = new_knapsack_ga_sequential();

    let start = Instant::now();
    ga.run(false);
    let duration = start.elapsed();

    println!("Sequential Execution Time: {:?}", duration);
}
