* and the columns of the jacobian.
*/
private final double orthoTolerance;
/** Threshold for QR ranking. */
public LevenbergMarquardtOptimizer(double initialStepBoundFactor, ConvergenceChecker<PointVectorValuePair> checker, double costRelativeTolerance, double parRelativeTolerance, double orthoTolerance, double threshold) {

super(checker);
this.initialStepBoundFactor = initialStepBoundFactor;

