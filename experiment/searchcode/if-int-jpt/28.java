package randomChoice;

import java.util.ArrayList;

import tester.Tester;

/**
 * <P>Class that tests the <code>checkOneOf</code>, <code>checkNoneOf</code>, 
 * <code>checkInexactOneOf</code> and <code>checkInexactNoneOf</code> methods
 * in the class <code>Tester</code></P>
 * 
 * @author Virag Shah
 * @since 14 March 2011
 * 
 */

public class ExamplesRandomChoice {

	public ExamplesRandomChoice() {}

	/** 
	 * an instance of a class that produces a random answer from a stock set 
	 */
	Reply answers = new Reply(makeAnswers());

	/** 
	 * Initialize the stock set of answers 
	 */
	ArrayList<String> makeAnswers() {
		ArrayList<String> tmp = new ArrayList<String>();
		tmp.add("Why do you want to know?");
		tmp.add("Who cares?");
		tmp.add("It does not matter.");
		return tmp;
	}

	/**
	 * Test the method reply in the class Reply
	 * 
	 * @param t the <CODE>{@link Tester Tester}</CODE> that performs the tests
	 */
	public void testReply(Tester t){

		double myGrade = 3.001;

		String s = answers.randomAnswer();
		t.checkExpect(s.equals("Why do you want to know?") ||
				s.equals("Who cares?") ||
				s.equals("It does not matter."));

		/**
		 * Tests for checkOneOf
		 */
		t.checkOneOf("Successs: checkOneOf", 
				s, 
				"Why do you want to know?", "Who cares?", "It does not matter.");

		t.checkOneOf("Should fail: checkOneOf", 
				"HI", 
				"Why do you want to know?", "Who cares?", "It does not matter.");

		/**
		 * Tests for checkNoneOf
		 */
		t.checkNoneOf("Success: checkNoneOf", 
				"HI", 
				"Why do you want to know?", "Who cares?", "It does not matter.");

		t.checkNoneOf("Should fail: checkNoneOf", 
				s, 
				"Why do you want to know?", "Who cares?", "It does not matter.");

		/**
		 * Tests for checkInexactOneOf
		 */
		t.checkInexactOneOf("Success: checkInexactOneOf",
				0.01,     // the tolerance
				myGrade,  // actual value
				3.333, 3.0, 2.666);

		t.checkInexactOneOf("Should fail: checkInexactOneOf",
				0.01,     // the tolerance
				myGrade,  // actual value
				3.333, 3.5, 2.666);

		/**
		 * Tests for checkInexactNoneOf
		 */
		t.checkInexactNoneOf("Success: checkInexactNoneOf",
				0.01,     // the tolerance
				myGrade,  // actual value
				3.333, 3.5, 2.666);

		t.checkInexactNoneOf("Should fail: checkInexactNoneOf",
				0.01,     // the tolerance
				myGrade,  // actual value
				3.333, 3.0, 2.666);

	}

	/**
	 * <P>Display all data defined in the <CODE>{@link ExamplesRandomChoice 
	 * ExamplesRandomChoice}</CODE> class.</P>
	 * <P>Run all tests defined in the <CODE>{@link ExamplesRandomChoice 
	 * ExamplesRandomChoice}</CODE> class.</P>
	 */
	public static void main(String[] argv) {

		ExamplesRandomChoice erc = new ExamplesRandomChoice();

		System.out.println("Show all data defined in the ExamplesRandomChoice class:");
		System.out.println("\n\n---------------------------------------------------");
		System.out.println("Invoke tester.runReport(this, true, true):");
		System.out.println("Print all data, all test results");

		Tester.runReport(erc, true, true);

		System.out.println("\n---------------------------------------------------");
		System.out.println("\n---------------------------------------------------");
		System.out.println("\n---------------------------------------------------");
		System.out.println("Invoke tester.runReport(this, false, false, true):");
		System.out.println("Print no data, all test results, no warnings");

		Tester.runReport(erc, false, false);
	}
}

/**
 * Class to define a sample of random replies to a question. Used to test 
 * the <code>checkOneOf</code> method of the <code>Tester</code> class.
 * 
 */
class Reply {

	ArrayList<String> answers;

	protected Reply(ArrayList<String> answers) {
		this.answers = answers;
	}

	/**
	 * Produce randomly one of the possible answers.
	 * @return the chosen answer
	 */
	protected String randomAnswer(){
		int index = Reply.randomInt(0, this.answers.size() - 1);
		return this.answers.get(index);
	}

	/** 
	 * <P>Return a random int r in the range min <= r <= max.</P>
	 * 
	 *  <P>This method appears in Math.Utilities class of the JPT</P>
	 *  <P>Path: edu.ccs.neu.jpt.util</P>
	 */
	protected static int randomInt(int min, int max) {
		/**
		 * To prevent overflow in the calculation, switch to long
		 * Also, sort the parameters
		 */
		long a;
		long b;

		if (min <= max) {
			a = min;
			b = max;
		}
		else {
			a = max;
			b = min;
		}

		/**
		 * produce a random number
		 */
		long r = a + (long)((b - a + 1) * Math.random());

		/**
		 * To prevent round off problems, make sure that r <= b
		 */
		if (r > b)
			r = b;

		/**
		 * Now return the int result
		 */
		return (int) r;
	}
}

