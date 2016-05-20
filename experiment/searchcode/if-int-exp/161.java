package balancedTernary;

/**
 * @author Tianyi Du
 * 
 *         Contains the core operations of a Balanced Ternary Calculator which
 *         executes add, subtract, multiply, and divide functions. Also includes
 *         negate, and conversion between the two representations.
 */
public class BTOperations {

	/**
	 * Converts an integer to a balanced ternary string.
	 * 
	 * @param n
	 *            Integer to be converted
	 * @return reversedString String balanced ternary representation of integer
	 */
	static String intToBt(int n) {
		int remainder = 0;
		String ternString = "";

		// If negative, treat like positive, then multiply by -1 when finished.
		if (n < 0)
			return negate(intToBt(-1 * n));
		else if (n > 0) {
			while (n >= 0) {
				if ((n == 0) && (remainder == 1))
					break;
				remainder = n % 3;
				n = n / 3;
				if (remainder == 0)
					ternString += "0";
				else if (remainder == 1)
					ternString += "1";
				else {
					ternString += "N";
					n += 1;
				}
			}
			return reverseString(ternString);
		} else
			return "0";
	}

	/**
	 * Converts a balanced ternary string to an integer.
	 * 
	 * @param bt1
	 *            String balanced ternary to be converted
	 * @return cubeSum Decimal representation of of balanced ternary
	 */
	static int btToInt(String bt1) {
		int exp = bt1.length();
		int cubeSum = 0;
		for (int i = 0; i < bt1.length(); i++) {
			exp -= 1;
			if (bt1.charAt(i) == '0')
				continue;
			else if (bt1.charAt(i) == '1')
				cubeSum += Math.pow(3, exp);
			else
				cubeSum -= Math.pow(3, exp);
		}
		return cubeSum;
	}

	/**
	 * Adds two balanced ternary strings.
	 * 
	 * @param bt1
	 *            First operand
	 * @param bt2
	 *            Second operand
	 * @return sumBT Sum of operands
	 */
	static String add(String bt1, String bt2) {
		int value1 = btToInt(bt1);
		int value2 = btToInt(bt2);
		int sumInt = value1 + value2;
		String sumBt = intToBt(sumInt);
		return sumBt;
	}

	/**
	 * Subtracts one balanced ternary string from another.
	 * 
	 * @param bt1
	 *            First operand
	 * @param bt2
	 *            Second operand
	 * @return diffBT Difference of first operand minus the second.
	 */
	static String subtract(String bt1, String bt2) {
		int value1 = btToInt(bt1);
		int value2 = btToInt(bt2);
		int diffInt = value1 - value2;
		String diffBt = intToBt(diffInt);
		return diffBt;
	}

	/**
	 * Multiplies two balanced ternary strings.
	 * 
	 * @param bt1
	 *            First operand
	 * @param bt2
	 *            Second operand
	 * @return prodBT The product of the two operands.
	 */
	static String multiply(String bt1, String bt2) {
		int value1 = btToInt(bt1);
		int value2 = btToInt(bt2);
		int prodInt = value1 * value2;
		String prodBt = intToBt(prodInt);
		return prodBt;
	}

	/**
	 * Divides the first operand by the second.
	 * 
	 * @param bt1
	 *            First operand
	 * @param bt2
	 *            Second operand
	 * @return divBT The quotient of the first operand divided by the second.
	 */
	static String divide(String bt1, String bt2) {
		int value1 = btToInt(bt1);
		int value2 = btToInt(bt2);
		int divInt = value1 / value2;
		String divBt = intToBt(divInt);
		return divBt;
	}

	/**
	 * Returns the inverted string of the operand given.
	 * 
	 * @param bt1
	 *            Operand to be inverted
	 * @return inverted Inverse of operand
	 */
	static String negate(String bt1) {
		bt1 = bt1.replace("-", "");
		String swapOne = bt1.replace("1", "x");
		String swapTwo = swapOne.replace("N", "1");
		String inverted = swapTwo.replace("x", "N");
		return inverted;
	}

	/**
	 * Reverses the order the characters of a string
	 * 
	 * @param string
	 *            string to be reversed
	 * @return newString reversed string
	 */
	static String reverseString(String string) {
		String newString = "";
		for (int i = string.length() - 1; i >= 0; i--) {
			newString += string.charAt(i);
		}
		return newString;
	}
}

