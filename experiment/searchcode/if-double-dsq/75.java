final double dy = cenLoc.getY() - objLoc.getY();
final double dsq = (dx * dx) + (dy * dy);
//System.err.println (&quot;side: &quot;+side+&quot;, dsq: &quot;+dsq);
if (dsq < eventHorizonSq) {
final double side = qTree.getSquare().getSideLength();

