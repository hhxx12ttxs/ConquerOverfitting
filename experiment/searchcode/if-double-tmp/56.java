public double pow(double x, int n) {
boolean neg = false;
double ans = 0;
if(n<0){
neg = true;
n = Math.abs(n);
return x;
}
double tmp = 0;
if(n%2==0){
tmp = pow(x,n/2);
ans = tmp * tmp;
}else{

