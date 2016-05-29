public void rotate(int[][] matrix)
{
if (matrix == null)
return;

int dim = matrix[0].length;

for (int i = 0; i < dim; i++)
for (int j = 0; j < dim - i; j++)
{
int temp = matrix[i][j];
matrix[i][j] = matrix[dim - 1 - j][dim - 1 - i];

