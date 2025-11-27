use rand::Rng;
use once_cell::sync::Lazy;

pub const GEN_SIZE: usize = 1000;
const WEIGHT_LIMIT: usize = 300;

pub static VALUES: Lazy<Vec<usize>> = Lazy::new(|| {
    let mut rng = rand::rng();
    let mut v = vec![0; GEN_SIZE];
    for i in 0..GEN_SIZE {
        v[i] = rng.random_range(0..100);
    }
    v
});

pub static WEIGHTS: Lazy<Vec<usize>> = Lazy::new(|| {
    let mut rng = rand::rng();
    let mut w = vec![0; GEN_SIZE];
    for i in 0..GEN_SIZE {
        w[i] = rng.random_range(0..100);
    }
    w
});

#[derive(Clone, Copy)]
pub struct Individual {
    pub selected_items: [bool; GEN_SIZE],
    pub fitness: isize,
}

pub fn new_individual_random(r: &mut impl Rng) -> Individual {
    let mut selected_items = [false; GEN_SIZE];

    for i in 0..GEN_SIZE {
        selected_items[i] = r.random_bool(0.5);
    }

    Individual {
        selected_items, fitness: 0,
    }
}

impl Individual {
    pub fn measure_fitness(&mut self) {
        let mut total_value = 0;
        let mut total_weight = 0;

        for i in 0..GEN_SIZE {
            if self.selected_items[i] {
                total_value += VALUES[i];
                total_weight += WEIGHTS[i];
            }
        }

        if total_weight > WEIGHT_LIMIT {
            self.fitness = -((total_weight - WEIGHT_LIMIT) as isize);
        } else {
            self.fitness = total_value as isize;
        }
    }

    pub fn crossover_with(&self, mate: &Individual, r: &mut impl Rng) -> Individual {
        let mut selected_items = [false; GEN_SIZE];
        let crossover_point = r.random_range(0..GEN_SIZE);

        for i in 0..GEN_SIZE {
            if i < crossover_point {
                selected_items[i] = self.selected_items[i];
            } else {
                selected_items[i] = mate.selected_items[i];
            }
        }

        Individual {
            selected_items, fitness: 0,
        }
    }

    pub fn mutate(&mut self, r: &mut impl Rng) {
        let mutation_point = r.random_range(0..GEN_SIZE);
        self.selected_items[mutation_point] = !self.selected_items[mutation_point];
    }
}
