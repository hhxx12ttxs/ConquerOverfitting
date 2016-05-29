public class ElitisticListPopulation extends org.apache.commons.math.genetics.ListPopulation {
private double elitismRate = 0.9;

public ElitisticListPopulation(java.util.List<org.apache.commons.math.genetics.Chromosome> chromosomes ,int populationLimit ,double elitismRate) {
public void setElitismRate(double elitismRate) {
if ((elitismRate < 0) || (elitismRate > 1))
throw new java.lang.IllegalArgumentException(&quot;Elitism rate has to be in [0,1]&quot;);

