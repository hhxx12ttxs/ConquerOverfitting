public static double sqrt(double x) {
if (x < 0) {
return x;
}

double mid, last;
double low = 0, high = x;
double epsilon = 0.001;

mid = low + (high - low) / 2;

do {
if (mid * mid > x) {

