package com.lysergicjava.runit;

import java.util.ArrayList;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

import com.lysergicjava.runit.http.HTTPCallAndResponse;
import com.lysergicjava.runit.http.HTTPMethodFactory;
import com.lysergicjava.runit.http.RUnitClient;

/**
 * An RUnitTest case. <br />
 * Instances of this method are capable of building and executing REST calls,
 * asserting conditions, and logging call/response histories for failures.
 * 
 * @author jharen
 */
public class RUnitTest
{
	private static final Logger logger = (Logger) LoggerFactory.getLogger(RUnitTest.class);
	
	/**
	 * Builds http methods ready for execution.
	 */
	protected HTTPMethodFactory methodFactory;
	
	/**
	 * Executes http methods and generates call/response histories of executions.
	 */
	protected RUnitClient client;
	
	/**
	 * If true, all test failures (failed assertions
	 * of calls to fail()) will cause the call history to be reset, before
	 * any subsequent tests.  If false, history will be preserved (until the
	 * end of the test class, at which point it is marked for garbage collection in any case).
	 */
	protected boolean failuresResetHistory = true;
	
	/**
	 * If true, the client will store call/response history
	 * for the duration of the test class, or until this flag
	 * is set to false.
	 */
	protected boolean storeHistory = true;
	
	/**
	 * Record of all calls made up to this point in the test.<br />
	 * Whenever a test class completes, this history is reset.  Additionally,
	 * test failures may reset this as well.
	 */
	protected ArrayList<HTTPCallAndResponse> history = new ArrayList<HTTPCallAndResponse>();
	
	/**
	 * Default constructor.<br />
	 */
	public RUnitTest()
	{
		this.methodFactory = new HTTPMethodFactory();
		this.client = new RUnitClient();
	}
	
	public RUnitTest(HttpConnectionManagerParams connectionParams)
	{
		this.methodFactory = new HTTPMethodFactory();
		this.client = new RUnitClient(connectionParams);
	}
	
	public HTTPCallAndResponse execute(HttpMethod method)
	{
		HTTPCallAndResponse car = null;
		try
		{
			car = this.client.executeMethod(method);
		}
		catch (Exception e)
		{
			this.fail(e.toString());
		}
		if (this.storeHistory)
			this.history.add(car);
		return car;
	}
	
	/**
	 * Wipes out the history, deleting every item.
	 */
	public void clearHistory()
	{
		this.history.clear();
	}
	
	/**
	 * Asserts that a condition is true. If it isn't it throws an
	 * AssertionFailedError with the given message.
	 */
	public void assertTrue(String message, boolean condition)
	{
		if (!condition)
		{
			this.fail(message);
		}
	}

	/**
	 * Asserts that a condition is true. If it isn't it throws an
	 * AssertionFailedError.
	 */
	public void assertTrue(boolean condition)
	{
		this.assertTrue(null, condition);
	}

	/**
	 * Asserts that a condition is false. If it isn't it throws an
	 * AssertionFailedError with the given message.
	 */
	public void assertFalse(String message, boolean condition)
	{
		this.assertTrue(message, !condition);
	}

	/**
	 * Asserts that a condition is false. If it isn't it throws an
	 * AssertionFailedError.
	 */
	public void assertFalse(boolean condition)
	{
		this.assertFalse(null, condition);
	}

	/**
	 * Fails a test with the given message, and, if history is being recorded,
	 * sends the history in the AssertionError.  Additionally, if the
	 * failuresResetHistory flag is set, this method will clear the history.
	 */
	public void fail(String testMessage)
	{
		String message = null;
		if (testMessage != null)
			message = testMessage + "\nCall and response log:\n";
		else message = "Call and response log:\n";
		
		if (this.storeHistory)
		{
			StringBuffer log = new StringBuffer(message);
			for (int i = 0; i < this.history.size(); i++)
			{
				HTTPCallAndResponse item = this.history.get(i);
				log.append("Item # " + (i + 1));
				log.append(item.toString());
				log.append("\n\n");
			}
			message = log.toString();
			logger.error(message);
			if (this.failuresResetHistory)
				this.clearHistory();
		}
		throw new AssertionError(message);
	}

	/**
	 * Fails a test with no message.
	 */
	public void fail()
	{
		this.fail(null);
	}

	/**
	 * Asserts that two objects are equal. If they are not an
	 * AssertionFailedError is thrown with the given message.
	 */
	public void assertEquals(String message, Object expected, Object actual)
	{
		if ((expected == null) && (actual == null))
		{
			return;
		}
		if ((expected != null) && expected.equals(actual))
		{
			return;
		}
		this.failNotEquals(message, expected, actual);
	}

