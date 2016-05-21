public final class Complex {
public static final Complex ZERO = new Complex(0, 0);
public static final Complex ONE = new Complex(1, 0);
public static final Complex I = new Complex(0, 1);
public Complex(double re, double im) {
this.re = re;
public Complex add(Complex c) {
return new Complex(re + c.re, im + c.im);
}
public Complex subtract(Complex c) {
return new Complex(re - c.re, im - c.im);

