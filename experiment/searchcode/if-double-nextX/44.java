private Vector max;
private boolean hasNext;
private double nextX;
private double nextY;
private double nextZ;
Vector answer = new Vector(nextX, nextY, nextZ);
if (++nextX <= max.getX())
{
return answer;
}

nextX = min.getX();

