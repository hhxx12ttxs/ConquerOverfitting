public int getNumberOfRows(Dimension screenDim, Dimension blockDim){
int  rows = screenDim.getHeight() / blockDim.getHeight();
if (hasPartialRowBlock(screenDim, blockDim)) {
private int computeTileHeight(int row, Dimension screenDim, Dimension blockDim)
{
if (isTopRowTile(row)) {

