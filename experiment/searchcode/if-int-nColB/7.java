/** {@inheritDoc} */
public RealMatrix getL() {
if ((cachedL == null) &amp;&amp; !singular) {
final int m = pivot.length;
public RealMatrix getP() {
if ((cachedP == null) &amp;&amp; !singular) {
final int m = pivot.length;

