* @param populationLimit maximal size of the population
*/
public ListPopulation (List<Chromosome> chromosomes, int populationLimit) {
if (chromosomes.size() > populationLimit) {
public ListPopulation (int populationLimit) {
if (populationLimit < 0) {
throw new NotPositiveException(LocalizedFormats.POPULATION_LIMIT_NOT_POSITIVE, populationLimit);

