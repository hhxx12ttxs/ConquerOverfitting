return new Fraction(this.numerator * fraction2.denominator, this.denominator * fraction2.numerator);
}

private boolean simplify() {
int gcd = 1;
if (denominator % numerator == 0) {
gcd = gcd(numerator, denominator);
}
if (denominator < 0) {
gcd = -gcd;
}
if (gcd != 1) {
numerator /= gcd;

