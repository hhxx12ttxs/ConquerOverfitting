long diag=(long)Math.ceil((Math.sqrt(8*n+1)-1)/2);
long endPoint=((diag*diag)+diag)/2;
long diff=0;
if(n>endPoint){
diff=n-endPoint;
}else{

