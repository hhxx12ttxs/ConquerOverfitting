public static double findSquareRoot(double n) {
double l = 0;
double u = 0;
int sign = 1;

if(n>=1) {
else if (n>=0 &amp;&amp; n < 1) {
l = n;
u = 1;
}
else if (n<= -1) {
sign = -1;
u = n * sign;

