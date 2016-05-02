package org.btrg.uti;

import java.math.BigDecimal;

public class MathUtils_ {

	public static String decimalMultiplication(long multiplicand,
			long multiplier, int places) {
		String returnString = "0";
		if (multiplicand != 0 && multiplier != 0) {
			double doubleMultiplier = (double) multiplier;
			double doubleMultiplicand = (double) multiplicand;
			double answer = doubleMultiplier * doubleMultiplicand;
			BigDecimal bigDecimalAnswer = new BigDecimal(answer).setScale(
					places, BigDecimal.ROUND_HALF_UP);
			returnString = bigDecimalAnswer.toPlainString();
		}
		return returnString;
	}

	public static String decimalDivision(long dividend, long divisor, int places) {
		String returnString = "NA";
		if (dividend != 0 && divisor != 0) {
			double doubleDivisor = (double) divisor;
			double doubleDividend = (double) dividend;
			double answer = doubleDivisor / doubleDividend;
			BigDecimal bigDecimalAnswer = new BigDecimal(answer).setScale(
					places, BigDecimal.ROUND_HALF_UP);
			returnString = bigDecimalAnswer.toPlainString();
		}
		return returnString;
	}

}

