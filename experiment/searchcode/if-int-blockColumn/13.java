return internalSudoku;
}

private boolean iteratePartialSolution(int row, int column, byte[] sudoku) {
if (row >= 9) {
for (int blockRow = startPos; blockRow < startPos + 27; blockRow += 9) {
for (int blockColumn = 0; blockColumn < 3; blockColumn++) {
if (sudoku[blockRow + blockColumn] > 0 &amp;&amp; (++distinctValueChecker[sudoku[blockRow + blockColumn]]) > 1) {

