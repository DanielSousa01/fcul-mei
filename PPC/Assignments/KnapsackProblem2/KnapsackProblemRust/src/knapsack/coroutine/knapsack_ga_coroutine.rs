use tokio::runtime::Runtime;
use rand::Rng;
use crate::knapsack::individual::{Individual, new_individual_random, GEN_SIZE};
use crate::knapsack::knapsack_ga::{tournament, N_GENERATIONS, POP_SIZE, PROB_MUTATION};


pub struct KnapsackGACoroutine {
    population : Vec<Individual>,
    chunk_size: usize,
}

pub fn new_knapsack_ga_coroutine(chunk_size: usize) -> KnapsackGACoroutine {
    let population: Vec<Individual> = vec![Individual { selected_items: [false; GEN_SIZE], fitness: 0 }; POP_SIZE];

    let mut ga: KnapsackGACoroutine = KnapsackGACoroutine {
        population,
        chunk_size,
    };

    ga.populate_initial_population_randomly();
    ga
}

impl KnapsackGACoroutine {
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
                println!("KnapsackGACoroutine: Best at generation {} has fitness {}", generation, best.fitness);
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

        let processor = move |idx: usize| {
            unsafe {
                let population = &mut *(population_ptr as *mut Vec<Individual>);
                population[idx].measure_fitness();
            }
        };

        let rt = Runtime::new().unwrap();
        rt.block_on(Self::compute_coroutines(POP_SIZE, 0, chunk_size, processor));
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

        let rt = Runtime::new().unwrap();
        rt.block_on(Self::compute_coroutines(POP_SIZE, 1, chunk_size, processor));
    }

    fn mutate_population(&mut self, new_population: &mut Vec<Individual>) {
        let population_ptr = new_population as *mut Vec<Individual> as usize;
        let chunk_size = self.chunk_size;

        let processor = move |idx: usize| {
            unsafe {
                let mut r = rand::rng();
                let population = &mut *(population_ptr as *mut Vec<Individual>);

                if r.random::<f64>() < PROB_MUTATION {
                    population[idx].mutate(&mut r);
                }
            }
        };

        let rt = Runtime::new().unwrap();
        rt.block_on(Self::compute_coroutines(POP_SIZE, 1, chunk_size, processor));
    }


    async fn compute_coroutines<F>(size: usize, start_idx: usize, chunk_size: usize, processor: F)
    where
        F: Fn(usize) + Send + Sync + Clone + 'static,
    {
        let mut handles = vec![];

        let mut idx = start_idx;
        while idx < size {
            let end_idx = usize::min(idx + chunk_size, size);
            let processor_clone = processor.clone();

            let handle = tokio::spawn(async move {
                for i in idx..end_idx {
                    processor_clone(i);
                }
            });

            handles.push(handle);
            idx += chunk_size;
        }

        for handle in handles {
            handle.await.unwrap();
        }
    }

}