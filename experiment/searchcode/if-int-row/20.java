public int[][] generateMatrix(int n) {
int[][] matrix = new int[n][n];
if(n==0)
return matrix;
int rowB = 0;
int rowE = n - 1;
for (int i = rowB; i <= rowE; i++)
matrix[i][colE] = count++;
colE--;
if (rowB <= rowE) {
for (int i = colE; i >= colB; i--)

