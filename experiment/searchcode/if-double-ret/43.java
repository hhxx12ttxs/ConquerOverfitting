private static final double EPSILON = 0.00000000001;

public static double[] invertAxisAngle(double[] r) {
double[] ret = new double[4];

ret[0] = r[0];
ret[1] = r[1];
ret[2] = r[2];
ret[3] = -r[3];

return ret;
}

public static double[] rotate3DVectorMatrix(double[] t, double[][] m) {

