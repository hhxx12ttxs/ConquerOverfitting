/** Current step start time. */
protected double stepStart;

/** Current stepsize. */
protected double stepSize;
throws MaxCountExceededException, DimensionMismatchException, NoBracketingException {

double previousT = interpolator.getGlobalPreviousTime();
final double currentT = interpolator.getGlobalCurrentTime();

