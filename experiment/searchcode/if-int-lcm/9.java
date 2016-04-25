package math.rational;

/**
 * User: Marcin Matuszak
 * Date: 11/26/12
 */
public class RationalNumber {

    public final static RationalNumber ZERO = new RationalNumber(0, 1);

    private int num;
    private int den;


    public RationalNumber(int numerator, int denominator) {

        int g = gcd(numerator, denominator);
        num = numerator / g;
        den = denominator / g;


        if (den < 0) {
            den = -den;
            num = -num;
        }
    }


    public double toDouble() {
        return (double) num / den;
    }

    public String toString() {
        if (den == 1) return num + "";
        else return num + "/" + den;
    }


    public int compareTo(RationalNumber b) {
        RationalNumber a = this;
        int lhs = a.num * b.den;
        int rhs = a.den * b.num;
        if (lhs < rhs) return -1;
        if (lhs > rhs) return +1;
        return 0;
    }


    public boolean equals(Object y) {
        if (y == null) return false;
        if (y.getClass() != this.getClass()) return false;
        RationalNumber b = (RationalNumber) y;
        return compareTo(b) == 0;
    }

    public int hashCode() {
        return this.toString().hashCode();
    }


    private static int gcd(int m, int n) {
        if (m < 0) m = -m;
        if (n < 0) n = -n;
        if (0 == n) return m;
        else return gcd(n, m % n);
    }


    private static int lcm(int m, int n) {
        if (m < 0) m = -m;
        if (n < 0) n = -n;
        return m * (n / gcd(m, n));
    }


    public RationalNumber times(RationalNumber b) {
        RationalNumber a = this;


        RationalNumber c = new RationalNumber(a.num, b.den);
        RationalNumber d = new RationalNumber(b.num, a.den);
        return new RationalNumber(c.num * d.num, c.den * d.den);
    }


    public RationalNumber plus(RationalNumber b) {
        RationalNumber a = this;


        if (a.compareTo(ZERO) == 0) return b;
        if (b.compareTo(ZERO) == 0) return a;


        int f = gcd(a.num, b.num);
        int g = gcd(a.den, b.den);


        RationalNumber s = new RationalNumber((a.num / f) * (b.den / g) + (b.num / f) * (a.den / g),
                lcm(a.den, b.den));


        s.num *= f;
        return s;
    }

    public RationalNumber negate() {
        return new RationalNumber(-num, den);
    }


    public RationalNumber minus(RationalNumber b) {
        RationalNumber a = this;
        return a.plus(b.negate());
    }


    public RationalNumber divides(RationalNumber b) {
        RationalNumber a = this;
        return a.times(b.reciprocal());
    }


    public RationalNumber reciprocal() {
        return new RationalNumber(den, num);
    }

    public boolean isZero() {
        return compareTo(ZERO) == 0;
    }

    public boolean isPositive() {
        return compareTo(ZERO) > 0;
    }

    public boolean isNegative() {
        return compareTo(ZERO) < 0;
    }


}

