final double logm = FastMath.log(denominatorDegreesOfFreedom);
final double lognxm = FastMath.log(numeratorDegreesOfFreedom * x +
public double cumulativeProbability(double x) throws MathException {
double ret;
if (x <= 0) {
ret = 0;
} else {
double n = numeratorDegreesOfFreedom;

