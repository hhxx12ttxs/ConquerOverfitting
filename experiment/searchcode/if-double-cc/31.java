package a3.s100502025;

public class LinearEquation {
	LinearEquation(double aa , double bb , double cc , double dd , double ee , double ff){  //constructor
		a = aa;
		b = bb;
		c = cc;
		d = dd;
		e = ee;
		f = ff;
	}
	
	public static int isSolvable( double aa , double bb , double cc , double dd){  //??????????????1?????2
		 if(aa*dd - bb*cc == 0){ 
			 return 1;
		 }
		 else{
			 return 2;
		 }
		 
	}
	
	public static double getX(double aa , double bb , double cc , double dd , double ee , double ff){  //??????X??
		double X = (ee*dd - bb*ff)/(aa*dd - bb*cc);
		return X;
	}
	
	public static double getY(double aa , double bb , double cc , double dd , double ee , double ff){  //??????Y??
		double Y = (aa*ff - ee*cc)/(aa*dd - bb*cc);
		return Y;
	}
	
	private double a , b , c , d , e , f ;  //??????private
}

