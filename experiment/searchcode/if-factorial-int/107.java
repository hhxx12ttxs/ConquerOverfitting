/**
 * 
 */
package p20;

/**
 * @author Son-Huy TRAN
 * 
 */
public class P20_FactorialDigitSum {

	private static String multiply(String number1, int number2) {
		StringBuilder resultBuilder = new StringBuilder();
		long remainder = 0, product;

		for (int i = number1.length() - 1; i >= 0; i--) {
			product = remainder + number2
					* Short.parseShort(number1.substring(i, i + 1));
			resultBuilder.insert(0, product % 10);
			remainder = product / 10;
		}
		
		if (remainder > 0) {
			resultBuilder.insert(0, remainder);
		}

		return resultBuilder.toString();
	}
	
	private static String factorial(int n) {
		String number = "1";

		for (int i = 2; i <= n; i++) {
			number = multiply(number, i);
		}
		
		return number;
	}
	
	private static long sumDigit(String number) {
		long sum = 0;

		for (int i = number.length() - 1; i >= 0; i--) {
			sum += Short.parseShort(number.substring(i, i + 1));
		}

		return sum;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(sumDigit(factorial(100)));
	}

}

