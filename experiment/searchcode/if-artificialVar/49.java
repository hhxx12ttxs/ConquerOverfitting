protected void initializeColumnLabels() {
if (getNumObjectiveFunctions() == 2) {
columnLabels.add(&quot;W&quot;);
for (int i = 0; i < getOriginalNumDecisionVariables(); i++) {
columnLabels.add(&quot;x&quot; + i);
}
if (!restrictToNonNegative) {
columnLabels.add(NEGATIVE_VAR_COLUMN_LABEL);

