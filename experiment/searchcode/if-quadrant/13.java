grid.add( new Quadrant(this, quadCounter++, dx*col, dy*row, dx, dy) );
}
}
}

public int size()
{
return size;
}

public Quadrant get( int i )
{
if( size > 0 &amp;&amp; i >= 0 &amp;&amp; i < size )
public synchronized void updateObjectGridQuadrant( SpaceObject obj )
{
if( obj != null )

