public void simplifiySign() {
if (denominator < 0) {
numerator   = -numerator;
denominator = -denominator;
public void setDenominator(int denominator) throws ArithmeticException {
if (denominator == 0) {
throw new ArithmeticException();

