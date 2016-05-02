package gcd;

/**
 * Breaking the following properties of gcd:
 * 		2. Commutative
 * 		5. Biggest
 * 		6. Multiplicative
 * 
 * @author Anna-Elisabeth Schnell, Arne Van der Stappen, Stefanie Verhulst
 *
 */
public class Gcd_Breaking_2_5_6 implements GcdInterface{
	
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
	 * Computing the gcd while breaking the properties 2, 5 and 6.
	 */
	@Override
	public int gcdIterative(int x, int y){
		return gcd(gcd(x,y) - 1, x);
	}
}

