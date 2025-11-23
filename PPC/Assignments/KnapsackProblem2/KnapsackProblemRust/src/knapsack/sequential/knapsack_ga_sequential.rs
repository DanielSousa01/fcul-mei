use rand::{Rng, SeedableRng};
use crate::knapsack::individual::{Individual, new_individual_random, GEN_SIZE};
use crate::knapsack::knapsack_ga::{tournament, N_GENERATIONS, POP_SIZE, PROB_MUTATION};

pub struct KnapsackGASequential {
    r : rand::rngs::StdRng,
    population : Vec<Individual>,
}

pub fn new_knapsack_ga_sequential() -> KnapsackGASequential {
    let r = rand::rngs::StdRng::seed_from_u64(0);
    let population: Vec<Individual> = vec![Individual { selected_items: [false; GEN_SIZE], fitness: 0 }; POP_SIZE];

    let mut ga: KnapsackGASequential = KnapsackGASequential {
        r,
        population,
    };

    ga.populate_initial_population_randomly();
    ga
}

impl KnapsackGASequential {
    pub fn populate_initial_population_randomly(&mut self) {
        for i in 0..POP_SIZE {
            self.population[i] = new_individual_random(&mut self.r);
        }
    }

    pub fn run(&mut self, silent: bool) -> Individual {
        for generation in 0..N_GENERATIONS {
            // Step1 - Calculate Fitness
            for i in 0..POP_SIZE {
                self.population[i].measure_fitness();
            }

            // Step2 - Print the best individual so far.
            let best = self.best_of_population();
            if !silent {
                println!("KnapsackGASequential: Best at generation {} has fitness {}", generation, best.fitness);
            }

            // Step3 - Find parents to mate (cross-over)
            let mut new_population: Vec<Individual> = vec![*best; POP_SIZE];

            for i in 1..POP_SIZE {
                let parent1 = tournament(&mut self.r, &self.population);
                let parent2 = tournament(&mut self.r, &self.population);

                new_population[i] = parent1.crossover_with(parent2, &mut self.r);
            }

            // Step4 - Mutate
            for i in 1..POP_SIZE {
                if self.r.random::<f64>() < PROB_MUTATION {
                    new_population[i].mutate(&mut self.r);
                }
            }

            self.population = new_population;
        }
        self.population[0]
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
 }