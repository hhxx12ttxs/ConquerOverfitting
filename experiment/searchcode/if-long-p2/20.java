public boolean intersects(Partition p2) {
if (p2.lowerBound >= lowerBound &amp;&amp; p2.lowerBound <= upperBound) {
return true;
}
if (p2.upperBound >= lowerBound &amp;&amp; p2.upperBound <= upperBound) {
return true;
}
if (p2.lowerBound <= lowerBound &amp;&amp; p2.upperBound >= upperBound) {

