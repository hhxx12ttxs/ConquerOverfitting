if (x == 0 || x == 1) {
return x;
}

long prev = 0;
long curr = 1;

while (curr * curr < x) {
while (curr >= prev) {
long mid = (curr + prev) / 2;

if (mid * mid == x) {
return (int) mid;

