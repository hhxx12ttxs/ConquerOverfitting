for (long numerator = fraction.numerator; numerator < denominator; numerator++) {
fraction = new Fraction(numerator, denominator);
if (fraction.compareTo(maxFraction) >= 0) {
break;
}
if (fraction.compareTo(fractionResult) > 0) {
fractionResult = fraction;

