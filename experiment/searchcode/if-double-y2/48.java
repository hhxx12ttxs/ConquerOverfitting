Clock clock = new Clock();

double x1 = 0;
double y1 = 10.1;
double x2 = 1.4;
double y2 = -9.6;
int ans=1;

while(true) {
double m0 = (y2-y1)/(x2-x1);
double m1 = deriv(x2,y2);

