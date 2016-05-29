/** Positive input variable used in determining the initial step bound. */
private final double initialStepBoundFactor;
/** Desired relative error in the sum of squares. */
*            product of initialStepBoundFactor and the euclidean norm of {@code diag * x} if non-zero, or else to {@code initialStepBoundFactor} itself. In most cases factor should lie in the interval {@code (0.1, 100.0)}. {@code 100} is a generally recommended value.

