double c2 = sxy * sxz - sx2 * syz;
double c3 = sx2 * sy2 - sxy * sxy;
if ((c1 / c2 < 0.0) || (c2 / c3 < 0.0)) {
// initialize the means
double fcMean = 0.0;
double fsMean = 0.0;

double currentX = observations[0].getX();

