for (int k = 0 ; k < (cols) ; ++k) {
double dk = jacNorm[k];
if (dk == 0) {
dk = 1.0;
dxNorm = java.lang.Math.sqrt(dxNorm);
double previousFP = fp;
fp = dxNorm - delta;
if (((java.lang.Math.abs(fp)) <= (0.1 * delta)) || (((parl == 0) &amp;&amp; (fp <= previousFP)) &amp;&amp; (previousFP < 0))) {

