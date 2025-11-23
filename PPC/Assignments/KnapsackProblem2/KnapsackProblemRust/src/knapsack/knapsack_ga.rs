use rand::Rng;
use super::individual::Individual;

pub const N_GENERATIONS: usize = 500;
pub const POP_SIZE: usize = 100000;
pub const PROB_MUTATION: f64 = 0.5;
const TOURNAMENT_SIZE: usize = 3;

pub fn tournament<'a>(r: &mut impl Rng, population: &'a [Individual]) -> &'a Individual {
    let mut best = &population[r.random_range(0..population.len())];

    for _ in 1..TOURNAMENT_SIZE {
        let other = &population[r.random_range(0..population.len())];
        if other.fitness > best.fitness {
            best = other;
        }
    }

    best
}