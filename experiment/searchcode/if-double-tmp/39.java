public class PowXN {
public double myPow(double x, int n) {
if (n == 0) {
return 1;
}
if (n == 1) {
return x;
}
double tmp = myPow(x, Math.abs(n / 2));
if (n > 0) {
if ((n &amp; 1) == 1) {

