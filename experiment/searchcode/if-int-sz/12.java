public class WeightedQuickUnion extends QuickUnion {
private final int[] sz;

public WeightedQuickUnion(final int n) {
super(n);
sz = new int[n];
Arrays.fill(sz, 1);
}

@Override
public void union(final int p, final int q) {

