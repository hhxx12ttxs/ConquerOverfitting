public static double polyval(final double x, final double y, final double[] coeff, int degree) {

setLoggerLevel();

if (degree < 0 || degree > 1000) {
degree = degreeFromCoefficients(coeff.length);
}

//// Evaluate polynomial ////
double sum = coeff[0];

if (degree == 1) {

