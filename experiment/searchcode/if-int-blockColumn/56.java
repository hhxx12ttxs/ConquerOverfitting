private void printBoard(Piece[][] board, boolean printSuccessfulBoards) {
if (printSuccessfulBoards) {
int boardRows = board.length;
for (int r = 0; r < boardRows; r++) {
for (int c = 0; c < boardColumns; c++) {
if (board[r][c] == null) {

