if (error <= 1.0) {
computeDerivatives(stepEnd, yTmp, yDot);
final double[] correctedScaled = new double[y0.length];
interpolatorTmp.storeTime(stepStart);
interpolatorTmp.shift();
interpolatorTmp.storeTime(stepEnd);
if (manager.evaluateStep(interpolatorTmp)) {

