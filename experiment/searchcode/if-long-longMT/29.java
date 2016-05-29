public void setSeed(int seed) {
// we use a long masked by 0xffffffffL as a poor man unsigned int
long longMT = seed;
longMT = (1812433253l * (longMT ^ (longMT >> 30)) + mti) &amp; 0xffffffffL;
mt[mti]= (int) longMT;
}
}

/** Reinitialize the generator as if just built with the given int array seed.

