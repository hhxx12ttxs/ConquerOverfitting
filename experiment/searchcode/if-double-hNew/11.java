derivs.derivs(xnew,yn,yend);
double h2=2.0*h;
for (int nn=1;nn<nstep;nn++) {
if (dense &amp;&amp; nn == nstep/2) {
int i,k=0;
double fac,h,hnew,hopt_int=0;
doubleW err = new doubleW(0);
boolean firstk;
double[] hopt =new double[IMAXX],work=new double[IMAXX];

