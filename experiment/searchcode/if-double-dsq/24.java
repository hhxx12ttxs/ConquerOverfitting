* Recursively walk the tree to do hackwalk calculation
**/
@Override
public final HG walkSubTree(double dsq, HG hg) {
if (subdivp(dsq, hg)) {
for (int k = 0; k < Cell.NSUB; k++) {
Node r = subp[k];
if (r != null) hg = r.walkSubTree(dsq / 4.0, hg);

