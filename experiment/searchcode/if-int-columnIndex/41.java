public Integer getIntAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
if (fields[columnIndex] != null &amp;&amp; !(fields[columnIndex] instanceof Integer)) {
public String getStringAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
if (fields[columnIndex] != null &amp;&amp; !(fields[columnIndex] instanceof String)) {

