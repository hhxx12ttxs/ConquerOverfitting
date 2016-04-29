/*
 * Created on Feb 23, 2006
 */
package de.torstennahm.math;


public class Complex {
	private final double r;
	private final double i;
	
	public static final Complex NaN = new Complex(Double.NaN, Double.NaN);
	
	public Complex(double r) {
		this.r = r;
		this.i = 0.0;
	}
	
	public Complex(double r, double i) {
		this.r = r;
		this.i = i;
	}
	
	public Complex add(Complex c) {
		return new Complex(r + c.r, i + c.i);
	}
	
	public Complex sub(Complex c) {
		return new Complex(r - c.r, i - c.i);
	}

	public Complex mul(Complex c) {
		return new Complex(r * c.r - i * c.i, i * c.r + r * c.i);
	}
	
	public Complex mul(double a) {
		return new Complex(r * a, i * a);
	}
	
	public Complex div(Complex c) {
		double d = 1.0 / (c.r * c.r + c.i * c.i);
		return new Complex((r * c.r + i * c.i) * d, (i * c.r - r * c.i) * d);
	}
	
	public Complex div(double a) {
		double inv = 1.0 / a;
		return new Complex(r * inv, i * inv);
	}
	
	public double re() {
		return r;
	}
	
	public double im() {
		return i;
	}
	
	public Complex conjugate() {
		return new Complex(r, -i);
	}
	
	public Complex neg() {
		return new Complex(-r, -i);
	}
	
	public double abs() {
		return Math.sqrt(r*r + i*i);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Complex) {
			Complex c = (Complex) o;
			return c.r == r && c.i == i;
		} else {
			return false;
		}
	}
}
