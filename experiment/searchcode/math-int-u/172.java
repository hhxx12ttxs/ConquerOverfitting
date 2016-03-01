/**
 * 
 */
package p136;

/**
 * @author Son-Huy TRAN
 * 
 */
public class P136_SingletonDifference {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int max = 50000000;
		short[] count = new short[(int) (max + 1)];

		for (int u = 1; u <= max; u++) {
			for (int v = 1; u * v <= max; v++) {
				if ((u + v) % 4 == 0 && 3 * v > u && ((3 * v - u) % 4) == 0)
					count[u * v]++;
			}
		}

		int count10 = 0;

		for (int i = 0; i < count.length; i++) {
			if (count[(int) i] == 1) {
				count10++;
			}
		}

		System.out.println(count10);
	}
}

