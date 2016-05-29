double power(double x, int n){
if(n<0){
x = 1/x;
n = -n;
}
dboule ret = 1.0;
while(n>0){
if( (n&amp;0x01) > 0 ){
ret *= x;
}
x *= x;
n >>>= 1;
}
return ret;
}

//Recursive
public double pow(double x, int n) {

