public class UnionFind {
private int[] sz;    // sz[i] = number of objects in subtree rooted at i
public UnionFind(int N) {

sz = new int[N];
for (int i = 0; i < N; i++) {

sz[i] = 1;

