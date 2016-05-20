import java.util.EmptyStackException;
import java.util.Stack;
/**
 * This class checks for balanced parentheses.
 * @author Seth Hobson (modified)
 * @version 4/6/2011
 * 
 */
public class Parentheses {
    private static final char L_PAREN    = '(';
    private static final char R_PAREN    = ')';
    private static final char L_BRACE    = '{';
    private static final char R_BRACE    = '}';
    private static final char L_BRACKET  = '[';
    private static final char R_BRACKET  = ']';

    public boolean isBalanced(String s) {
    	// adds another parameter, initially the empty string
    	//return checkBalancedRecursive(s, "");
    	// additional empty string isn't needed since I'm using a stack
    	return checkBalancedIterative(s);
    }
    
    public boolean isOpen(char ch) {
    	return (ch == L_PAREN) || (ch == L_BRACE) || (ch == L_BRACKET);
    }
    
    public boolean isClose(char ch) {
    	return (ch == R_PAREN) || (ch  == R_BRACE) || (ch  == R_BRACKET) ;
    }   
    
    public boolean isMatch(char leftChar, char rightChar) {
    	return ((leftChar == L_PAREN) && (rightChar == R_PAREN)) ||
    	((leftChar == L_BRACE) && (rightChar == R_BRACE)) ||
    	((leftChar == L_BRACKET) && (rightChar == R_BRACKET));
    }
    
    /**
     * Task: Tests the input string to see if it contains a balanced 
     * parentheses.
     * @param input the string to be evaluated
     * @return true if all parentheses match
     */
    public boolean checkBalancedIterative(String input) {
    	
    	Stack<Character> stack = new Stack<Character>(); // create an empty stack
    	boolean balanced = true;
    	int index = 0;
    	try {
    		while (balanced && index < input.length()) {  // loop until empty or unbalanced parentheses found
    			char nextChar = input.charAt(index); // grab char at index
    			if(isOpen(nextChar)) { // if char is open
    				stack.push(nextChar); // push it on the stack
    			}
    			else if (isClose(nextChar)) {  // if nextChar is closed
    				char lastChar = stack.pop(); // pop the last char off the stack
    				balanced = isMatch(lastChar, nextChar); // check them for balance
    			}
    			index++; // update the index
    		}
    	} catch (EmptyStackException e) {
    		balanced = false;  // in case we catch one that isn't balanced
    	}
    	return balanced && stack.empty();
    }
    
    public boolean checkBalancedRecursive(String input, String openCharacters){
    	if(input.isEmpty() && openCharacters.isEmpty()) return true; // all done, no errors
    	else if(input.isEmpty()) return false;
    	else {
//    		System.out.println("Debug: input = "+input+" and open = "+openCharacters);
    		char firstChar = input.charAt(0); 
    		if(isOpen(firstChar)) return checkBalancedRecursive(input.substring(1),firstChar+openCharacters);
    		else if (isClose(firstChar)) {
    			return (!openCharacters.isEmpty()) && isMatch(openCharacters.charAt(0),firstChar) &&
    			       checkBalancedRecursive(input.substring(1), openCharacters.substring(1));
    		}
    		else // ignore all other characters
    			return checkBalancedRecursive(input.substring(1), openCharacters);
    	}
    }

    private void testBalanced(){
    	String[] tests = {"()[]<>{}","(<","]}","()<","(][)","{(X)[XY]}"};
    	boolean result;
    	for(int j=0; j < tests.length; j++){
    		result = isBalanced(tests[j]);
    		System.out.print("The string " + tests[j] + " is ");
            if(!result) System.out.print("not ");
            System.out.println("balanced.");
    	}
    }
 
    public static void main(String[] args) {
       Parentheses p = new Parentheses();
       p.testBalanced();
    }
}
