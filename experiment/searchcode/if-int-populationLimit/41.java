public ListPopulation (List<Chromosome> chromosomes, int populationLimit) {
if (chromosomes.size() > populationLimit) {
throw new NumberIsTooLargeException(LocalizedFormats.LIST_OF_CHROMOSOMES_BIGGER_THAN_POPULATION_SIZE,
* @param populationLimit maximal size of the population
*/
public ListPopulation (int populationLimit) {
if (populationLimit < 0) {

