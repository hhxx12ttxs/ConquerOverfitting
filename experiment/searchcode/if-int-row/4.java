private void helper(int n,int row,int[] colForRow){
if(row==n){
num++;
return;
}
for(int i=0;i<n;i++){
private boolean check(int row,int[] colForRow){
for(int i=0;i<row;i++){
if(colForRow[i]==colForRow[row]||Math.abs(row-i)==Math.abs(colForRow[row]-colForRow[i]))

