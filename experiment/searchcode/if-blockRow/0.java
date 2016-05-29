public boolean solver(char[][]board){
int[] first = findFirstEmpty(board);
if (first[0] == -1 &amp;&amp; first[1] == -1) {
board[first[0]][first[1]] = (char) (i + &#39;0&#39;);
System.out.println(solver(board));
if (solver(board) &amp;&amp; isValidBoard(board, first)){

