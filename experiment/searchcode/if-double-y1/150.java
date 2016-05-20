package pudgewars.util;

import pudgewars.interfaces.BBOwner;

public class CollisionBox {

	public double x0, y0;
	public double x1, y1;
	public BBOwner owner;

	public CollisionBox(BBOwner e, double x0, double y0, double x1, double y1) {
		this.x0 = x0;
		this.x1 = x1;
		this.y0 = y0;
		this.y1 = y1;
		owner = e;
	}

	public boolean intersects(Vector2 v) {
		if (x1 <= v.x || x0 >= v.x || y1 <= v.y || y0 >= v.y) return false;
		return true;
	}

	public boolean intersects(CollisionBox b) {
		if (x1 <= b.x0 || x0 >= b.x1 || y1 <= b.y0 || y0 >= b.y1) return false;
		return true;
	}

	public boolean intersects(int xx0, int yy0, int xx1, int yy1) {
		if (x1 <= xx0 || x0 >= xx1 || y1 <= yy0 || y0 >= yy1) return false;
		return true;
	}

	public CollisionBox grow(double x) {
		return new CollisionBox(owner, x0 - x, y0 - x, x1 + x, y1 + x);
	}
}

