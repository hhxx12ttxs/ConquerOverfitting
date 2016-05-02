package tests.user;

import java.util.*;

import tester.*;

/**
 * Copyright 2007, 2008 Viera K. Proulx
 * This program is distributed under the terms of the 
 * GNU Lesser General Public License (LGPL)
 */

/**
 * Class that tests the <code>checkOneOf</code> method in the class
 * <code>Tester</code> and the <code>checkExpect</code> method 
 * in the class <code>Tester</code> when the data to be compared are
 * two instances of a class that implements the <code>Map</code> 
 * interface.
 */
@Example
public class ExMapRandom{
  
  public ExMapRandom(){}
  
  /** an instance of a class that produces a random answer from a stock set */
  Reply answers = new Reply(makeAnswers());
  
  /** Initialize the stock set of answers */
  ArrayList<String> makeAnswers(){
    ArrayList<String> tmp = new ArrayList<String>();
    tmp.add("Why do you want to know?");
    tmp.add("Who cares?");
    tmp.add("It does not matter.");
    return tmp;
  }
  
  /**
   * Test the method reply in the class Reply
   * @param t the <CODE>{@link Tester Tester}</CODE> that performs the tests
   */
  @TestMethod
  public void testReply(Tester t){
    
    String s = answers.randomAnswer();
    t.checkExpect(s.equals("Why do you want to know?") ||
                  s.equals("Who cares?") ||
                  s.equals("It does not matter."));
    
    
    t.checkOneOf(s, new String[]{"Why do you want to know?", 
        "Who cares?",
        "It does not matter."}, "HOORAY");

    /*t.checkOneOf("HI", new String[]{"Why do you want to know?", 
                                    "Who cares?",
                                    "It does not matter."}, "It fails :)");*/
  }
  
  // test new Random().nextInt() % 7
  @TestMethod
  public void testRandom(Tester t){
    t.checkOneOf(new Integer(new Random().nextInt(3)),
        new Integer[]{0, 1, 2});
  }
  
  /** answer to the question that starts with 'who' */
  Reply who = new Reply(whoAnswers()); 
  ArrayList<String> whoAnswers(){
    ArrayList<String> tmp = new ArrayList<String>();
    tmp.add("Who cares?");
    return tmp;
  }
  
  /** answer to the question that starts with 'why' */
  Reply why = new Reply(whyAnswers());  
  ArrayList<String> whyAnswers(){
    ArrayList<String> tmp = new ArrayList<String>();
    tmp.add("Why do you want to know?");
    return tmp;
  }
  
  /** answer to the question that starts with 'when' */
  Reply when = new Reply(whenAnswers());  
  ArrayList<String> whenAnswers(){
    ArrayList<String> tmp = new ArrayList<String>();
    tmp.add("It does not matter.");
    return tmp;
  }
  
  /** hash map of three replies */
  HashMap<String, Reply> replies = makeReplies();
  HashMap<String, Reply> makeReplies(){
    HashMap<String, Reply> tmp = new HashMap<String, Reply>();
    tmp.put("who", who);
    tmp.put("why", why);
    tmp.put("when", when);
    return tmp;
  }
  
  /** hash map of two replies */
  HashMap<String, Reply> replies2 = makeReplies2();
  HashMap<String, Reply> makeReplies2(){
    HashMap<String, Reply> tmp = new HashMap<String, Reply>();
    tmp.put("why", why);
    tmp.put("who", who);
    return tmp;
  }
 
  /**
   * Test the method <code>checkExpect</code> in the class <code>Tester</code>
   * when the two arguments are instances of <code>HashMap</code>
   * @param t
   */
  @TestMethod
  public void testHashMapCompare(Tester t){
    // two different maps
    t.checkFail(replies, replies2, "should be false");
    
    // make the second contain the same entry set as the first one
    replies2.put("when", when);
    
    // add a lot of new entries to change the load factor and size
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
    
    // remove the extra entries - load factor will not move back
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
    
    // compare the two hashmaps - they should be the same
    t.checkExpect(replies, replies2, "should be true");
  }

}

/**
 * Class to define a sample of random replies to a question. Used to test 
 * the <code>checkOneOf</code> method of the <code>Tester</code> class.
 * 
 * @author Viera K. Proulx
 *
 */
class Reply{
 ArrayList<String> answers;
 
 protected Reply(ArrayList<String> answers){
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
     // To prevent overflow in the calculation, switch to long
     // Also, sort the parameters
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
     
     // produce a random number
     long r = a + (long)((b - a + 1) * Math.random());
     
     // To prevent round off problems, make sure that r <= b
     if (r > b)
         r = b;
     
     // Now return the int result
     return (int) r;
 }
}

