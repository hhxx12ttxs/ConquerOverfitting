+ &quot;pmf: f(x) = e^(-mn) (mn)^(n - 1) / n!\n&quot;
+ &quot;----------&quot;;

private double mu;

public Borel(double mu) throws ParameterException {
throw new ParameterException(&quot;Borel parameter 0 <= mu <= 1&quot;);
} else {
this.mu = mu;
}
}

protected double mass(int x) {
if (x <= 0) {
return 0;

