private double gtol; // tolerance for curvature condition. gtol >= 0
private double xtol; // relative tolerance for an acceptable step. xtol >= 0
* @param xtol Relative tolerance for acceptable step. xtol >= 0. Larger value for loose tolerance.  Try 1e-4.
*/
public LineSearchMore94(double ftol, double gtol, double xtol ) {
if( stpmax < stpmin )

