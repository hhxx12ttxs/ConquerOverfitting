private final int rows;

private Row[] blockRows;

public Board(int columns, int rows) {
this(columns, rows, newBlockRows(columns, rows));
int notFullRowCnt = 0;
for (int y = bottom; y >= top; y--) {
if (blockRows[y].isNotFull()) {

