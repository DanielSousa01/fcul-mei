use crate::knapsack::individual::Individual;

pub struct ProcessIndividuals {
    pub idx: usize,
    pub population: Vec<Individual>,
}

pub struct ProcessedIndividuals {
    pub idx: usize,
    pub population: Vec<Individual>,
}

pub struct ProcessCrossoverIndividuals {
    pub idx: usize,
    pub chunk_size: usize,
    pub population: Vec<Individual>,
}

pub struct ProcessedCrossoverIndividuals {
    pub idx: usize,
    pub chunk_size: usize,
    pub population: Vec<Individual>,
}