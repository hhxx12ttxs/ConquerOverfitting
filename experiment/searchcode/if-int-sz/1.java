
public class QuickUnion {
private int[] ids,sz;

public QuickUnion(int N ){
ids = new int[N];
public void union(int p, int q){
int i = root(p);
int j = root(q);
if(i == j) return;
if(sz[i] < sz[j]){
ids[i] =j;

