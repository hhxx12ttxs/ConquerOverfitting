p1 = iter.next();
p2 = iter.next();
}

public double valueAt(double x) {
wind(x);
if (x < p1.x) {
return p1.y;
private double interpolate(double x) {
if (x == p1.x) return p1.y;
if (x == p2.x) return p2.y;
return p1.y + (p2.y - p1.y) * ((x-p1.x) / (p2.x-p1.x));

