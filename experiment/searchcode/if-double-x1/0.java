// x2 = x1 - f(x1)/f&#39;(x1)
// x2 = x1 - (x1^2-a^2)/(2*x1)
public int mySqrt(int a) {
if (a == 0)
return 0;
double eps = 1E-4;
double x1 = a;
while (true) {

