while (right - left > EPS) {
double mid = (left + right) / 2;
if (test(mid, t, x, C))
right = mid;
private boolean test(double mid, int[] t, int[] x, int C) {
double c = 0;
for (int i = 0; i < t.length; i++) {

