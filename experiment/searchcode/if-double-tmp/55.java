public static double rekursiv(double n){

if(n==1){

return 1;
}

n = rekursiv(n-1)+n+n-1;
return n;
}

public static double iterativ(double n){

double tmp = 0;

for(int i = 1 ; i < n+1 ; ++i){

