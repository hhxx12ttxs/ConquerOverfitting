public class WeightedUF {
private int[] id;
private int[] sz;

public WeightedUF(int N) {
public void union(int p, int q) {
int i = root(p);
int j = root(q);
if( sz[i] < sz[j]) {
id[i] = j;

