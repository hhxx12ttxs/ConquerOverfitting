* And determine the lex-smallest of those rows.
*/
double minRatio = getMinRatio(enteringVariable);
//		List<Integer> minRatioRows = getMinRatioRows(minRatio, enteringVariable);
double minRatio = Double.POSITIVE_INFINITY;
for (int i = 1; i < nrOfRows(); i++) {
if(Util.greater(tableau[pivotColumn][i],0)){

