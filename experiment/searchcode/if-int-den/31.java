private final int den;

public Rational(int numerator, int denominator) {
num = numerator;
den = denominator;
return new Rational(num/gcd, den/gcd);
}

public static int gcd(int num, int den) {
if (den == 0) return num;
return gcd(den, num % den);

