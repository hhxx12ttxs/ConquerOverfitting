public static double pow(double x, int n) {
if (x == 0){
return 0;
}

if(n==0){
return 1/x;
}

double result;
double mid = pow(x, n/2);

if(n%2==1){

