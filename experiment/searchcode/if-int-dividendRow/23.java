protected java.lang.Integer getBasicRow(final int col) {
java.lang.Integer row = null;
for (int i = 0 ; i < (getHeight()) ; i++) {
if ((org.apache.commons.math.util.MathUtils.equals(getEntry(i, col), 1.0, epsilon)) &amp;&amp; (row == null)) {
for (int i = 0 ; i < (coefficients.length) ; i++) {
int colIndex = columnLabels.indexOf((&quot;x&quot; + i));
if (colIndex < 0) {

