double stepa = 0;
double stepb = 0;
if(nfm <= 2 * n) {
if(nfm >= 1 &amp;&amp; nfm <= n) {
stepa = initialTrustRegionRadius;
if(numEval >= 2 &amp;&amp; numEval <= n + 1) {
gradientAtTrustRegionCenter.setEntry(nfmm, (f - fbeg) / stepa);
if(npt < numEval + n) {
final double oneOverStepA = ONE / stepa;

