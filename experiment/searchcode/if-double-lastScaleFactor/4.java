final double b, final double epsilon) {
double p0, p1, q0, q1, c, relativeError, vv, ww, p2, q2, r, scaleFactor, lastScaleFactor, scale;
scaleFactor = 1d;
lastScaleFactor = 1d;

scale = Math.max(vv, ww);
if (scale <= 0) {
return Double.NaN;

