private int[] id;
//(由触点索引的)各个根节点所对应的分量的大小
private int[] sz;
//分量数量
private int count;

public UF(int N) {
for (int i = 0; i < N; i++) {
id[i] = i;
}
sz = new int[N];
for (int i = 0; i < N; i++) {

