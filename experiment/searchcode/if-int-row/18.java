return count;
}

public void nqueens(int[] A, int row){
if(row==A.length)
nqueens(A,row+1);
}
}
}

public boolean valid(int[] A, int row){
for(int i=0;i<row;i++){
if(A[i]==A[row] || Math.abs(A[i]-A[row])==row-i)

