public class ElitisticListPopulation extends ListPopulation {

/** percentage of chromosomes copied to the next generation */
private double elitismRate = 0.9;
public void setElitismRate(double elitismRate) {
if (elitismRate < 0 || elitismRate > 1)

