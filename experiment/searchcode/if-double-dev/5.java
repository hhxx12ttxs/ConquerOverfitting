public static double covarStd(double[] x, double[] y, boolean population) {
if (population) {
return covar(x, y) / (ListStats.popStdDev(x) * ListStats.popStdDev(y));
return covar(x, y) / (ListStats.sampStdDev(x) * ListStats.sampStdDev(y));
}
}

public static double covarStd(double[] x, double[] y, double xStdDev, double yStdDev) {

