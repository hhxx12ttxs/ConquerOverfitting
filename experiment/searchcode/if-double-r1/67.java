* Compare 2 Rectangle2D.Double objects
* @param r1 rectangle to compare
* @param r2 another rectangle to compare
public int compare(Rectangle2D.Double r1, Rectangle2D.Double r2) {
if (r1.getX() != r2.getX()) {
if (r1.getX() > r2.getX()) return 1;

