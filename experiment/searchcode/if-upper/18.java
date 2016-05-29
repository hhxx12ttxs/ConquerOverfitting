public int mySqrt(int x) {
long upper = x;
long lower = 0;

while (upper >= lower) {
long mid = (upper + lower) / 2;

if (x < mid * mid) {
upper = mid - 1;

