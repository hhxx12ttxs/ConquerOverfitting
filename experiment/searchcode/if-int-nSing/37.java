// x_orig = new double[noOfFreeParameters];
int count = 0;
for (int i = 0; i < params.length; i++) {
if (!params[i].isFixed()) {
* private double dnrm2_j(int noOfParameters, double x[], int incx) { double absxi, norm, scale, ssq, fac; int ix,
* limit; if (noOfParameters < 1 || incx < 1) { norm = 0.0; } else if (noOfParameters == 1) { norm = Math.abs(x[0]); }

