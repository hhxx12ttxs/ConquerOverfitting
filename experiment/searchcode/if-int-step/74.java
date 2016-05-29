step = step.getPreviousStep();
step = step.changeDirection();
if (getCurrentPosition() == getLastPosition()) {
private void updateMatrix(int i, int j) {
if (matrix[i][j] == 0) {
counter++;
matrix[i][j] = counter;

