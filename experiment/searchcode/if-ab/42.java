for (i = 0; i < n - 1; i++)
{
for (j = i + 1; j < n; j++)
{
if (Math.abs(AB[i][i]) < eps) return false;
s = AB[i][n];
for (j = n - 1; j >= i + 1; j--)
s -= AB[i][j] * X[j];
if (Math.abs(AB[i][i]) < eps) return false;

