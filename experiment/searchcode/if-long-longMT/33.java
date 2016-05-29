// we use a long masked by 0xffffffffL as a poor man unsigned int
long longMT = seed;
mt[0]= (int) longMT;
for (mti = 1; mti < N; ++mti) {
// See Knuth TAOCP Vol2. 3rd Ed. P.106 for multiplier.
// initializer from the 2002-01-09 C version by Makoto Matsumoto
longMT = (1812433253l * (longMT ^ (longMT >> 30)) + mti) &amp; 0xffffffffL;

