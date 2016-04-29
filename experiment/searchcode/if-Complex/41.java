package com.ntobler.space;

public class Complex {

	public static final Complex ZERO = new Complex(0, 0);
	
	public final double x;
	public final double y;
	
	public Complex (double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Complex (Complex c) {
		this.x = c.x;
		this.y = c.y;
	}
	
	public static Complex normalFromAngle (double angle) {
		return new Complex(Math.sin(angle), Math.cos(angle));
	}
	
	public Complex plus(final Complex c) {
		return new Complex(x + c.x, y + c.y);
	}
	
	public Complex minus(final Complex c) {
		
		return new Complex(x - c.x, y - c.y);
	}
	
	public Complex scalarMultiply(double a) {
		
		return new Complex(x * a, y * a);
	}
	
	public Complex scalarDivide(double a) {
		
		return new Complex(x / a, y / a);
	}
	
	public Complex normalVector() {
		double abs = this.abs();
		if (abs == 0) {
			return new Complex(0,0);
		}
		else {
			return this.scalarDivide(abs);
		}
	}
	
	public double getAngle() {
		return Math.atan2(this.x, this.y);
	}
	
	public Complex addAngle(double angle) {
		
		Complex norm = normalFromAngle(angle + getAngle());
		
		return norm.scalarMultiply(this.abs()) ;
	}
	
	public Complex add90deg() {
		
		return new Complex(this.y, 0 - this.x);
	}
	
	public Complex sub90deg() {
		
		return new Complex(0 - this.y, this.x);
	}
	
	
	
	public double abs() {
		return  Math.sqrt((x*x) + (y*y));
	}
}

