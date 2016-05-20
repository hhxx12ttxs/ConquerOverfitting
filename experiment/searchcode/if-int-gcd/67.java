package gcd;

/**
 * A class representing the correct version of computing the gcd.
 * 
 * Properties:
 * 		1. Associative ( gcd(gcd(x, y), z) = gcd(x, gcd(y, z)) )
 * 		2. Commutative ( gcd(x, y) = gcd(y, x) )
 * 		3. x mod gcd == 0
 * 		4. y mod gcd == 0
 * 		5. Biggest
 * 		6. Multiplicative ( gcd(a*x, a*y) = a * gcd(x, y) )
 *
 * @author Anna-Elisabeth Schnell, Arne Van der Stappen, Stefanie Verhulst
 */
public class GcdCorrect implements GcdInterface{

	/**
	 * Computing the correct gcd of the two given numbers.
	 */
	public int gcdIterative(int x, int y){
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
} 