	/**
	 * Asserts that two objects are equal. If they are not an
	 * AssertionFailedError is thrown.
	 */
	public void assertEquals(Object expected, Object actual)
	{
		this.assertEquals(null, expected, actual);
	}

	/**
	 * Asserts that two Strings are equal.
	 */
	public void assertEquals(String message, String expected, String actual)
	{
		if ((expected == null) && (actual == null))
		{
			return;
		}
		if ((expected != null) && expected.equals(actual))
		{
			return;
		}
		this.failNotEquals(message, expected, actual);
	}

	/**
	 * Asserts that two Strings are equal.
	 */
	public void assertEquals(String expected, String actual)
	{
		this.assertEquals(null, expected, actual);
	}

	/**
	 * Asserts that two doubles are equal concerning a delta. If they are not an
	 * AssertionFailedError is thrown with the given message. If the expected
	 * value is infinity then the delta value is ignored.
	 */
	public void assertEquals(String message, double expected, double actual, double delta)
	{

		// handle infinity specially since subtracting to infinite values gives
		// NaN and the
		// the following test fails
		if (Double.isInfinite(expected))
		{
			if (!(expected == actual))
			{
				this.failNotEquals(message, new Double(expected), new Double(actual));
			}
		} 
		else if (!(Math.abs(expected - actual) <= delta))
		{ 
			// Because comparison with NaN always returns false
			this.failNotEquals(message, new Double(expected), new Double(actual));
		}
	}

	/**
	 * Asserts that two doubles are equal concerning a delta. If the expected
	 * value is infinity then the delta value is ignored.
	 */
	public void assertEquals(double expected, double actual, double delta)
	{
		this.assertEquals(null, expected, actual, delta);
	}

	/**
	 * Asserts that two floats are equal concerning a delta. If they are not an
	 * AssertionFailedError is thrown with the given message. If the expected
	 * value is infinity then the delta value is ignored.
	 */
	public void assertEquals(String message, float expected, float actual, float delta)
	{
		// handle infinity specially since subtracting to infinite values gives
		// NaN and the following test fails
		if (Float.isInfinite(expected))
		{
			if (!(expected == actual))
			{
				this.failNotEquals(message, new Float(expected), new Float(actual));
			}
		}
		else if (!(Math.abs(expected - actual) <= delta))
		{
			this.failNotEquals(message, new Float(expected), new Float(actual));
		}
	}

	/**
	 * Asserts that two floats are equal concerning a delta. If the expected
	 * value is infinity then the delta value is ignored.
	 */
	public void assertEquals(float expected, float actual, float delta)
	{
		this.assertEquals(null, expected, actual, delta);
	}

	/**
	 * Asserts that two longs are equal. If they are not an AssertionFailedError
	 * is thrown with the given message.
	 */
	public void assertEquals(String message, long expected, long actual)
	{
		this.assertEquals(message, new Long(expected), new Long(actual));
	}

	/**
	 * Asserts that two longs are equal.
	 */
	public void assertEquals(long expected, long actual)
	{
		this.assertEquals(null, expected, actual);
	}

	/**
	 * Asserts that two booleans are equal. If they are not an
	 * AssertionFailedError is thrown with the given message.
	 */
	public void assertEquals(String message, boolean expected, boolean actual)
	{
		this.assertEquals(message, Boolean.valueOf(expected), Boolean.valueOf(actual));
	}

	/**
	 * Asserts that two booleans are equal.
	 */
	public void assertEquals(boolean expected, boolean actual)
	{
		this.assertEquals(null, expected, actual);
	}

	/**
	 * Asserts that two bytes are equal. If they are not an AssertionFailedError
	 * is thrown with the given message.
	 */
	public void assertEquals(String message, byte expected, byte actual)
	{
		this.assertEquals(message, new Byte(expected), new Byte(actual));
	}

	/**
	 * Asserts that two bytes are equal.
	 */
	public void assertEquals(byte expected, byte actual)
	{
		this.assertEquals(null, expected, actual);
	}

	/**
	 * Asserts that two chars are equal. If they are not an AssertionFailedError
	 * is thrown with the given message.
	 */
	public void assertEquals(String message, char expected, char actual)
	{
		this.assertEquals(message, new Character(expected), new Character(actual));
	}

	/**
	 * Asserts that two chars are equal.
	 */
	public void assertEquals(char expected, char actual)
	{
		this.assertEquals(null, expected, actual);
	}

