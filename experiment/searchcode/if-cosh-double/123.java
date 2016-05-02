package ch.marcsladek.commons.math;

import java.io.Serializable;
import java.util.Objects;

/**
 * This class implements complex numbers z = (a+bi). Methods with "_inp" in its name can
 * be used for faster calculations due to not creating a new object (inplace).
 * 
 */

public class Complex implements Serializable {

  private static final long serialVersionUID = 201301132059L;

  private double a;
  private double b;

  private static final Complex C_ONE_REAL = new Complex(1, 0);

  public Complex() {
    this(0, 0, false);
  }

  public Complex(double a) {
    this(a, 0, false);
  }

  public Complex(double a, double b) {
    this(a, b, false);
  }

  public Complex(Complex c) {
    this(c.getReal(), c.getImag(), false);
  }

  public Complex(double r, double phi, boolean isPolar) {
    if (isPolar) {
      a = r * Math.cos(phi);
      b = r * Math.sin(phi);
    } else {
      a = r;
      b = phi;
    }
  }

  public double getReal() {
    return a;
  }

  public void setReal(double a) {
    this.a = a;
  }

  public double getImag() {
    return b;
  }

  public void setImag(double b) {
    this.b = b;
  }

  public double getRad() {
    return abs();
  }

  public double getPhi() {
    return getPhi(Double.MAX_VALUE);
  }

  public double getPhi(double scale) {
    if ((Math.round(a * scale) / scale) == 0) {
      if ((Math.round(b * scale) / scale) == 0) {
        return Double.NaN;
      } else if (b > 0) {
        return Math.PI / 2;
      } else {
        return -Math.PI / 2;
      }
    } else if (a < 0) {
      if (b >= 0) {
        return Math.atan(b / a) + Math.PI;
      } else {
        return Math.atan(b / a) - Math.PI;
      }
    } else {
      return Math.atan(b / a);
    }
  }

  public Complex toCartesian() {
    double r = a;
    a = r * Math.cos(b);
    b = r * Math.sin(b);
    return this;
  }

  public Complex toPolar() {
    double phi = getPhi();
    a = getRad();
    b = phi;
    return this;
  }

  /* (a+bi)+r = (a+r)+bi */
  public Complex add(double in) {
    return new Complex(a + in, b);
  }

  /* (a+bi)+(c+di) = (a+c)+(b+d)i */
  public Complex add(Complex in) {
    return new Complex(a + in.a, b + in.b);
  }

  public Complex add_inp(double in) {
    a += in;
    return this;
  }

  public Complex add_inp(Complex in) {
    a += in.a;
    b += in.b;
    return this;
  }

  /* (a+bi)+r = (a+r)+bi */
  public Complex sub(double in) {
    return new Complex(a - in, b);
  }

  /* (a+bi)+(c+di) = (a-c)+(b-d)i */
  public Complex sub(Complex in) {
    return new Complex(a - in.a, b - in.b);
  }

  public Complex sub_inp(double in) {
    a -= in;
    return this;
  }

  public Complex sub_inp(Complex in) {
    a -= in.a;
    b -= in.b;
    return this;
  }

  /* (a+bi)*r = (ar)+(br)i */
  public Complex mult(double in) {
    return new Complex(in * a, in * b);
  }

  /* (a+bi)(c+di) = (ac-bd)+(ad+bc)i */
  public Complex mult(Complex in) {
    return new Complex((in.a * a) - (in.b * b), (in.a * b) + (in.b * a));
  }

  public Complex mult_inp(double in) {
    a *= in;
    b *= in;
    return this;
  }

  public Complex mult_inp(Complex in) {
    double this_a_tmp = a, in_a_tmp = in.a;
    a = (in.a * a) - (in.b * b);
    b = (in_a_tmp * b) + (in.b * this_a_tmp);
    return this;
  }

  /* (a+bi)/(c+di) = (ac+bd)/(c^2+d^2)+(bc-ad)/(c^2+d^2)i */
  public Complex div(Complex in) {
    double denom = (in.a * in.a) + (in.b * in.b);
    if (denom == 0) {
      return new Complex(Double.NaN, Double.NaN);
    } else {
      return new Complex(((in.a * a) + (in.b * b)) / denom, ((b * in.a) - (a * in.b))
          / denom);
    }
  }

