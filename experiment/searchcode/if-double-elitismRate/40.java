public class ElitisticListPopulation extends ListPopulation {

/** percentage of chromosomes copied to the next generation */
private double elitismRate = 0.9;
*            next generation [in %]
*/
public ElitisticListPopulation(List<Chromosome> chromosomes, int populationLimit, double elitismRate) {

