* @param rhs
*            skalar
* @return nova instance Vec2D
*/
public Vec2D mul(double rhs) {
return new Vec2D(x * rhs, y * rhs);
*            vektor (x,y)
* @return nova instance Vec2D
*/
public double dot(Vec2D rhs) {
return x * rhs.x + y * rhs.y;

