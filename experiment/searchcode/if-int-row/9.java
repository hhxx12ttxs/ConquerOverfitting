public int NQueensNum(int[] rowPos, int row) {
if (row >= rowPos.length) {
return 1;
}
int sum = 0;
for (int i = 0; i < rowPos.length; i++) {
rowPos[row] = i;
if (valid(rowPos, row)) {

