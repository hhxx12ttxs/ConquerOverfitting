public BlockEntryKey(int blockRow, int blockCol, int src) {
this.row.set(blockRow);
this.col.set(blockCol);
public int compareTo(BlockEntryKey second) {
if (this.row.get() == second.row.get()){
if (this.col.get() == second.col.get()) {

