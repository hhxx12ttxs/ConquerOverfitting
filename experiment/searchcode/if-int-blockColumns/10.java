List<List<Short>> blockColumns = new ArrayList<List<Short>>(blockSize);
for (int k = 0; k < blockSize; k++) {
vDegree = ((ShortWritable) blockEntry[2]).get();

if (blockColumns.get(vIndexInBlock).isEmpty()) {
blockColumns.get(vIndexInBlock).add(vDegree);

