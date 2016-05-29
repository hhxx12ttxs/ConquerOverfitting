/** Number of bits in the pool. */
private static final int K = 44497;

/** First parameter of the algorithm. */
final int z2Prime  = ((z2 << 9) ^ (z2 >>> 23)) &amp; 0xfbffffff;
final int z2Second = ((z2 &amp; 0x00020000) != 0) ? (z2Prime ^ 0xb729fcec) : z2Prime;
int z4             = z0 ^ (z1 ^ (z1 >>> 20)) ^ z2Second ^ z3;

