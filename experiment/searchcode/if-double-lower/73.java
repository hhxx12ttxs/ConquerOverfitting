public static double qsignrank(double x, double n, boolean lower_tail, boolean log_p) {
double f, p, q;


if (Double.isNaN(x) || Double.isNaN(n)) {
if (n <= 0) {
return Double.NaN;
}

if (x == R_DT_0(lower_tail, log_p)) {
return (0);
}

if (x == R_DT_1(lower_tail, log_p)) {

