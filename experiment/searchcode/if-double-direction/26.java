private static final long serialVersionUID = 1L;

// Data members
/** Direction in radians. */
private double direction;
* @param direction direction in radians
*/
public Direction(double direction) {
this.direction = cleanDirection(direction);

