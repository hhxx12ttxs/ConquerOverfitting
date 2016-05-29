Quadrant endPointQuadrant = endPoint.getQuadrant();
// TODO 判断是否给力
if (endPointF.x >= startPointF.x &amp;&amp; endPointF.y <= startPointF.y) {
} else if (endPointF.x <= startPointF.x &amp;&amp; endPointF.y <= startPointF.y) {
endPointQuadrant.setQuadrant(Quadrant.TWO);
} else if (endPointF.x <= startPointF.x &amp;&amp; endPointF.y >= startPointF.y) {

