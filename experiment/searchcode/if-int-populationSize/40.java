private int chromosomeSize;
private int populationSize;

private Chromosome[] population;
private Random rand = new Random();
this.populationSize = populationSize;

// 1. Initialize population
this.population = new Chromosome[populationSize];

for (int i = 0; i < populationSize; i++) {

