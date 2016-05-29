double sqrt(double x){
if(x<0) return -1;
if(x==0 || x==1) return x;
double left, right;
double e = 0.000001;
while(Math.abs(left-right)<=e){
double mid = left + (right-left)/2;
if(Math.abs(x/mid-mid)<=e){
return mid;

