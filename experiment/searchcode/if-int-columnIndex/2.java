int findMax(int rowIndex, int columnIndex, int[][] matrix) {
if (rowIndex < matrix.length &amp;&amp; columnIndex < matrix[0].length) {
Math.max(findMax(rowIndex + 1, columnIndex, matrix), findMax(rowIndex, columnIndex + 1, matrix));
}
if (rowIndex < matrix.length) {
return matrix[rowIndex][columnIndex] + findMax(rowIndex + 1, columnIndex, matrix);

