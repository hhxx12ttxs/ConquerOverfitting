if ((org.apache.commons.math.util.MathUtils.compareTo(entry, 0, epsilon)) > 0) {
final double ratio = rhs / entry;
if (org.apache.commons.math.util.MathUtils.equals(ratio, minRatio, epsilon)) {
throw new org.apache.commons.math.optimization.linear.UnboundedSolutionException();
}
double pivotVal = tableau.getEntry(pivotRow, pivotCol);
tableau.divideRow(pivotRow, pivotVal);

