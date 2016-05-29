return 1/helper(x,n);
}
}
public double helper(double x, int n){
if( n == 0){
return 1;
}
if( n == 1 || n == -1){
return x;
}
double tmp=helper(x,n/2);
tmp=tmp*tmp;
if(n%2 != 0){
tmp=tmp*x;
}
return tmp;
}
}