  /* c1.div_op(c2) == c2.div(c1) */
  public Complex div_op(Complex in) {
    double denom = (a * a) + (b * b);
    if (denom == 0) {
      return new Complex(Double.NaN, Double.NaN);
    }
    return new Complex(((in.a * a) + (in.b * b)) / denom, ((in.b * a) - (in.a * b))
        / denom);
  }

  public Complex div_inp(Complex in) {
    double denom = (in.a * in.a) + (in.b * in.b), this_a_tmp = a, in_a_tmp = in.a;
    if (denom == 0) {
      a = b = Double.NaN;
    } else {
      a = ((in.a * a) + (in.b * b)) / denom;
      b = ((b * in_a_tmp) - (this_a_tmp * in.b)) / denom;
    }
    return this;
  }

  public Complex div_op_inp(Complex in) {
    double denom = (a * a) + (b * b), this_a_tmp = a, in_a_tmp = in.a;
    if (denom == 0) {
      a = b = Double.NaN;
    } else {
      a = ((in.a * a) + (in.b * b)) / denom;
      b = ((in.b * this_a_tmp) - (in_a_tmp * b)) / denom;
    }
    return this;
  }

  /*
   * (a+bi)^p pow_polar is faster than pow_mult for in > 151 and <-167
   */
  public Complex pow(int in) {
    if (in == 0) {
      return new Complex(1);
    } else if (in == 1) {
      return new Complex(this);
    } else if (in == 2) {
      return this.sqr();
    } else if ((-167 < in) || (in < 152)) {
      return this.pow_mult(in);
    } else {
      return this.pow_polar(in);
    }
  }

  public Complex pow_inp(int in) {
    if (in == 0) {
      a = 1;
      b = 0;
      return this;
    } else if (in == 1) {
      return this;
    } else if (in == 2) {
      return this.sqr_inp();
    } else if ((-167 < in) || (in < 152)) {
      return this.pow_mult_inp(in);
    } else {
      return this.pow_polar_inp(in);
    }
  }

  private Complex pow_mult(int in) {
    Complex ret = new Complex(this);
    for (int i = 0; i < (Math.abs(in) - 1); i++) {
      ret.mult_inp(this);
    }
    if (in < 0) {
      ret.div_op_inp(Complex.C_ONE_REAL);
    }
    return ret;
  }

  private Complex pow_mult_inp(int in) {
    Complex c = new Complex(this);
    for (int i = 0; i < (Math.abs(in) - 1); i++) {
      this.mult_inp(c);
    }
    if (in < 0) {
      this.div_op_inp(Complex.C_ONE_REAL);
    }
    return this;
  }

  private Complex pow_polar(int in) {
    double r = Math.pow(this.getRad(), in), phi_this = this.getPhi();
    return new Complex(r * Math.cos(in * phi_this), r * Math.sin(in * phi_this));
  }

  private Complex pow_polar_inp(int in) {
    double r = Math.pow(this.getRad(), in), phi_this = this.getPhi();
    a = r * Math.cos(in * phi_this);
    b = r * Math.sin(in * phi_this);
    return this;
  }

  public Complex[] root(int in) {
    if (in == 0) {
      return new Complex[] { new Complex(1, 0) };
    } else if (in < 0) {
      return this.pow(new Frac(-1, Math.abs(in)));
    }
    Complex[] ret = new Complex[in];
    double r = Math.pow(this.getRad(), 1d / in), phi, phi_this = this.getPhi();

    for (int i = 0; i <= (in - 1); i++) {
      phi = (phi_this + (i * 2 * Math.PI)) / in;
      ret[i] = new Complex(r, phi, true);
    }
    return ret;
  }

  public Complex root_f(int in) {
    if (in == 0) {
      return new Complex(1, 0);
    } else if (in < 0) {
      return this.pow_f(new Frac(-1, Math.abs(in)));
    }
    double r = Math.pow(this.getRad(), 1d / in), phi = this.getPhi() / in;
    return new Complex(r, phi, true);
  }

