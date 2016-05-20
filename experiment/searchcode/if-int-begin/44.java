package com.objectwave.utility;

import java.util.*;
import java.text.ParseException;

/**
 * A class which will evaluate stringified arithmetic / boolean expressions.
 * Standard order of evaluation of operators is honored.
 * Nested parentheses are supported, only round ones though: "(" and ")".
 *
 * Where each #, $, and "b" is any expression which evaluates to a numberic, string, or boolean
 * value, respectively:
 *
 * Arithmetic operators supported are  #+#  #-#  -#  #*#  #/# #^#            (result type #)
 * Boolean operators supported are     b&&b  b||b  !b                        (result type b)
 * Numeric comparisons supported are   #==#  #!=#  #<=#  #>=#  #<#  #>#      (result type b)
 * Numeric comparisons supported are   $==$  $!=$  $<=$  $>=$  $<$  $>$ $in$ (result type b)
 * The "condition assignment" operator b?#:#                                 (result type #)
 *
 * There is a reserved keyword "null" for null comparisons.  The only support for
 * this explicit null object is the "==" operator.  All of
 *          <anyAtomicValue> == null
 *          null == <anyAtomicValue>
 *          null == null
 * are valid expressions, all of which return true IFF <anyAtomicValue> is
 * the "null" object.  This allows expressions to handle objects that may be null:
 * they'd be represented in the expression by "null" rather than by their value
 * type.  The null comparison would probably by useful in conjunction with the
 * "?:" operator: "(%a%==null ? 0:0 : %a%)" ==> "(23.4==null ? 0.0 : 23.4)"
 * (This example assumes that external methods provide symbol-substitution
 * capabilities.)
 *
 * All numeric values are promoted to type "double" for execution. The following
 * patterns are recognized as numbers, where {0-9}* means 0 or more digits,
 * {0-9}+ means 1 or more digits, and [xxx] means "xxx" is optional:
 *
 *          {0-9}*.{0-9}+   or   {0-9}+[.{0-9}*]
 *
 * Since all numeric expressions are represented as doubles, people unfamiliar with
 * floating-point arithmetic may be surprised at some of the results.  For
 * instance, the comparison "2^54 == 2^54-1" will return true, since for these
 * extremely large numbers (on the order of 10^16) floating point representation
 * cannot distingish between these numbers (their internal representations are
 * identical).
 *
 * String are supported only for comparison operations with other strings. A string is
 * defined as a literal, delimited by double-quote characters (").  Note that there is no
 * support for escape sequences, so a string cannot contain a (") character, and "\t" will not
 * be translated to a tab character.  Ditto for \r, \n, \\, and any other escape sequence.
 *
 * The special operator for strings, "in", requires some explanation.  "in" takes two string
 * operands, the rhs being assumed to be a comma-delimited list of substrings.  The method will
 * return true iif the lhs string exists as one on the comma-delimited elements of the rhs
 * string.  This match must be exact for the method to succeed: whitespace is not taken care
 * of for you.
 *
 * The tokens "true" and "false" are recognized as static boolean values.
 *
 * Note that the string "9 * -5" is not valid: two arithmetic operators cannot be
 * adjacent.  Use the string "9 * (-5)" instead.
 *
 * Note that the string "5<6<7" is not valid: the communative property of comparison
 * operators is not honored: you must use "5<6 && 6<7" instead.
 *
 * Note that the symbol "_" is explicitly disallowed.  It is substituted internally
 * to represent the negation operator, to distingish between subtraction and
 * negation operations.
 *
 * Finally, note that the boolean expression "5<6 && !2<3" is valid: a "!" operator
 * can follow another boolean operator.  Also, "!!true" is valid.  Redundant, but
 * valid.
 *
 * The order of evaluation of operators is (from highest to lowest priority):
 *
 *     #^#, -#, #/#, #*#, #-#, #+#, #<=#, #>=#, #==#, #!=#, #<#, #>#,
 *     $<=$, $>=$, $==$, $!=$, $<$, $>$, $in$, !b, b&&b, b||b, b?#:#
 *
 * @author Steve Sinclair
 * @version $Id: StringCalculator.java,v 2.3 2004/12/14 02:26:07 dave_hoag Exp $
 */
public class StringCalculator
{
	/**************************************************************************
	 *     Private Section:
	 */

