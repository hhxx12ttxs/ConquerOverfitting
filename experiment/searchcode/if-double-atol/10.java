int i,nvar=4;
final double atol=1.0e-6,rtol=atol,h1=0.01,hmin=0.0,x1=1.0,x2=2.0;
double sbeps;
//dydx= new double[nvar]
double[] y= new double[nvar],yout= new double[nvar],yexp= new double[nvar];

