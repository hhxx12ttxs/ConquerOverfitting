ArgChecker.inRangeInclusive(x, 0d, 1d, &quot;x&quot;);
double pp, p, t, h, w, lnA, lnB, u, a1 = _a - 1;
final double b1 = _b - 1;
lnB = Math.log(_b / (_a + _b));
t = Math.exp(_a * lnA) / _a;
u = Math.exp(_b * lnB) / _b;
w = t + u;
if (x < t / w) {

