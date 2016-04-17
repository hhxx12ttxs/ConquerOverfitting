/**
 * NumberFormatter.java
 * com.qagwaai.starmalaccamax.shared
 * StarMalaccamax
 * Created Mar 15, 2011 at 10:01:39 AM
 */
package com.qagwaai.starmalaccamax.shared;

/**
 * @author pgirard
 * 
 */
public final class NumberFormatter {
    /**
	 * 
	 */
    private NumberFormatter() {
    }

    /**
     * 
     * @param number
     *            the number to format
     * @param digits
     *            the number of digits in front of the decimal
     * @param decimals
     *            the number of digits behind the decimal
     * @return the formatted number
     */
    public static String formatNumber(final long number, final int digits, final int decimals) {
        String formatted = null;
        String abbreviation = null;
        String prefix = null;
        long divisor = 1;
        long absNumber = Math.abs(number);

        String numDigits = "1";
        for (int i = 0; i < digits - 1; i++) {
            numDigits += "0";
        }

        String numDecimals = "1";
        for (int i = 0; i < decimals; i++) {
            numDecimals += "0";
        }
        long numDigitsDivider = Long.valueOf(numDigits);
        long numDecimalsDivider = Long.valueOf(numDecimals);

        if ((absNumber >= 0) && (absNumber < 10)) {
            abbreviation = "";
            prefix = "";
            divisor = 1;
        } else if ((absNumber >= 10) && (absNumber < 100)) {
            abbreviation = "da";
            prefix = "deca";
            divisor = 10;
        } else if ((absNumber >= 100) && (absNumber < 1000)) {
            abbreviation = "h";
            prefix = "hecto";
            divisor = 100;
        } else if ((absNumber >= 1000) && (absNumber < 1000000)) {
            abbreviation = "K";
            prefix = "kilo";
            divisor = 1000;
        } else if ((absNumber >= 1000000) && (absNumber < 1000000000L)) {
            abbreviation = "M";
            prefix = "mega";
            divisor = 1000000;
        } else if ((absNumber >= 1000000000L) && (absNumber < 1000000000000L)) {
            abbreviation = "G";
            prefix = "giga";
            divisor = 1000000000L;
        } else if ((absNumber >= 1000000000000L) && (absNumber < 1000000000000000L)) {
            abbreviation = "T";
            prefix = "tera";
            divisor = 1000000000000L;
        } else if ((absNumber >= 1000000000000000L) && (absNumber < 1000000000000000000L)) {
            abbreviation = "P";
            prefix = "peta";
            divisor = 1000000000000000L;
        } else if ((absNumber >= 1000000000000000000L) && (absNumber < Long.MAX_VALUE)) {
            abbreviation = "E";
            prefix = "exa";
            divisor = 1000000000000000000L;
        } else {
            return String.valueOf(number);
        }
        double divided = Double.valueOf(number) / divisor;

        divided = divided * numDigitsDivider;
        divided = divided * numDecimalsDivider;
        long shrunk = Math.round(divided);
        double finalNumber = Double.valueOf(shrunk) / numDecimalsDivider;
        formatted = String.valueOf(finalNumber) + abbreviation;

        return formatted;
    }
}

