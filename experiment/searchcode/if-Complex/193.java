int M = signal.length;
public class Complex {
public double re;
public Complex(double re, double im) {
this.re = re;
public static Complex[] makeComplexVector(int M) {
Complex[] g = new Complex[M];
for (int i = 0; i < M; i++) {
g[i] = new Complex(0,0);
}
public static Complex[] makeComplexVector(double[] signal) {

