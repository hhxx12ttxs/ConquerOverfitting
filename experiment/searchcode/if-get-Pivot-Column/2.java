for (int l = 0; l < table.get(0).length; l++) {
if (l != pivotColumn) {
table.get(k)[l] = table.get(k)[l] - table.get(k)[pivotColumn] * table.get(pivotRow)[l];
for (int columnIndex = 0; columnIndex < table.iterator().next().length - 1; columnIndex++) {
Double value = table.get(pivotRow)[columnIndex];
if (value < 0) {

