public double x1, y1;
public BBOwner owner;

public BB(BBOwner owner, double x0, double y0, double x1, double y1) {
this.x1 = x1;
this.y1 = y1;
}

public boolean intersects(double xx0, double yy0, double xx1, double yy1) {

