* @return
*/
public boolean intersect(Line line,
double invX,
int[] sign) {
double tmin = (box.bounds[sign[0]].x - line.position.x) * invX;
double tmax = (box.bounds[1 - sign[0]].x - line.position.x) * invX;

