&amp;&amp; depth <= rows - 1 - depth; depth++) {
if (depth == cols - 1 - depth) {
for (int i = depth, j = depth; i <= rows - 1 - depth; i++) {
res.add(matrix[i][j]);
}
} else if (depth == rows - 1 - depth) {
for (int i = depth, j = depth; j <= cols - 1 - depth; j++) {

