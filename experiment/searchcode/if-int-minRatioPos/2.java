private Integer getPivotColumn(SimplexTableau tableau) {
double minValue = 0;
Integer minPos = null;
for (int i = tableau.getNumObjectiveFunctions(); i < tableau.getWidth() - 1; i++) {
Integer minRatioPos = null;
for (int i = tableau.getNumObjectiveFunctions(); i < tableau.getHeight(); i++) {

