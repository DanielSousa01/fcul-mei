use std::sync::mpsc;
use std::thread;
use std::sync::{Arc, Mutex};
use rand::Rng;
use crate::knapsack::individual::{Individual, new_individual_random, GEN_SIZE};
use crate::knapsack::knapsack_ga::{tournament, N_GENERATIONS, POP_SIZE, PROB_MUTATION};


pub struct KnapsackGAChannel {
    population: Vec<Individual>,
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
        let (work_tx, work_rx) = mpsc::channel::<(usize, usize, Vec<Individual>)>();
        let work_rx = Arc::new(Mutex::new(work_rx));

        let (result_tx, result_rx) = mpsc::channel::<(usize, Vec<Individual>)>();

        let chunk_size = self.chunk_size;
        let population_clone = self.population.clone();

        let producer = thread::spawn(move || {
            let mut start = 0;
            while start < POP_SIZE {
                let end = (start + chunk_size).min(POP_SIZE);
                let chunk = population_clone[start..end].to_vec();
                work_tx.send((start, end, chunk)).ok();
                start = end;
            }
        });

        let mut workers = vec![];
        for _ in 0..self.n_workers {
            let work_rx = Arc::clone(&work_rx);
            let result_tx = result_tx.clone();

            workers.push(thread::spawn(move || {
                loop {
                    let work = work_rx.lock().unwrap().recv();
                    match work {
                        Ok((start_idx, _end_idx, mut chunk)) => {
                            for individual in chunk.iter_mut() {
                                individual.measure_fitness();
                            }
                            result_tx.send((start_idx, chunk)).ok();
                        }
                        Err(_) => break,
                    }
                }
            }));
        }

        producer.join().ok();
        drop(result_tx);

        for worker in workers {
            worker.join().ok();
        }

        while let Ok((start_idx, chunk)) = result_rx.recv() {
            let end_idx = start_idx + chunk.len();
            self.population[start_idx..end_idx].copy_from_slice(&chunk);
        }
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
        let (work_tx, work_rx) = mpsc::channel::<(usize, usize)>();
        let work_rx = Arc::new(Mutex::new(work_rx));

        let (result_tx, result_rx) = mpsc::channel::<(usize, Vec<Individual>)>();

        let chunk_size = self.chunk_size;
        let producer = thread::spawn(move || {
            let mut start = 1;
            while start < POP_SIZE {
                let end = (start + chunk_size).min(POP_SIZE);
                work_tx.send((start, end)).ok();
                start = end;
            }
        });

        let population_clone = self.population.clone();
        let mut workers = vec![];

        for _ in 0..self.n_workers {
            let work_rx = Arc::clone(&work_rx);
            let result_tx = result_tx.clone();
            let population = population_clone.clone();

            workers.push(thread::spawn(move || {
                let mut r = rand::rng();
                loop {
                    let work = work_rx.lock().unwrap().recv();
                    match work {
                        Ok((start_idx, end_idx)) => {
                            let mut offspring = Vec::new();

                            for _ in start_idx..end_idx {
                                let parent1 = tournament(&mut r, &population);
                                let parent2 = tournament(&mut r, &population);
                                offspring.push(parent1.crossover_with(parent2, &mut r));
                            }

                            result_tx.send((start_idx, offspring)).ok();
                        }
                        Err(_) => break,
                    }
                }
            }));
        }

        producer.join().ok();
        drop(result_tx);

        for worker in workers {
            worker.join().ok();
        }

        while let Ok((start_idx, chunk)) = result_rx.recv() {
            let end_idx = start_idx + chunk.len();
            new_population[start_idx..end_idx].copy_from_slice(&chunk);
        }
    }

    fn mutate_population(&mut self, new_population: &mut Vec<Individual>) {
        let (work_tx, work_rx) = mpsc::channel::<(usize, usize, Vec<Individual>)>();
        let work_rx = Arc::new(Mutex::new(work_rx));

        let (result_tx, result_rx) = mpsc::channel::<(usize, Vec<Individual>)>();

        let chunk_size = self.chunk_size;
        let population_clone = new_population[1..].to_vec();

        let producer = thread::spawn(move || {
            let mut start = 0;
            let total_size = population_clone.len();

            while start < total_size {
                let end = (start + chunk_size).min(total_size);
                let chunk = population_clone[start..end].to_vec();
                work_tx.send((start, end, chunk)).ok();
                start = end;
            }
        });

        let mut workers = vec![];

        for _ in 0..self.n_workers {
            let work_rx = Arc::clone(&work_rx);
            let result_tx = result_tx.clone();

            workers.push(thread::spawn(move || {
                let mut r = rand::rng();
                loop {
                    let work = work_rx.lock().unwrap().recv();
                    match work {
                        Ok((start_idx, _end_idx, mut chunk)) => {
                            for individual in chunk.iter_mut() {
                                if r.random::<f64>() < PROB_MUTATION {
                                    individual.mutate(&mut r);
                                }
                            }
                            result_tx.send((start_idx, chunk)).ok();
                        }
                        Err(_) => break,
                    }
                }
            }));
        }

        producer.join().ok();
        drop(result_tx);

        for worker in workers {
            worker.join().ok();
        }

        while let Ok((start_idx, chunk)) = result_rx.recv() {
            let actual_start = start_idx + 1;
            let actual_end = actual_start + chunk.len();
            new_population[actual_start..actual_end].copy_from_slice(&chunk);
        }
    }
}