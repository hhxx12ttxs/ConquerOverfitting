public void computeDerivatives(final double t, final double[] y, final double[] yDot)
throws MathUserException {
if (++evaluations > maxEvaluations) {
// trigger the event
interpolator.setInterpolatedTime(eventT);
final double[] eventY = interpolator.getInterpolatedState();

