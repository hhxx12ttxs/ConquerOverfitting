private int[] sz;

public WeightedQuickUnionPathCompression(int n) {
id = new int[n];
sz = new int[n];
public void union(int p, int q) {
int i = root(p);
int j = root(q);
if (i==j) return;
if (sz[i] < sz[j]) { id[i]=j; sz[j] += sz[i]; }

