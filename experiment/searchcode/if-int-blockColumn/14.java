private SudokuGrid iteratePartialSolution(int row, int column, SudokuGrid sudokuGrid) {
if (row >= 9) {
int newColumn = (column + 1) % 9;
int newRow = row + (newColumn == 0 ? 1 : 0);
if (sudokuGrid.getFieldValue(row, column) == 0) {

