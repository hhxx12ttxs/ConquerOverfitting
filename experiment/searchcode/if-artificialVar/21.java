this.tableau = createTableau((goalType == (org.apache.commons.math.optimization.GoalType.MAXIMIZE)));
initializeColumnLabels();
}

protected void initializeColumnLabels() {
if ((getNumObjectiveFunctions()) == 2) {
for (int i = 0 ; i < (getOriginalNumDecisionVariables()) ; i++) {
columnLabels.add((&quot;x&quot; + i));
}
if (!(restrictToNonNegative)) {

