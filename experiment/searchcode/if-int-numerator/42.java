package numbers;

public class Rational implements Comparable<Rational> {
    private static Rational zero = new Rational(0, 1);

    private int numerator;
    private int denominator;

    public Rational(int numerator, int denominator) {

        int g = gcd(numerator, denominator);
        this.numerator = numerator   / g;
        this.denominator = denominator / g;

        if (this.denominator < 0) { this.denominator = -this.denominator; this.numerator = -this.numerator; }
    }

    public int getNumerator()   { return numerator; }
    public int getDenominator() { return denominator; }

    public double toDouble() {
        return (double) numerator / denominator;
    }

    public String toString() { 
        if (denominator == 1) return numerator + "";
        else          return numerator + "/" + denominator;
    }

    public int compareTo(Rational b) {
        Rational a = this;
        int lhs = a.numerator * b.denominator;
        int rhs = a.denominator * b.numerator;
        if (lhs < rhs) return -1;
        if (lhs > rhs) return +1;
        return 0;
    }

    public boolean equals(Object y) {
        if (y == null) return false;
        if (y.getClass() != this.getClass()) return false;
        Rational b = (Rational) y;
        return compareTo(b) == 0;
    }

    public int hashCode() {
        return this.toString().hashCode();
    }


    public static Rational mediant(Rational r, Rational s) {
        return new Rational(r.numerator + s.numerator, r.denominator + s.denominator);
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

    public Rational times(Rational b) {
        Rational a = this;

        Rational c = new Rational(a.numerator, b.denominator);
        Rational d = new Rational(b.numerator, a.denominator);
        return new Rational(c.numerator * d.numerator, c.denominator * d.denominator);
    }

    public Rational plus(Rational b) {
        Rational a = this;

        if (a.compareTo(zero) == 0) return b;
        if (b.compareTo(zero) == 0) return a;

        int f = gcd(a.numerator, b.numerator);
        int g = gcd(a.denominator, b.denominator);

        Rational s = new Rational((a.numerator / f) * (b.denominator / g) + (b.numerator / f) * (a.denominator / g),
                                  lcm(a.denominator, b.denominator));

        s.numerator *= f;
        return s;
    }

    // return -a
    public Rational negate() {
        return new Rational(-numerator, denominator);
    }

    // return a - b
    public Rational minus(Rational b) {
        Rational a = this;
        return a.plus(b.negate());
    }


    public Rational reciprocal() { return new Rational(denominator, numerator);  }

    // return a / b
    public Rational divides(Rational b) {
        Rational a = this;
        return a.times(b.reciprocal());
    }

}


