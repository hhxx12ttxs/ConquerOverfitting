// Construct a population
public Population(int populationSize, boolean initialise) {
tours = new Tour[populationSize];
// Loop through individuals to find fittest
for (int i = 1; i < populationSize(); i++) {
if (fittest.getFitness() <= getTour(i).getFitness()) {