	protected boolean verbose = (System.getProperty("ow.stringCalcVerbose")!=null);
	protected Object result = null;
	protected static CalcItem staticCalcItem = null;
	/**
	 * Parse the symbols from the string, convert their ordering for easier
	 * execution, and execute the expression.
	 * Note that there's three types of exception thrown by this method.
	 */
	public Object evaluateExpression(String expression)
		throws java.lang.ArithmeticException,
			   java.text.ParseException,
			   java.util.EmptyStackException
	{
		ArrayList symbols = createSymbols(expression); // build symbols ArrayList
		symbols = convertToReversePolish(symbols); // reorder symbols ArrayList
		if (verbose)
		{
			System.out.println("Reverse-polish ordering:");
			dumpSymbols(symbols);
		}
		return executeExpression(symbols); // "execute" reordered symbols ArrayList
	}
	/**
	 */
	public StringCalculator()
	{
		if (staticCalcItem == null)
			staticCalcItem = new CalcItem();
	}
	/**
	 * A call to createSymbols must be made prior to calling this method.
	 * @see #createSymbols(java.lang.String)
	 */
	public ArrayList convertToReversePolish(ArrayList symbols) throws ParseException
	{
		if(symbols == null) throw new IllegalStateException("Symbols have not been provided. Call createSymbols(String) to get symbol list.");
		return convertToReversePolish(0, symbols.size(), symbols);
	}
	/**
	 */
	private ArrayList convertToReversePolish(int begin, int end, final ArrayList symbols) throws ParseException
	{
		return convertToReversePolish(begin, end, null, symbols);
	}
	/** Convert to reverse polish: make infix and prefix operations postfix.
	 *  For example, "-2" --> "2-", "1+2" --> "12+", "1+2*3" --> "123*+", and
	 *  "2*(6-7)" --> "2(67-)*".  Convert symbols[begin..end-1] while all operators
	 *  have a greater priority than op.
	 */
	private ArrayList convertToReversePolish(int begin, int end, String op, final ArrayList symbols)
		throws ParseException
	{
		if (verbose)
			System.out.println("Convert from index " + begin + " to " + (end-1) + ", op = " + op);
		ArrayList v = new ArrayList();
		for (int i = begin; i < end; ++i)
		{
			CalcItem curr = (CalcItem)symbols.get(i);
			if ((curr.type & staticCalcItem.TYPE_BRACKET) != 0 && curr.str.equals("("))
			{
				// (If processBrackets() fails, a ParseException will be thrown.)
				ArrayList v2 = processBrackets(i, end, symbols);
				v.add(symbols.get(i));
				for (Iterator e = v2.iterator(); e.hasNext(); )
					v.add(e.next());
				v.add(symbols.get(i + v2.size() + 1));
				i += v2.size() + 1; // skip parenthesized expression
			}
			else if ( (curr.type & staticCalcItem.TYPE_OPERATOR) != 0)
			{
				if (curr.str.equals("-"))
				{
					// Special case for "-": negation or subtraction?
					// If the lhs symbol is null, "(", number, or boolean,
					// then we assume a negation operation. Note that accessing
					// "prev" can be outside of the "begin..end" range of this method
					// call: it's a necessary evil.
					//
					if (i == 0)
						curr.str = "_";
					else
					{
						CalcItem prev = (CalcItem)symbols.get(i-1);
						if ((prev.isNum() && prev.isBoolean()) ||
							((prev.type & staticCalcItem.TYPE_BRACKET) != 0 && prev.str.equals("(") ))
						{
							curr.str = "_";
						}
					}
				}
				if (op != null && operatorComparison(op, curr.str) >= 0)
				{
					return v;
				}
				int newPos = i+1;
				if (newPos >=end)
				{
					v.add(curr);
					break; // we are finished
				}
				CalcItem next = (CalcItem)symbols.get(newPos);

				int booleanInversion = -1;
				if ((next.type & staticCalcItem.TYPE_OPERATOR) != 0 &&
						 next.str.equals("!"))
				{
					// We do allow "true && !false", so we must allow for a ! following an
					// operator.  (If the preceeding operator is not a boolean, then that's
					// ok: it'll get caught during the execution of the expression.)
					//
					booleanInversion = newPos;
					next = (CalcItem)symbols.get(++newPos);
					++i;
				}

				if ((next.type & staticCalcItem.TYPE_ATOMIC) != 0)
				{
					//
					// Simplest case: We find an atomic value after the operator.
					//
					v.add(next);
					++i; // skip "next"
				}
				else if ((next.type & staticCalcItem.TYPE_BRACKET) != 0 && next.str.equals("("))
				{
					ArrayList v2 = processBrackets(newPos, end, symbols);
					if (v2 == null)
						return null;
					v.add(next);
					for (Iterator e = v2.iterator(); e.hasNext(); )
						v.add(e.next());
					v.add(symbols.get(newPos + v2.size()+1));
					i = newPos + v2.size()+1; // skip parenthesized expression
				}
				else
				{
					throw new ParseException("Unexpected symbol follows operator", curr.pos);
				}

				//
				//   All expressions should be "[arg],op,arg,op,arg,op,...,arg", where an "arg"
				// is either a boolean, number, or a parenthesized expression.  Since we've just
				// completed processing an "op,arg" sequence, we must now be at the end of the
				// expression or at the next "op".
				//
				if (i+1 != end)
				{
					next = (CalcItem)symbols.get(i+1); // next operator, we hope
					if ((next.type & staticCalcItem.TYPE_OPERATOR) == 0)
					{
						throw new ParseException("Expected operator after symbol #" + (i+1) + ".", next.pos);
					}
					// on error, a ParseException will be thrown
					ArrayList v2 = convertToReversePolish(i+1, end, curr.str, symbols);
					for (Iterator e = v2.iterator(); e.hasNext(); )
						v.add(e.next());
					i += v2.size();
				}

				if (booleanInversion >= 0)
					v.add(symbols.get(booleanInversion));
				v.add(curr);
			}
			else
				v.add(curr);
		}
		return v;
	}
	/**
	 */
	public ArrayList createSymbols(String expression) throws ParseException
	{
		return createSymbols(expression, false);
	}
	/**
	 * @param defaultUnknown - If a symbol is unknown, or unresolvable, just assume it is a string.
	 */
	public ArrayList createSymbols(String expression, boolean defaultUnknown) throws ParseException
	{
		if (expression == null)
			throw new ParseException("Expression to parse is null!", -1);
		int curr = 0;
		ArrayList symbols = new ArrayList();
		for (;;)
		{
			curr = nextNonWhitespace(expression, curr);
			if (curr >= expression.length() || curr < 0)
				break;
			CalcItem item = null;
			int pos = curr;
			int len = prefixIsOperator(expression, curr);
			if (len >= 0)
			{
				if (len == 1 && expression.charAt(curr) == '_')
					throw new ParseException("Unrecognizable symbol \"_\" at index " + curr, curr);
				item = new CalcItem(expression.substring(curr, curr+len),
									 staticCalcItem.TYPE_OPERATOR);
				curr += len;
			}
			else if (expression.charAt(curr) == '(' || expression.charAt(curr) == ')')
			{
				item = new CalcItem("" + expression.charAt(curr),
									 staticCalcItem.TYPE_BRACKET);
				++curr;
			}
			else if (expression.substring(curr).startsWith("true"))
			{
				item = new CalcItem(true);
				curr += 4;
			}
			else if (expression.substring(curr).startsWith("false"))
			{
				item = new CalcItem(false);
				curr += 5;
			}
			else if (expression.substring(curr).startsWith("null"))
			{
				item = new CalcItem();
				item.setToNull();
				curr += 4;
			}
			else if (expression.substring(curr).startsWith("\""))
			{
				int stringEndPoint = expression.indexOf('\"', curr+1);
				if (stringEndPoint < 0)
				{
					throw new ParseException("No end found to string starting at index " + curr, curr);
				}
				item = new CalcItem(expression.substring(curr+1, stringEndPoint));
				curr = stringEndPoint+1;
			}
			else
			{
				ParseNumber pn = new ParseNumber(expression.substring(curr));
				pn.setAcceptNegatives(false);
				len = pn.parse();
				if (len < 0)
				{
					if(defaultUnknown)
					{
						int stringEndPoint = expression.indexOf(' ', curr+1);
						if (stringEndPoint < 0)
							stringEndPoint = expression.indexOf('&', curr+1);
						if (stringEndPoint < 0)
							stringEndPoint = expression.indexOf('|', curr+1);
						if (stringEndPoint < 0)
							stringEndPoint = expression.indexOf(')', curr+1);
						if (stringEndPoint < 0)
							stringEndPoint = expression.length();

						item = new CalcItem(expression.substring(curr, stringEndPoint));
						curr = stringEndPoint+1;
					}
					else
					{
						throw new ParseException("Unrecognizable symbol at index " + curr, curr);
					}
				}
				else
				{
					item = new CalcItem(pn.getNumber());
					curr += len;
				}
			}
			item.pos = pos;
			symbols.add(item);
		}
		if (verbose)
		{
			System.out.println("\"virgin\" symbols:");
			dumpSymbols(symbols);
			shortDump(symbols);
		}
		return symbols;
	}
	/**
	 * Used for debugging.
	 */
	public void dumpSymbols(ArrayList v)
	{
		dumpSymbols(v, 0, v==null ?0:v.size());
	}
	/**
	 * Used for debugging.
	 */
	public void dumpSymbols(ArrayList v, int begin, int end)
	{
		if (v == null)
			System.out.println(v);
		else
		{
			System.out.println("#symbols = " + v.size());
			for (int i=begin; i < Math.min(end,v.size()); ++i)
				System.out.println((CalcItem)v.get(i));
		}
	}
	/**
	 *  Assuming that the "symbols" ArrayList is a reverse-polish ordered sequence
	 *  of CalcItem objects, evaluate the expression using a simple stack.
	 */
	public Object executeExpression(ArrayList symbols)
		throws ArithmeticException, ParseException, EmptyStackException
	{
		if (verbose)
		{
			System.out.println("execute:");
			shortDump(symbols);
		}
		Stack stack = new Stack();
		String err = null;
		if (symbols == null)
			throw new ArithmeticException("Cannot execute a null expression.");

		for (Iterator e = symbols.iterator(); e.hasNext(); )
		{
			CalcItem curr = (CalcItem)e.next();
			if ((curr.type & staticCalcItem.TYPE_OPERATOR) != 0)
			{
				performOperation(curr, stack);
			}
			else
			if ((curr.type & staticCalcItem.TYPE_BRACKET) != 0  &&
					 curr.str.equals(")"))
			{
				if (stack.size() < 2)
				{
					err = "parentheses error: stack too small";
					break;
				}
				CalcItem value = (CalcItem)stack.pop();
				if ((value.type & staticCalcItem.TYPE_ATOMIC) == 0)
				{
					err = "parentheses error: didn't resolve body to an atomic value";
					break;
				}
				CalcItem top = (CalcItem)stack.pop();
				if ((top.type & staticCalcItem.TYPE_BRACKET) != 0 &&
					top.str.equals("("))
				{
					stack.push(value);
				}
				else
				{
					err = "parentheses error: didn't find expected left-parentheses.";
					break;
				}
			}
			else
			{
				stack.push(curr);
			}
		}

		CalcItem topItem = (CalcItem)stack.peek();
		if (err != null)
			throw new ArithmeticException("Error executing expression: " + err);
		if (stack.size() != 1)
			throw new ArithmeticException("Result error: stack size " + stack.size() + " != 1");
		else
		if ((topItem.type & staticCalcItem.TYPE_ATOMIC) == 0)
			throw new ArithmeticException("Result error: final stack item is not an atomic type");

		result = (topItem.isNum())
						? (Object)(new Double(topItem.num) )
						: (new Integer(topItem.bool ? 1 : 0));

		return getResult();
	}
	/**
	 */
	public Object getResult()
	{
		return result;
	}
	/**
	 */
	public static void main(String args[])
	{
		StringCalculator calc = new StringCalculator();
		try
		{
			ArrayList v = calc.createSymbols(args[0], true);
			v = calc.convertToReversePolish(v);
//			calc.dumpSymbols(v);
//			calc.shortDump(v);
			int count = 0;
			for(int i = 0; i < v.size(); ++i)
			{
				CalcItem item = (CalcItem)v.get(i);
				if(item.isOperator())
				{
					if(" && || ".indexOf(item.valueStr()) < 0)
					{
						item.str = "&&";
					}
				}
				else
				if(item.isNum())
				{
					item.type = item.TYPE_ATOMIC|item.TYPE_BOOLEAN;
					item.bool = true;
				}
				else
				if(item.isString() && item.isAtomic())
				{
					count++;
				}
			}
//			calc.shortDump(v);
			if(count == 0)
			{
				System.out.println("RESULT " + calc.executeExpression(v).equals(new Integer(1)));
				return;
			}
			ArrayList result = calc.getTrueResults(v);
			System.out.println("there are " + result.size() + " ways to reach true");
			for(int i = 0; i < result.size(); ++i)
			{
				System.out.println(result.get(i));
			}
		}
		catch (java.text.ParseException ex)
		{
			ex.printStackTrace();
		}
	}
	/**
	 * Find all of the permutations that will make the symbols evalutate to true.
	 *
	 * @param symbols ArrayList of CalcItems, should have been converted to reverse polish notation prior to calling this method.
	 */
	public ArrayList getTrueResults(ArrayList symbols)
	{
		return  getResults(this, symbols, null, true);
	}
	/**
	 * Find all of the permutations that will make the symbols evalutate to false.
	 *
	 * @param symbols ArrayList of CalcItems, should have been converted to reverse polish notation prior to calling this method.
	 */
	public ArrayList getFalseResults(ArrayList symbols)
	{
		return  getResults(this, symbols, null, false);
	}
	/**
	 * @param symbols ArrayList of CalcItems, should have been converted to reverse polish notation prior to calling this method.
	 * @param calc - The calculator that was/will be used to evaluate the expression.
	 * @param settings ArrayList - A ArrayList used internally to keep track of the value list.
	 * @param condition boolean -  Get the patterns that make the condition evaluate to this value.
	 * @return ArrayList Each element in the ArrayList is the string of conditions that would make the expression
	 * evaluate to true.
	 */
	protected static ArrayList getResults(StringCalculator calc, ArrayList symbols, ArrayList settings, boolean condition)
	{
		ArrayList result = new ArrayList();
		if(settings == null)
		{
			settings = new ArrayList();
		}
		int count = 0;
		symbols = (ArrayList)symbols.clone();
		int origIdx = -1;
		CalcItem orig = null;
		for(int i = 0; i < symbols.size(); ++i)
		{
			CalcItem item = (CalcItem)symbols.get(i);
			if(item.isString() && item.isAtomic())
			{
				origIdx = i;
				orig = item;
				break;
			}
		}
		if(origIdx < 0) //We have a complete expression set.
		{
			try
			{
				int trueOrFalse = ((Integer)calc.executeExpression(symbols)).intValue();
				if((trueOrFalse == 1) == condition)
				{
					String str = "";
					for(int i = 0; i < settings.size(); ++i)
					{
						str += settings.get(i).toString() + ' ';
					}
					result.add(str);
					return result;
				}
				return null;
			}
			catch(java.text.ParseException ex)
			{
				System.out.println(ex);
				calc.shortDump(symbols);
				return null;
			}
		}

		symbols.set(origIdx, calc.new CalcItem(true));
		settings.add(orig.valueStr() + " = true");

		ArrayList subResult= getResults(calc, symbols, settings, condition);

		if(subResult != null)
		{
			result.addAll(subResult);
		}
		settings.remove(settings.size() - 1 );
		settings.add(orig.valueStr() + " = false");
		symbols.set(origIdx, calc.new CalcItem(false));

		subResult = getResults(calc, symbols, settings, condition);
		if(subResult != null)
		{
			result.addAll(subResult);
		}

		settings.remove(settings.size() - 1 );
		return result;
	}
	/**
	 */
	public static void main2(String args[])
	{
		System.out.println("Test stringified expression calculator: ");
		boolean setVerbose = false;
		if (args.length != 1)
		{
			if (args.length == 2 && args[1].equalsIgnoreCase("verbose"))
				setVerbose = true;
			else
			{
				System.out.println("Expected argument: <string_to_parse>");
				return;
			}
		}
		StringCalculator calc = new StringCalculator();
		if (setVerbose)
			calc.verbose = true; // I can do this since I can access private data.
		System.out.println("Original String: \"" + args[0] + "\"");
		try
		{
			calc.evaluateExpression(args[0]);
			if (calc.resultIsNumeric())
				System.out.println("Expression result = " + calc.getResult());
			else
				System.out.println("Expression result = " + // boolean result:
									( ((Integer)calc.getResult()).intValue() == 0
												? false : true) );
		}
		catch (java.text.ParseException e)
		{ System.out.println("Parse exception near offset " + e.getErrorOffset() + ": " + e); }
		catch (java.lang.ArithmeticException e)
		{ System.out.println("Arithmetic exception: " + e); }
		catch (java.util.EmptyStackException e)
		{ System.out.println("Stack exception: " + e); }
	}
	/** Assuming that symbols[idx] is a "(" symbol, return the index of it's matching
	 *  rhs symbol.
	 */
	protected int matchBracket(int idx, final ArrayList symbols)
	{
		if (symbols == null || idx >= symbols.size()-1)
			return -1;
		int i;
		for (i = idx+1; i < symbols.size(); ++i)
		{
			CalcItem item = (CalcItem)symbols.get(i);
			if ((item.type & staticCalcItem.TYPE_BRACKET) != 0)
			{
				if (item.str.equals(")"))
					return i;
				if (item.str.equals("(")) // nested brackets: recurse
				{
					i = matchBracket(i, symbols);
					if (i < 0)
						return -1;
				}
			}
		}
		return (i == symbols.size() ? -1 : i);
	}
	/** Returns str.length() if EOS is reached.
	 */
	protected int nextNonWhitespace(String str, int idx)
	{
		String ws = " \r\n\t";
		if (idx >= str.length())
			return str.length();
		for (int i=idx; i<str.length(); ++i)
			if (ws.indexOf(str.charAt(i)) < 0)
				return i;
		return str.length();
	}
	/** Return -1 if opA is less tightly bound the opB, 0 is they're the same, else 1.
	 */
	private int operatorComparison(String opA, String opB)
	{
		return (operatorPriority(opA) < operatorPriority(opB))
					? -1 : (operatorPriority(opA) == operatorPriority(opB)
							  ? 0 : 1);
	}
	/**
	 *  Return the priority of the given operation "op".  "op" is assumed to be
	 *  a valid operation.
	 */
	protected int operatorPriority(String op)
	{
		if (op.length() < 1 || op.length() > 2) return -1;
		switch (op.charAt(0))
		{
			case '^': return 120; // (exponent)
			case '_': return 110; // (negation)
			case '/': return 100;
			case '*': return 90;
			case '-': return 80;
			case '+': return 70;
			case '<':
			case '>': if (op.length()==2 && op.charAt(1) != '=') return -1;
			case '=': return 60; // all comparison operators have equivalent priority
			case 'i': if (op.length()==2 && op.charAt(1) != 'n') return -1; // "in" is an operator
			case '!': return (op.length()==2 && op.charAt(1) == '=') ? 60 : 50; // "!" or "!="
			case '&': return 40;
			case '|': return 30;
			case '?':
			case ':': return 20; // both used in the operation "bool ? num : num"
			default: return -1;
		}
	}
	protected void performOperation(CalcItem operation, Stack expressionStack)
		throws ArithmeticException, ParseException
	{
		String op = operation.str;
		String binaryArithmetic = "^ + - / * < > == <= >= != in ";
		String unaryArithmetic  = "_ "; // negate: substituted for "-" during reversePolish conversion
		String binaryBoolean = "&& || ";
		String unaryBoolean = "! ";
		CalcItem arg1, arg2 = null, arg3 = null;
		CalcItem result = new CalcItem();
		result.type = staticCalcItem.TYPE_ATOMIC;

		if (verbose)
			System.out.println("Perform operation " + operation);

		if ((binaryArithmetic+unaryArithmetic+binaryBoolean+unaryBoolean+"? :").indexOf(op) < 0)
		{
			throw new ParseException("Unknown operator \"" + op + "\".", operation.pos);
		}

		arg1 = (CalcItem)expressionStack.pop();

		// We've gotta treat "?" and ":" as special-case operations since their argument types
		// are weird: "?" expects bool,num and ":" expects bool,num,num.
		//
		if (op.equals("?"))
		{
			arg2 = (CalcItem)expressionStack.pop();
			if (!arg1.isNum() || !arg2.isBoolean())
			{
				throw new ArithmeticException("Wrong operand types for \"?\": expected \"bool?num:num\".");
			}
			// Don't do anything else: the actual functionality is accomplished by the ":" operator.
			expressionStack.push(arg2);
			expressionStack.push(arg1);
			return;
		}
		if (op.equals(":"))
		{
			arg2 = (CalcItem)expressionStack.pop();
			arg3 = (CalcItem)expressionStack.pop();
			if (!arg1.isNum() || !arg2.isNum() || !arg3.isBoolean())
			{
				throw new ArithmeticException("Wrong operand types for \"?\": expected \"bool?num:num\".");
			}
			expressionStack.push(arg3.bool ? arg2 : arg1);
			return;
		}

		if (binaryBoolean.indexOf(op) >= 0 ||
			(binaryArithmetic.indexOf(op) >= 0 && !op.equals("!")) )
		{
			arg2 = (CalcItem)expressionStack.pop();
		}

		// "==" and "!=" are special cases: they must check for "null" comparisons as well
		// as numeric comparisons and string comparisons.
		//
		if ("==".equals(op) || "!=".equals(op))
		{
			boolean equals = ("==".indexOf(op) >= 0);
			result.type |= staticCalcItem.TYPE_BOOLEAN;
			boolean arg1isNull = (arg1.type & staticCalcItem.TYPE_NULL) != 0;
			boolean arg2isNull = (arg2.type & staticCalcItem.TYPE_NULL) != 0;
			if (arg1isNull || arg2isNull)
			{
				result.bool = !(equals ^ (arg1isNull && arg2isNull));
			}
			else if ( arg1.isNum() && arg2.isNum() )
			{
				result.bool = !(equals ^ (arg2.num == arg1.num));
			}
			else if (arg1.isString() && arg2.isString())
			{
				result.bool = !(equals ^ arg1.str.equals(arg2.str));
			}
			else
				throw new ArithmeticException("Operator \"==\" expected two numeric operands or at least one \"null\" operand.");
		}
		else if (binaryArithmetic.indexOf(op) >= 0 && !op.equals("!"))
		{
			if ("^ + - / *".indexOf(op) >= 0)
			{
				if (!arg1.isNum() || !arg2.isNum())
				{
					throw new ArithmeticException("Operator \"" + op + "\" expected two numeric operands.");
				}
				result.type |= staticCalcItem.TYPE_NUMBER;
			}
			else if ("<= >= < > in".indexOf(op) >= 0)
			{
				// If both ops aren't numbers and both ops aren't strings:
				//
				if (!( (arg1.isNum() && arg2.isNum()) || (arg1.isString() && arg2.isString())))
				{
					throw new ArithmeticException("Operator \"" + op + "\" expected two numeric or two string operands.");
				}
				result.type |= staticCalcItem.TYPE_BOOLEAN;
			}
			else
				throw new ArithmeticException("Internal error: \"" + op + "\" is not one of ^,+,-,*,/,<,>,<=,>=,==,!=,in");

			if (op.equals("+"))
				result.num = arg2.num + arg1.num;
			else if (op.equals("^"))
				result.num = Math.pow(arg2.num, arg1.num);
			else if (op.equals("-"))
				result.num = arg2.num - arg1.num;
			else if (op.equals("*"))
				result.num = arg2.num * arg1.num;
			else if (op.equals("/"))
			{
				if (arg1.num == 0)
					throw new ArithmeticException("Attempted division by 0.");
				result.num = arg2.num / arg1.num;
			}
			else if (op.equals("<"))
				result.bool = arg1.isNum() ? (arg2.num < arg1.num) : arg2.str.compareTo(arg1.str)<0;
			else if (op.equals(">"))
				result.bool = arg1.isNum() ? (arg2.num > arg1.num) : arg2.str.compareTo(arg1.str)>0;
			else if (op.equals("<="))
				result.bool = arg1.isNum() ? (arg2.num <= arg1.num) : arg2.str.compareTo(arg1.str)<=0;
			else if (op.equals(">="))
				result.bool = arg1.isNum() ? (arg2.num >= arg1.num) : arg2.str.compareTo(arg1.str)>=0;
			else if (op.equals("in"))
			{
				if (arg1.isNum())
					throw new ArithmeticException("Operator \"in\" only takes string operands.");
				result.bool = false;
				for (int i=0; i > -1 && i < arg1.str.length(); )
				{
					int eoss = arg1.str.indexOf(',', i);
					if (eoss < 0)
						eoss = arg1.str.length();
					String substr = arg1.str.substring(i, eoss);
					if (arg2.str.equals(substr))
					{
						result.bool = true;
						break;
					}
					i = ++eoss;
				}
			}
		}
		else if (unaryArithmetic.indexOf(op) >= 0)
		{
			if (!arg1.isNum())
				throw new ArithmeticException("Operator \"" + op + "\" expected a numeric operand.");
			result.type |= staticCalcItem.TYPE_NUMBER;
			if (op.equals("_"))
				result.num = -(arg1.num);
		}
		else if (binaryBoolean.indexOf(op) >= 0)
		{
			// expect two boolean operands
			if (!arg1.isBoolean() || !arg2.isBoolean())
			{
				throw new ArithmeticException("Operator \"" + op + "\" expected two boolean operands.");
			}
			result.type |= staticCalcItem.TYPE_BOOLEAN;
			if (op.equals("&&"))
				result.bool = (arg1.bool && arg2.bool);
			else if (op.equals("||"))
				result.bool = (arg1.bool || arg2.bool);
		}
		else if (unaryBoolean.indexOf(op) >= 0)
		{
			// expect one boolean operand
			if (!arg1.isBoolean())
				throw new ArithmeticException("Operator \"" + op + "\" expected a boolean operand.");
			result.type |= staticCalcItem.TYPE_BOOLEAN;
			if (op.equals("!"))
				result.bool = !(arg1.bool);
		}
		expressionStack.push(result);
	}
	/** Return # of chars which are the operator, else 0.
	 */
	protected int prefixIsOperator(String string, int idx)
	{
		String s = string.substring(idx);
		String len2 = "&& || <= >= == != in";
		String len1 = "^ + - * / ! < > _ ? :";
		if (s.length() >= 2 && len2.indexOf(s.substring(0, 2)) >= 0)
		{
			// We must look for "<=" and ">=" _before_ "<" or ">", otherwise there'll be
			// parsing errors.
			//
			return 2;
		}
		if (s.length() >= 1 && len1.indexOf(s.substring(0, 1)) >= 0)
		{
			return 1;
		}
		return -1;
	}
	/**
	* We've found a "(" in place of an atomic value: that's ok, so long as it has
	* a matching ")" with parsable contents in between.
	*/
	protected ArrayList processBrackets(int leftParen, int end, final ArrayList symbols) throws ParseException
	{
		int here = matchBracket(leftParen, symbols);
		int pos = ((CalcItem)symbols.get(leftParen)).pos;
		if (here < 0 || here >= end)
		{
			throw new ParseException("Failed to match bracket beginning at symbol #" + leftParen + ".", pos);
		}
		if (here == leftParen+1)
		{
			throw new ParseException("Empty parentheses are illegal (at symbol #" + leftParen + ").", pos);
		}
		if (verbose)
			System.out.println("Matched bracket indexed " + leftParen + "," + here);
		ArrayList v = convertToReversePolish(leftParen+1, here, symbols);
		if (v == null)
		{
			throw new ParseException("Failed to convert contents of parentheses at symbol #"+leftParen+".", pos);
		}
		return v;
	}
	/** */
	public boolean resultIsNumeric()
	{
		if (result == null)
			return false;
		return Double.class.isInstance(result);
	}
	/** */
	public void shortDump(ArrayList v)
	{
		String str = "[ ";
		if (v == null)
			str += "null";
		else
			for (int i=0; i < v.size(); ++i)
				str += ((CalcItem)v.get(i)).valueStr() + " ";
		System.out.println(str+"]");
	}
	/**
	 * An abstraction of the symbols found within an expression string.
	 *
	 */
	public class CalcItem
	{
		public final int TYPE_STRING   = 0x01;
		public final int TYPE_OPERATOR = 0x02;
		public final int TYPE_BRACKET  = 0x04;
		public final int TYPE_NUMBER   = 0x08;
		public final int TYPE_BOOLEAN  = 0x10;
		public final int TYPE_ATOMIC   = 0x20;
		public final int TYPE_NULL     = 0x40;
		public String  str  = "";
		public boolean bool = false;
		public double  num  = -1;
		public int     type = 0;
		public int     pos  = -1; // index of 1st char of item in the expression

