for (int row = 0; row < 9; row++) {
for (int column = 0; column < 9; column++) {
if (sudoku[row][column] > 0) {
for (int blockRow = blockRowStart; blockRow < blockRowStart + 3; blockRow++) {
for (int blockColumn = blockColumnStart; blockColumn < blockColumnStart + 3; blockColumn++) {

