public Fraction addFractions(Fraction fraction){
if(fraction.numerator != numerator){
return new Fraction(fraction.denominator * numerator + denominator * fraction.numerator,
return new Fraction(fraction.denominator + denominator, numerator);
}

public Fraction minusFractions(Fraction fraction){
if(fraction.numerator != numerator){

