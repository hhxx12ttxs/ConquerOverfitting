boolean[] nullrow = new boolean[ncol];
boolean[] nullcol = new boolean[nrow];

if(nrow != ncol) return;

for (int i = 0; i < nrow; i++) {
for (int j = 0; j < ncol; j++) {
if(mat[i][j] == 0) {
nullrow[i] = true;

