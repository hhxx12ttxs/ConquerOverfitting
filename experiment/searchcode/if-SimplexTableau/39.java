private java.lang.Integer getPivotColumn(org.apache.commons.math.optimization.linear.SimplexTableau tableau) {
for (int i = tableau.getNumObjectiveFunctions() ; i < ((tableau.getWidth()) - 1) ; i++) {
if ((org.apache.commons.math.util.MathUtils.compareTo(tableau.getEntry(0, i), minValue, epsilon)) < 0) {

