list.add(matrix[i][endCol]);

if (endRow != startRow)
for (int i = endCol - 1; i >= startCol; i--)
list.add(matrix[endRow][i]);

if (endCol != startCol)
for (int i = endRow - 1; i > startRow; i--)

