private double ymin;
private double ymax;
private double xmin;
private double xmax;

public Rectangle(double xmin, double ymin, double xmax, double ymax) {
if (xmin > xmax) throw new MyIllegalArgumentException(&quot;xmin should be less than xmax&quot;);

