public double sqrt(double n) {
double i = 0, j = n;
double mid = (i+j) / 2.0;
while(Math.abs(mid*mid-n)>0.0001) {
if(n/mid > mid) i = mid;
else j = mid;
mid = (i+j) / 2.0;
}
return mid;
//if we want to get the result of accuracy of two decimal places:

