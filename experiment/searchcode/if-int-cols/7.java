int[] res = new int[1];
if(n <= 0)
return 0;
int[] cols = new int[n];
dfs(n, res, 0, cols);
return res[0];
}
public void dfs(int n, int[] res, int rows, int[] cols) {
if(rows == n) {

