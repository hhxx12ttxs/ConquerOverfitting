double left = 0.0;
double right = number;
double mid = 0.0;

while (left + epsilon < right) {
mid = (left + right)/2.0;
if (mid*mid > number) {
right = mid;
} else {
left = mid;

