return this.numResult;
}

public void dfs(int[] board, int rowIndex, int n){
if(rowIndex == n){
numResult++;
dfs(board,rowIndex+1,n);
}
}

// checks if the position in row rowIndex conflicts with previous rows
public boolean isValid(int[] board, int rowIndex){

