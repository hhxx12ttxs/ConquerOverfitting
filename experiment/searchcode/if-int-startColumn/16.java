for (int i = 0; i < height; i++) {
for (int j = 0; j < width; j++) {
if (findWord(board, i, j, chars, 0)) {
private boolean findWord(char[][] board, int startRow, int startColumn, char[] chars, int pos) {
if (board[startRow][startColumn] != chars[pos]) {

