package maps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import tester.Tester;

/**
 * Class that tests the <code>checkExpect</code> and <code>checkFail</code> methods 
 * in the class <code>Tester</code> when the data to be compared are two instances
 * of a class that implements the <code>Map</code> interface. These two classes are
 * <code>HashMap</code> and <code>TreeMap</code>.
 * 
 * @author Virag Shah
 * @since 21 March 2011
 * 
 */

public class ExamplesMaps {

	public ExamplesMaps() {}

	/** 
	 * answer to the question that starts with 'who' 
	 */
	Reply who = new Reply(whoAnswers()); 
	ArrayList<String> whoAnswers() {
		ArrayList<String> tmp = new ArrayList<String>();
		tmp.add("Who cares?");
		return tmp;
	}

	/** 
	 * answer to the question that starts with 'why' 
	 */
	Reply why = new Reply(whyAnswers());  
	ArrayList<String> whyAnswers() {
		ArrayList<String> tmp = new ArrayList<String>();
		tmp.add("Why do you want to know?");
		return tmp;
	}

	/** 
	 * answer to the question that starts with 'when' 
	 */
	Reply when = new Reply(whenAnswers());  
	ArrayList<String> whenAnswers() {
		ArrayList<String> tmp = new ArrayList<String>();
		tmp.add("It does not matter.");
		return tmp;
	}

	/** 
	 * hash map of three replies 
	 */
	HashMap<String, Reply> replies = makeReplies();
	HashMap<String, Reply> makeReplies() {
		HashMap<String, Reply> tmp = new HashMap<String, Reply>();
		tmp.put("who", who);
		tmp.put("why", why);
		tmp.put("when", when);
		return tmp;
	}

	/** 
	 * hash map of two replies 
	 */
	HashMap<String, Reply> replies2 = makeReplies2();
	HashMap<String, Reply> makeReplies2(){
		HashMap<String, Reply> tmp = new HashMap<String, Reply>();
		tmp.put("why", why);
		tmp.put("who", who);
		return tmp;
	}

	/** 
	 * tree map of three replies 
	 */
	TreeMap<String, Reply> replies3 = makeReplies3();
	TreeMap<String, Reply> makeReplies3() {
		TreeMap<String, Reply> tmp = new TreeMap<String, Reply>();
		tmp.put("who", who);
		tmp.put("why", why);
		tmp.put("when", when);
		return tmp;
	}

	/** 
	 * tree map of two replies 
	 */
	TreeMap<String, Reply> replies4 = makeReplies4();
	TreeMap<String, Reply> makeReplies4(){
		TreeMap<String, Reply> tmp = new TreeMap<String, Reply>();
		tmp.put("why", why);
		tmp.put("who", who);
		return tmp;
	}

	/**
	 * Test the method <code>checkExpect</code> in the class <code>Tester</code>
	 * when the two arguments are instances of <code>HashMap</code>
	 * 
	 * @param t the <CODE>{@link Tester Tester}</CODE> that performs the tests
	 */
	public void testHashMap(Tester t) {

		/**
		 * two different hash maps
		 */
		t.checkFail(replies, replies2, "Test to fail: Different hash maps");

		/**
		 * make the second contain the same entry set as the first one
		 */
		replies2.put("when", when);

		/**
		 * add a lot of new entries to change the load factor and size
		 */
		replies2.put("where", when);
		replies2.put("how", when);
		replies2.put("what", when);
		replies2.put("whatever", when);
		replies2.put("whose", when);
		replies2.put("wheres", when);
		replies2.put("hows", when);
		replies2.put("whats", when);
		replies2.put("whatevers", when);
		replies2.put("whoses", when);

		/**
		 * remove the extra entries - load factor will not move back
		 */
		replies2.remove("where");
		replies2.remove("how");
		replies2.remove("what");
		replies2.remove("whatever");
		replies2.remove("whose");
		replies2.remove("wheres");
		replies2.remove("hows");
		replies2.remove("whats");
		replies2.remove("whatevers");
		replies2.remove("whoses");

		/**
		 * compare the two hash maps - they should be the same
		 */
		t.checkExpect(replies, replies2, "Success: Same hash maps");

		/**
		 * change hash map 2
		 */
		replies2.remove("when");

		/**
		 * two different hash maps
		 */
		t.checkFail(replies, replies2, "Test to fail: Different hash maps");
	}

	/**
	 * Test the method <code>checkExpect</code> in the class <code>Tester</code>
	 * when the two arguments are instances of <code>TreeMap</code>
	 * 
	 * @param t the <CODE>{@link Tester Tester}</CODE> that performs the tests
	 */
	public void testTreeMap(Tester t) {

		/**
		 * two different tree maps
		 */
		t.checkFail(replies3, replies4, "Test to fail: Different tree maps");

		/**
		 * make the second contain the same entry set as the first one
		 */
		replies4.put("when", when);

		/**
		 * add a lot of new entries to change the load factor and size
		 */
		replies4.put("where", when);
		replies4.put("how", when);
		replies4.put("what", when);
		replies4.put("whatever", when);
		replies4.put("whose", when);
		replies4.put("wheres", when);
		replies4.put("hows", when);
		replies4.put("whats", when);
		replies4.put("whatevers", when);
		replies4.put("whoses", when);

		/**
		 * remove the extra entries - load factor will not move back
		 */
		replies4.remove("where");
		replies4.remove("how");
		replies4.remove("what");
		replies4.remove("whatever");
		replies4.remove("whose");
		replies4.remove("wheres");
		replies4.remove("hows");
		replies4.remove("whats");
		replies4.remove("whatevers");
		replies4.remove("whoses");

		/**
		 * compare the two tree maps - they should be the same
		 */
		t.checkExpect(replies3, replies4, "Success: Same tree maps");

		/**
		 * change tree map 4
		 */
		replies4.remove("when");

		/**
		 * two different tree maps
		 */
		t.checkFail(replies3, replies4, "Test to fail: Different tree maps");
	}

	/**
	 * <P>Display all data defined in the <CODE>{@link ExamplesMaps 
	 * ExamplesMaps}</CODE> class.</P>
	 * <P>Run all tests defined in the <CODE>{@link ExamplesMaps 
	 * ExamplesMaps}</CODE> class.</P>
	 */
	public static void main(String[] argv) {

		ExamplesMaps em = new ExamplesMaps();

		System.out.println("Show all data defined in the ExamplesMaps class:");
		System.out.println("\n\n---------------------------------------------------");
		System.out.println("Invoke tester.runReport(this, true, true):");
		System.out.println("Print all data, all test results");

		Tester.runReport(em, true, true);

		System.out.println("\n---------------------------------------------------");
		System.out.println("\n---------------------------------------------------");
		System.out.println("\n---------------------------------------------------");
		System.out.println("Invoke tester.runReport(this, false, false, true):");
		System.out.println("Print no data, all test results, no warnings");

		Tester.runReport(em, false, false);
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

