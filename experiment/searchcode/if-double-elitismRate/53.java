SelectionMethod selectionMethod,
Random generator, double crossoverRate, double mutationRate,
double elitismRate, Crossover crossover, Mutation mutation,
BuildPopulation object = null;

if (useGeneticOperators) {

object = new BuildNextGeneration(selectionMethod, generator,

