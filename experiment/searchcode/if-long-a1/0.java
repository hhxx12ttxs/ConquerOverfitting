public int pow(int a, int b, int m) {
if (a == 0) {
return 0;
}
long a1 = a % m;
long p = 1;
while (b > 0) {
if ((b &amp; 1) == 1) {
p = (p * a1) % m;

