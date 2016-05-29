* @param scalRelativeTolerance allowed relative error
* @exception IllegalArgumentException if order is 1 or less
// predict a first estimate of the state at step end
final double stepEnd = stepStart + stepSize;
interpolator.shift();

