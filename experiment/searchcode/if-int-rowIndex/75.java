for (int i = 0; i < m; i++) {
for (int j = 0; j < n; j++) {
if (dfs(board, word, 0, i, j, visited))
public boolean dfs(char[][] board, String word, int index, int rowindex, int colindex, boolean[][] visited) {

