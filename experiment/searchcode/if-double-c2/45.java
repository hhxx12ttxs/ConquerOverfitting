public void filt(final double[] a, final int n, final int isign) {
final double C0 = 0.4829629131445341, C1 = 0.8365163037378077, C2 = 0.2241438680420134, C3 = -0.1294095225512603;
int nh, i, j;
if (n < 4)
return;
double[] wksp = new double[n];
nh = n >> 1;

