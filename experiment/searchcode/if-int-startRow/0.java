private static int sum2d(int[][] a, int startRow, int startCol) {
int sum = 0;
if (startRow < a.length) {
if (startCol < a[startRow].length) {
sum = a[startRow][startCol] + sum2d(a, startRow, startCol + 1);

