for (int i = tableau.getNumObjectiveFunctions(); i < tableau.getWidth() - 1; i++) {
final double entry = tableau.getEntry(0, i);
// check if the entry is strictly smaller than the current minimum
final double rhs = tableau.getEntry(i, tableau.getWidth() - 1);
final double entry = tableau.getEntry(i, col);

if (Precision.compareTo(entry, 0d, maxUlps) > 0) {

