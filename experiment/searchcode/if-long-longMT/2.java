public void setSeed(int seed) {
long longMT = seed;
mt[0] = ((int)(longMT));
for (mti = 1 ; (mti) < (N) ; ++(mti)) {
longMT = ((1812433253L * (longMT ^ (longMT >> 30))) + (mti)) &amp; 4294967295L;
mt[mti] = ((int)(longMT));

