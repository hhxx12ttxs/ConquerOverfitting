public static double regularizedBeta(double x, final double a,
final double b, double epsilon, int maxIterations) {
double ret;

if (Double.isNaN(x) || Double.isNaN(a) || Double.isNaN(b) || x < 0
hN = hPrev * deltaN;

if (Double.isInfinite(hN)) {
throw new RuntimeException(&quot;Fraction did not converge&quot;);

