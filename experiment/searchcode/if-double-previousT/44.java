public void computeDerivatives(final double t, final double[] y, final double[] yDot)
throws MathUserException {
if (++evaluations > maxEvaluations) {
double previousT = interpolator.getGlobalPreviousTime();
final double currentT = interpolator.getGlobalCurrentTime();

