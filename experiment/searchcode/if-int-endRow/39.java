public static void spiralPrint(int[][] matrix) {
int startRow = 0, endRow, startCol = 0, endCol;
int m = matrix.length;
int n = matrix[0].length;
while (startRow <= endRow &amp;&amp; startCol <= endCol) {
for (int i = startCol; i <= endCol; i++) {
System.out.print(matrix[startRow][i] + &quot; &quot;);

