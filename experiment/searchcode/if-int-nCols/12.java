package test;

import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
/**
 * A spreadsheet consists of a two-dimensional array of cells, labeled A1, B1, ...., A2, B2, ....
 * Each cell contains either a number (its value) or an expression.
 *
 * <p> For simplicity, expressions are given in reverse-polish notation (e.g. "10 2 /" evaluates to 5).
 * They may contain space-separated terms that are either numbers, cell references,
 * and the operators '+', '-', '*', '/'.
 *
 * <p> The exercise is to fill in the code for the class below, in such a way that the tests
 * in TestSpreadSheet pass.
 *
 * <p>
 * Comments: You can assume that there are no more than 26 rows (A-Z) in the spreadsheet.
*/
public class SpreadSheet {

	// to keep given parameters
	private int nRows = 0;
	private int nCols = 0;
	private String[] exprArray = null;

	// this is for return values
	private Double[] values = null;

	// to check if already tried to calcurate the expression (for CircularReferenceException)
	private boolean[] visited = null;

	//private List<List<String>> rows = new ArrayList<List<String>>();;

    /**
     * Construct a nRows x nCols SpreadSheet, with cells containing
     * the expressions passed in the exprArray.
     *
     * <p> The expressions passed in the exprArray String array are in row
     * by row order, i.e.:
     * <table border="1">
     * <tr><td>A1</td><td>B1</td><td>C1</td></tr>
     * <tr><td>A2</td><td>B2</td><td>C2</td></tr>
     * </table>
     * etc.
     * @param nRows
     * @param nCols
     * @param exprArray
     */
    public SpreadSheet(int nRows, int nCols, String... exprArray) {
		// initialize member valiables
		this.nRows = nRows;
		this.nCols = nCols;
		this.exprArray = exprArray;
		this.values = new Double[nRows * nCols];
		for(int i = 0; i < values.length; i++) values[i] = null;
		this.visited = new boolean[nRows*nCols];
		for(int i = 0; i < visited.length; i++) visited[i] = false;
    }

    /**
     * @return the values from a "solved" SpreadSheet
     */
    public Double[] dump() throws CircularReferenceException {
		// iterate entire list to calcurate all expresison
		// Note: it is not necessary to nexted loop. This is for visulalizing table.
		for(int i = 0; i < nRows; i++){
			for(int j = 0; j < nCols; j++){
				// to check if it is already calcurated
				if(values[nRows * i + j] ==  null){
					// retrieve expression
					String expr = exprArray[nRows * i + j];
					// calcurate expresion
					// index is for store value
					solve(nRows * i + j, expr);
				}
			}
		}
		// return values
        return values;
    }
	/**
	 * Calcurate the value for expression. If find the circular reference, throws CircularReferenceException.
	 *
	 * @return the values for expression
	 * @param index expresion's index in the list
	 * @param expr expression
	 * @thrown CircularReferenceException in case of circular reference
     */
    private double solve(int index, String expr) throws CircularReferenceException {

		// avoid to calcurate multiple times
		if(values[index] != null){
			return values[index];
		}

		// to check if expression has circular reference
		if(visited[index] == true) throw new CircularReferenceException(expr);

		// set visited flag for finding circular reference
		visited[index] = true;

		// Use Stack for calcurating the expression
		Stack<String> exprStack = new Stack<String>();
		// tokenize expression by whitespace
		String[] tokens = expr.split("\\s+");
		// iterate all tokens, and store them into stack
		for (String token : tokens){
			exprStack.push(token);
		}
		// calcurate the value
		double value = calcurate(exprStack);
		// store the value in the value's list
		values[index] = value;
		// return value
		return value;
	}

	/**
	 * Calcurate the value from expression stack
	 * @param exprStack the expression stack (@see solve(int index, String expr))
	 * @return the value
	 */
	private double calcurate(Stack<String> exprStack){
		double value = 0;
		String token = exprStack.pop();
		// check if poped token is operator
		if(isOperator(token)){
			// calcurate the right side value
			double val1 = calcurate(exprStack);
			// calcurate the left side value
			double val2 = calcurate(exprStack);
			if(token.equals("+"))
				value = val2 + val1;
			else if(token.equals("-"))
				value = val2 - val1;
			else if(token.equals("*"))
				value = val2 * val1;
			else if(token.equals("/"))
				value = val2 / val1;
		}else{
			// if check if toke is number
			if(isNumber(token)){
				// return number
				return Double.valueOf(token);
			}else{
				// In case token is substitution of other cell:
				// caclurate index value for substituted cell expression
				int substituteIndex = getIndex(token);
				// get expression
				String substituteExpr = exprArray[substituteIndex];
				// calcurate the value
				return solve(substituteIndex, substituteExpr);
			}
		}

		// should not come here...
		return value;

	}

	/**
	 * Check if token is operator
	 */
	private boolean isOperator(String token){
		if( token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/") )
			return true;
		else
			return false;
	}

	/**
	 * Check if token/expression is number
	 */
	private boolean isNumber(String expr){
		try{
			Double.valueOf(expr);
			return true;
		}catch(NumberFormatException e){
			return false;
		}
	}

    /**
     * Calcurate index of list for cell substitution
	 */
	private int getIndex(String expr){
		int col = expr.charAt(0) - 'A';
		int row = Integer.valueOf(expr.substring(1)) - 1;
		int index = nCols * row + col;
		return index;
	}


    public class CircularReferenceException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public CircularReferenceException(String msg) {
            super(msg);
        }
    }
}

