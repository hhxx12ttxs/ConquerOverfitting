

public class Vector {
public static double[] vectorsSum(double[] v1, double[] v2) {
double[] result = new double[v1.length];
public static double vectorInnerProduct(double[] v1, double[] v2) {
if (v1.length != v2.length) {
throw new ArithmeticException();
}
double result = 0;

