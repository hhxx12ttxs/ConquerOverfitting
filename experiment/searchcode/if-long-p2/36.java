public class CosineDistance implements DistanceMeasure {

private static final long serialVersionUID = 8188347590074168067L;
for (int i = 0; i < p1.length; i++) {
// if p1[i]==0 or p2[i]==0 it doesn&#39;t use
if (p1[i] != -1 &amp;&amp; p2[i] != -1) {
sum += (p1[i] * p2[i]);

