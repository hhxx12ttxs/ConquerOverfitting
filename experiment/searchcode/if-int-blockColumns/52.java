private final int blockRows;
/** Number of block columns of the matrix. */
private final int blockColumns;
final int iHeight = blockHeight(iBlock);
for (int jBlock = 0; jBlock < blockColumns; ++jBlock, ++index) {
if (blockData[index].length != iHeight * blockWidth(jBlock)) {

