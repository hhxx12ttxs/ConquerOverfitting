private int[] sz;
private int size;

public QU(int N) {
this.id = new int[N];
this.sz = new int[N];
this.size = N;
for(int i=0; i<N;i++){
id[i]=i;
sz[i]=1;
}

}

private int root(int i) {

