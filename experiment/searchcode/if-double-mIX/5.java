* @see fr.unistra.pelican.algorithms.experimental.perret.CC.Ordering.VectorialOrdering#compare(double[], double[])
*/
@Override
public int compare(double[] o1, double[] o2) {
if (bandMix == null) {
for (int i = 0; i < o1.length; i++) {
int c = Double.compare(o1[i], o2[i]);
if (c != 0) {
return c;

