public static double Gauss(double a,double b,double c,double z) throws IllegalArgumentException {
if(Math.abs(z) >= 1)
return Double.NaN;
if (a<=0 | b<=0 | c<=0 | z<=0) return Double.NaN;
double y = uj;
double diff = 1;
while(diff > EPS){
double lastY = y;
++j;
uj = uj*(a+j-1)*(b+j-1)*z / (c+j-1) / j;

