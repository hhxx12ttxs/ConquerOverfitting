private boolean rowMajor;
int[] pos  = {-1,-1};

public StringGridIterator(StringGrid grid, boolean RowMajor)
int [] newPos = {pos[0],pos[1]};
if (rowMajor)
{
newPos[1] = (newPos[1] + 1) % stringGrid.getColumnCount();

