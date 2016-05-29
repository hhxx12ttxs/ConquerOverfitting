public class AliceGameEasy {

public long findMinimumValue(long x, long y) {
long a = 1;
long b = 1;
long c = -2*(x+y);
long d = b*b - 4*a*c;
double nd = (-b + Math.pow(d, 0.5))/2*a;
if (Math.round(nd)!=nd) return -1;

