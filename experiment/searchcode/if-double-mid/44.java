public double sqRoot (double n, double eps) {
if (n < 0) {
return -1;
}
if (0 == n) {
return 0;
}
double left, right, mid;
if (n < 1) {
mid = left + (right - left)/2;
while (right - left > eps) {
mid = left + (right - left)/2;
if (mid * mid == n) {

