((Imaginary) other).imaginaryPart());
return make(re, im);
} else if (other instanceof Real) {
Real re = (Real) realPart().plus(other);
return new FloatingPoint(Math.sqrt(d.doubleValue()));
}

public String toString() {
if (imaginaryPart().isPositive()) {
return realPart().toString() + &quot;+&quot; + imaginaryPart().toString() + &quot;i&quot;;

