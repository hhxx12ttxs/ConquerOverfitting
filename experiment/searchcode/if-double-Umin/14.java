p = p + (s - yy) * (s - yy);
e = Math.abs(y[i] - s);
if (e > umax)
umax = e;
if (e < umin)
umin = e;
private static int chlk(double[] a, int n, int m, double[] d) {
int i, j, k, u, v;
if ((a[0] + 1.0 == 1.0) || (a[0] < 0.0)) {

