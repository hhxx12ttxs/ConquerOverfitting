protected double[] residuals;

protected double cost;

private int maxCostEval;

private int costEvaluations;

private int jacobianEvaluations;
setMaxCostEval(DEFAULT_MAX_COST_EVALUATIONS);
}

public final void setMaxCostEval(int maxCostEval) {
this.maxCostEval = maxCostEval;
}

public final int getCostEvaluations() {

