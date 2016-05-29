public double myPow(double x, int n) {
if (x == 0) {
return 0;
}

if (n == 0) {
return 1;
}

if (n < 0) {
double ret = x * myPow(x, -(n + 1));

