public class BitSet {
long[] bits;

BitSet(int numBits) {
int numLongs = numBits >>> 6;
if ((numBits &amp; 0x3f) != 0) {
numLongs++;
}
bits = new long[numLongs];

