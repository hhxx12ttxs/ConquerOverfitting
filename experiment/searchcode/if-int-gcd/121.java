package gcd;

/**
 * Second way of breaking the following properties of gcd:
 * 		2. Commutative
 * 		3. x mod gcd == 0
 * 		5. Biggest
 * 		6. Multiplicative
 * 
 * @author Anna-Elisabeth Schnell, Arne Van der Stappen, Stefanie Verhulst
 *
 */
public class Gcd_Breaking_2_3_5_6_b implements GcdInterface{
	
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
	 * Computing the gcd while breaking the properties 2, 3, 5 and 6.
	 */
	@Override
	public int gcdIterative(int x, int y){
		return gcd(x, y-1);
	}
}

