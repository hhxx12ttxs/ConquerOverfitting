private int populationSize;
private int maxEvaluations;
private DifferentialEvolutionCrossover crossoverOperator;
public DifferentialEvolutionBuilder setMaxEvaluations(int maxEvaluations) {
if (maxEvaluations < 0) {
throw new JMetalException(&quot;MaxEvaluations is negative: &quot; + maxEvaluations);

