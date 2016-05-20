package gcd;

/**
 * Breaking the following properties of gcd:
 * 		1. Associative
 * 		3. x mod gcd == 0
 * 		4. y mod gcd == 0
 * 
 * @author Anna-Elisabeth Schnell, Arne Van der Stappen, Stefanie Verhulst
 *
 */
public class Gcd_Breaking_1_3_4 implements GcdInterface{
	
	/**
	 * Computing the gcd of the two given numbers while breaking the properties 1, 3 and 4.
	 */
	@Override
	public int gcdIterative(int x, int y){
		int k = 1;
		int gcd = 1;
		while ((k <= x) && (k <= y)) {
		    if ((x % k == 0) && (y % k == 0)) {
		        gcd = 2*k;
		    }
		    k++;
		}
		return gcd;
	}
}

