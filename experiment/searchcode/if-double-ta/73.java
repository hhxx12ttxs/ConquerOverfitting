package a3.s100502016;

public class LinearEquation {
	private static double a;
	private static double b;
	private static double c;
	private static double d;
	private static double e;
	private static double f;

	LinearEquation(double ta, double tb, double tc, double td, double te, double tf) {
		a = ta;
		b = tb;
		c = tc;
		d = td;
		e = te;
		f = tf;
	}
	public static boolean isSolvable(){
		if(a*d-b*c==0){
			return false;
		}else{
			return true;
		}
		
	}

	public static double getX(){
    	return (e*d-b*f)/(a*d-b*c);
    }
    public static double getY(){
    	return (a*f-e*c)/(a*d-b*c);
    }
}

