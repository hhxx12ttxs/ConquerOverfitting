double minRatio = Double.MAX_VALUE;
Integer minRatioPos = null;
for (int i = tableau.getNumObjectiveFunctions(); i < tableau.getHeight(); i++) {
// set the rest of the pivot column to 0
for (int i = 0; i < tableau.getHeight(); i++) {
if (i != pivotRow) {

