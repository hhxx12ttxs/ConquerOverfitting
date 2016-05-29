/** Desired relative error in the sum of squares. */
private final double costRelativeTolerance;
/** Desired relative error in the approximate solution parameters. */
public LevenbergMarquardtOptimizer(double initialStepBoundFactor, ConvergenceChecker<PointVectorValuePair> checker, double costRelativeTolerance, double parRelativeTolerance, double orthoTolerance, double threshold) {

super(checker);

