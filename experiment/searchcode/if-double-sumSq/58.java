public static double findElbowPointLinePlusExp(double[] y, double[] x) {
if (y.length != x.length) {
throw new IllegalStateException(&quot;Arrays are of a different size&quot;);
public static double getSumSquares(double[] y, double[] x, double slope, double offset) {
double sumSq = 0;
for (int i = 0; i < x.length; i++) {
sumSq += Math.pow(y[i] - ((x[i] * slope) + offset), 2);

