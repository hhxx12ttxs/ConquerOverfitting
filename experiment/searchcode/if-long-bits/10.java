throw new IllegalArgumentException(&quot;n must be positive&quot;);

if ((n &amp; -n) == n)  // i.e., n is a power of 2
return nextLong() &amp; (n - 1); // only take the bottom bits

long bits, val;
do {
bits = nextLong() &amp; 0x7FFFFFFFFFFFFFFFL; // make nextLong non-negative

