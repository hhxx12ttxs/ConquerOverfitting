int np;
double last;

int ipt = 0;

public Interpolator(double[] x, double[] y) {
xpts = x;
ypts = y;
ret = ypts[np - 1];

} else {
if (x < xpts[ipt]) {
while (true) {
if (ipt == 0 || x < xpts[ipt-1]) {
break;

