public  double[] getRegionOfSearch(double a, double h){
double a1 = a;
double a2 = a + h;
double f1 = getfx(a1);
double f2 = getfx(a2);
double[] region = new double[2];

if (f2 < f1){ //如果f(a)>f(a+h),则步长加倍

