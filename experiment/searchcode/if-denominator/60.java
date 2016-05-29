long middleDenominator = 2L * lowerDenominator * upperDenominator;
if (middleDenominator <= LIMIT_DENOMINATOR) {
int mediantNumerator = lowerNumerator + upperNumerator;
stack.push(new Rational(mediantNumerator, mediantDenominator));
if (LIMIT_FRACTION * middleNumerator < middleDenominator) {

