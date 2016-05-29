private double powHelper(double x, int n) {
if (n == 0) return 1;
if (x == 0) return 0;
if (x == 1) return 1;
if (x == -1) return n % 2 == 1 ? -1 : 1;

double tmp = powHelper(x, Math.abs(n/2));

