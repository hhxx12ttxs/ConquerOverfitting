for (int row = nreq - 1; row > 0; --row) {
if (!this.lindep[row]) {
final int start = (row - 1) * (nvars + nvars - row) / 2;
double[] wk = new double[this.nvars];
int pos;
double total;

if (row_data.length > nvars) {

