* @return the U matrix (or null if decomposed matrix is singular)
*/
public RealMatrix getU() {
if ((cachedU == null) &amp;&amp; !singular) {
final int m = pivot.length;
public RealMatrix getP() {
if ((cachedP == null) &amp;&amp; !singular) {
final int m = pivot.length;

