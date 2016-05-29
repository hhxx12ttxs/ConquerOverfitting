public enum GeneticAlgorithmVariant {GENERATIONAL, STEADY_STATE}
/**
* Builder class
*/
private Problem<S> problem;
private int maxEvaluations;
private int populationSize;
this.variant = GeneticAlgorithmVariant.GENERATIONAL ;
}

public GeneticAlgorithmBuilder<S> setMaxEvaluations(int maxEvaluations) {
this.maxEvaluations = maxEvaluations;

