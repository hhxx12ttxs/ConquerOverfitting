package solution.math;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="www.sureinterview.com">SureInterview</a> check
 *         http://www.sureinterview.com/shwqst/43005/
 */
public class ImplementOperators {

	final static Map<Integer, Integer> bitMap = new HashMap<Integer, Integer>();

	public ImplementOperators() {
		for (int i = 0; i < 32; i++) {
			bitMap.put(1 << i, i);
		}
	}

	// check http://www.sureinterview.com/shwqst/43005/
	int add(int a, int b) {
		int cry, add;
		do {
			add = a ^ b;
			cry = (a & b) << 1;

			a = add;
			b = cry;
		} while (cry != 0);
		return add;
	}

	// check http://www.sureinterview.com/shwqst/43005/
	int divide(int a, int b) {
		if (b == 0)
			throw new ArithmeticException();

		boolean neg = (a > 0) ^ (b > 0);
		if (a < 0) {
			a = -a;
		}

		if (b < 0) {
			b = -b;
		}

		if (a < b)
			return 0;

		int msb = 0;
		for (msb = 0; msb < 32; msb++) {
			if ((b << msb) >= a) {
				break;
			}
		}

		int q = 0;
		for (int i = msb; i >= 0; i--) {
			if ((b << i) > a) {
				continue;
			}
			q |= (1 << i);
			a -= (b << i);
		}

		if (neg)
			return -q;

		return q;
	}

	// check http://www.sureinterview.com/shwqst/43005/
	int multiply(int a, int b) {
		boolean neg = (b < 0);
		if (b < 0) {
			b = -b;
		}

		int sum = 0;
		while (b > 0) {
			int lastBit = bitMap.get(b & ~(b - 1));
			sum += a << lastBit;
			b &= b - 1;
		}

		if (neg) {
			sum = -sum;
		}

		return sum;
	}

	// check http://www.sureinterview.com/shwqst/43005/
	int subtract(int a, int b) {
		return add(a, add(~b, 1));
	}

	@Test
	public void test() {
		for (int i = 0; i < 100000; i++) {
			int a = (int) (Math.random() * 10000) - 5000;
			int b = (int) (Math.random() * 3000) - 1500;
			Assert.assertEquals(a + b, add(a, b));
			Assert.assertEquals(a - b, subtract(a, b));
			Assert.assertEquals(a * b, multiply(a, b));
			if (b != 0) {
				Assert.assertEquals(a / b, divide(a, b));
			}
		}
	}

}

