package interview.minstack.minstack;

import java.util.Iterator;
import java.util.Random;
import java.util.Stack;

import junit.framework.TestCase;

/**
 * Unit test for simple App.
 */
public class MinStackTest extends TestCase {
	private int stackSize = 100;
	private int sampleSize = 100;

	/**
	 * Rigourous Test :-)
	 */
	public void testGetMin() {
		MinStack<Double> ms = new MinStack<Double>(this.stackSize);
		Random rand = new Random();
		for (int i = 0; i < this.sampleSize; i++) {
			ms.push(rand.nextDouble());
		}

		for (int i = 0; i < this.sampleSize; i++) {
			ms.pop();
			ms.getMin();
		}

		assertTrue(true);
	}

	public void testGetMin2() {
		MinStack2 ms = new MinStack2();
		Random rand = new Random();
		for (int i = 0; i < this.sampleSize; i++) {
			ms.push(rand.nextDouble());
		}

		for (int i = 0; i < this.sampleSize; i++) {
			ms.pop();
			ms.getMin();
		}

		assertTrue(true);
	}

	public void testGetMinNormalStack() {
		Stack<Double> s = new Stack<Double>();

		Random rand = new Random();
		for (int i = 0; i < this.sampleSize; i++) {
			s.push(rand.nextDouble());
		}

		for (int i = 0; i < this.sampleSize; i++) {
			s.pop();
			Double min = Double.MAX_VALUE;
			Double temp = Double.MAX_VALUE;
			Iterator<Double> iter = s.listIterator();
			while (iter.hasNext()) {
				temp = iter.next();
				if (min > temp) {
					min = temp;
				}
			}
		}

		assertTrue(true);
	}

	public void testAgreement() {
		Stack<Double> s = new Stack<Double>();
		MinStack<Double> ms = new MinStack<Double>(this.stackSize);

		Random rand = new Random();
		double randOne = 0;
		for (int i = 0; i < this.sampleSize; i++) {
			randOne = rand.nextDouble();
			s.push(randOne);
			ms.push(randOne);
		}

		for (int i = 0; i < this.sampleSize; i++) {

			Double min = Double.MAX_VALUE;
			Double temp = Double.MAX_VALUE;
			Iterator<Double> iter = s.listIterator();
			while (iter.hasNext()) {
				temp = iter.next();
				if (min > temp) {
					min = temp;
				}
			}

			Double min2 = ms.getMin();
			// System.out.println(min.toString() + "\n" + min2.toString());
			assertTrue(min.equals(min2));

			s.pop();
			ms.pop();

		}

	}

	public void testAgreement2() {
		MinStack2 s = new MinStack2();
		MinStack<Double> ms = new MinStack<Double>(this.stackSize);

		Random rand = new Random();
		double randOne = 0;
		for (int i = 0; i < this.sampleSize; i++) {
			randOne = rand.nextDouble();
			s.push(randOne);
			ms.push(randOne);
		}

		for (int i = 0; i < this.sampleSize; i++) {

			Double min = s.getMin();

			Double min2 = ms.getMin();
			// System.out.println(min.toString() + "\n" + min2.toString());
			assertEquals(min, min2);

			assertEquals(s.pop(), ms.pop());

		}

	}
}

