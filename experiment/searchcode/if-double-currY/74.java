Solution s = front.next();
double currX = s.getNormalizedX(minX, maxX);
double currY = s.getNormalizedY(minY, maxY);
if (0 <= currX &amp;&amp; currX <= 1 &amp;&amp; 0 <= currY &amp;&amp; currY <= 1) {
if (currX < lastX || currY > lastY) {

