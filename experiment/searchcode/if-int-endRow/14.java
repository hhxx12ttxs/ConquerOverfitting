for (int i = 0; i < total - 1; i++) {
mat[i][i] = 1;
if (row[i] == 0) {
int targetRow = 0;
int targetColumn = (column[i] + 1) % M;
mat[i][total - 1] += .5;
for (int j = 0; j < total - 1; j++) {
if (row[j] == targetRow &amp;&amp; column[j] == targetColumn) {

