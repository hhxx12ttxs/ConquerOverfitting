/** Creates a new random number generator using an int array seed.
* @param seed the initial seed (32 bits integers array), if null
final int z2 = (vM2 >>> 9) ^ (vM3 ^ (vM3 >>> 1));
final int z3 = z1      ^ z2;
int z4 = z0 ^ (z1 ^ (z1 << 9)) ^ (z2 ^ (z2 << 21)) ^ (z3 ^ (z3 >>> 21));

