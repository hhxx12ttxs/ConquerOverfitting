int start = ignoreObjectiveRows ? getNumObjectiveFunctions() : 0;
for (int i = start; i < getHeight(); i++) {
if (MathUtils.equals(getEntry(i, col), 1.0, epsilon) &amp;&amp; (row == null)) {
protected void divideRow(final int dividendRow, final double divisor) {
for (int j = 0; j < getWidth(); j++) {