  public Complex root_f_inp(int in) {
    if (in == 0) {
      a = 1;
      b = 0;
      return this;
    } else if (in < 0) {
      return this.pow_f_inp(new Frac(-1, Math.abs(in)));
    }
    double r = Math.pow(this.getRad(), 1d / in), phi = this.getPhi() / in;
    a = r;
    b = phi;
    return this.toCartesian();
  }

  public Complex[] pow(Frac in) {
    Complex[] ret = this.root(in.getDen());
    for (Complex c : ret) {
      c.pow_inp(in.getNum());
    }
    return ret;
  }

  public Complex pow_f(Frac in) {
    if (in.getDen() == 1) {
      return this.pow(in.getNum());
    } else if (in.getNum() == 1) {
      return this.root_f(in.getDen());
    } else {
      return this.root_f(in.getDen()).pow_inp(in.getNum());
    }
  }

  public Complex pow_f_inp(Frac in) {
    if (in.getDen() == 1) {
      return this.pow_inp(in.getNum());
    } else if (in.getNum() == 1) {
      return this.root_f_inp(in.getDen());
    } else {
      return this.root_f_inp(in.getDen()).pow_inp(in.getNum());
    }
  }

  public Complex[] pow(double in) {
    return this.pow(new Frac(in));
  }

  public Complex pow_f(Double in) {
    return this.pow_f(new Frac(in));
  }

  public Complex pow_f_inp(Double in) {
    return this.pow_f_inp(new Frac(in));
  }

  // (a+bi)^2
  public Complex sqr() {
    return this.mult(this);
  }

  public Complex sqr_inp() {
    return this.mult_inp(this);
  }

  /* |(a+bi)| = sqrt(a^2+b^2) */
  public double abs() {
    return Math.sqrt((a * a) + (b * b));
  }

  /* |(a+bi)|^2 = a^2+b^2 */
  public double abs_sqr() {
    return (a * a) + (b * b);
  }

  /* (|a|+|b|i) = (sqrt(a^2)+sqrt(b^2)i) */
  public Complex abs_sep() {
    a = Math.abs(a);
    b = Math.abs(b);
    return this;
  }

  /* conj(a+bi) = (a-bi) */
  public Complex conj() {
    return new Complex(a, -b);
  }

  public Complex conj_inp() {
    b *= -1;
    return this;
  }

  public Complex sin() {
    return new Complex(Math.sin(a) * Math.cosh(b), Math.cos(a) + Math.sinh(b));
  }

  public Complex sin_inp() {
    double a_tmp = a;
    a = Math.sin(a) * Math.cosh(b);
    b = Math.cos(a_tmp) + Math.sinh(b);
    return this;
  }

  public Complex cos() {
    return new Complex(Math.cos(a) * Math.cosh(b), -Math.sin(a) + Math.sinh(b));
  }

  public Complex cos_inp() {
    double a_tmp = a;
    a = Math.cos(a) * Math.cosh(b);
    b = Math.sin(a_tmp) + Math.sinh(b);
    return this;
  }

  @Override
  public int hashCode() {
    int prime = 41;
    int result = 1;
    result = (prime * result) + Double.valueOf(a).hashCode();
    result = (prime * result) + Double.valueOf(b).hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof Complex) {
      Complex other = (Complex) obj;
      return Objects.equals(a, other.a) && Objects.equals(b, other.b);
    } else {
      return false;
    }
  }

  /**
   * Return the String representation of this complex number rounded to scale 1000
   */
  @Override
  public String toString() {
    return this.toString(1000);
  }

  /**
   * Return the String representation of this complex number rounded to given scale
   * 
   * @param scale
   *          - rounds number to scale
   * */
  public String toString(int scale) {
    return this.round(scale).toStringEx();
  }

  /** Return the String representation of this complex number exactly */
  public String toStringEx() {
    return new StringBuffer("(").append(a).append(b < 0 ? " - i" : " + i")
        .append(Math.abs(b)).append(")").toString();
  }

  /**
   * Return this complex number rounded to given scale
   * 
   * @param scale
   *          - rounds number to scale
   * */
  public Complex round(int scale) {
    return new Complex(Math.round(this.getReal() * scale) / (1.0 * scale),
        Math.round(this.getImag() * scale) / (1.0 * scale));
  }

}

