public double x1, y1;
public BBOwner owner;

public BB(BBOwner owner, double x0, double y0, double x1, double y1) {
public boolean intersects(double xx0, double yy0, double xx1, double yy1) {
if (xx0 >= x1 || yy0 >= y1 || xx1 <= x0 || yy1 <= y0) return false;

