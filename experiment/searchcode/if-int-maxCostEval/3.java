protected double cost;

/** Maximal allowed number of cost evaluations. */
private int maxCostEval;
int m = problem.getMeasurements().length;
int p = problem.getUnboundParameters().length;
if (m <= p)
throw new EstimationException(

