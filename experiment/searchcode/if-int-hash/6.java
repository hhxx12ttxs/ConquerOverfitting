HashSet<Character> hashB = new HashSet<Character>();
HashSet<Character> hashC = new HashSet<Character>();
int k, l;
for (int i = 0; i < 9; i++) {
for (int j = 0; j < 9; j++) {
k = 3 * (i % 3) + j % 3;
l = 3 * (i / 3) + j / 3;
if (board[i][j] != &#39;.&#39; &amp;&amp; hashA.contains(board[i][j]))
return false;
if (board[j][i] != &#39;.&#39; &amp;&amp; hashB.contains(board[j][i]))

