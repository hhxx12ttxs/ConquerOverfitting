/** Amount of error to accept in floating point comparisons (as ulps). */
private final int maxUlps;
public SimplexSolver(final double epsilon, final int maxUlps) {
this.epsilon = epsilon;
this.maxUlps = maxUlps;

