class AnvilBlockColumn extends AbstractAnvil implements BlockColumn {
final AnvilChunk chunk;
final int index;

public AnvilBlockColumn(AnvilChunk chunk, int index) {
if (index < 0 || index >= CHUNK_HORIZONTAL_LENGTH) {

