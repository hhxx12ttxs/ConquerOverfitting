this.f2 = f2;
}

public double apply(double x) {
return f1.apply(x) + f2.apply(x);
}

public Function derivative() {
public Function integrand() {
Function result = null;
if (f1 instanceof Integrandable &amp;&amp; f2 instanceof Integrandable) {

