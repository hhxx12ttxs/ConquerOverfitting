* @param populationLimit maximal size of the population
*/
public ListPopulation (List<Chromosome> chromosomes, int populationLimit) {
if (chromosomes.size() > populationLimit) {
throw new IllegalArgumentException(&quot;List of chromosomes bigger than maxPopulationSize.&quot;);
}
if (populationLimit < 0) {

