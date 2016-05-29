public class Matrix {
public static double[][] times(double[][] a, double[][] b) {
int n2 = b.length;

if (n*n2 == 0) {
throw new IllegalArgumentException(&quot;Do not put empty arrays.&quot;);

