public class WeightedQuickUnionUF {
private int[] id;
private int[] sz;

public WeightedQuickUnionUF(int N) {
id = new int[N];
sz = new int[N];
for (int i = 0; i < id.length; i++) {
id[i] = i;

