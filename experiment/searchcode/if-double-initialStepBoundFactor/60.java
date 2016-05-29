/** Positive input variable used in determining the initial step bound. */
private final double initialStepBoundFactor;
/** Desired relative error in the sum of squares. */
* {@code diag * x} if non-zero, or else to {@code initialStepBoundFactor}
* itself. In most cases factor should lie in the interval

