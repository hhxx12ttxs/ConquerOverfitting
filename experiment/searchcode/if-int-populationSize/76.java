// Construct a population
public Population(int populationSize, boolean initialise) {

individuals = new MultiChromosome[populationSize];
// Loop through individuals to find fittest
for (int i = 1; i < populationSize(); i++) {
if (fittest.getFitness() >= individuals[i].getFitness()) {

