// predict a first estimate of the state at step end
final double stepEnd = stepStart + stepSize;
interpolator.shift();
computeDerivatives(stepEnd, y, yDot);

// update Nordsieck vector
final double[] predictedScaled = new double[y0.length];

