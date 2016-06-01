for (int i = tableau.getNumObjectiveFunctions(); i < tableau.getWidth() - 1; i++) {
final double entry = tableau.getEntry(0, i);
if (Precision.compareTo(entry, minValue, maxUlps) < 0) {
final double entry = tableau.getEntry(i, col);

if (Precision.compareTo(entry, 0d, maxUlps) > 0) {
final double ratio = rhs / entry;

