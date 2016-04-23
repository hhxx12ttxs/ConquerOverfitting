public class Binomial {
	/*
         * @author Joaquim Lobo Silva
         * @version 1
         */
	private static int factorial (int number) {
		int result = 1;
		for (int i = number; i > 0; i--) {
			result = result * i;
		}
		return result;
	}
	
	private static int nCr (int n, int k) {
		if (k == 0) {
			k = n;
		}
		int f = n - k
		int result = factorial(n) / (factorial(k) * factorial(f));
		return result;
	}
	
/*
 * Expands a binomial equation such as (x + y)^3
 * @param x The first operand of the binomial
 * @param y The second operand of the binomial
 * @param power The binomial's degree
 * @return A String containing the expanded equivalent (using the Binomial theorem)
 */
	public static String expandBinomial(String x, String y, int power) {
		int k = 0;
		String result = "";
		for (int i = power + 1; i > 0; i--) {
			result = result + "[";
			if (nCr(power, k) != 1) {
				result = result + "(" + nCr(power, k); + ")";
			}
			if (power - k != 0) {
				if ((power - k) == 1) {
					result = result + x;
				}
				else {
					result = result + x + superScript(Integer.toString(power-k));
				}
			}
			if (k != 0) {
				if (k == 1) {
					result = result + y;
				}
				else {
					result = result + y + superScript(Integer.toString(k));
				}
			}
			result = result + "]";
			if (i != 1) {
				result = result + " + ";
			}
			k++;
		}
		return result;
	}
	
	private static String superScript(String originalString) {
		String result = "";
		for (int i = 0; i < originalString.length(); i++) {
			switch (Integer.parseInt(originalString.substring(i))) {
				case 1:
					result = result + "š";
					break;
				case 2:
					result = result + "˛";
					break;
				case 3:
					result = result + "ł";
					break;
				case 4:
					result = result + "?";
					break;
				case 5:
					result = result + "?";
					break;
				case 6:
					result = result + "?";
					break;
				case 7:
					result = result + "?";
					break;
				case 8:
					result = result + "?";
					break;
				case 9:
					result = result + "?";
					break;
				case 0:
					result = result + "?";
					break;
				default:
					break;
			}
		}
		return result;
	}
}
