public FastMatrix(double f) { a00 = a11 = a22 = f; }

public FastMatrix(double[][] m) {
if ((m.length != 3 &amp;&amp; m.length != 4)
public static double dotProduct(double[] a, double[] b) {
double result = 0;
if (a.length != b.length)
throw new IllegalArgumentException(

