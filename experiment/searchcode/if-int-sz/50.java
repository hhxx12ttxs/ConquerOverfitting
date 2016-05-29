private int[] id;
private int[] sz;

public  QuickWeightedUnionUF(int N){
id = new int[N];
sz = new int[N];
for(int i=0; i < N; i++){
id[i] = i;
sz[i] = 1;
}
}

private int root(int i){
while(i != id[i]) i = id[i];

