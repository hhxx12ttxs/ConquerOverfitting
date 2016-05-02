package a3.s100502021;

public class LinearEquation {
	private static Double aa; //set private member
	private static Double bb;
	private static Double cc;
	private static Double dd;
	private static Double ee;
	private static Double ff;
	
	LinearEquation(Double a,Double b,Double c,Double d,Double e,Double f){ //set value
		aa = a;
		bb = b;
		cc = c;
		dd = d;
		ee = e;
		ff = f;
	}	
	public static Double getX(){
		return ((ee*dd - bb*ff)/(aa*dd - bb*cc));
	}
	public static Double getY(){
		return ((aa*ff - ee*cc)/(aa*dd - bb*cc));
	}
	public static boolean isSolvable(){
		if(aa*dd-bb*cc==0){
			System.out.print("The equation has no solution.");
			return false;
		}
		else {
			return true;
		}
	}
}

