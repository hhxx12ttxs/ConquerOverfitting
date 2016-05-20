package princeton.algo.ch1.work;

import java.util.Arrays;

import princeton.algo.ch1.ds.Stack;

/**
 * Based on @link Evaluate 
 * @author fernando
 * @see Algorithms 4th Edition, p131
 */
public class StackCalculator {

	private enum Term {
		OPEN_PARENTHESIS("("),CLOSE_PARENTHESIS(")"),
		PLUS("+"),MINUS("-"),MULTIPLY("*"),DIVIDE("/"),
		SQUARE_ROOT("sqrt");
		
		private final String termSymbol;

		private Term(final String symbol){
			this.termSymbol = symbol;
		}
		
		static Term from(final String value){
			for (Term term : values()) {
				if(term.termSymbol.equals(value)) return term;
			}
			throw new EnumConstantNotPresentException(Term.class, value);
		}
		
		@Override
		public String toString() {
			return termSymbol;
		}
	}
	
	public Double evaluate(final String expression){
		final Stack<String> ops = new Stack<String>();
		final Stack<Double> vals = new Stack<Double>();
		return parse(expression.split(" "), ops, vals);
	}

	private Double parse(final String[] terms, final Stack<String> ops, final Stack<Double> vals) {
		if(terms == null || terms.length < 5) {
			throw new IllegalArgumentException("Expression " + Arrays.toString(terms) + " is too short.");
		}
		
		for (String term : terms) {
			if(isNumeric(term)){
				vals.push(Double.parseDouble(term));
				continue;
			}
			
			switch(Term.from(term)){
				case OPEN_PARENTHESIS: continue;
				case PLUS:
				case MINUS: 
				case MULTIPLY:
				case DIVIDE: 
				case SQUARE_ROOT: ops.push(term); break;
				case CLOSE_PARENTHESIS: evaluateAndPush(ops.pop(), vals); break;
			}
		}
		return vals.pop();
	}

	private void evaluateAndPush(final String term, final Stack<Double> vals) {
		double v = vals.pop();	
		switch(Term.from(term)){
			case PLUS: v = vals.pop() + v; break;
			case MINUS: v = vals.pop() - v; break;
			case MULTIPLY: v = vals.pop() * v; break;
			case DIVIDE: v = vals.pop() / v; break;
			case SQUARE_ROOT: v = Math.sqrt(v); break;
			default : throw new IllegalStateException("No operation matched: " + term);
		}
		vals.push(v);
	}

	private boolean isNumeric(final String value){
		for (int i = 0; i < value.length(); i++) {
            final char character = value.charAt(i);
			if (Character.isDigit(character) || Character.valueOf('.').equals(character) ) {
                return true;
			}
        }
        return false;
    }
}

