System.out.println();
}

//AC: 38%
public int[][] multiply(int[][] A, int[][] B) {
if (A == null || A.length == 0 || A[0].length == 0 || B == null || B.length == 0 || B[0].length == 0) return null;
int nRowA = A.length;
int nColARowB = A[0].length;
int nColB = B[0].length;
int i, j;
int[][] narrarrRet = new int[nRowA][nColB];

