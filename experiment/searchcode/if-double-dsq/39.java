* Recursively walk the tree to do hackwalk calculation
**/
@Override
public final HG walkSubTree(double dsq, HG hg) {
if (subdivp(dsq, hg)) {
* @return true if the cell is too close.
**/
public final boolean subdivp(double dsq, HG hg) {
MathVector dr = new MathVector();

