public static double div(double a, double b) {
if (b == 0.0) {
return 0.0;
} else {
return a / b;
}
}

public static double divdivmin(double a, double b, double c, double d) {
public static double normalInequality(double muL, double varL, double muR, double varR, double cov) {
double stDev = Math.sqrt(varL + varR + 2*cov);
if (Double.isNaN(stDev)) {

