double c2 = sxy * sxz - sx2 * syz;
double c3 = sx2 * sy2 - sxy * sxy;
if ((c1 / c2 < 0) || (c2 / c3 < 0)) {
// observations are sorted.
final double xRange = observations[last].getX() - observations[0].getX();
if (xRange == 0) {

