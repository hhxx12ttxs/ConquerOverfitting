* @author Tillmann Rendel
*/
public abstract class Coordinate {
public static double wrapAroundFactor(final double x, final double y) {
final double xxyy = x * x + y * y;
double factor = 1.0;
if (xxyy > 10000.0) {

