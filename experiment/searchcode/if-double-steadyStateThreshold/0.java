private static final double DEFAULT_CONVERGENCE = 1.0E-6;

private double steadyStateThreshold;

private double convergence;

public GaussNewtonEstimator() {
this.steadyStateThreshold = steadyStateThreshold;
this.convergence = convergence;
}

public void setConvergence(final double convergence) {

