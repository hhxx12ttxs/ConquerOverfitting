*            An indication if the solution should be externally or internally tangent (+1/-1) to c3
* @return The circle that is tangent to c1, c2 and c3.
public static Circle solveApollonius(Circle c1, Circle c2, Circle c3, int s1, int s2, int s3) {
double x1 = c1.center[0];
double y1 = c1.center[1];

