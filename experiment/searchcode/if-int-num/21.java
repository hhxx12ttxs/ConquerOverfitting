public class Solution {
public int uniquePaths(int m, int n) {
if (m == 0 || n == 0)
return 0;
int num[][] = new int[m][n];

num[0][0] = 1;
for (int i = 0; i < m; i++) {

