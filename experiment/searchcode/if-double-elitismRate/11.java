/** percentage of chromosomes copied to the next generation */
private double elitismRate = 0.9;

/**
* Creates a new ElitisticListPopulation instance.
public void setElitismRate(double elitismRate) {
if (elitismRate < 0 || elitismRate > 1)
throw new IllegalArgumentException(&quot;Elitism rate has to be in [0,1]&quot;);

