STEPFAC5=0.5,KFAC1=0.7,KFAC2=0.9;
int i,k=0;
double fac,h,hnew,err=0;
boolean firstk;
double[] hopt = new double[IMAXX],work = new double[IMAXX];
h = forward ? hnew : -hnew;
firstk=false;
reject=false;
if (abs(h) <= abs(x)*EPS)

