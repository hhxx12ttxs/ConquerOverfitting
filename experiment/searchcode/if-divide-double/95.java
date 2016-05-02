package geometry;

import java.lang.Math;

public class Vector {
	private double x;
	private double y;
	
	public double getX() { return x; }
	public double getY() { return y; }
	
	public Vector(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Vector(double bearing) {
		double adjustedBearing = ((bearing - (Math.PI / 2)) * -1);
		this.x = Math.cos(adjustedBearing);
		this.y = - Math.sin(adjustedBearing);
	}

	public String toString() {
		return String.format("(%.2f, %.2f)", x, y);
	}
	
	public static double magnitudeSquared(Vector v) {
		return dotProduct(v, v);
	}
	public double magnitudeSquared() {
		return magnitude(this);
	}
	
	public static double magnitude(Vector v) {
		return Math.sqrt(magnitudeSquared(v));
	}
	public double magnitude() {
		return magnitude(this);
	}
	
	public static double dotProduct(Vector v1, Vector v2) {
		return v1.x * v2.x + v1.y * v2.y;
	}
	public double dotProduct(Vector v) {
		return dotProduct(this, v);
	}
	
	public static Vector add(Vector v1, Vector v2) {
		return new Vector(v1.x + v2.x, v1.y + v2.y);
	}
	public Vector add(Vector v) {
		return add(this, v);
	}
	
	public static Vector subtract(Vector v1, Vector v2) {
		return new Vector(v1.x - v2.x, v1.y - v2.y);
	}
	public Vector subtract(Vector v) {
		return subtract(this, v);
	}
	
	public static Vector multiply(Vector v1, Vector v2) {
		return new Vector(v1.x * v2.x, v1.y * v2.y);
	}
	public Vector multiply(Vector v) {
		return multiply(this, v);
	}
	
	public static Vector multiply(Vector v, double m) {
		return new Vector(v.x * m, v.y * m);
	}
	public Vector multiply(double m) {
		return multiply(this, m);
	}
	
	public static Vector divide(Vector v1, Vector v2) {
		return new Vector(v1.x / v2.x, v1.y / v2.y);
	}
	public Vector divide(Vector v) {
		return divide(this, v);
	}
	public static Vector divide(Vector v, double d) {
		return new Vector(v.x / d, v.y / d);
	}
	public Vector divide(double d) {
		return divide(this, d);
	}
	
	public static Vector square(Vector v) {
		return multiply(v, v);
	}
	public Vector square() {
		return square(this);
	}
	
	public static double distanceSquared(Vector v1, Vector v2) {
		return magnitudeSquared(subtract(v1, v2));
	}
	public double distanceSquared(Vector v) {
		return distanceSquared(this, v);
	}
	
	public static double distance(Vector v1, Vector v2) {
		return magnitude(subtract(v1, v2));
	}
	public double distance(Vector v) {
		return distance(this, v);
	}
	
	public static double angle(Vector v) {
		return mod((Math.PI / 2) - Math.atan2(-v.y, v.x));
	}
	public double angle() {
		return angle(this);
	}
	
	public static double angleToVector(Vector v1, Vector v2) {
		return mod(v2.angle() - v1.angle());
	}
	public double angleToVector(Vector v) {
		return angleToVector(this, v);
	}
	
	public static double angleBetweenPoints(Vector point, Vector target) {
		return target.subtract(point).angle();
	}
	public double angleBetweenPoints(Vector target) {
		return angleBetweenPoints(this, target);
	}

	private static double mod(double d) {
		double result = d % (Math.PI*2);
		if (result < 0) {
			result += Math.PI*2;
		}
		return result;
	}


	public static Vector intersectX(Vector position, Vector direction, double x) {
		// Cases where it never intersects
		if (direction.x > 0 && position.x > x) return null;
		if (direction.x < 0 && position.x < x) return null;
		double m = (x - position.x) / direction.x;
		return new Vector(x, position.y + m * direction.y);
	}
	public Vector intersectX(Vector direction, double x) {
		return intersectX(this, direction, x);
	}

	public static Vector intersectY(Vector position, Vector direction, double y) {
		// Cases where it never intersects
		if (direction.y > 0 && position.y > y) return null;
		if (direction.y < 0 && position.y < y) return null;
		double m = (y - position.y) / direction.y;
		return new Vector(position.x + m * direction.y, y);
	}
	public Vector intersectY(Vector direction, double y) {
		return intersectY(this, direction, y);
	}
}

