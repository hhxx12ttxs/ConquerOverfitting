e=Math.abs((b-a)/2);
x=(b+a)/2;
if(fun.f(x)>0)
b=x;
if(fun.f(x)<0)
a=x;
}
return x;
}


static double puntounito(MFunction fun, double eps, double x0){

double[] x = new double[200];
double[] dx = new double[200];

