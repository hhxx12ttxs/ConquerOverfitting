static double area(double x1, double y1, double x2, double y2, double x3, double y3) {
return Math.abs((x1*(y2-y3) + x2*(y3-y1)+ x3*(y1-y2))/2.0);
double x1 = 12.5, y1 = 8.5;
double x2 = 22.5, y2 = 8.5;
double x3 = 17.5, y3 = 3.5;

if (isInside(x1, y1, x2, y2, x3, y3, x, y) || ((x >= 12.5) &amp; (x <= 17.5) &amp; (y >= 8.5) &amp; (y <= 13.5))

