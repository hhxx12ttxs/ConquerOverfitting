package rainbow.types;

import rainbow.ArcError;

public class Rational extends ArcNumber {
  private long numerator;
  private long denominator;
  public static final Rational ZERO = make(0);
  public static final Rational ONE = make(1);
  public static final Rational TEN = make(10);

  public Rational() {
  }

  public Rational(long numerator) {
    this.numerator = numerator;
    this.denominator = 1L;
  }

  public Rational(long numerator, long denominator) {
    if (denominator == 0 && numerator != 0) {
      throw new ArcError("/: division by zero");
    }
    long gcd = gcd(numerator, denominator);
    this.numerator = numerator / gcd;
    this.denominator = denominator / gcd;
  }

  public static Rational parse(String rep) {
    String[] parts = rep.split("/");
    return make(Long.parseLong(parts[0]), Long.parseLong(parts[1]));
  }

  public static Rational parseHex(String rep) {
    rep = rep.substring(2);
    return new Rational(Long.parseLong(rep, 16));
  }

  public static Rational make(long result) {
    return new Rational(result);
  }

  public static Rational make(long a, long b) {
    return new Rational(a, b);
  }

  public String toString() {
    return numerator + (denominator == 1 ? "" : "/" + denominator);
  }

  public boolean isInteger() {
    return denominator == 1;
  }

  public double toDouble() {
    return (double) numerator / (double) denominator;
  }

  public long toInt() {
    if (numerator == 0) {
      return 0;
    } else {
      return numerator / denominator;
    }
  }

  private long gcd(long a, long b) {
    if (b == 0) {
      return a;
    }
    return gcd(b, a % b);
  }

  public ArcObject mod(ArcNumber other) {
    if (!isInteger() || !other.isInteger()) {
      throw new ArcError("modulo: expects integer, got (" + this + " " + other + ")");
    }
    long divisor = other.toInt();
    long result = numerator % divisor;
    if (result < 0) {
      result += divisor;
    }
    return Rational.make(result);
  }

  public Rational times(Rational other) {
    return new Rational(numerator * other.numerator, denominator * other.denominator);
  }

  public Rational plus(Rational other) {
    long num = (this.numerator * other.denominator) + (other.numerator * this.denominator);
    long div = this.denominator * other.denominator;
    return make(num, div);
  }

  public Rational negate() {
    return make(-numerator, denominator);
  }

  public Rational invert() {
    return new Rational(denominator, numerator);
  }

  public Object unwrap() {
    if (isInteger()) {
      return toInt();
    } else {
      return toDouble();
    }
  }

  public int hashCode() {
    return (int) ((37 * numerator) + denominator);
  }

  public boolean equals(Object object) {
    return object instanceof Rational && sameValue((Rational) object);
  }

  private boolean sameValue(Rational other) {
    return this.numerator == other.numerator && this.denominator == other.denominator;
  }

  public static Rational cast(ArcObject argument, Object caller) {
    try {
      return (Rational) argument;
    } catch (ClassCastException e) {
      throw new ArcError("Wrong argument type: " + caller + " expected a Rational, got " + argument);
    }
  }

  public ArcObject round() {
    if (denominator == 1L) {
      return this;
    } else if (denominator == 2L) {
      long r = numerator / denominator;
      return Rational.make(r + ((r % 2 == 1) ? 1 : 0));
    } else {
      return Rational.make(Math.round(((double)numerator) / ((double)denominator)));
    }
  }

  public ArcObject roundJava() {
    if (denominator == 1L) {
      return this;
    } else {
      return Rational.make(Math.round(((double)numerator) / ((double)denominator)));
    }
  }

  public ArcObject stringify(ArcNumber base) {
    String num = Long.toString(numerator, (int) base.toInt());
    if (isInteger()) {
      return ArcString.make(num);
    } else {
      String den = Long.toString(denominator, (int) base.toInt());
      return ArcString.make(num + "/" + den);
    }
  }

  public ArcObject sqrt() {
    double d = Math.sqrt((double) numerator / (double) denominator);
    if ((long)d == d) {
      return make((long) d);
    } else {
      return Real.make(d);
    }
  }

  public ArcObject multiply(ArcObject other) {
    if (other instanceof Complex) {
      return ((Complex)other).multiply(this);
    } else if (other instanceof Real) {
      return ((Real)other).multiply(this);
    } else if (other instanceof Rational) {
      return this.times((Rational) other);
    } else {
      throw new ArcError("*: expects a number, got " + other.type() + " " + other);
    }
  }

  public ArcObject add(ArcObject other) {
    if (other instanceof Complex) {
      return ((Complex)other).plus(this);
    } else if (other instanceof Real) {
      return ((Real)other).plus(this);
    } else if (other instanceof Rational) {
      return this.plus((Rational) other);
    } else {
      throw new ArcError("+: expects a number, got " + other.type() + " " + other);
    }
  }
}

