public class Solution {
public int uniquePaths(int m, int n) {
if(m == 0 || n == 0)
return 0;
int[][] result = new int[m][n];
for(int i = 0; i < m; i ++)

