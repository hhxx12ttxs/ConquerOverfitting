public double pow(double x, int n) {
if (n < 0) {
n = -n;
x = 1 / x;
}
double result = 1;
for (double f = x; n > 0; n = n >> 1) {
System.out.println(&quot;Result=&quot;+result+&quot; F=&quot;+f+&quot; N=&quot;+n);

