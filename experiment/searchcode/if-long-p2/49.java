public class PointPairClosest implements Comparable<PointPairClosest>,
Serializable {
/**
*
*/
private static final long serialVersionUID = 1L;
+ Math.pow(p2.getX() - p1.getX(), 2));
}

public boolean isSamePoint() {
if (this.p1.getX() == this.p2.getX()
&amp;&amp; this.p1.getY() == this.p2.getY()) {

