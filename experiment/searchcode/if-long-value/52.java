uint64(long v) {
super();
_v = v;
}

public static long shl(long a, long b) {
if (b >= 0 &amp;&amp; b < 64) {
return a << b;
}
return 0;
}

public static long shr(long a, long b) {
if (b >= 0 &amp;&amp; b < 64) {

