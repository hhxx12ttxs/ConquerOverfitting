for (int blockColumn = blockColumnStart; blockColumn < blockColumnStart + 3; blockColumn++) {
if (sudoku[blockRow][blockColumn] > 0 &amp;&amp; (++distinctValueChecker[sudoku[blockRow][blockColumn]]) > 1) {

