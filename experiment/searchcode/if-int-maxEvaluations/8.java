public static final int NO_LIMIT = -1;

protected boolean terminateOnOptimal;
protected int maxEvaluations;
protected int maxRestarts;
public static TerminationPolicy createMaxEvaluationsTerminationPolicy(int maxEvaluations) {
return new TerminationPolicy(true, maxEvaluations, NO_LIMIT, NO_LIMIT);

