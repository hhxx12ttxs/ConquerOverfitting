public static double[][] apply(double[] data, int size) throws Exception {
if (data.length > size) {
double[] real = new double[size];
double[] imag = new double[size];
System.arraycopy(data,0, real, 0, data.length);

