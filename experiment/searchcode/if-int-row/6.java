private void dfs(int row, int n, int[] a, int[] ret){
if(row == n){
ret[0] ++;
return;
}

for(int i = 0; i < n; i++){
for(int i = 0; i < row; i++){
if(a[i] == a[row] || Math.abs(a[i] - a[row]) == Math.abs(i - row)){

