private final double stopFitness;
/** Stop if x-changes larger stopTolUpX. */
private double stopTolUpX;
private void updateBD(double negccov) {
if (ccov1 + ccovmu + negccov > 0 &amp;&amp;
(iterations % 1. / (ccov1 + ccovmu + negccov) / dimension / 10.) < 1) {

