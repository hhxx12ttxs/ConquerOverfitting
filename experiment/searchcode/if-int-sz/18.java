int [] ids = null;
int [] sz = null;

public QuickUnionWeighted(int n){
this.ids = new int[n];
this.sz = new int[n];
public void unite(int p, int q) {
int i = root(p);
int j = root(q);
if(sz[i] < sz[j]){
ids[i] = j;

