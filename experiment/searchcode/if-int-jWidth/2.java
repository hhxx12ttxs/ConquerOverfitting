final int blockColumns = ((columns + (BLOCK_SIZE)) - 1) / (BLOCK_SIZE);
for (int i = 0 ; i < (rawData.length) ; ++i) {
final int length = rawData[i].length;
if (length != columns) {
final double[] block = blocks[((iBlock * (blockColumns)) + jBlock)];
final int available = (mBlock.length) - mIndex;
if (jWidth > available) {

