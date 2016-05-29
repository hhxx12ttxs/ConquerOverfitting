public class Regression {

public static double[] getLinearRegressionCoefficients(double[] x, double[] y) {
if (y.length != x.length) {
double meanX = Descriptives.mean(x);
double meanY = Descriptives.mean(y);
double sXY = 0;
double sXX = 0;

