public Point(int x, int y)
{
this.x = x;
this.y = y;
}

public boolean adjacent(Point p2)
{
if(this.x == p2.x)
{
if(Math.abs(this.y - p2.y) == 1)
{
return true;

