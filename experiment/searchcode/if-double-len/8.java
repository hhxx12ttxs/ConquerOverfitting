public static double[] concat(double[] a, double[] b) {
int aLen = a.length;
int bLen = b.length;
double[] c = new double[aLen+bLen];
System.arraycopy(a, 0, c, 0, aLen);
System.arraycopy(b, 0, c, aLen, bLen);

