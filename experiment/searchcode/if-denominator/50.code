private long denominator;

public Number(long numerator, long denominator) {
this.numerator = numerator;
for (long prime : primeCollection) {
if (numerator < prime || denominator < prime)
break;
reduce(prime);

