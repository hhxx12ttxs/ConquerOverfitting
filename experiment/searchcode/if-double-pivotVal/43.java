for (int i = tableau.getNumObjectiveFunctions(); i < tableau.getWidth() - 1; i++) {
final double entry = tableau.getEntry(0, i);
if (MathUtils.compareTo(entry, minValue, getEpsilon(entry)) < 0) {
final double entry = tableau.getEntry(i, col);

if (MathUtils.compareTo(entry, 0d, getEpsilon(entry)) > 0) {
final double ratio = rhs / entry;

