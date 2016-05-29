private long denominator;

public Number(long numerator, long denominator) {
this.numerator = numerator;
this.denominator = denominator;
reduce();
}

public Number(long number) {

