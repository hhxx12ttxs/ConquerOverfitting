for (int i=0; i < NSUB; i++) {
Node r = subp[i];
if (r != null) {
double mr = r.hackcofm();
* Recursively walk the tree to do hackwalk calculation
**/
final HG walkSubTree(double dsq, HG hg)
{
if (subdivp(dsq, hg)) {

