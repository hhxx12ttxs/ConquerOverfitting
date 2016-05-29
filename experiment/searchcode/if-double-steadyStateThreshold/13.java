/** Threshold for cost steady state detection. */
private double steadyStateThreshold;

/** Threshold for cost convergence. */
public GaussNewtonEstimator(final int maxCostEval, final double convergence,
final double steadyStateThreshold) {
setMaxCostEval(maxCostEval);
this.steadyStateThreshold = steadyStateThreshold;

