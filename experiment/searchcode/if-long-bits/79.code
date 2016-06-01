map.put(0L, 0);
return findSize((1L << n) - 1);
}

int findSize(long bits) {
Integer size = map.get(bits);
for (int i = 0; level <= bits; ++i, level <<= 1) {
if ((bits &amp; level) != 0) {
long newBits = bits &amp; masks[i];

