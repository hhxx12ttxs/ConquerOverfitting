points = new ArrayList<PointF>();
}

@Override
public void stroke(Canvas c, float x, float y)
{

if (prevY < y)
{
double angle = Math.atan2(x - prevX, y - prevY);

if (angle < Math.PI / 3.0 &amp;&amp; angle > -Math.PI / 3.0)

