private int maxEvaluations;
private int populationSize;
private CrossoverOperator<S>  crossoverOperator;
public NSGAIIBuilder<S> setMaxEvaluations(int maxEvaluations) {
if (maxEvaluations < 0) {
throw new JMetalException(&quot;maxEvaluations is negative: &quot; + maxEvaluations);

