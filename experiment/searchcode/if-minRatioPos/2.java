for (int i = tableau.getNumObjectiveFunctions(); i < tableau.getWidth() - 1; i++) {
if (MathUtils.compareTo(tableau.getEntry(0, i), minValue, epsilon) < 0) {
Integer minRatioPos = null;
for (int i = tableau.getNumObjectiveFunctions(); i < tableau.getHeight(); i++) {

