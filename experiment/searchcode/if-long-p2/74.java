primes2.reset();
for (long p2 : primes2) {
if (p2 < p1) {
continue;
} else if (p1 * p2 >= LIMIT) {
break;

