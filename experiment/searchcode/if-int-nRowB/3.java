public Mat() {
}

public Mat(int rows, int cols) {
if (rows > 0 &amp;&amp; cols > 0) {
data = new double[rows][cols];
public Mat subMat(int startRow, int endRow, int startColumn, int endColumn) {
if (startRow < 0 || startRow > endRow || endRow > data.length ||

