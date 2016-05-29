// = { x^floor(n/2)*x^floor(n/2)*x if n odd, n > 0
// = { x^ceil(n/2)*x^ceil(n/2)/x if n odd, n < 0
public double pow(double x, int n) {
if (n == 0)
return 1.0;

double x2 = pow(x, n / 2);
if ((n &amp; 0x1) == 0) {
// Even
return x2 * x2;

