public static double[] GetVerticalPoint(double x0,double y0,double x1,double y1,double x2,double y2){
//当平行与y轴
if(x1 == x2){
double length = Distance(xa,ya,xb,yb);
if(length<distance){
return null;
}
double xc = xa+(xb-xa)*distance/length;

