public static DoubleMatrix polyval(final DoubleMatrix x, final DoubleMatrix y, final DoubleMatrix coeff, int degree) {

if (!x.isColumnVector()) {
logger.warn(&quot;polyValGrid: require (x) standing data vectors!&quot;);
public static double polyval(final double x, final double y, final double[] coeff, int degree) {

if (degree < 0 || degree > 1000) {

