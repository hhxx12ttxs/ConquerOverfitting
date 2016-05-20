package gcd;

/**
 * Breaking the following properties of gcd:
 * 		5. Biggest
 * 
 * @author Anna-Elisabeth Schnell, Arne Van der Stappen, Stefanie Verhulst
 *
 */
public class Gcd_Breaking_5 implements GcdInterface{
	
	/**
	 * Computing the correct gcd of the two given numbers.
	 */
	private int gcd(int x, int y){
		int k = 1;
		int gcd = 1;
		while ((k <= x) && (k <= y)) {
		    if ((x % k == 0) && (y % k == 0)) {
		        gcd = k;
		    }
		    k++;
		}
		return gcd;
	}
	
	/**
	 * Computing the gcd while breaking the fifth property.
	 */
	@Override
	public int gcdIterative(int x, int y){
		if ( x % 4 == 0 && y % 4 == 0){
			return 2;
		}
		else return gcd(x, y);
	}
}

