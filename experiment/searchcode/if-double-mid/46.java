double high = x;
while (Math.floor(low) != Math.floor(high)) {
double mid = (low + high) / 2;
if (mid * mid > x) {
high = mid;
} else {

