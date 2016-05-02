package ch.marcsladek.commons.math;

import java.io.Serializable;

public class Frac implements Serializable {

  private static final long serialVersionUID = 201301101655L;

  private int num;
  private int den;

  public Frac(int num) {
    this(num, 1);
  }

  public Frac(double d) {
    den = (int) Math.pow(10, getDecPlace(d));
    num = (int) (d * den);
    reduce();
  }

  private int getDecPlace(double d) {
    if (d != 0) {
      return Double.toString(d).length() - (int) Math.log10((int) d) - 2;
    } else {
      return 1;
    }
  }

  public Frac(Frac frac) {
    this(frac.num, frac.den);
  }

  public Frac(int num, int den) {
    this.num = num;
    this.den = den;
    reduce();
  }

  public Frac copy() {
    return new Frac(this.num, this.den);
  }

  public int getNum() {
    return num;
  }

  public int getDen() {
    return den;
  }

  public double toDouble() {
    return (double) num / den;
  }

  public int toInt() {
    return (int) toDouble();
  }

  public Frac add(int i) {
    return new Frac(num + (i * den), den).reduce();
  }

  public Frac add(Frac f) {
    return new Frac((num * f.den) + (f.num * den), den * f.den).reduce();
  }

  public Frac add_inp(int i) {
    num += i * den;
    return reduce();
  }

  public Frac add_inp(Frac f) {
    num = (num * f.den) + (f.num * den);
    den *= f.den;
    return reduce();
  }

  public Frac sub(int i) {
    return new Frac(num - (i * den), den).reduce();
  }

  public Frac sub(Frac f) {
    return new Frac((num * f.den) - (f.num * den), den * f.den).reduce();
  }

  public Frac sub_inp(int i) {
    num -= i * den;
    return reduce();
  }

  public Frac sub_inp(Frac f) {
    num = (num * f.den) - (f.num * den);
    den *= f.den;
    return reduce();
  }

  public Frac mul(int i) {
    return new Frac(i * num, den).reduce();
  }

  public Frac mul(Frac f) {
    return new Frac(num * f.num, den * f.den).reduce();
  }

  public Frac mul_inp(int i) {
    num *= i;
    return reduce();
  }

  public Frac mul_inp(Frac f) {
    num *= f.num;
    den *= f.den;
    return reduce();
  }

  public Frac div(int i) {
    return new Frac(1, i).mul_inp(this).reduce();
  }

  public Frac div(Frac f) {
    return new Frac(num * f.den, den * f.num).reduce();
  }

  public Frac div_inp(int i) {
    den *= i;
    return reduce();
  }

  public Frac div_inp(Frac f) {
    num *= f.den;
    den *= f.num;
    return reduce();
  }

  // a mod b = a - toWhole(a/b) * b
  public Frac mod(int i) {
    Frac wholeDividend = new Frac(this).div_inp(i).toWhole();
    Frac ret = this.sub(wholeDividend.mul_inp(i));
    return ret.reduce();
  }

  public Frac mod(Frac f) {
    Frac wholeDividend = new Frac(this).div_inp(f).toWhole();
    Frac ret = this.sub(wholeDividend.mul_inp(f));
    return ret.reduce();
  }

  public Frac mod_inp(int i) {
    Frac wholeDividend = new Frac(this).div_inp(i).toWhole();
    this.sub_inp(wholeDividend.mul_inp(i));
    return reduce();
  }

  public Frac mod_inp(Frac f) {
    Frac wholeDividend = new Frac(this).div_inp(f).toWhole();
    this.sub_inp(wholeDividend.mul_inp(f));
    return reduce();
  }

  public Frac abs() {
    setDenPositive();
    if (num < 0) {
      negate();
    }
    return this;
  }

  public Frac negate() {
    num *= -1;
    return this;
  }

  public Frac invert() {
    int t = num;
    num = den;
    den = t;
    return this;
  }

  public int compareTo(int i) {
    long p = (long) den * i;
    return (num > p) ? 1 : ((num < p) ? -1 : 0);
  }

  public int compareTo(Frac f) {
    long p1 = (long) num * f.den;
    long p2 = (long) den * f.num;
    return (p1 > p2) ? 1 : ((p1 < p2) ? -1 : 0);
  }

  @Override
  public int hashCode() {
    int prime = 37;
    int result = 1;
    result = (prime * result) + num;
    result = (prime * result) + den;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj instanceof Frac) {
      Frac other = (Frac) obj;
      return this.compareTo(other) == 0;
    } else if (obj instanceof Integer) {
      return this.compareTo((Integer) obj) == 0;
    } else if (obj instanceof Double) {
      return (Double) obj == toDouble();
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return num + "/" + den;
  }

  public String toString(int denDecPlaces) {
    setDenPositive();
    int l = Integer.toString(den).length();
    if (denDecPlaces > l) {
      long m = (int) Math.pow(10, denDecPlaces - l);
      return (num * m) + "/" + (den * m);
    } else {
      return toString();
    }
  }

  /*--------------------------------------------------------------------------------------
   * HELPER METHODS
   */

  private Frac reduce() {
    if (num == 0) {
      den = 1;
    } else if (den == 0) {
      throw new ArithmeticException("Denominator of Fraction '" + this + "' is zero.");
    } else {
      int gcd = gcd(num, den);
      num /= gcd;
      den /= gcd;
      setDenPositive();
    }
    return this;
  }

  private void setDenPositive() {
    if (den < 0) {
      num *= -1;
      den *= -1;
    }
  }

  private int gcd(int a, int b) {
    int r;
    while (b != 0) {
      r = a % b;
      a = b;
      b = r;
    }
    return a;
  }

  // num is set to the next whole number smaller or equal than num/den
  private Frac toWhole() {
    num = toInt() - (num < 0 ? 1 : 0);
    den = 1;
    return this;
  }

}

