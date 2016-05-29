public double powRecursive(double x, int n) {
if (n==0) return 1;
if (n<0){
n=-n; //need to /2 right away because Integer.min*-1 will overflow
}
double tmp = powRecursive(x,n/2);
return n%2==0?tmp*tmp:tmp*tmp*x;

