@Override
public void decodeRow(byte[] source, int sourceOffset, byte[] result, int resultOffset) {
if (sourceOffset == 1) {
System.arraycopy(source, 1, result, resultOffset, colors);

for (int x = 1; x < getResultRowSize(); x++) {

