for (int blockRow = startPos; blockRow < startPos + 48; blockRow += 16) {
for (int blockColumn = 0; blockColumn < 3; blockColumn++) {
if (sudoku[blockRow + blockColumn] > 0 &amp;&amp; (++distinctValueChecker[sudoku[blockRow + blockColumn]]) > 1) {

