private int maxCostEval;

/** Number of cost evaluations. */
private int costEvaluations;

/** Number of jacobian evaluations. */
int m = problem.getMeasurements().length;
int p = problem.getUnboundParameters().length;
if (m <= p) {

