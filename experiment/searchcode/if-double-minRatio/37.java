double[] fitparams={coef[0],coef[1],t};
double c2val=c2(fitparams,data,xvals);
if(first){
c2min=c2val;
double c2min=0.0;
double[] minparams=new double[6];
boolean first=true;
for(double t1=mint;t1<(maxt/minratio);t1*=tmult){

