public Box(double xmin, double ymin, double xmax, double ymax) {
if (xmin > xmax) {
double temp = xmin;
xmin = xmax;
xmax = temp;
}
if (ymin > ymax) {
double temp = ymin;
ymin = ymax;
ymax = temp;

