* Iterative-vs-Recursive-Approaches&quot;
************************************************/
if (n == 0) return 0;
if (n == 1) return 1;

long prevPrev = 0;
long prev = 1;
long result = 0;

for (long i = 2; i <= n; i++)

