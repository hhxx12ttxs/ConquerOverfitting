private double imaginaryPart;

public Rectangular(double real, double imaginary) {
this.realPart = real;
this.imaginaryPart = imaginary;
return this;
}

@Override
public String toString() {
String number = &quot;&quot; + realPart;
if (imaginaryPart >= 0) {

