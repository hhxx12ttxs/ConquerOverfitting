private SpecialFunctions() { }

private static double gammaLnByLanczosApproximation(final double x) {
if (x <= 0)
return Double.NaN;
private static double regularizedUpperGammaByContinuedFraction(final double a, final double x) {
final double relativeError = 1e-10;

// setup
double D = 1.0 / (x + 1.0 - a);