		CalcItem()                { type = 0; }
		CalcItem(String s)        { type = TYPE_STRING|TYPE_ATOMIC; str = s; }
		CalcItem(String s, int t) { type = TYPE_STRING|t; str = s; }
		CalcItem(boolean b)       { type = TYPE_ATOMIC|TYPE_BOOLEAN; bool = b; }
		CalcItem(double  d)       { type = TYPE_ATOMIC|TYPE_NUMBER;  num = d; }

		public void setToNull()
		{
			type = (TYPE_ATOMIC|TYPE_NULL);
		}

		public String toString()
		{
			return "CalcItem(type=" + typeStr() + " value=" + valueStr() + " pos=" + pos + ")";
		}
		public boolean isNum()     { return ((type & TYPE_NUMBER)  != 0); }
		public boolean isString()  { return ((type & TYPE_STRING)  != 0); }
		public boolean isBoolean() { return ((type & TYPE_BOOLEAN) != 0); }
		public boolean isOperator() { return ((type & TYPE_OPERATOR) != 0); }
		public boolean isAtomic() { return ((type & TYPE_ATOMIC) != 0); }

		public String typeStr()
		{
			String s = "";
			if ((type & TYPE_ATOMIC)   != 0) { s += "Atomic"; }
			if ((type & TYPE_STRING)   != 0) { s += "String"; }
			if ((type & TYPE_OPERATOR) != 0) { s += "Operator"; }
			if ((type & TYPE_BRACKET)  != 0) { s += "Bracket"; }
			if ((type & TYPE_NUMBER)   != 0) { s += "Number";  }
			if ((type & TYPE_BOOLEAN)  != 0) { s += "Boolean"; }
			if ((type & TYPE_NULL)     != 0) { s += "Null"; }
			return s;
		}
		public String valueStr()
		{
			String s = (str==null) ?"":str;
			if ((type & TYPE_NUMBER)   != 0) { s = ""+num; }
			if ((type & TYPE_BOOLEAN)  != 0) { s = ""+bool; }
			if ((type & TYPE_NULL)     != 0) { s = "null"; }
			return s;
		}
	}
	/**
	 * Parse a string to a double.  More specifically, parse the longest prefix of
	 * the given string that still yields a valid number.
	 */
	protected class ParseNumber
	{
		String str;
		int    idx = -1;
		char   c;
		double result;
		boolean acceptNegatives = true;

