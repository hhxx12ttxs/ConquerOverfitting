Writable[][] blockColumns = inValue.get();
for (int k = 0; k < blockColumns.length; k++) {
Writable[] blockColumn = blockColumns[k];
if (blockColumn.length > 0) {
int vDegree = ((ShortWritable) blockColumn[0]).get();

