// binary search
int nRow = matrix.length;
int nCol = matrix[0].length;
int size = nRow * nCol;
int mid = (lo + hi) / 2;
if (mid == size) {
return false;
}
int val = getValue(matrix, mid, nCol);

