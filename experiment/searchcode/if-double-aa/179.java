package a3.s100502515;

public class LinearEquation {
	private static double a;
	private static double b;
	private static double c;
	private static double d;
	private static double e;
	private static double f;

	LinearEquation(double aa, double bb, double cc, double dd, double ee,
			double ff) {
		a = aa;
		b = bb;
		c = cc;
		d = dd;
		e = ee;
		f = ff;
	}

	public boolean isSolvable() {
		if (a * d - b * c == 0)
			return true;
		else
			return false;
	}

	public double getX() {
		return (e * d - b * f) / (a * d - b * c);
	}

	public double getY() {
		return (a * f - e * c) / (a * d - b * c);
	}

}

