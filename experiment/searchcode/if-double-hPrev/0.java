private final List<Point> points_ = new ArrayList<>();
private Spline[] splines_;

public boolean add(double x, double y){
if (!points_.isEmpty() &amp;&amp; points_.get(points_.size()-1).x>=x)
points_.add(new Point(x,y));
return true;
}

public double evaluate(double x) {
if (!calc()) {