	/**
	 * Asserts that two shorts are equal. If they are not an
	 * AssertionFailedError is thrown with the given message.
	 */
	public void assertEquals(String message, short expected, short actual)
	{
		this.assertEquals(message, new Short(expected), new Short(actual));
	}

	/**
	 * Asserts that two shorts are equal.
	 */
	public void assertEquals(short expected, short actual)
	{
		this.assertEquals(null, expected, actual);
	}

	/**
	 * Asserts that two ints are equal. If they are not an AssertionFailedError
	 * is thrown with the given message.
	 */
	public void assertEquals(String message, int expected, int actual)
	{
		this.assertEquals(message, new Integer(expected), new Integer(actual));
	}

	/**
	 * Asserts that two ints are equal.
	 */
	public void assertEquals(int expected, int actual)
	{
		this.assertEquals(null, expected, actual);
	}

	/**
	 * Asserts that an object isn't null.
	 */
	public void assertNotNull(Object object)
	{
		this.assertNotNull(null, object);
	}

	/**
	 * Asserts that an object isn't null. If it is an AssertionFailedError is
	 * thrown with the given message.
	 */
	public void assertNotNull(String message, Object object)
	{
		this.assertTrue(message, object != null);
	}

	/**
	 * Asserts that an object is null.
	 */
	public void assertNull(Object object)
	{
		this.assertNull(null, object);
	}

	/**
	 * Asserts that an object is null. If it is not an AssertionFailedError is
	 * thrown with the given message.
	 */
	public void assertNull(String message, Object object)
	{
		this.assertTrue(message, object == null);
	}

	/**
	 * Asserts that two objects refer to the same object. If they are not an
	 * AssertionFailedError is thrown with the given message.
	 */
	public void assertSame(String message, Object expected, Object actual)
	{
		if (expected == actual)
		{
			return;
		}
		this.failNotSame(message, expected, actual);
	}

	/**
	 * Asserts that two objects refer to the same object. If they are not the
	 * same an AssertionFailedError is thrown.
	 */
	public void assertSame(Object expected, Object actual)
	{
		this.assertSame(null, expected, actual);
	}

	/**
	 * Asserts that two objects refer to the same object. If they are not an
	 * AssertionFailedError is thrown with the given message.
	 */
	public void assertNotSame(String message, Object expected, Object actual)
	{
		if (expected == actual)
		{
			this.failSame(message);
		}
	}

	/**
	 * Asserts that two objects refer to the same object. If they are not the
	 * same an AssertionFailedError is thrown.
	 */
	public void assertNotSame(Object expected, Object actual)
	{
		this.assertNotSame(null, expected, actual);
	}

	/**
	 * Asserts that two byte arrays refer to the same object. If they are not an
	 * AssertionFailedError is thrown with the given message.
	 */
	public void assertEquals(final byte[] expected, final byte[] actual)
	{
		this.assertEquals("", expected, actual);
	}

	/**
	 * Asserts that two byte arrays contain the same data. If they do not an
	 * AssertionFailedError is thrown with the given message.
	 */
	public void assertEquals(final String message, final byte[] expected, final byte[] actual)
	{
		if (expected == actual)
		{
			return;
		}
		if (null == expected)
		{
			this.fail("expected a null array, but a non-null array was found. " + message);
		}
		if (null == actual)
		{
			this.fail("expected non-null array, but null found. " + message);
		}

		this.assertEquals("arrays don't have the same size. " + message, expected.length, actual.length);

		for (int i = 0; i < expected.length; i++)
		{
			if (expected[i] != actual[i])
			{
				this.fail("arrays differ firstly at element [" + i + "]; " + format(message, expected[i], actual[i]));
			}
		}
	}

	private void failSame(String message)
	{
		String formatted = "";
		if (message != null)
		{
			formatted = message + " ";
		}
		this.fail(formatted + "expected not same");
	}

	private void failNotSame(String message, Object expected, Object actual)
	{
		String formatted = "";
		if (message != null)
		{
			formatted = message + " ";
		}
		this.fail(formatted + "expected same:<" + expected + "> was not:<" + actual + ">");
	}

	private void failNotEquals(String message, Object expected, Object actual)
	{
		this.fail(this.format(message, expected, actual));
	}

	/**
	 * Formats an error message.
	 * @param message
	 * @param expected
	 * @param actual
	 * @return
	 */
	private String format(String message, Object expected, Object actual)
	{
		String formatted = "";
		if (message != null)
		{
			formatted = message + " ";
		}
		return formatted + "expected:<" + expected + "> but was:<" + actual + ">";
	}
	
}

