* @throws NotStrictlyPositiveException if row or column dimension is not
* positive.
* @see #createBlocksLayout(int, int)
final int heightExcess = iHeight + rowsShift - BLOCK_SIZE;
final int widthExcess = jWidth + columnsShift - BLOCK_SIZE;
if (heightExcess > 0) {

