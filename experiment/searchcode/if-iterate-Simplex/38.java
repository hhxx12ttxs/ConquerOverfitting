* @throws org.apache.commons.math4.exception.NotStrictlyPositiveException
* if the reference simplex does not contain at least one point.
* @throws org.apache.commons.math4.exception.DimensionMismatchException
original, 1, comparator);
if (comparator.compare(reflected, best) < 0) {
// Compute the expanded simplex.

