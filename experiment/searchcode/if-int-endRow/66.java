List<Integer> result = new ArrayList<Integer>();

if (matrix.length == 0) {
return result;
}

int beginRow = 0;
int beginCol = 0;
int endRow = matrix.length - 1;
result.add(matrix[i][endCol]);
}
endCol--;

if (beginRow <= endRow) {
for (int i = endCol; i >= beginCol; i--) {

