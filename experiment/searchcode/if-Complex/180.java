public class Complex implements Number {
return NumberTower
.multiply(new Complex(Rational.ZERO, Rational.ONE), n);
}
public Complex(int real, int imag) {
real_part = new Rational(real, 1);
public Complex(Number real, Number imag) {
ArgumentChecker.checkAtomType(real, new PropertyChecker() {
}
}, \"real number\", \"Complex constructor\", 1);

