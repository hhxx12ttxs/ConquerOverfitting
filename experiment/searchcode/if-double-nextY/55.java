private boolean hasNext;
private double nextX;
private double nextY;
private double nextZ;

public VectorIterator(Vector min, Vector max) {
Vector answer = new Vector(nextX, nextY, nextZ);
if (++nextX <= max.getX()) {
return answer;

