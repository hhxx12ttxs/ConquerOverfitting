package org.imogene.android.util.field;

import java.text.SimpleDateFormat;

public class FieldPattern {
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
	private static final SimpleDateFormat DATI_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
	
	/**
	 * Validation test for floats
	 * @param pattern the validation pattern
	 * @param value the value to test
	 * @return true if the test matches
	 */
	public static boolean matchesInt(String pattern, int value) {
		try {
			if (pattern.startsWith("<=")) {
				String integer = pattern.substring(2);
				return value <= Integer.parseInt(integer);
			}

			if (pattern.startsWith("<")) {
				String integer = pattern.substring(1);
				return value < Integer.parseInt(integer);
			}

			if (pattern.startsWith(">=")) {
				String integer = pattern.substring(2);
				return value >= Integer.parseInt(integer);
			}

			if (pattern.startsWith(">")) {
				String integer = pattern.substring(1);
				return value > Integer.parseInt(integer);
			}

			if (pattern.startsWith("!=")) {
				String integer = pattern.substring(2);
				return value != Integer.parseInt(integer);
			}

			if (pattern.startsWith("==")) {
				String integer = pattern.substring(1);
				return value == Integer.parseInt(integer);
			}
			
			if (pattern.contains(";")) {
				String[] integers = pattern.split(";");
				return Integer.parseInt(integers[0]) < value && value < Integer.parseInt(integers[1]);
			}
		} catch (Exception e) {
			return false;
		}

		return false;
	}

	/**
	 * Validation test for floats
	 * @param pattern the validation pattern
	 * @param value the value to test
	 * @return true if the test matches
	 */
	public static boolean matchesFloat(String pattern, float value) {
		try {
			if (pattern.startsWith("<=")) {
				String number = pattern.substring(2);
				return value <= Float.parseFloat(number);
			}

			if (pattern.startsWith("<")) {
				String number = pattern.substring(1);
				return value < Float.parseFloat(number);
			}

			if (pattern.startsWith(">=")) {
				String number = pattern.substring(2);
				return value >= Float.parseFloat(number);
			}

			if (pattern.startsWith(">")) {
				String number = pattern.substring(1);
				return value > Float.parseFloat(number);
			}

			if (pattern.startsWith("!=")) {
				String number = pattern.substring(2);
				return value != Float.parseFloat(number);
			}

			if (pattern.startsWith("==")) {
				String number = pattern.substring(2);
				return value == Float.parseFloat(number);
			}
			
			if (pattern.contains(";")) {
				String[] integers = pattern.split(";");
				return Float.parseFloat(integers[0]) < value && value < Float.parseFloat(integers[1]);
			}
		} catch (Exception ex) {
			return false;
		}

		return false;
	}

	/**
	 * Validation test for date
	 * @param pattern the validation pattern
	 * @param value the value to test
	 * @return true if the test matches
	 */
	public static boolean matchesDate(String pattern, long value) {
		try {
			if (pattern.startsWith("<=")) {
				long limit = DATE_FORMAT.parse(pattern.substring(2)).getTime();
				return value <= limit;
			}

			if (pattern.startsWith("<")) {
				long limit = DATE_FORMAT.parse(pattern.substring(1)).getTime();
				return value < limit;
			}

			if (pattern.startsWith(">=")) {
				long limit = DATE_FORMAT.parse(pattern.substring(2)).getTime();
				return value >= limit;
			}

			if (pattern.startsWith(">")) {
				long limit = DATE_FORMAT.parse(pattern.substring(1)).getTime();
				return value > limit;
			}

			if (pattern.startsWith("!=")) {
				long limit = DATE_FORMAT.parse(pattern.substring(2)).getTime();
				return value != limit;
			}

			if (pattern.startsWith("==")) {
				long limit = DATE_FORMAT.parse(pattern.substring(2)).getTime();
				return value == limit;
			}
			
			if (pattern.contains(";")) {
				String[] integers = pattern.split(";");
				long limitInf = DATE_FORMAT.parse(integers[0]).getTime();
				long limitSup = DATE_FORMAT.parse(integers[1]).getTime();
				return limitInf < value && value < limitSup;
			}
		} catch (Exception ex) {
			return false;
		}

		return false;
	}
	
	/**
	 * Validation test for date time
	 * @param pattern the validation pattern
	 * @param value the value to test
	 * @return true if the test matches
	 */
	public static boolean matchesDateTime(String pattern, long value) {
		try {
			if (pattern.startsWith("<=")) {
				long limit = DATI_FORMAT.parse(pattern.substring(2)).getTime();
				return value <= limit;
			}

			if (pattern.startsWith("<")) {
				long limit = DATI_FORMAT.parse(pattern.substring(1)).getTime();
				return value < limit;
			}

			if (pattern.startsWith(">=")) {
				long limit = DATI_FORMAT.parse(pattern.substring(2)).getTime();
				return value >= limit;
			}

			if (pattern.startsWith(">")) {
				long limit = DATI_FORMAT.parse(pattern.substring(1)).getTime();
				return value > limit;
			}

			if (pattern.startsWith("!=")) {
				long limit = DATI_FORMAT.parse(pattern.substring(2)).getTime();
				return value != limit;
			}

			if (pattern.startsWith("==")) {
				long limit = DATI_FORMAT.parse(pattern.substring(2)).getTime();
				return value == limit;
			}
			
			if (pattern.contains(";")) {
				String[] integers = pattern.split(";");
				long limitInf = DATI_FORMAT.parse(integers[0]).getTime();
				long limitSup = DATI_FORMAT.parse(integers[1]).getTime();
				return limitInf < value && value < limitSup;
			}
		} catch (Exception ex) {
			return false;
		}

		return false;
	}
	
	/**
	 * Validation test for time
	 * @param pattern the validation pattern
	 * @param value the value to test
	 * @return true if the test matches
	 */
	public static boolean matchesTime(String pattern, long value) {
		try {
			if (pattern.startsWith("<=")) {
				long limit = TIME_FORMAT.parse(pattern.substring(2)).getTime();
				return value <= limit;
			}

			if (pattern.startsWith("<")) {
				long limit = TIME_FORMAT.parse(pattern.substring(1)).getTime();
				return value < limit;
			}

			if (pattern.startsWith(">=")) {
				long limit = TIME_FORMAT.parse(pattern.substring(2)).getTime();
				return value >= limit;
			}

			if (pattern.startsWith(">")) {
				long limit = TIME_FORMAT.parse(pattern.substring(1)).getTime();
				return value > limit;
			}

			if (pattern.startsWith("!=")) {
				long limit = TIME_FORMAT.parse(pattern.substring(2)).getTime();
				return value != limit;
			}

			if (pattern.startsWith("==")) {
				long limit = TIME_FORMAT.parse(pattern.substring(2)).getTime();
				return value == limit;
			}
			
			if (pattern.contains(";")) {
				String[] integers = pattern.split(";");
				long limitInf = TIME_FORMAT.parse(integers[0]).getTime();
				long limitSup = TIME_FORMAT.parse(integers[1]).getTime();
				return limitInf < value && value < limitSup;
			}
		} catch (Exception ex) {
			return false;
		}

		return false;
	}
}

