public static Point evaluateCoordinates(double xa,double ya, double xb, double yb, double xc, double yc, double ra, double rb, double rc){
double y = (vb * (xc - xb) - va * (xa - xb)) / ((ya - yb) * (xc - xb) - (yc - yb) * (xa - xb));
double x;
if((xc - xb)!=0){
x = (va - y * (yc - yb)) / (xc - xb);

