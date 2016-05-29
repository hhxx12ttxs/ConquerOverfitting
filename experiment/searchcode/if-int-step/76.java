public int totalNQueens(int n) {
dfs(new int[n],0,n);
return count;
}
public void dfs(int[] pos,int step,int n) {
if(step==n) {
count++;
return;
}
for(int i=0;i<n;i++) {

