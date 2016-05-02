package gcd;

/**
 * Breaking the following properties of gcd:
 * 		2. Commutative
 * 		5. Biggest
 * 
 * @author Anna-Elisabeth Schnell, Arne Van der Stappen, Stefanie Verhulst
 *
 */
public class Gcd_Breaking_2_5 implements GcdInterface{
	
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
	 * Computing the gcd while breaking the properties 2 and 5.
	 */
	@Override
	public int gcdIterative(int x, int y){
		if (x % 2 == 0){
			return gcd(x/2, y);
		}
		return gcd(x, y);
	}
}

