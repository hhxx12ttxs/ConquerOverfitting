Integer minRatioPos = null;
for (int i = tableau.getNumObjectiveFunctions(); i < tableau.getHeight(); i++) {
public boolean isOptimal(final SimplexTableau tableau) {
if (tableau.getNumArtificialVariables() > 0) {
return false;
}
for (int i = tableau.getNumObjectiveFunctions(); i < tableau.getWidth() - 1; i++) {

