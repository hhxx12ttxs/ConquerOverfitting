private double elitismRate;

public LinksElitisticListPopulation(int populationLimit, double elitismRate)
public void setElitismRate(final double elitismRate)
throws OutOfRangeException {
if (elitismRate < 0 || elitismRate > this.getPopulationLimit()) {

