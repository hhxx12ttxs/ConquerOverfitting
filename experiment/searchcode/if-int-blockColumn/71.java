throw new AssertionError();
}
int blockColumn = firstBlockColumn + m;
DataSpace memSpace = new DataSpace(Math.min(blockColumn - firstBlockRow + 1, kernelBlock.rows()));
@Override
public int[] next() {
if (!hasNext()) {

