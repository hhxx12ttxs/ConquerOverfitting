java.lang.Integer row = null;
for (int i = 0 ; i < (getHeight()) ; i++) {
if ((org.apache.commons.math.util.MathUtils.equals(getEntry(i, col), 1.0, epsilon)) &amp;&amp; (row == null)) {
for (int i = 0 ; i < (getNumArtificialVariables()) ; i++) {
int col = i + (getArtificialVariableOffset());
if ((getBasicRow(col)) == null) {

