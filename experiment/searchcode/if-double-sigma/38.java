public static double blackScholes(double s, double x, double r, double sigma, int t) {
double a = (Math.log(s/x) + (r + (sigma*sigma))/2 * t);
double b = a- sigma*Math.sqrt(t);
public static double Phi(double z) {
if (z < -8.0) return 0.0;
if (z > 8.0) return 1;

