package basicas;

/**
 * @author Luis Trevińo
 * 
 * Clase abstracta con dos metodos para compara doubles con n decimales de precision.
 */

import java.lang.Double;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public final class CompararDouble {

	
	public static boolean iguales(double a, double b, int precision){
		BigDecimal n1 = new BigDecimal(Double.toString(a), MathContext.UNLIMITED);
		n1 = n1.setScale(precision, RoundingMode.HALF_UP);
		BigDecimal n2 = new BigDecimal(Double.toString(b), MathContext.UNLIMITED);
		n2 = n2.setScale(precision, RoundingMode.HALF_UP);
		if (n1.doubleValue() != n2.doubleValue())
			return false;
		return true;
	}
	
	public static int comparar(double a, double b, int precision){
		BigDecimal n1 = new BigDecimal(Double.toString(a), MathContext.UNLIMITED);
		n1 = n1.setScale(precision, RoundingMode.HALF_UP);
		BigDecimal n2 = new BigDecimal(Double.toString(b), MathContext.UNLIMITED);
		n2 = n2.setScale(precision, RoundingMode.HALF_UP);
		if (n1.doubleValue() < n2.doubleValue()) 		return -1;
		else if (n1.doubleValue() > n2.doubleValue())	return 1;
		else 											return 0;
	}

}

