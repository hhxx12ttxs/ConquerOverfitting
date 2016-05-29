// predict a first estimate of the state at step end (P in the PECE sequence)
final double stepEnd = stepStart + stepSize;
interpolator.setInterpolatedTime(stepEnd);
// evaluate a final estimate of the derivative (second E in the PECE sequence)
final double stepEnd = stepStart + stepSize;
computeDerivatives(stepEnd, yTmp, yDot);

