public void solve(int testNumber, InputReader in, OutputWriter out) {
int rowCount = in.readInt();
int columnCount = in.readInt();
if (rowCount > columnCount) {
rowCount = columnCount;
columnCount = temp;
}
int answer;
if (rowCount == 1) {
answer = columnCount;

