public static boolean isInside1(double x1,double y1,double x2,double y2,
double x3,double y3,double x,double y){
double area1=getAreaOfTriangle(x1,y1,x2,y2,x,y);
public static boolean isInside2(double x1,double y1,double x2,double y2,
double x3,double y3,double x,double y){
if(getCrossProduct(x3-x1,y3-y1,x2-x1,y2-y1)>=0){//换B和c

