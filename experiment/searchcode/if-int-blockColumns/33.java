/** Number of block columns of the matrix. */
private final int blockColumns;

/**
* Create a new matrix with the supplied row and column dimensions.
for(int jBlock = 0; jBlock < blockColumns; ++jBlock, ++index) {
if(blockData[index].length != iHeight * blockWidth(jBlock)) {

