for (int i = tableau.getNumObjectiveFunctions(); i < tableau.getWidth() - 1; i++) {
if (MathUtils.compareTo(tableau.getEntry(0, i), minValue, epsilon) < 0) {
double ratio = rhs / tableau.getEntry(i, col);
if (ratio < minRatio) {
minRatio = ratio;
minRatioPos = i;

