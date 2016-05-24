public Complex divide(Complex c) {
if (c.a == 0) return new Complex(-b/c.b, -a/c.b);
if (c.b == 0) return new Complex(a/c.a, b/c.a);
public Complex divide(double v) {
return new Complex(a/v, b/v);
}

public double arg() {
if (a == 0 &amp;&amp; b == 0) return Double.NaN;

