double lastScaleFactor = 1d;
final int maxPower = 5;
final double scale = Math.max(a, b);
if (scale <= 0) {	// Can&#39;t scale
q2 = (a / scaleFactor * q1) + q0 / lastScaleFactor;
}
infinite = Double.isInfinite(p2) || Double.isInfinite(q2);
if (!infinite) {