		public double getNumber()        { return result; }
		public ParseNumber()             { str = null; }
		public ParseNumber(String _s)    { str = _s; }
		public void setString(String _s) { str = _s; idx = -1; result = 0.0; }
		public void setAcceptNegatives(boolean n) { acceptNegatives = n; }
		public boolean getAcceptNegatives()       { return acceptNegatives; }
		/**
		 * @return int Index where we stopped parsing the number.
		 */
		public int parse()
		{
			boolean is_fraction = false;
			boolean min_criteria = false;
			String digits = "0123456789";
			String parsed = "";
			String natural = "";
			String fraction = "";
			try
			{
				if (str == null) return -1;
				nextChar();
				if (acceptNegatives && c == '-')
				{ parsed = "-"; nextChar(); }
				if (digits.indexOf(c) >= 0) // identify the natural number
				{
					min_criteria = true;
					int begin = idx; // [xxx762] --> 4 (index of first digit)
					try
					{
						do
						{ nextChar(); }
						while (digits.indexOf(c) >= 0);
					}
					catch (EndOfString e)
					{
						natural = str.substring(begin);
						throw e;
					}
					natural = str.substring(begin, idx);
				}
				if (c == '.')
				{
					if (digits.indexOf(nextChar()) >= 0)
					{
						min_criteria = is_fraction = true;
						int begin = idx; // [xxx762] --> 4 (index of first digit)
						try
						{
							do
							{ nextChar(); }
							while (digits.indexOf(c) >= 0);
						}
						catch (EndOfString e)
						{
							fraction = str.substring(begin);
							throw e;
						}
						fraction = str.substring(begin, idx);
					}
				}
			}
			catch (EndOfString e)
			{
			}
			parsed += natural + (is_fraction?".":"") + fraction;
			if (min_criteria)
				result = (Double.valueOf(parsed)).doubleValue();

			return min_criteria ? idx : -1;
		}
		private char nextChar() throws EndOfString
		{
			if (str == null || ++idx >= str.length())
			{
				throw new EndOfString();
			}
			return (c = str.charAt(idx));
		}
		private class EndOfString extends Throwable
		{
			EndOfString() { super(); }
		}
	}
}

