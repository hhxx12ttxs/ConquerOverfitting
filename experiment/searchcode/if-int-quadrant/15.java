* Returns which quadrant a point is in
*/
public static int getQuadrant(Point2D p) {
int quadrantNum = 0;
if(p.x() > 0) {
quadrantNum = p.y() > 0 ? 1 : 4;
}
else {

