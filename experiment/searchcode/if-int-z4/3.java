/** Number of bits in the pool. */
private static final int K = 19937;

/** First parameter of the algorithm. */
final int z3 = z1      ^ z2;
int z4 = z0 ^ (z1 ^ (z1 << 9)) ^ (z2 ^ (z2 << 21)) ^ (z3 ^ (z3 >>> 21));

