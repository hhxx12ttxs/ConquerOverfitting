for (int i = getNumObjectiveFunctions(); i < getArtificialVariableOffset(); i++) {
if (MathUtils.compareTo(tableau.getEntry(0, i), 0, epsilon) > 0) {
for (int i = 0; i < getNumArtificialVariables(); i++) {
int col = i + getArtificialVariableOffset();
if (getBasicRow(col) == null) {

