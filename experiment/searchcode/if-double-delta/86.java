package com.ericsson.eunit;

import com.ericsson.eunit.TestCase;

public class Assert {

	/*
	 * protect constructor since there is static method only
	 */
	protected Assert() {
	}

	/*
	 * assert method for object to be used in assert method for other types
	 */
	public static void assertEquals(String message, Object expected,
			Object actual) {
		if (expected == null && actual == null)
			return;
		else if (expected != null && isEquals(expected, actual))
			return;
		else
			assertEqualsFailed(message, expected, actual);
	}

	/*
	 * to test whether expected and actual is equals
	 */
	private static boolean isEquals(Object expected, Object actual) {
		return expected.equals(actual);
	}

	/*
	 * if assertion failed, go into this method
	 */
	private static void assertEqualsFailed(String message, Object expected,
			Object actual) {
		System.out.println(format(message, expected, actual));
		TestCase.failedCaseNum += 1;
	}

	/*
	 * format the output error message
	 */
	private static String format(String message, Object expected, Object actual) {
		StringBuilder errorMessageBuilder = new StringBuilder();
		if (message != null && !message.equals(""))
			errorMessageBuilder.append(message);
		String expectedStr = String.valueOf(expected);
		String actualStr = String.valueOf(actual);
		errorMessageBuilder.append(": expected: ").append(expectedStr)
				.append(", actual: ").append(actualStr);
		return errorMessageBuilder.toString();
	}

	/*
	 * assert method without error message
	 */
	public static void assertEquals(Object expected, Object actual) {
		assertEquals(null, expected, actual);
	}

	/*
	 * assert method for int
	 */
	public static void assertEquals(String message, int expected, int actual) {
		assertEquals(message, (Integer)expected, (Integer)actual);
	}

	/*
	 * assert method without error message for int
	 */
	public static void assertEquals(int expected, int actual) {
		assertEquals(null, expected, actual);
	}

	/*
	 * assert method for short
	 */
	public static void assertEquals(String message, short expected, short actual) {
		assertEquals(message, (Short) expected, (Short) actual);
	}

	/*
	 * assert method without error message for short
	 */
	public static void assertEquals(short expected, short actual) {
		assertEquals(null, expected, actual);
	}

	/*
	 * assert method for long
	 */
	public static void assertEquals(String message, long expected, long actual) {
		assertEquals(message, (Long) expected, (Long) actual);
	}

	/*
	 * assert method without error message for long
	 */
	public static void assertEquals(long expected, long actual) {
		assertEquals(null, expected, actual);
	}

	// below for double and float, allow a delta when assert equals

	/*
	 * assert method for double
	 */
	public static void assertEquals(String message, double expected,
			double actual, double delta) {
		if (Double.compare(expected, actual) == 0)
			return;
		else if (!(Math.abs(expected - actual) <= delta))
			assertEqualsFailed(message, new Double(expected),
					new Double(actual));
	}

	/*
	 * assert method without message for double
	 */
	public static void assertEquals(double expected, double actual, double delta) {
		assertEquals(null, expected, actual, delta);
	}

	/*
	 * assert method for float
	 */
	public static void assertEquals(String message, float expected,
			float actual, float delta) {
		if (Float.compare(expected, actual) == 0)
			return;
		else if (!(Math.abs(expected - actual) <= delta))
			assertEqualsFailed(message, new Float(expected), new Float(actual));
	}

	/*
	 * assert method without message for float
	 */
	public static void assertEquals(float expected, float actual, float delta) {
		assertEquals(null, expected, actual, delta);
	}

}

