private static int getFibonacciLastDigit(int n) {
int re = 0;
int[] series = new int[n];
if (n == 0 &amp;&amp; n == 1) {
re = 1;
}else if (n >= 2) {
series[0] = 1;
series[1] = 1;
int i = 2;
for (; i < n; i++) {

