public class WeightedQuickUnionUF extends UF  {
private int[] sz;
public WeightedQuickUnionUF(int N) {
super(N);
sz = new int[N];
for (int i = 0; i < N; i++)

