double x1 = 0;
double y1 = 0;

double currx = x1;
double curry = y1;

while (noIts < DrawingApp.MAX_ITS){
x1 = currx*currx - curry*curry + a;
y1 = 2*currx*curry + b;

double length =  x1*x1 + y1*y1;

if ( length > 4) {

