upperBound = new Vector2(Math.max(p1.x, p2.x) + extend, Math.max(p1.y, p2.y) + extend);
}

public double getPerimeter()
{
double w = upperBound.x - lowerBound.x;
double h = upperBound.y - lowerBound.y;
public void updateAABB(Vector2 p1, Vector2 p2, double extend)
{
if (node != null)
throw new UnsupportedOperationException(&quot;Update on AABBrect while in a AABBTree&quot;);

