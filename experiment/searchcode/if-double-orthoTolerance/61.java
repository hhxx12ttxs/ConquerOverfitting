private final double orthoTolerance;
/** Threshold for QR ranking. */
private final double qrRankingThreshold;
public LevenbergMarquardtOptimizer(double initialStepBoundFactor, ConvergenceChecker<PointVectorValuePair> checker, double costRelativeTolerance, double parRelativeTolerance, double orthoTolerance, double threshold) {

super(checker);

