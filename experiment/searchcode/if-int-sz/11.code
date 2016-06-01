protected int[] sz;

public WeightedQuickUnion(int N) {
super(N);
sz = new int[N];
for(int i = 0; i < N; i++){
sz[i] = 1;
}
}

@Override
public void union(int p, int q) {
int i = find(p);
int j = find(q);

