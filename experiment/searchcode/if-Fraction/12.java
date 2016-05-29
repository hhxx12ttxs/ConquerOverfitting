public void canCreateFractionObject() {
Fraction fraction = new Fraction();
public void fractionsAreEqualIfReducedToLowestTermsAreEqual() {
Fraction fraction = new Fraction(33, 45);
fraction = fraction.reduceToLowestTerms();

