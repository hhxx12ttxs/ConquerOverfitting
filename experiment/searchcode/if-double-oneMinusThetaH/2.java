@Override
protected void computeInterpolatedStateAndDerivatives(final double theta,
final double oneMinusThetaH) {
if ((previousState != null) &amp;&amp; (theta <= 0.5)) {

