private static final byte[] minValue = &quot;-9223372036854775808&quot;.getBytes();

public static byte[] toBytes(long i) {
if (i == Long.MIN_VALUE)
return minValue;
static int stringSize(long x) {
long p = 10;
for (int i = 1; i < 19; i++) {
if (x < p)

