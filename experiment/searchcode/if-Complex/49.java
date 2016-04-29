/*
	Java Experiment 5 - MyComplex
	Author: Bird Liu (Liu Xin)

	This is an Open-source Project under GNU GPLv2
*/

public class MyComplex {
	private double x;
	private double y;
	MyComplex() {}
	MyComplex(double x_input, double y_input) { x = x_input; y = y_input; }
	static MyComplex add(MyComplex a, MyComplex b) {
		MyComplex r = new MyComplex();
		r.x = a.x + b.x;
		r.y = a.y + b.y;
		return r;
	}
	static MyComplex sub(MyComplex a, MyComplex b) {
		MyComplex r = new MyComplex();
		r.x = a.x - b.x;
		r.y = a.y - b.y;
		return r;
	}
	static MyComplex mul(MyComplex a, MyComplex b) {
		MyComplex r = new MyComplex();
		r.x = a.x * b.x - a.y * b.y;
		r.y = a.y * b.x + a.x * b.y;
		return r;
	}
	static MyComplex div(MyComplex a, MyComplex b) {
		MyComplex r = new MyComplex();
		r.x = (a.x * b.x + a.y * b.y) / ((b.x * b.x) + (b.y * b.y));
		r.y = (a.y * b.x - a.x * b.y) / ((b.x * b.x) + (b.y * b.y));
		return r;
	}
	public boolean equals(MyComplex t) {
		if ( x == t.x && y == t.y )
			return true;
		else
			return false;
	}
	public String toString() {
		if (y < 0)
			return String.valueOf(x) + String.valueOf(y) + "i";
		else
			return String.valueOf(x) + "+" + String.valueOf(y) + "i";
	}
}

class Test {
	static public void main(String[] args) {
		MyComplex m1 = new MyComplex(3.4, 8.0);
		MyComplex m2 = new MyComplex(3.4, 8.0);
		System.out.println("m1 = " + m1);
		System.out.println("m2 = " + m2);
		System.out.println("m1 == m2 = " + (m1==m2));
		System.out.println("m1.equals(m2) = " + m1.equals(m2));
		MyComplex m3 = new MyComplex(4.4, -8.9);
		MyComplex m4 = MyComplex.add(m1, m3);
		MyComplex m5 = MyComplex.sub(m2, m3);
		MyComplex m6 = MyComplex.mul(m1, m2);
		MyComplex m7 = MyComplex.div(m1, m2);
		System.out.println("m1 + m3 = " + m4);
		System.out.println("m2 - m3 = " + m5);
		System.out.println("m1 * m3 = " + m6);
		System.out.println("m1 / m2 = " + m7);
	}
}
