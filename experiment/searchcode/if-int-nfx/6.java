for (int i = 0; i < n; i++) {
for (int j = 0; j < n; j++) {
if (map[i][j] == &#39;#&#39;) {
return 0;
}
return ret;
}

int dfs(int fy, int ty, int fx, int tx) {
if (memo[fy][ty][fx][tx] != -1) {

