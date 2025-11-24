use std::sync::mpsc;
use std::thread;
use rand::Rng;
use crate::knapsack::individual::{Individual, new_individual_random, GEN_SIZE};
use crate::knapsack::knapsack_ga::{tournament, N_GENERATIONS, POP_SIZE, PROB_MUTATION};


pub struct KnapsackGAChannel {
    population : Vec<Individual>,
    n_workers: usize,
    chunk_size: usize,
}

pub fn new_knapsack_ga_channel(n_workers: usize, chunk_size: usize) -> KnapsackGAChannel {
    let population: Vec<Individual> = vec![Individual { selected_items: [false; GEN_SIZE], fitness: 0 }; POP_SIZE];

    let mut ga: KnapsackGAChannel = KnapsackGAChannel {
        population,
        n_workers,
        chunk_size,
    };

    ga.populate_initial_population_randomly();
    ga
}

impl KnapsackGAChannel {
    pub fn populate_initial_population_randomly(&mut self) {
        let mut r = rand::rng();
        for i in 0..POP_SIZE {
            self.population[i] = new_individual_random(&mut r);
        }
    }

    pub fn run(&mut self, silent: bool) -> Individual {
        for generation in 0..N_GENERATIONS {
            // Step1 - Calculate Fitness
            self.calculate_fitness();

            // Step2 - Print the best individual so far.
            let best = self.best_of_population();
            if !silent {
                println!("KnapsackGAChannel: Best at generation {} has fitness {}", generation, best.fitness);
            }

            // Step3 - Find parents to mate (cross-over)
            let mut new_population: Vec<Individual> = vec![*best; POP_SIZE];

            self.crossover_population(&mut new_population);

            // Step4 - Mutate
            self.mutate_population(&mut new_population);

            self.population = new_population;
        }
        self.population[0]
    }

    fn calculate_fitness(&mut self) {
        let population_ptr = &mut self.population as *mut Vec<Individual> as usize;
        let chunk_size = self.chunk_size;
        let n_workers = self.n_workers;

        let processor = move |idx: usize| {
            unsafe {
                let population = &mut *(population_ptr as *mut Vec<Individual>);
                population[idx].measure_fitness();
            }
        };

        Self::compute_channel(POP_SIZE, 0, chunk_size, n_workers, processor);
    }

    fn best_of_population(&self) -> &Individual {
        let mut best = &self.population[0];

        for i in 1..POP_SIZE {
            if self.population[i].fitness > best.fitness {
                best = &self.population[i];
            }
        }

        best
    }

    fn crossover_population(&mut self, new_population: &mut Vec<Individual>) {
        let population_ptr = &mut self.population as *mut Vec<Individual> as usize;
        let new_population_ptr = new_population as *mut Vec<Individual> as usize;
        let chunk_size = self.chunk_size;
        let n_workers = self.n_workers;

        let processor = move |idx: usize| {
            unsafe {
                let mut r = rand::rng();
                let population = &mut *(population_ptr as *mut Vec<Individual>);
                let new_pop = &mut *(new_population_ptr as *mut Vec<Individual>);

                let parent1 = tournament(&mut r, population);
                let parent2 = tournament(&mut r, population);

                new_pop[idx] = parent1.crossover_with(parent2, &mut r);
            }
        };

        Self::compute_channel(POP_SIZE, 1, chunk_size, n_workers, processor);
    }

    fn mutate_population(&mut self, new_population: &mut Vec<Individual>) {
        let population_ptr = new_population as *mut Vec<Individual> as usize;
        let chunk_size = self.chunk_size;
        let n_workers = self.n_workers;

        let processor = move |idx: usize| {
            unsafe {
                let mut r = rand::rng();
                let population = &mut *(population_ptr as *mut Vec<Individual>);

                if r.random::<f64>() < PROB_MUTATION {
                    population[idx].mutate(&mut r);
                }
            }
        };

        Self::compute_channel(POP_SIZE, 1, chunk_size, n_workers, processor);
    }


    fn compute_channel<F>(size: usize, start_idx: usize, chunk_size: usize, n_workers: usize, processor: F)
    where
        F: Fn(usize) + Send + Sync + Clone + 'static,
    {
        let (sx, rx) = mpsc::channel::<(usize, usize)>();
        let rx = std::sync::Arc::new(std::sync::Mutex::new(rx));

        // Producer thread
        let producer = thread::spawn(move || {
            let mut i = start_idx;
            while i < size {
                let end = (i + chunk_size).min(size);
                sx.send((i, end)).ok();
                i += chunk_size;
            }
        });

        // Worker threads
        let mut workers = vec![];
        for _ in 0..n_workers {
            let processor_clone = processor.clone();
            let rx_clone = rx.clone();

            workers.push(thread::spawn(move || {
                loop {
                    let item = rx_clone.lock().unwrap().recv();
                    match item {
                        Ok((start, end)) => {
                            for j in start..end {
                                processor_clone(j);
                            }
                        }
                        Err(_) => break,
                    }
                }
            }));
        }

        // Wait for producer to finish
        producer.join().ok();

        // Wait for all workers to finish
        for worker in workers {
            worker.join().ok();
        }
    }


}