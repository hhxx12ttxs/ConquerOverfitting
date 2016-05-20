package a3.sB10007039;

public class LinearEquation {
	private double a,b,c,d,e,f;
	private double X,Y;
	private boolean error = false;
	
	public LinearEquation(double aa,double ab,double ac, double ad,double ae,double af) {
		this.a = aa;
		this.b = ab;
		this.c = ac;
		this.d = ad;
		this.e = ae;
		this.f = af;
		if ((this.a*this.d-this.b*this.c) == 0) {
			this.error = true;
		} else {
			this.X = (this.e*this.d-this.b*this.f)/(this.a*this.d-this.b*this.c);
			this.Y = (this.a*this.f-this.e*this.c)/(this.a*this.d-this.b*this.c);
		}
	}
	
	public double getA() {
		return this.a;
	}
	public double getB() {
		return this.b;
	}
	public double getC() {
		return this.c;
	}
	public double getD() {
		return this.d;
	}
	public double getE() {
		return this.e;
	}
	public double getF() {
		return this.f;
	}
	public double getX() {
		return this.X;
	}
	public double getY() {
		return this.Y;
	}
	public boolean isSolvable() {
		return this.error;
	}
}
