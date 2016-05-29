package geometry;

public class Geometry {
private static final double EPSILON = 0.00000000001;
private static double[] multiplyQuaternion(double[] q1, double[] q2) {
double[] ret = new double[4];

ret[0] = q1[3] * q2[0] + q1[0] * q2[3] + q1[1] * q2[2] - q1[2] * q2[1];

