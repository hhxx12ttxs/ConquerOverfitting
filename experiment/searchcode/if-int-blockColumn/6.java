for (int blockRow = blockRowStart; blockRow < blockRowStart + 3; blockRow++) {
for (int blockColumn = blockColumnStart; blockColumn < blockColumnStart + 3; blockColumn++) {
distinctValueChecker[sudoku[blockRow][blockColumn]] = 1;
}
}

int score = 0;
for (int value : distinctValueChecker){

