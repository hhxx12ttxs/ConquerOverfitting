UnivariateFunction f = new UnivariateFunction() {
public double value(double x) {
if (x < lower) {
throw new NumberIsTooSmallException(x, lower, true);
} else if (x > upper) {
throw new NumberIsTooLargeException(x, upper, true);

