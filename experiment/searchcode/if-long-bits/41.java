public static long combineWithValue(long key, long value, int keyBits) {
Preconditions.checkArgument(keyBits >= 0 &amp;&amp; keyBits <= 64, &quot;keyBits must be [0,64]&quot;);

int valueBits = 64 - keyBits;
long valueMask = valueBits == 64 ? 0xFFFFFFFFFFFFFFFFL : (1L << valueBits) - 1;

