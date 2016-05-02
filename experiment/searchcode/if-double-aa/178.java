package a3.s982003034;

public class LinearEquation {
	private double a, b, c, d, e, f;
	
	public LinearEquation (
			double aa, double bb, double cc, double dd, double ee, double ff
			) {
		a = aa;
		b = bb;
		c = cc;
		d = dd;
		e = ee;
		f = ff;
	}
	
	// the six get variable methods
	public double geta () {
		return a;
	}
	
	public double getb () {
		return b;
	}
	
	public double getc () {
		return c;
	}
	
	public double getd () {
		return d;
	}
	
	public double gete () {
		return e;
	}
	
	public double getf () {
		return f;
	}
	
	public boolean isSolvable () {
		if (a*d - b*c != 0) return true;
		else return false;
	}
	
	public double getX () {
		return (e*d - b*f) / (a*d - b*c);
	}
	
	public double getY () {
		return (a*f - e*c) / (a*d - b*c);
	}
	
}

