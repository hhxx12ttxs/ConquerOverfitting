class ExponentialFit extends Fit {
protected double getFitAtX(double c0, double c1, double xi) {
if (c0 == 0 &amp;&amp; c1 == 0)
return 0;
return Math.exp(c0 + c1 * xi) + ymin - 1;
}

protected double fx(double xi) {
return xi;

