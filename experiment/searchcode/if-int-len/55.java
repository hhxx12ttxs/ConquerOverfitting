for (int i = 0; i < m; i++) {
if (matrix[i][0] == &#39;1&#39;) {
maxLen = 1;
len[i][0] = 1;
}
}
for (int i = 0; i < n; i++) {
if (matrix[0][i] == &#39;1&#39;) {
maxLen = 1;
len[0][i] = 1;
}
}
for (int i = 1; i < m; i++) {

