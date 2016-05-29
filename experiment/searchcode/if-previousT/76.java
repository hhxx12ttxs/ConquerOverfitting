throws DimensionMismatchException, NumberIsTooSmallException,
MaxCountExceededException, NoBracketingException {

if (y0.length != equations.getDimension()) {
throw new DimensionMismatchException(y0.length, equations.getDimension());
}
if (y.length != equations.getDimension()) {
throw new DimensionMismatchException(y.length, equations.getDimension());

