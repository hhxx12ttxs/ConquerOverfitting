public Point_3 getPointbyHigh(double h){
double fpx = fp.getx();
double fpy = fp.gety();
double fpz = fp.getz();
double c = h-epy;
double d= c/b;
if(Double.isNaN(d)){
d=0;
}
if(Double.isInfinite(d)){

