int diffX = destination.getX() - source.getX();
int diffY = destination.getY()- source.getY();

if (diffX >0  &amp;&amp; diffY>0 )	return Direction.NORTHEAST;
public static int returnDirecitonFromDeltas (int diffX, int diffY){
if (diffX >0  &amp;&amp; diffY>0 )	return Direction.NORTHEAST;
else if (diffX>0 &amp;&amp; diffY <0 )	return Direction.SOUTHEAST;

