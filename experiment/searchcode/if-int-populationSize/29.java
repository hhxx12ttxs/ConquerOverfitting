private final int populationSize;

public Population(int populationSize) {
this.populationSize = populationSize;
this.population = new ArrayList<>(populationSize);
}

public Population(int populationSize, int chromosomeLength) {

