public class Rectangle extends Shape {
private Logger logger = Logger.getLogger(&quot;cp120a.dia99.HW05&quot;);

private double ymin;
public Rectangle(double xmin, double ymin, double xmax, double ymax) {
if (xmin > xmax) throw new MyIllegalArgumentException(&quot;xmin should be less than xmax&quot;);

