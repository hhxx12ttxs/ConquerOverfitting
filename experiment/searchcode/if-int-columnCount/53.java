public int getMinimum(String[] board, int K) {
int rowCount = board.length;
int columnCount = board[0].length();
int answer = Integer.MAX_VALUE;
for (int k = 0; k < columnCount; k++) {
int current = 0;
for (int l = k; l < columnCount; l++) {

