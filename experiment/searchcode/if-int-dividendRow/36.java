int col = 0;
for (int j = 0; j < getWidth(); j++) {
if (!columnsToDrop.contains(j)) {
for (int i = 0; i < coefficients.length; i++) {
int colIndex = columnLabels.indexOf(&quot;x&quot; + i);
if (colIndex < 0) {

