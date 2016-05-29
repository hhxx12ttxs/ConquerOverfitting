private void dfs(DiGraph G, int u) {
seenU[u] = true;
for(int v:G.adj(u)) if (!seenU[v])
dfs(G,v);
}

private void findLCA(DiGraph G, int u) {
if (seenU[u] &amp;&amp; height[u] > LCAHeight) {

