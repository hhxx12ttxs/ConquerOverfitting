private static long bitPosition(int index) {
return 1L << (index % BITS_PER_LONG);
}

public BitSet(int bitLength) {
if (bitLength % BITS_PER_LONG == 0) {
if (bits == null || bits.length < newSize) {
long[] newBits = new long[newSize + 1];
if (bits != null) {
System.arraycopy(bits, 0, newBits, 0, bits.length);

