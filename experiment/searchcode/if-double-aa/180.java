package a3.s100502014;

public class LinearEquation {
	//constructor
	LinearEquation(double aa, double bb, double cc, double dd, double ee,double ff) {
		a = aa;
		b = bb;
		c = cc;
		d = dd;
		e = ee;
		f = ff;
	}
	double getA() {
		return a;
	}
	double getB() {
		return b;
	}
	double getC() {
		return c;
	}
	double getD() {
		return d;
	}
	double getE() {
		return e;
	}
	double getF() {
		return f;
	}
	
	//test whether it is solvable
	int isSolvable() {
		//solvable solution
		if(a*d-b*c!=0)
			return 1;
		
		//multiple solutions
		else if(e*d-b*f==0 && a*f-e*c==0)
			return 0;
		
		//no solution
		else
			return -1;
	}
	double getX() {
		return (e*d-b*f)/(a*d-b*c);
	}
	double getY() {
		return (a*f-e*c)/(a*d-b*c);
	}
	private double a;
	private double b;
	private double c;
	private double d;
	private double e;
	private double f;
}

