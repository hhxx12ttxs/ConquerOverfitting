private RealNumber realPart = null;
private ImaginaryNumber imaginaryPart = null;
public Complex(double real){
public ComplexType getType(){
if (this.realPart != null &amp;&amp; this.imaginaryPart != null){
return ComplexType.BOTH;

