// Note: The Solution object is instantiated only once and is reused by
// each test case.
if (n < 1) {
throw new IllegalArgumentException();
}

int[][] result = new int[n][n];
private void helper(int[][] matrix, int startRow, int endRow, int startCol,
int endCol, int num) {
if (startRow > endRow || startCol > endCol) {

