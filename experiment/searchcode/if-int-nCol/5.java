int nrow = grid.length;
if (nrow == 0) return 0;
int ncol = grid[0].length;

int[][] table = new int[nrow][ncol];

for (int i = 0; i < nrow; i++) {
for (int j = 0; j < ncol; j++) {

