public class Solution
{
public int uniquePaths (int m, int n)
{
if (m == 0 || n == 0)
return 0;
int[][] numPath = new int[m][n];
numPath[0][0] = 1;
for (int i = 1; i < n; ++i)

