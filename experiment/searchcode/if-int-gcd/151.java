package gcd;

/**
 * Breaking the following properties of gcd:
 * 		3. x mod gcd == 0
 * 		4. y mod gcd == 0
 * 		6. Multiplicative
 * 
 * @author Anna-Elisabeth Schnell, Arne Van der Stappen, Stefanie Verhulst
 *
 */
public class Gcd_Breaking_3_4_6 implements GcdInterface{
	
	/**
	 * Computing the gcd of the two given numbers while breaking the properties 3, 4 and 6.
	 */
	@Override
	public int gcdIterative(int x, int y){
		int k = 1;
		int gcd = 1;
		while ((k <= x) && (k <= y)) {
		    if (((x - 1) % k == 0) && ((y - 1) % k == 0)) {
		        gcd = k;
		    }
		    k++;
		}
		return gcd;
	}
	
}

