/** percentage of chromosomes copied to the next generation */
private double elitismRate = 0.9;

/**
* Creates a new ElitisticListPopulation instance.
*            next generation [in %]
*/
public void setElitismRate(double elitismRate) {
if (elitismRate < 0 || elitismRate > 1)

