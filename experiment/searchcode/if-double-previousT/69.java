throws MaxCountExceededException, DimensionMismatchException, NoBracketingException {

double previousT = interpolator.getGlobalPreviousTime();
final double currentT = interpolator.getGlobalCurrentTime();

// initialize the events states if needed

