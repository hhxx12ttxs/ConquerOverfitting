* @param scalRelativeTolerance allowed relative error
* @exception IllegalArgumentException if order is 1 or less
final double stepEnd = stepStart + stepSize;
interpolator.shift();
interpolator.setInterpolatedTime(stepEnd);

