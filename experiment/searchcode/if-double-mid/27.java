*  1) Special case for x == 1
*  2) Integer overflow, when start+end or mid*mid
*
*  Double output: http://www.careercup.com/question?id=4419686
public double sqrt(double x, double delta, double start, double end){
double mid = (start+end)/2;
if ( Math.abs(x-mid*mid)<=delta)

