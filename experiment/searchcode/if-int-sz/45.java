public class QuickWeightedUnionUF {
private final int[] id;
private final int[] sz;

public QuickWeightedUnionUF(int N) {
id = new int[N];
return;
int i = root(p);
int j = root(q);
if (sz[i] > sz[j]) {
id[j] = i;
sz[i] += sz[j];

