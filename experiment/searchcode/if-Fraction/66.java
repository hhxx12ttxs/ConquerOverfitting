private Fraction checkTheValueandDivide(Fraction fraction) {
if (fraction.chis % 2 == 0 &amp;&amp; fraction.znam % 2 == 0) {
fraction.chis = fraction.chis / 2;
return new Fraction(fraction.chis, fraction.znam);
}

public Fraction adding(Fraction fraction1) {
if (this.znam == fraction1.znam) {

