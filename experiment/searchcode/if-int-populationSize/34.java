this.config = config;
}

@Override
public Population createInitialPopulation(final int populationSize) {
private void createEvenlyDistributedArmy(List<Individual> individuals, final int populationSize) {
if (individuals.size() < populationSize) {
int[] genomArray = new int[GENOM_SIZE];

