private double steadyStateThreshold;

/** Threshold for cost convergence. */
private double convergence;
public GaussNewtonEstimator(final int maxCostEval, final double convergence,
final double steadyStateThreshold) {
setMaxCostEval(maxCostEval);

