else if(n==-1)
return 1/x;
else if(n>0){
double tmp=myPow(x,n/2);
return n%2==0?tmp*tmp:tmp*tmp*x;
}

else{
double tmp=myPow(x,n/2);

