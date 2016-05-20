package gcd;

/**
 * Second way of breaking the following properties of gcd:
 * 		1. Associative
 * 		2. Commutative
 * 
 * @author Anna-Elisabeth Schnell, Arne Van der Stappen, Stefanie Verhulst
 *
 */
public class Gcd_Breaking_1_2_b implements GcdInterface{
	
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
	 * Computing the gcd while breaking the properties 1 and 2.
	 */
	@Override
	public int gcdIterative(int x, int y){
		if (y % 2 == 0){
			return gcd(x, y/2);
		}
		else return gcd(x, y);
	}
}

