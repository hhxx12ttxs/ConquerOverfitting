protected Pair<Double, Double> calcAlgebraicPrediction(Pair<Double, Double> d1, Pair<Double, Double> d2)
{
if (d1 != null &amp;&amp; d2 != null) {
return calcArithmeticPrediction(mean1, dev1, mean2, dev2);
}
else if (d1 == null &amp;&amp; d2 == null) {
Pair<Double, Double> ret = null;

