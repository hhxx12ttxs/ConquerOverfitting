static double root(double low, double high,double num){
double mid = (low+high)/2;
if(low >= high)
return high;
if(mid * mid == num)
return mid;
if(mid*mid < num)

