private double elitismRate = 0.9;

/**
* Creates a new ElitisticListPopulation instance.
* @throws OutOfRangeException if the elitism rate is outside the [0, 1] range
*/
public void setElitismRate(final double elitismRate) {
if (elitismRate < 0 || elitismRate > 1) {

