return CHUNK_SIZE_Y;
}

@Override
public AnvilChunkSection getSection(final int index) {
if (index < 0 || index >= getSectionCount()) {
BlockColumn[] res = new BlockColumn[getColumnCount()];
for (int i = 0; i < res.length; i++) {
res[i] = getColumn(i);

