public void rotate(int[][] matrix) {
int len = matrix.length;
if (len == 0)
return;
// 1,2  to 1,3
// 3,4     2,4
// do transpose
for (int i = 0; i < len; i++) {

