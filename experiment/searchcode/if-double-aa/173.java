package a3.s975002502;

public class LinearEquation {
	// declare the attributes of this class
	private static double a;
	private static double b;
	private static double c;
	private static double d;
	private static double e;
	private static double f;
	
	// constructor with initialization
	LinearEquation(double aa, double bb, double cc,double dd, double ee, double ff){
		a = aa;
		b = bb;
		c = cc;
		d = dd;
		e = ee;
		f = ff;
	}
	
	// method getA() to get the value of a
	double getA() {
	    return a;
	}

	// method getB() to get the value of b
	double getB() {
	    return b;
	}

	// method getC() to get the value of c
	double getC() {
		return c;
	}
	
	// method getD() to get the value of d
	double getD() {
	    return d;
	}

	// method getE() to get the value of e
	double getE() {
	    return e;
	}

	// method getF() to get the value of f
	double getF() {
		return f;
	}

	// method isSolvable() to check whether the linear equation is solvable or not
	boolean isSolvable(){
		if(((a*d)-(b*c))==0 ){
			return false;
		}
		else{
			return true;
		}
	}
	
	// method getX() to get the solution of X
	double getX(){
		return ((e*d)-(b*f))/((a*d)-(b*c));
	}
	
	// method getX() to get the solution of Y
	double getY(){
		return ((a*f)-(e*c))/((a*d)-(b*c));
	}
}
