private int[] id;
private int[] sz;
private int count;

public WeightedQuickUnionUF(int N) {
count = N;
id = new int[N];
for (int i = 0; i < N; i++) id[i] = i;
sz = new int[N];

