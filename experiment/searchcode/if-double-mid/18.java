public static double findSquareRoot(double n){
double precision = .0001;
double mid = 0;
double guess, high, low;
if(n < 1){
low = n;
while(Math.abs(n - guess) > precision){
if(guess < n){
low = mid;
}else{
high = mid;
}
mid = (low + high)/2;

