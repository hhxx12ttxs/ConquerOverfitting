public static Path find(int sourceX, int sourceY, int destX, int destY) {
Path path = new Path();
double curX = sourceX;
while (true) {
curX += deltaX;
curY += deltaY;

if (prevX != (int) curX || prevY != (int) curY) {

