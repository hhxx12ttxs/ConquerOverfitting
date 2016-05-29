* @return The Quadrant this point is part of.
*/
public static Quadrant getQuadrant(int x, int y) {
return getQuadrant(x > 0,  y > 0);
* Get the Quadrant by its position relative to the coordinate axes.
* @param isRight If a quadrant right of the y-axis is to be returned. (positive values of x)

