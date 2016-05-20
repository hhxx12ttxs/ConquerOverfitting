package a3.s100502034;
public class LinearEquation {
	private static double a0;//abcdef
	private static double b0;
	private static double c0;
	private static double d0;
	private static double e0;
	private static double f0;
	public LinearEquation(double a , double b, double c, double d, double e ,double f ){
		a0 = a;//?abcdef????
		b0 = b;
		c0 = c;
		d0 = d;
		e0 = e;
		f0 = f;
	}
	public static boolean isSolvable(){//??????
		if(a0*d0-b0*c0 == 0){
			return false;
		}
		else
			return true;
	}
	public static double getX(){
		return (e0*d0 - b0*f0) / (a0*d0 - b0*c0);//X?
	}
	public static double getY(){
		return (a0*f0 - e0*c0) / (a0*d0 - b0*c0);//Y?
	}
}
