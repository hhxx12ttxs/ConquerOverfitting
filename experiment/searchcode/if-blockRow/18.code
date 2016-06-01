public byte[][] solve(byte[][] sudoku) {
sortTestValuesByFrequency(sudoku);
sortIndexesByNumberOfPrefilled(sudoku);
if (iteratePartialSolution((byte) 0, (byte) 0, sudoku)) {
for (int row = 0; row < 9; row++) {
for (int column = 0; column < 9; column++) {
if (sudoku[row][column] > 0) {

