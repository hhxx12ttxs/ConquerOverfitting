/** percentage of chromosomes copied to the next generation */
private double elitismRate = 0.9;

/**
* Creates a new {@link ElitisticListPopulation} instance.
public void setElitismRate(final double elitismRate) throws OutOfRangeException {
if (elitismRate < 0 || elitismRate > 1) {

