// initialize working arrays
if (y != y0) {
System.arraycopy(y0, 0, y, 0, n);
}
final double[] yDot = new double[n];
// predict a first estimate of the state at step end
final double stepEnd = stepStart + stepSize;

