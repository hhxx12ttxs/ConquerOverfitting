* @return column with the most negative coefficient
*/
private Integer getPivotColumn(SimplexTableau tableau) {
final double entry = tableau.getEntry(0, i);
if (Precision.compareTo(entry, minValue, maxUlps) < 0) {

