package com.barsoft.example.lab2;

public class Complex {
	protected double x;
	protected double y;

	public Complex(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public static Complex Sum(Complex a, Complex b) {
		return new Complex(a.x + b.x, a.y + b.y);
	}

	public static Complex Mul(Complex a, Complex b) {
		return new Complex(a.x * b.x - a.y * b.y, a.x * b.y + a.y * b.x);
	}

	public void Sum(Complex b) {
		Complex c = new Complex(this.x + b.x, this.y + b.y);
		this.x = c.x;
		this.y = c.y;
	}

	public void Mul(Complex b) {
		Complex c = new Complex(this.x * b.x - this.y * b.y, this.x * b.y
				+ this.y * b.x);
		this.x = c.x;
		this.y = c.y;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null) {
			if (obj instanceof Complex) {
				Complex cmp = (Complex) obj;
				if (this.x == cmp.x && this.y == cmp.x) {
					return true;
				}
			} else if (obj instanceof Double) {
				Double dbl = (Double) obj;
				if (this.x == dbl && this.y==0){
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return x + "," + y + "i";
	}

}

