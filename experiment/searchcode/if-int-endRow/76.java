public static final int THREAD_COUNT = 4;
public static final int MATRIX_SIZE = 875;

private float[][] a,b,c;
private int startRow, endRow;
int startRow = i * rowsPerThread;
int endRow = startRow + rowsPerThread - 1;
if (endRow >= matrixSize)
endRow = matrixSize - 1;

