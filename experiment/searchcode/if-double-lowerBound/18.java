public AABBrect(Vector2 p1, Vector2 p2, double extend)
{
lowerBound = new Vector2(Math.min(p1.x, p2.x) - extend, Math.min(p1.y, p2.y) - extend);
upperBound = new Vector2(Math.max(p1.x, p2.x) + extend, Math.max(p1.y, p2.y) + extend);
}

public double getPerimeter()
{
double w = upperBound.x - lowerBound.x;
double h = upperBound.y - lowerBound.y;

