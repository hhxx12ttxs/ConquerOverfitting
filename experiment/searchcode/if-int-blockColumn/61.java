int blockRow = block.getLeft();
int blockColumn = block.getRight();
GaborFilter gaborFilter = new GaborFilter(frequencies.get(block), orientationField[blockRow][blockColumn]
int roiRow = PROCESSING_BLOCK_SIZE*(blockRow - startBlockRow) + i;
int roiColumn = PROCESSING_BLOCK_SIZE*(blockColumn - startBlockColumn) + j;

