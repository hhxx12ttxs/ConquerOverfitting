for (int blockRow = 0; blockRow < board.size.blocksXblocks.horizontalCount; blockRow++) {
for (int blockColumn = 0; blockColumn < board.size.blocksXblocks.verticalCount; blockColumn++) {
private static Coordinate getPrevCoord(Board board, int row, int column) {
if (column == 0) {
return new Coordinate(row - 1,

