* @author Ibrahima Diarra
*
*/
public class WeightedQuickUnion extends QuickUnion {

private int[] sz;

public WeightedQuickUnion(int N) {
super(N);
sz = new int[N];
for (int i = 0; i < N; i++)
sz[i] = 1;

