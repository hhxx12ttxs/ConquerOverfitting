public class PopulationAsList extends Population {

private static int populationSize = Configuration.getPopulationSize();
private void initRandom() {
population = new ArrayList<>(populationSize);
for (int i = 0; i < populationSize; i++) {

