if (table.isEmpty() || table.get(0).length == 0) {
return -1;
}
Integer pivotRow = getPivotRow();
if (pivotRow == -1) {
table.get(pivotRow)[l] = table.get(pivotRow)[l] / pivotValue;
}
for (int k = 0; k < table.size(); k++) {
if (k != pivotRow &amp;&amp; table.get(k)[pivotColumn] != 0) {

