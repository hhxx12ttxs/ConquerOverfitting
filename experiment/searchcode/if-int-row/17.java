int[] colForRow = new int[n];
if(n <= 0)
return 0;
placeQueen(n,colForRow,0);
return this.total;
}
private void placeQueen(int n, int[] colForRow, int row){
if(row == n){

