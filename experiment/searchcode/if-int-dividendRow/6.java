* @return the row that the variable is basic in.  null if the column is not basic
*/
protected Integer getBasicRow(final int col) {
Integer row = null;
for (int i = 0; i < getHeight(); i++) {
if (MathUtils.equals(getEntry(i, col), 1.0, epsilon) &amp;&amp; (row == null)) {

