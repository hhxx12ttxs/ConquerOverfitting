public double evaluate(double x){
double value=0;
double var=1;
for (int i=0;i<a.length;i++){
value+=a[i]*var;
var*=x;
}
return value;
}

public int getDegree(){
for (int i=a.length-1;i>=0;i--){
if (a[i]!=0){
return i;
}
}
return Integer.MIN_VALUE;
}
}

