public static double[][] polyval(final double[] x, final double[] y, final double coeff[], int degree) {

setLoggerLevel();

if (degree < -1) {
public static double polyval(final double x, final double y, final double[] coeff, int degree) {

setLoggerLevel();

if (degree < 0 || degree > 1000) {

