// we need the pivot, assume that pivot is at (0,0) for now.
Block pivotBlock = blocks[0];

if (pivotBlock != null) {
int[] pivotVector = { pivotBlock.getRow(), pivotBlock.getColumn() };
for (Block block : blocks) {
int blockColumn = block.getColumn();
list.add(blockColumn);

