PrecisionRectangle correction = new PrecisionRectangle();
makeRelative(container.getContentPane(), correction);

if (gridX > 0 &amp;&amp; (snapLocations2 &amp; EAST) != 0) {
snapLocations2 &amp;= ~EAST;
}

if ((snapLocations2 &amp; (WEST | HORIZONTAL)) != 0 &amp;&amp; gridX > 0) {
double leftCorrection = Math.IEEEremainder(rect2.preciseX - origin.x*dpuX,

