class BezierUtils {

static double quadraticBezier(double p0, double p1, double p2, double t) {
static Vector2 quadraticBezier(Vector2 p0, Vector2 p1, Vector2 p2, double t) {

return new Vector2(quadraticBezier(p0.x, p1.x, p2.x, t), quadraticBezier(p0.y, p1.y, p2.y, t));

