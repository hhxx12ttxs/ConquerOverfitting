public class QuickUnionWeighted extends QuickUnionUF {
protected int[] sz;

public QuickUnionWeighted(int N) {
super(N);
sz = new int[N];
for(int i = 0; i < N; i++) sz[i] = 1;
}
protected int root(int i) {
while(i != id[i]) {

