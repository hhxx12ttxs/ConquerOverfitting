//Sieve the Totient Function (phi(n))
final int N = 10000000;
double minRatio = 10000.0; //high enough
int minN = 0;
int[] phi = new int[N + 1];
for(int j = N / i ; j >= 2 ; --j )
phi[i * j] -= phi[j];

if(minRatio > (i/(double)phi[i]) &amp;&amp; xMath.isPermutation(i, phi[i]))

