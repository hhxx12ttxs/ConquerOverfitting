protected void computeInterpolatedStateAndDerivatives(final double theta, final double oneMinusThetaH) {

final double coeffDot2 = 2 * theta;
final double coeffDot1 = 1 - coeffDot2;
if((previousState != null) &amp;&amp; (theta <= 0.5)) {
final double coeff1 = theta * oneMinusThetaH;

