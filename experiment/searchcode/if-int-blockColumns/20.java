/** Number of block columns of the matrix. */
private final int blockColumns;

/**
* Create a new matrix with the supplied row and column dimensions.
// number of columns in smaller blocks at the right side of the matrix
final int lastColumns = columns - (blockColumns - 1) * BLOCK_SIZE;

