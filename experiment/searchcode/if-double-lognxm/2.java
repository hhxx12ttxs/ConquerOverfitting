double denominatorDegreesOfFreedom,
double inverseCumAccuracy)
throws NotStrictlyPositiveException {
if (numeratorDegreesOfFreedom <= 0) {
final double logm = FastMath.log(denominatorDegreesOfFreedom);
final double lognxm = FastMath.log(numeratorDegreesOfFreedom * x +

