for (int i = 0; i < total - 1; i++) {
mat[i][i] = 1;
if (row[i] == 0) {
int targetRow = 0;
int targetColumn = (column[i] + 1) % M;
mat[i][total - 1] += p * (endRow - startRow + endColumn - startColumn + 2);
for (int k = 0; k < total - 1; k++) {
if (targetRow == row[k] &amp;&amp; targetColumn == column[k]) {

