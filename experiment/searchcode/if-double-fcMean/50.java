double c2 = sxy * sxz - sx2 * syz;
double c3 = sx2 * sy2 - sxy * sxy;
if ((c1 / c2 < 0) || (c2 / c3 < 0)) {
final double y = observations[i].getY();
if (y < yMin) {
yMin = y;

