for (int jBlock = 0; jBlock < blockColumns; ++jBlock, ++index) {
if (blockData[index].length != iHeight * blockWidth(jBlock)) {
final T[] block = blocks[iBlock * blockColumns + jBlock];
for (int i = 0; i < iHeight; ++i) {
if (outIndex >= outBlock.length) {

