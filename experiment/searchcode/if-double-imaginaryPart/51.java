public Complex(double realPart, double imaginaryPart){
this.realPart = new Rational(realPart);
this.imaginaryPart = new Rational(imaginaryPart);
Rational comp = (Rational)val;
if(!this.imaginaryPart.isZero()){
return false;
}else{
return this.realPart.equals(comp);

