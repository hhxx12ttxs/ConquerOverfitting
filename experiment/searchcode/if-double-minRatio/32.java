long startTime = System.nanoTime();

addPrimes();

long maxn = 0;
double minratio = Double.MAX_VALUE;

for (int i = 0; i < allprimes.length; i++) {
int n = allprimes[i] * allprimes[i2];
if (n > 10000000) break;

if ((double)n/(double)phi < minratio &amp;&amp; permute(phi, n)) {

