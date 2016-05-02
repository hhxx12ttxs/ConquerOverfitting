package expFormula;
import java.util.*;

/** 
 * ExpFormula is a class that will allow you to input a mathematical formula
 * in text format and manipulate it as a mathematical expression.  The main features
 * of this class are the ability to input a mathematical expression as a string,
 * evaluate the expression, create arithmetic combinations of the expressions and 
 * compute symbolic derivatives and partial derivatives of the functions.  
 * This class uses the ComplexNumber
 * class to store all numeric constants hence all rational arithmetic is exact and
 * infinite precision.  This class uses a binary expression tree structure with 
 * nodes of ExpTreenode type to store and manipulate all expressions.  
 * The implementation of this 
 * class is encapsulated so that the user need never use the tree structure 
 * directly but we have included the methods getRoot() and setRoot() for those 
 * who wish to manipulate the binary tree structures.  If you are not familiar with 
 * binary expression trees you should not use these methods.  
 * 
 * <P>
 * The expressions should take the form of any real or complex valued function 
 * in up to 24 variables.  A variable is any single letter other then e or i since 
 * those are reserved for constants.  You may also use standard log, exponential, 
 * trigonometric and hyperbolic functions.  
 * The set of built-in functions are: ln(x), log(x), exp(x), sqrt(x), 
 * sin(x), cos(x), tan(x), cot(x), sec(x), csc(x), sinh(x), cosh(x), 
 * tanh(x), coth(x), sech(x), csch(x), asin(x), acos(x), atan(x), acot(x), 
 * asec(x), acsc(x), asinh(x), acosh(x), atanh(x), acoth(x), asech(x), 
 * acsch(x), abs(x), mod(x) (the modulus of x), arg(x) (the argument of x), 
 * sign(x) (the sign of a real number x, non-real arguments produce a syntax 
 * error), zeron(x) (returns 0 for any non-zero real number x, non-real 
 * arguments or 0 produce a syntax error).
 * 
 * <P>
 * Arithmetic operations use the standard characters (+, -, *, / and ^) 
 * and juxtaposition is not supported. So for example, the expression 1/2 2/3 
 * would cause a syntax error but 1/2 * 2/3 would be simplified to 1/3.
 * 
 * <P>
 * It should be noted that this is not a computer algebra system (CAS) like Maxima, 
 * Maple or Mathematica.  It is a system that will do symbolic differentiation 
 * and evaluate expressions in either exact (for rational numbers) or approximate 
 * form.  So if you are creating an application where you need to process user 
 * input as a mathematical expression for evaluation or graphical display you will 
 * find this class of use.  On the other hand if you are looking for a system that 
 * will simplify general expressions or do symbolic integration you need to look 
 * elsewhere.
 * 
 * <P>
 * <B>Variable List:</B> a, b, c, d, f, g, h, j, k, l, m, n, o, p, q, r, s, t, 
 * u, v, w, x, y, z.  (any single letter that is not e or i)
 * 
 * <P>
 * <B>Constant List:</B> e (2.7182818284590452354...), pi (3.1415926535897932385) 
 * and i (the imaginary unit).
 * 
 * <P>
 * <B>Function List:</B>
 * 
 * <UL>
 * <LI>ln(z) - natural log, base e.
 * <LI>log(z) - natural log, base e.
 * <LI>log10(z) - common log, base 10.
 * <LI>exp(z) - e^z.
 * <LI>abs(z) - the absolute value of z, in the case of z complex it is the modulus. 
 * <LI>sign(z) - the sign of the real part of z.
 * <LI>sqrt(z) - primitive square root of z.
 * <LI>sin(z) - sine of z.
 * <LI>cos(z) - cosine of z.
 * <LI>tan(z) - tangent of z.
 * <LI>cot(z) - cotangent of z.
 * <LI>sec(z) - secant of z.
 * <LI>csc(z) - cosecant of z.
 * <LI>asin(z) - arcsine of z.
 * <LI>acos(z) - arccosine of z.
 * <LI>atan(z) - arctangent of z.
 * <LI>acot(z) - arccotangent of z.
 * <LI>asec(z) - arcsecant of z.
 * <LI>acsc(z) - arccosecant of z.
 * <LI>sinh(z) - hyperbolic sine of z.
 * <LI>cosh(z) - hyperbolic cosine of z.
 * <LI>tanh(z) - hyperbolic tangent of z.
 * <LI>coth(z) - hyperbolic cotangent of z.
 * <LI>sech(z) - hyperbolic secant of z.
 * <LI>csch(z) - hyperbolic cosecant of z.
 * <LI>asinh(z) - inverse hyperbolic sine of z.
 * <LI>acosh(z) - inverse hyperbolic cosine of z.
 * <LI>atanh(z) - inverse hyperbolic tangent of z.
 * <LI>acoth(z) - inverse hyperbolic cotangent of z.
 * <LI>asech(z) - inverse hyperbolic secant of z.
 * <LI>acsch(z) - inverse hyperbolic cosecant of z.
 * <LI>mod(z) - modulus of z.
 * <LI>arg(z) - argument of z.
 * <LI>carg(z) - argument of z.
 * </UL>
 * 
 * @author Don Spickler
 * @version 1.2
 */

public class ExpFormula implements Cloneable
{
	private final String Terminal = "abcdfghjklmnopqrstuvwxyz";
	private final String FctAlpha = "aceilmpstz";
	private final String NumStr = "1234567890.";
	private final String NumStrND = "1234567890";
	private final String Operator = "+-*/^";
	private final String AddOperator = "+-";
	private final String MultOperator = "*/";
	private final String PowOperator = "^";
	private final String Paren = "()";
	
	private boolean simplified = false;
	
	private int tokenpos = 0;
	private String token = "";
	private String expstr = "";
	private ExpTreenode root = null;

/**
 * Default constructor that produces an empty expression.
 */
	public ExpFormula() { }
	
/**
 * Constructor that takes as input a mathematical expression and parses it into
 * an expression tree. 	
 * @param str the mathematical expression.
 */
	public ExpFormula(String str) 
	{ 
		expstr = str.toLowerCase().trim() + "~~~~~";
		root = null;
		ProcessExpFormula();
	}

/**
 * Returns the root of the expression tree.
 * @return The root of the expression tree.
 */
	public ExpTreenode getRoot()
	{
		return root;
	}

/**
 * Sets the root of the expression tree to the input {@link ExpTreenode ExpTreenode}
 * node.
 * @param r the ExpTreenode node that is to be used as the root of the 
 * expression tree.
 */
	public void setRoot(ExpTreenode r)
	{
		root = r;
	}
	
	private String FindFct(String strtest)
	{
		ArrayList<Object> fcts = new ArrayList<Object>();
		fcts.add("ln"); fcts.add("log"); fcts.add("sin"); fcts.add("cos"); fcts.add("tan"); 
		fcts.add("cot"); fcts.add("sec"); fcts.add("csc"); fcts.add("e"); fcts.add("i"); 
		fcts.add("exp"); fcts.add("sinh"); fcts.add("cosh"); fcts.add("tanh"); fcts.add("pi"); 
		fcts.add("asin"); fcts.add("acos"); fcts.add("atan"); fcts.add("abs"); fcts.add("sign"); 
		fcts.add("zeron"); fcts.add("sech"); fcts.add("coth"); fcts.add("csch"); fcts.add("asec"); 
		fcts.add("acsc"); fcts.add("acot"); fcts.add("asinh"); fcts.add("acosh"); fcts.add("atanh"); 
		fcts.add("asech"); fcts.add("acsch"); fcts.add("acoth"); fcts.add("mod"); fcts.add("arg");
		fcts.add("carg"); fcts.add("sqrt"); fcts.add("log10");
		
		strtest.toLowerCase().trim(); 

		boolean found = false;
		for (int i = 0; i < fcts.size(); i++)
			if (strtest.equals((String)fcts.get(i)))
				found = true;

		if (found)
			return strtest;
		else	
			return "";
	}

	private String GetToken()
	{
		boolean tokenFound = false;
		String testchar = "";
		String ReturnString = "";

		while (expstr.substring(tokenpos, tokenpos+1).equals(" "))
			tokenpos++;

		testchar = expstr.substring(tokenpos, tokenpos+1).toLowerCase();

		if (NumStr.indexOf(testchar) >= 0)
		{
			int startpos = tokenpos; 
			boolean inStr = true;
			boolean onE = false;
			boolean onePastE = false;
			boolean pastE = false;
			while	(inStr)
			{
				inStr = false;
				String curChar = expstr.substring(tokenpos, tokenpos+1).toLowerCase();

				if (onePastE) pastE = true;
				if (onE) onePastE = true;
				
				if ((NumStr.indexOf(curChar) >= 0) && !(onE || pastE || onePastE))
					inStr = true;

				if (curChar.equals("e") && !onE)
				{
					inStr = true;
					onE = true;
				}

				if (((NumStrND.indexOf(curChar) >= 0) || (curChar.equals("-"))) && onePastE && !pastE)
					inStr = true;

				if ((NumStrND.indexOf(curChar) >= 0) && pastE)
					inStr = true;
					
				if (inStr) tokenpos++;	
			}	
			ReturnString = expstr.substring(startpos, tokenpos);
			tokenFound = true;
		}

		if ((FctAlpha.indexOf(testchar) >= 0) && (!tokenFound))
		{
			boolean FunctionFound = false; 
			for (int j = 6; j >= 1; j--)
			{
				String str = expstr.substring(tokenpos, tokenpos+j).toLowerCase();
				if (!FunctionFound && !FindFct(str).equals(""))
				{
					FunctionFound = true;
					ReturnString = str;
					tokenpos += str.length();
					tokenFound = true;
				}
			}			
		}
		
		if ((Terminal.indexOf(testchar) >= 0) && (!tokenFound))
		{
			tokenpos++;	
			ReturnString = testchar;
			tokenFound = true;
		}
		
		if ((Operator.indexOf(testchar) >= 0) && (!tokenFound))
		{
			tokenpos++;	
			ReturnString = testchar;
			tokenFound = true;
		}
		
		if ((Paren.indexOf(testchar) >= 0) && (!tokenFound))
		{
			tokenpos++;	
			ReturnString = testchar;
			tokenFound = true;
		}
		
		if ((testchar.equals("~")) && (!tokenFound))
		{
			tokenpos++;	
			ReturnString = testchar;
			tokenFound = true;
		}

		if (tokenFound)
			return ReturnString.trim().toLowerCase();
		else
			throw new SyntaxErrorException();
	}

/**
 * Replaces the current expression with str.
 * @param str the mathematical expression.
 */
	public void ParseFct(String str)
	{
		expstr = str.toLowerCase().trim() + "~~~~~";		
		root = null;
		ProcessExpFormula();
	}
	
	private void ProcessExpFormula()
	{
		try
		{
			presyntaxcheck(expstr);
			token = GetToken();
			root = FctExpression();
			if (!token.equals("~")) throw new SyntaxErrorException();
		}
		catch (Exception e)
		{
			root = null;
			throw new SyntaxErrorException();
		}
	}

	private ExpTreenode FctExpression()
	{
		ExpTreenode retnode = null;
		
		if (!token.equals("-"))
			retnode = FctTerm();
		else
		{
			token = GetToken();
			ExpTreenode rightnode = FctTerm();
			retnode = new ExpTreenode("-", null, null, rightnode);
		}
		
		while (token.equals("+") || token.equals("-"))
		{
			String temptoken = token;
			token = GetToken();
			ExpTreenode rightnode = FctTerm();
			retnode = new ExpTreenode(temptoken, null, retnode, rightnode);
		}
		return retnode;
	}

	private ExpTreenode FctTerm()
	{
		ExpTreenode retnode = FctFactor();
		
		while (token.equals("*") || token.equals("/"))
		{
			String temptoken = token;
			token = GetToken();
			ExpTreenode rightnode = FctFactor();
			retnode = new ExpTreenode(temptoken, null, retnode, rightnode);
		}
		return retnode;
	}

	private ExpTreenode FctFactor()
	{
		ExpTreenode retnode = FctPower();
		
		while (token.equals("^"))
		{
			token = GetToken();
			ExpTreenode rightnode = FctPower();
			retnode = new ExpTreenode("^", true, null, retnode, rightnode);
			retnode = adjustExpons(retnode);
		}
		return retnode;
	}

	private ExpTreenode FctPower()
	{
		ExpTreenode retnode = null;
		
		if (token.equals("e"))
		{
			retnode = new ExpTreenode(new ComplexNumber(new NumberValue(Math.E)));
			token = GetToken();
		}
		else if (token.equals("pi"))
		{
			retnode = new ExpTreenode(new ComplexNumber(new NumberValue(Math.PI)));
			token = GetToken();
		}
		else if (token.equals("i"))
		{
			retnode = new ExpTreenode(new ComplexNumber(NumberValue.makeOne(), false));
			token = GetToken();
		}
		else if (token.equals("("))
		{
			token = GetToken();
			retnode = FctExpression();
			if (token.equals(")"))
				token = GetToken();
			else
				throw new SyntaxErrorException();
		}
		else if (!FindFct(token).equals(""))
		{
			String temptoken = token;
			token = GetToken();
			if (token.equals("("))
			{
				token = GetToken();
				retnode = FctExpression();
				if (token.equals(")"))
					token = GetToken();
				else
					throw new SyntaxErrorException();
				
				retnode = new ExpTreenode(temptoken, null, null, retnode);
			}
			else
				throw new SyntaxErrorException();
		}
		else if (Terminal.indexOf(token) >= 0)
		{
			retnode = new ExpTreenode(token);
			token = GetToken();
		}
		else if (NumStr.indexOf(token.substring(0, 1)) >= 0)
		{
			retnode = new ExpTreenode("#", ProcessNumber(token), null, null);
			token = GetToken();
		}		
		else
			throw new SyntaxErrorException();
		
		removeExpons(retnode);
		return retnode;
	}

	private void removeExpons(ExpTreenode root)
	{
		if (root == null) return;
		root.expon = false;
		removeExpons(root.left);
		removeExpons(root.right);
	}
	
	private ExpTreenode adjustExpons(ExpTreenode root)
	{
		if (root == null) return null;
		
		try{
			if (root.op_str.equalsIgnoreCase("^") && root.expon)
			{
				if (root.left != null)  //  Should always be true
				{
					ExpTreenode L = root.left;
					if (L.op_str.equalsIgnoreCase("^") && L.expon)
					{
						ExpTreenode LR = L.right;
						L.right = root;
						root.left = LR;
						root = L;
					}
				}
			}
			
		}catch (Exception e)
		{
			throw new SyntaxErrorException();
		}

		root.left = adjustExpons(root.left);
		root.right = adjustExpons(root.right);
		
		return root; 
	}
	
/**
 * Given a complex number as a string this method returns a ComplexNumber object
 * storing the given number.
 * @param str the complex number string.
 * @return The complex number in a ComplexNumber object.
 */
	public ComplexNumber ProcessNumber(String str)
	{
		str = str.trim();
		String newstr = "";
		boolean firstDecimalPoint = false;
		if (str.indexOf(".") >= 0)
		{
			for (int i = 0; i < str.length(); i++)
			{
				if (str.substring(i, i+1).equals("."))
				{
					if (!firstDecimalPoint)
					{
						firstDecimalPoint = true;
						newstr += str.substring(i, i+1);
					}
				}
				else
				{
					newstr += str.substring(i, i+1);
				}
			}
			str = newstr;
		}
		
		if ((str.indexOf("e") >= 0) || (str.indexOf(".") >= 0))
			str = "" + Double.valueOf(str);
		
		str = str.toLowerCase();
		
		NumberValue val;
		if ((str.indexOf(".") >= 0) || (str.indexOf("e") >= 0))
			val = new NumberValue(str);
		else
			val = new NumberValue(str, "1");
			
		return new ComplexNumber(val);
	}

	private void presyntaxcheck(String FormStr)  // String syntax check before processing
	{
		//  Check to see if there are two operators in a row.
		
		tokenpos = 0;
		token = GetToken();
		String nexttoken = "";
		
		while (!token.equals("~"))
		{
			nexttoken = GetToken();
			
			if ((Operator.indexOf(token) >= 0) && (Operator.indexOf(nexttoken) >= 0))
				throw new SyntaxErrorException();
				
			token = nexttoken;
		}
		
		tokenpos = 0;
	}

/**
 * Adds the current function to the function f and returns the result. 
 * @param f function to add to current function.
 * @return The result of adding the current function to f. 
 */
	public ExpFormula add(ExpFormula f)
	{
		return new ExpFormula("(" + toCodeString() + ")+(" + f.toCodeString() + ")");
	}

/**
 * Subtracts f from the current function and returns the result.
 * @param f function to subtract from the current function.
 * @return The result of subtracting f from the current function.
 */
	public ExpFormula subtract(ExpFormula f)
	{
		return new ExpFormula("(" + toCodeString() + ")-(" + f.toCodeString() + ")");
	}

/**
 * Returns the negative of the current function.	
 * @return The negative of the current function.
 */
	public ExpFormula negate()
	{
		return new ExpFormula("-(" + toCodeString() + ")");
	}

/**
 * Multiplies f with the current function and returns the result.
 * @param f function to multiply by the current function.
 * @return The result of multiplying f and the current function.
 */
	public ExpFormula multiply(ExpFormula f)
	{
		return new ExpFormula("(" + toCodeString() + ")*(" + f.toCodeString() + ")");
	}

/**
 * Divides f into the current function and returns the result.
 * @param f function to divide into the current function.
 * @return The result of dividing f into the current function.
 */
	public ExpFormula divide(ExpFormula f)
	{
		return new ExpFormula("(" + toCodeString() + ")/(" + f.toCodeString() + ")");
	}

/**
 * Raises f as a power to the current function and returns the result.
 * @param f function to be used as the exponent.
 * @return The result of raising the current function to the power of f.
 */
	public ExpFormula pow(ExpFormula f)
	{
		return new ExpFormula("(" + toCodeString() + ")^(" + f.toCodeString() + ")");
	}

/**
 * Clones the current function and returns the result as an ExpFormula object.
 * @return A copy of the current function.
 */
	public synchronized ExpFormula clone()
	{
		ExpFormula retstr = new ExpFormula();		
		retstr.root = (ExpTreenode)root.clone();
		return retstr;
	}	
	
/**
 * Determines if the current function and elt are equal. Note that no 
 * simplification is done in this check, the method simply determines if 
 * the two functions have the same expression tree. 
 * @param elt the function to be tested with the current function.
 * @return true if the two are equal and false otherwise.
 */
	public synchronized boolean equals(ExpFormula elt)
	{
		return root.equals(elt.root);
	}	

/**
 * Determines if the expression is a number or not.	
 * @return true if the expression represents a number and false otherwise.
 */
	public synchronized boolean isNumber()
	{
		return root.op_str.equals("#");
	}	

	private synchronized boolean isNumericTree(ExpTreenode t)
	{
		if (t == null) return true;
		if (Terminal.indexOf(t.op_str.toLowerCase()) >= 0) return false;
		return isNumericTree(t.left) && isNumericTree(t.right);
	}	

/**
 * Determines if the expression is zero or not.
 * @return true if the expression represents the number 0 and false otherwise.
 */
	public synchronized boolean isZero()
	{
		if (!isNumber()) return false;
		return root.num.isZero();
	}	

/**
 * Determines if the expression is zero or not, using the given NumericTolerances.
 * @param tol the given NumericTolerances
 * @return true if the expression represents zero under the constraints of the 
 * given NumericTolerances.
 */
	public synchronized boolean isZero(NumericTolerances tol)
	{
		if (!isNumber()) return false;
		return root.num.isZero(tol);
	}	

/**
 * Returns the complex number that the expression represents if it in fact represents
 * a number.  If it does not then null is returned.	
 * @return The complex number that the expression represents if it in fact represents
 * a number.  If it does not then null is returned.
 */
	public ComplexNumber getNumber()
	{
		if (!isNumber()) return null;
		return root.num.clone();
	}	

/**
 * Determines if the expression is a real valued function.	
 * @return true if the expression is a real valued function and false otherwise.
 */
	public boolean isRealExpression()
	{
		return isRealExpressionRec(root);
	}
	
	private boolean isRealExpressionRec(ExpTreenode t)
	{
		if (t == null) return true;
		if (t.op_str.equals("#"))
			return t.num.isReal();

		return isRealExpressionRec(t.left) && isRealExpressionRec(t.right);
	}

/**
 * Returns an ArrayList of strings, each string is a single character which is one
 * variable in the expression.  	
 * @return An ArrayList of strings, each string is a single character which 
 * is one variable in the expression.
 */
	public ArrayList<String> getVariableList()
	{
		ArrayList<String> retList = new ArrayList<String>();
		ArrayList<String> fullList = getVariableListRec(root);
		for (int i = 0; i < fullList.size(); i++)
		{
			boolean found = false;
			for (int j = 0; j < retList.size(); j++)
				if (((String)retList.get(j)).equalsIgnoreCase((String)fullList.get(i)))
				 found = true;

			if (!found)
				retList.add((String)fullList.get(i));
		}
		return retList;
	}

	private ArrayList<String> getVariableListRec(ExpTreenode CurOp)
	{
		ArrayList<String> AL = new ArrayList<String>();
		if (CurOp != null) 
		{
			if ((Terminal.indexOf(CurOp.op_str.substring(0, 1)) >= 0) && (CurOp.op_str.trim().length() == 1))
				AL.add(CurOp.op_str.trim());
			
			ArrayList<String> ALL = getVariableListRec(CurOp.left);
			ArrayList<String> ALR = getVariableListRec(CurOp.right);
			
			for (int i = 0; i < ALL.size(); i++)
				AL.add(((String)ALL.get(i)).trim());

			for (int i = 0; i < ALR.size(); i++)
				AL.add(((String)ALR.get(i)).trim());
		}
		return AL;
	}

	private ArrayList<String> getVariableList(ExpTreenode t)
	{
		ArrayList<String> retList = new ArrayList<String>();
		ArrayList<String> fullList = getVariableListRec(t);
		for (int i = 0; i < fullList.size(); i++)
		{
			boolean found = false;
			for (int j = 0; j < retList.size(); j++)
				if (((String)retList.get(j)).equalsIgnoreCase((String)fullList.get(i)))
					found = true;

			if (!found)
				retList.add((String)fullList.get(i));
		}
		return retList;
	}

	private String polynomialToLaTeXStringRec(ExpTreenode root, String var)
	{
		if (root == null) return "";
		
		if (root.op_str.equals("#"))
		{
			if (root.num.hasTwoTerms())
				return "("+root.num.toLaTeXString()+")";
			else
				return root.num.toLaTeXString();
		}
		else if (var.equalsIgnoreCase(root.op_str))
			return var;
		else if (isSinglePolyTerm(root, var))
		{
			if (root.op_str.equals("*"))
			{
				if ((root.left != null) && (root.right != null))
				{
					if (root.left.op_str.equals("#") && var.equalsIgnoreCase(root.right.op_str))
						if (root.left.num.hasTwoTerms())
							return "("+root.left.num.toLaTeXString()+")"+var;
						else
							return root.left.num.toLaTeXString()+var;
						
					if (root.right.op_str.equals("#") && var.equalsIgnoreCase(root.left.op_str))
						if (root.right.num.hasTwoTerms())
							return "("+root.right.num.toLaTeXString()+")"+var;
						else
							return root.right.num.toLaTeXString()+var;

					if (root.left.op_str.equals("#") && root.right.op_str.equals("^"))
						if (root.right.right.op_str.equals("#") && root.right.left.op_str.equalsIgnoreCase(var))
							if (root.left.num.hasTwoTerms())
								return "("+root.left.num.toLaTeXString()+")" + var + "^" + root.right.right.num.toLaTeXString();
							else
								return root.left.num.toLaTeXString() + var + "^" + root.right.right.num.toLaTeXString();
				
					if (root.right.op_str.equals("#") && root.left.op_str.equals("^"))
						if (root.left.right.op_str.equals("#") && root.left.left.op_str.equalsIgnoreCase(var))
							if (root.right.num.hasTwoTerms())
								return "("+root.right.num.toLaTeXString()+")" + var + "^" + root.left.right.num.toLaTeXString();
							else
								return root.right.num.toLaTeXString() + var + "^" + root.left.right.num.toLaTeXString();
				}
			}
			else if (root.op_str.equals("^"))
			{
				if ((root.left != null) && (root.right != null))
					if (root.right.op_str.equals("#") && root.left.op_str.equalsIgnoreCase(var))
						return var + "^" + root.right.num.toLaTeXString();
			}
		}
		else
			return polynomialToLaTeXStringRec(root.left, var) + " " + root.op_str + " " + polynomialToLaTeXStringRec(root.right, var);

		return "";  // should not happen
	}

	private String polynomialToStringRec(ExpTreenode root, String var)
	{
		if (root == null) return "";
		
		if (root.op_str.equals("#"))
		{
			if (root.num.hasTwoTerms())
				return "("+root.num.toString()+")";
			else
				return root.num.toString();
		}
		else if (var.equalsIgnoreCase(root.op_str))
			return var;
		else if (isSinglePolyTerm(root, var))
		{
			if (root.op_str.equals("*"))
			{
				if ((root.left != null) && (root.right != null))
				{
					if (root.left.op_str.equals("#") && var.equalsIgnoreCase(root.right.op_str))
						if (root.left.num.hasTwoTerms())
							return "("+root.left.num.toString()+")"+var;
						else
							return root.left.num.toString()+var;
						
					if (root.right.op_str.equals("#") && var.equalsIgnoreCase(root.left.op_str))
						if (root.right.num.hasTwoTerms())
							return "("+root.right.num.toString()+")"+var;
						else
							return root.right.num.toString()+var;

					if (root.left.op_str.equals("#") && root.right.op_str.equals("^"))
						if (root.right.right.op_str.equals("#") && root.right.left.op_str.equalsIgnoreCase(var))
							if (root.left.num.hasTwoTerms())
								return "("+root.left.num.toString()+")" + var + "^" + root.right.right.num.toString();
							else
								return root.left.num.toString() + var + "^" + root.right.right.num.toString();
				
					if (root.right.op_str.equals("#") && root.left.op_str.equals("^"))
						if (root.left.right.op_str.equals("#") && root.left.left.op_str.equalsIgnoreCase(var))
							if (root.right.num.hasTwoTerms())
								return "("+root.right.num.toString()+")" + var + "^" + root.left.right.num.toString();
							else
								return root.right.num.toString() + var + "^" + root.left.right.num.toString();
				}
			}
			else if (root.op_str.equals("^"))
			{
				if ((root.left != null) && (root.right != null))
					if (root.right.op_str.equals("#") && root.left.op_str.equalsIgnoreCase(var))
						return var + "^" + root.right.num.toString();
			}
		}
		else
			return polynomialToStringRec(root.left, var) + " " + root.op_str + " " + polynomialToStringRec(root.right, var);

		return "";  // should not happen
	}
	
/**
 * Returns a string representation of the function.  This string may not look
 * identical to the one used to input the function. 
 * @return A string representation of the function.
 */
	public String toString()
	{
		return toStringRec(root, "", false);
	}
	
	private String toStringRec(ExpTreenode root, String ParentOp, boolean rightside)
	{
		String leftstr = "";
		String rightstr = "";
		
		if (root == null) return "";

		ArrayList<String> varlist = getVariableList(root);
		boolean OneVar = (varlist.size() == 1);
		String var = "";
		if (OneVar)
			var = ((String)varlist.get(0)).toLowerCase();

		if ((!root.op_str.equals("#")) && OneVar && isPolynomialStandardForm(root, var) && (Terminal.indexOf(root.op_str) < 0))
		{
			boolean includeParens = false;

			if (!ParentOp.equals(""))
			{
				if (MultOperator.indexOf(ParentOp) >= 0)
					includeParens = true;

				if (PowOperator.indexOf(ParentOp) >= 0)
					includeParens = true;

				if (ParentOp.equals("-") && rightside)
					includeParens = true;
			}
				
			if (includeParens)
				return "(" + polynomialToStringRec(root, var) + ")";
			else
				return polynomialToStringRec(root, var);
		}
		
		if (Operator.indexOf(root.op_str) >= 0) 
		{
			leftstr = toStringRec(root.left, root.op_str, false);
			rightstr = toStringRec(root.right, root.op_str, true);

			boolean includeParens = false;
			if (!ParentOp.equals(""))
				if ((Operator.indexOf(root.op_str) >= 0) && (Operator.indexOf(ParentOp) >= 0))
				{
					if ((AddOperator.indexOf(root.op_str) >= 0) && (MultOperator.indexOf(ParentOp) >= 0))
						includeParens = true;
	
					if ((AddOperator.indexOf(root.op_str) >= 0) && (PowOperator.indexOf(ParentOp) >= 0))
						includeParens = true;
	
					if ((MultOperator.indexOf(root.op_str) >= 0) && (PowOperator.indexOf(ParentOp) >= 0))
						includeParens = true;

					if ((MultOperator.indexOf(root.op_str) >= 0) && (MultOperator.indexOf(ParentOp) >= 0))
						includeParens = true;

					if ((PowOperator.indexOf(root.op_str) >= 0) && (PowOperator.indexOf(ParentOp) >= 0))
						includeParens = true;
						
					if ((AddOperator.indexOf(root.op_str) >= 0) && ParentOp.equals("-") && rightside)
						includeParens = true;

					if (root.op_str.equals("-") && (root.left == null))
						includeParens = true;
				}
			
			if (includeParens)
				return "(" + leftstr + root.op_str + rightstr + ")";
			else
				return leftstr + root.op_str + rightstr;
		}			
		else if (Terminal.indexOf(root.op_str) >= 0)
		{
			return root.op_str;
		}
		else if (root.op_str.equals("e"))
		{
			return "e";
		}
		else if (root.op_str.equals("pi"))
		{
			return "pi";
		}
		else if (root.op_str.equals("i"))
		{
			return "i";
		}
		else if (root.op_str.equals("#"))
		{
			boolean includeParens = false;
			if (!ParentOp.equals(""))
			{
				if (Operator.indexOf(ParentOp) >= 0)
				{
					if (root.num.hasTwoTerms()) 
						includeParens = true;
					else if (root.num.hasNegativeFirstTerm())
						includeParens = true;
				}
				
				if (PowOperator.indexOf(ParentOp) >= 0)
				{
					if (!root.num.isRealInteger())
						includeParens = true;
				}
			}
			if (includeParens)
				return "(" + root.num.toString() + ")";
			else
				return root.num.toString();
		}
		else if (!FindFct(root.op_str).equals(""))
		{
			String retstr = "";
			rightstr = toStringRec(root.right, root.op_str, true);

			if (root.op_str.equals("exp")) 
				retstr = "e^("+ rightstr + ")";
			else if (root.op_str.equals("abs")) 
				retstr = "|"+ rightstr + "|";
			else if (root.op_str.equals("mod")) 
				retstr = "|"+ rightstr + "|";
			else
				retstr = root.op_str + "("+ rightstr + ")";

			return retstr;
		}
		
		return "";  //  Should not happen.
	}

/**
 * Returns a code string representation of the function.  This string can be used
 * as input for the parsers in this class. 
 * @return A code string representation of the function.
 */
	public String toCodeString()
	{
		return toCodeStringRec(root, "", false);
	}
	
	private String toCodeStringRec(ExpTreenode root, String ParentOp, boolean rightside)
	{
		String leftstr = "";
		String rightstr = "";
		
		if (root == null) return "";
		
		if (Operator.indexOf(root.op_str) >= 0) 
		{
			leftstr = toCodeStringRec(root.left, root.op_str, false);
			rightstr = toCodeStringRec(root.right, root.op_str, true);
			
			boolean includeParens = false;
			if (!ParentOp.equals(""))
				if ((Operator.indexOf(root.op_str) >= 0) && (Operator.indexOf(ParentOp) >= 0))
				{
					if ((AddOperator.indexOf(root.op_str) >= 0) && (MultOperator.indexOf(ParentOp) >= 0))
						includeParens = true;
	
					if ((AddOperator.indexOf(root.op_str) >= 0) && (PowOperator.indexOf(ParentOp) >= 0))
						includeParens = true;
	
					if ((MultOperator.indexOf(root.op_str) >= 0) && (PowOperator.indexOf(ParentOp) >= 0))
						includeParens = true;

					if ((MultOperator.indexOf(root.op_str) >= 0) && (MultOperator.indexOf(ParentOp) >= 0))
						includeParens = true;

					if ((PowOperator.indexOf(root.op_str) >= 0) && (PowOperator.indexOf(ParentOp) >= 0))
						includeParens = true;
						
					if ((AddOperator.indexOf(root.op_str) >= 0) && ParentOp.equals("-") && rightside)
						includeParens = true;

					if (root.op_str.equals("-") && (root.left == null))
						includeParens = true;
				}
			
			if (includeParens)
				return "(" + leftstr + root.op_str + rightstr + ")";
			else
				return leftstr + root.op_str + rightstr;
		}			
		else if (Terminal.indexOf(root.op_str) >= 0)
		{
			return root.op_str;
		}
		else if (root.op_str.equals("e"))
		{
			return "e";
		}
		else if (root.op_str.equals("pi"))
		{
			return "pi";
		}
		else if (root.op_str.equals("i"))
		{
			return "i";
		}
		else if (root.op_str.equals("#"))
		{
			boolean includeParens = false;
			if (!ParentOp.equals(""))
				if (Operator.indexOf(ParentOp) >= 0)
				{
					if (root.num.hasTwoTerms()) 
						includeParens = true;
					else if (root.num.hasNegativeFirstTerm())
						includeParens = true;
				}
				
				if (PowOperator.indexOf(ParentOp) >= 0)
				{
					if (!root.num.isRealInteger())
						includeParens = true;
				}
			
			if (includeParens)
				return "(" + root.num.toCodeString() + ")";
			else
				return root.num.toCodeString();
		}
		else if (!FindFct(root.op_str).equals(""))
		{
			rightstr = toCodeStringRec(root.right, root.op_str, true);
			return root.op_str + "(" + rightstr + ")";
		}
		return "";  //  Should not happen.
	}

/**
 * Returns a Maple string representation of the function. 
 * @return A Maple string representation of the function.
 */
	public String toMapleCodeString()
	{
		return toMapleCodeStringRec(root, "", false);
	}
	
	private String toMapleCodeStringRec(ExpTreenode root, String ParentOp, boolean rightside)
	{
		String leftstr = "";
		String rightstr = "";
		
		if (root == null) return "";
		
		if (Operator.indexOf(root.op_str) >= 0) 
		{
			leftstr = toMapleCodeStringRec(root.left, root.op_str, false);
			rightstr = toMapleCodeStringRec(root.right, root.op_str, true);
			
			boolean includeParens = false;
			if (!ParentOp.equals(""))
				if ((Operator.indexOf(root.op_str) >= 0) && (Operator.indexOf(ParentOp) >= 0))
				{
					if ((AddOperator.indexOf(root.op_str) >= 0) && (MultOperator.indexOf(ParentOp) >= 0))
						includeParens = true;
	
					if ((AddOperator.indexOf(root.op_str) >= 0) && (PowOperator.indexOf(ParentOp) >= 0))
						includeParens = true;
	
					if ((MultOperator.indexOf(root.op_str) >= 0) && (PowOperator.indexOf(ParentOp) >= 0))
						includeParens = true;

					if ((PowOperator.indexOf(root.op_str) >= 0) && (PowOperator.indexOf(ParentOp) >= 0))
						includeParens = true;
						
					if ((AddOperator.indexOf(root.op_str) >= 0) && ParentOp.equals("-") && rightside)
						includeParens = true;

					if (root.op_str.equals("-") && (root.left == null))
						includeParens = true;
				}
			
			if (includeParens)
				return "(" + leftstr + root.op_str + rightstr + ")";
			else
				return leftstr + root.op_str + rightstr;
		}			
		else if (Terminal.indexOf(root.op_str) >= 0)
		{
			return root.op_str;
		}
		else if (root.op_str.equals("e"))
		{
			return "exp(1)";
		}
		else if (root.op_str.equals("pi"))
		{
			return "Pi";
		}
		else if (root.op_str.equals("i"))
		{
			return "I";
		}
		else if (root.op_str.equals("#"))
		{
			return "(" + root.num.toMapleCodeString() + ")";
		}
		else if (!FindFct(root.op_str).equals(""))
		{
			rightstr = toMapleCodeStringRec(root.right, root.op_str, true);
			
			String fctStr = root.op_str;
			String MapleFctStr = fctStr;
			
			//  Change the ones that are not the same.
			
			if (fctStr.equals("log")) MapleFctStr = "ln";
			if (fctStr.equals("asin")) MapleFctStr = "arcsin";
			if (fctStr.equals("acos")) MapleFctStr = "arccos";
			if (fctStr.equals("atan")) MapleFctStr = "arctan";
			if (fctStr.equals("asec")) MapleFctStr = "arcsec";
			if (fctStr.equals("acsc")) MapleFctStr = "arccsc";
			if (fctStr.equals("acot")) MapleFctStr = "arccot";
			if (fctStr.equals("mod")) MapleFctStr = "abs";
			if (fctStr.equals("arg")) MapleFctStr = "argument";
			if (fctStr.equals("carg")) MapleFctStr = "argument";
			if (fctStr.equals("zeron")) MapleFctStr = "abs(2,";
			if (fctStr.equals("sign")) MapleFctStr = "abs(1,";
			if (fctStr.equals("asinh")) MapleFctStr = "arcsinh";
			if (fctStr.equals("acosh")) MapleFctStr = "arccosh";
			if (fctStr.equals("atanh")) MapleFctStr = "arctanh";
			if (fctStr.equals("asech")) MapleFctStr = "arcsech";
			if (fctStr.equals("acsch")) MapleFctStr = "arccsch";
			if (fctStr.equals("acoth")) MapleFctStr = "arccoth";

			String retstr = MapleFctStr + "(" + rightstr + ")";
			if (fctStr.equals("zeron") || fctStr.equals("sign")) retstr += ")";
			return retstr;
		}
		return "";  //  Should not happen.
	}

	/**
	 * Returns a Maxima string representation of the function. 
	 * @return A Maxima string representation of the function.
	 */
		public String toMaximaCodeString()
		{
			return toMaximaCodeStringRec(root, "", false);
		}
		
		private String toMaximaCodeStringRec(ExpTreenode root, String ParentOp, boolean rightside)
		{
			String leftstr = "";
			String rightstr = "";
			
			if (root == null) return "";
			
			if (Operator.indexOf(root.op_str) >= 0) 
			{
				leftstr = toMaximaCodeStringRec(root.left, root.op_str, false);
				rightstr = toMaximaCodeStringRec(root.right, root.op_str, true);
				
				boolean includeParens = false;
				if (!ParentOp.equals(""))
					if ((Operator.indexOf(root.op_str) >= 0) && (Operator.indexOf(ParentOp) >= 0))
					{
						if ((AddOperator.indexOf(root.op_str) >= 0) && (MultOperator.indexOf(ParentOp) >= 0))
							includeParens = true;
		
						if ((AddOperator.indexOf(root.op_str) >= 0) && (PowOperator.indexOf(ParentOp) >= 0))
							includeParens = true;
		
						if ((MultOperator.indexOf(root.op_str) >= 0) && (PowOperator.indexOf(ParentOp) >= 0))
							includeParens = true;

						if ((PowOperator.indexOf(root.op_str) >= 0) && (PowOperator.indexOf(ParentOp) >= 0))
							includeParens = true;
							
						if ((AddOperator.indexOf(root.op_str) >= 0) && ParentOp.equals("-") && rightside)
							includeParens = true;

						if (root.op_str.equals("-") && (root.left == null))
							includeParens = true;
					}
				
				if (includeParens)
					return "(" + leftstr + root.op_str + rightstr + ")";
				else
					return leftstr + root.op_str + rightstr;
			}			
			else if (Terminal.indexOf(root.op_str) >= 0)
			{
				return root.op_str;
			}
			else if (root.op_str.equals("e"))
			{
				return "%e";
			}
			else if (root.op_str.equals("pi"))
			{
				return "%pi";
			}
			else if (root.op_str.equals("i"))
			{
				return "%i";
			}
			else if (root.op_str.equals("#"))
			{
				return "(" + root.num.toMaximaCodeString() + ")";
			}
			else if (!FindFct(root.op_str).equals(""))
			{
				rightstr = toMaximaCodeStringRec(root.right, root.op_str, true);
				
				String fctStr = root.op_str;
				String MaximaFctStr = fctStr;
				
				//  Change the ones that are not the same.
				
				if (fctStr.equals("ln")) MaximaFctStr = "log";
				if (fctStr.equals("mod")) MaximaFctStr = "abs";
				if (fctStr.equals("arg")) MaximaFctStr = "carg";
				if (fctStr.equals("sign")) MaximaFctStr = "signum";

				String retstr = MaximaFctStr + "(" + rightstr + ")";
				
				if (fctStr.equals("zeron"))
					retstr += "(if ("+rightstr+")=0 then und else 0)";
				if (fctStr.equals("log10"))
					retstr += "log("+rightstr+")/log(10)";

				return retstr;
			}
			return "";  //  Should not happen.
		}

/**
 * Returns a Mathematica string representation of the function. 
 * @return A Mathematica string representation of the function.
 */
	public String toMathematicaCodeString()
	{
		return toMathematicaCodeStringRec(root, "", false);
	}
	
	private String toMathematicaCodeStringRec(ExpTreenode root, String ParentOp, boolean rightside)
	{
		String leftstr = "";
		String rightstr = "";
		
		if (root == null) return "";
		
		if (Operator.indexOf(root.op_str) >= 0) 
		{
			leftstr = toMathematicaCodeStringRec(root.left, root.op_str, false);
			rightstr = toMathematicaCodeStringRec(root.right, root.op_str, true);
			
			boolean includeParens = false;
			if (!ParentOp.equals(""))
				if ((Operator.indexOf(root.op_str) >= 0) && (Operator.indexOf(ParentOp) >= 0))
				{
					if ((AddOperator.indexOf(root.op_str) >= 0) && (MultOperator.indexOf(ParentOp) >= 0))
						includeParens = true;
	
					if ((AddOperator.indexOf(root.op_str) >= 0) && (PowOperator.indexOf(ParentOp) >= 0))
						includeParens = true;
	
					if ((MultOperator.indexOf(root.op_str) >= 0) && (PowOperator.indexOf(ParentOp) >= 0))
						includeParens = true;

					if ((PowOperator.indexOf(root.op_str) >= 0) && (PowOperator.indexOf(ParentOp) >= 0))
						includeParens = true;
						
					if ((AddOperator.indexOf(root.op_str) >= 0) && ParentOp.equals("-") && rightside)
						includeParens = true;

					if (root.op_str.equals("-") && (root.left == null))
						includeParens = true;
				}
			
			if (includeParens)
				return "(" + leftstr + root.op_str + rightstr + ")";
			else
				return leftstr + root.op_str + rightstr;
		}			
		else if (Terminal.indexOf(root.op_str) >= 0)
		{
			return root.op_str;
		}
		else if (root.op_str.equals("e"))
		{
			return " E ";
		}
		else if (root.op_str.equals("pi"))
		{
			return " Pi ";
		}
		else if (root.op_str.equals("i"))
		{
			return " I ";
		}
		else if (root.op_str.equals("#"))
		{
			return "(" + root.num.toMathematicaCodeString() + ")";
		}
		else if (!FindFct(root.op_str).equals(""))
		{
			rightstr = toMathematicaCodeStringRec(root.right, root.op_str, true);
			
			String fctStr = root.op_str;
			String MathematicaFctStr = fctStr;
			
			//  Change the ones that are not the same.
			
			if (fctStr.equals("log")) MathematicaFctStr = "Log";
			if (fctStr.equals("ln")) MathematicaFctStr = "Log";
			if (fctStr.equals("sin")) MathematicaFctStr = "Sin";
			if (fctStr.equals("cos")) MathematicaFctStr = "Cos";
			if (fctStr.equals("tan")) MathematicaFctStr = "Tan";
			if (fctStr.equals("sec")) MathematicaFctStr = "Sec";
			if (fctStr.equals("csc")) MathematicaFctStr = "Csc";
			if (fctStr.equals("cot")) MathematicaFctStr = "Cot";
			if (fctStr.equals("asin")) MathematicaFctStr = "ArcSin";
			if (fctStr.equals("acos")) MathematicaFctStr = "ArcCos";
			if (fctStr.equals("atan")) MathematicaFctStr = "ArcTan";
			if (fctStr.equals("asec")) MathematicaFctStr = "ArcSec";
			if (fctStr.equals("acsc")) MathematicaFctStr = "ArcCsc";
			if (fctStr.equals("acot")) MathematicaFctStr = "ArcCot";
			if (fctStr.equals("mod")) MathematicaFctStr = "Abs";
			if (fctStr.equals("arg")) MathematicaFctStr = "Arg";
			if (fctStr.equals("carg")) MathematicaFctStr = "Arg";
			if (fctStr.equals("sign")) MathematicaFctStr = "Sign";
			if (fctStr.equals("sinh")) MathematicaFctStr = "Sinh";
			if (fctStr.equals("cosh")) MathematicaFctStr = "Cosh";
			if (fctStr.equals("tanh")) MathematicaFctStr = "Tanh";
			if (fctStr.equals("sech")) MathematicaFctStr = "Sech";
			if (fctStr.equals("csch")) MathematicaFctStr = "Csch";
			if (fctStr.equals("coth")) MathematicaFctStr = "Coth";
			if (fctStr.equals("asinh")) MathematicaFctStr = "ArcSinh";
			if (fctStr.equals("acosh")) MathematicaFctStr = "ArcCosh";
			if (fctStr.equals("atanh")) MathematicaFctStr = "ArcTanh";
			if (fctStr.equals("asech")) MathematicaFctStr = "ArcSech";
			if (fctStr.equals("acsch")) MathematicaFctStr = "ArcCsch";
			if (fctStr.equals("acoth")) MathematicaFctStr = "ArcCoth";
			if (fctStr.equals("sqrt")) MathematicaFctStr = "Sqrt";

			String retstr = ""; //MathematicaFctStr + "[" + rightstr + "]";

			if (fctStr.equals("zeron")) 
				retstr += "Piecewise[{{0, "+rightstr+" < 0}, {0, "+rightstr+" > 0}}]";
			else if (fctStr.equals("exp"))
					retstr += "E^("+rightstr+")";
			else if (fctStr.equals("log10")) 
					retstr += "Log["+rightstr+", 10]";
			else
				retstr += MathematicaFctStr + "[" + rightstr + "]";
			
			return retstr;
		}
		return "";  //  Should not happen.
	}

/**
 * Returns a LaTeX string representation of the function. 
 * @return A LaTeX string representation of the function.
 */
	public String toLaTeXString()
	{
		return toLaTeXCodeStringRec(root, "", false);
	}
	
	private String toLaTeXCodeStringRec(ExpTreenode root, String ParentOp, boolean rightside)
	{
		String leftstr = "";
		String rightstr = "";
		
		if (root == null) return "";

		if ((!root.op_str.equals("#")) && isPolynomialNoPower(root) && (Terminal.indexOf(root.op_str) < 0))
		{
			ArrayList<String> varlist = getVariableList(root);
			boolean OneVar = (varlist.size() == 1);
			
			if (OneVar)
			{
				String var = ((String)varlist.get(0)).toLowerCase();
				if (isPolynomialStandardForm(root, var))
				{
					boolean includeParens = false;

					if (!ParentOp.equals(""))
					{
						if (ParentOp.equals("*"))
							includeParens = true;
	
						if (PowOperator.indexOf(ParentOp) >= 0)
							includeParens = true;
	
						if (ParentOp.equals("-") && rightside)
							includeParens = true;
					}
						
					if (includeParens)
						return "(" + polynomialToLaTeXStringRec(root, var) + ")";
					else
						return polynomialToLaTeXStringRec(root, var);
				}
			}
		}
		
		if (Operator.indexOf(root.op_str) >= 0) 
		{
			leftstr = toLaTeXCodeStringRec(root.left, root.op_str, false);
			rightstr = toLaTeXCodeStringRec(root.right, root.op_str, true);
			
			boolean includeParens = false;
			if (!ParentOp.equals(""))
				if ((Operator.indexOf(root.op_str) >= 0) && (Operator.indexOf(ParentOp) >= 0))
				{
					if ((AddOperator.indexOf(root.op_str) >= 0) && ParentOp.equals("*"))
						includeParens = true;
					
					if (ParentOp.equals("^") && !root.op_str.equals("^"))
						includeParens = true;
	
					if ((AddOperator.indexOf(root.op_str) >= 0) && ParentOp.equals("-") && rightside)
						includeParens = true;
				}
			
			String retopstr = "";
			if (includeParens)
				retopstr += " \\left( ";
				
			if (root.op_str.equals("/"))
				retopstr += " \\frac{ " + leftstr + " }{ " + rightstr + " } ";
			else if (root.op_str.equals("*"))
			{
				retopstr += leftstr;
				if (includeParens)
					retopstr += " \\right) " + " \\left( ";
				else if (isNumericTree(root.left) && isNumericTree(root.right))
					retopstr += " \\cdot ";
				retopstr += rightstr;
			}
			else if (root.op_str.equals("^"))
				retopstr += " { " + leftstr + " }^{ " + rightstr + " } ";
			else
				retopstr += leftstr + root.op_str + rightstr;
				
			if (includeParens)
				retopstr += " \\right) ";
			return retopstr;

		}
		else if (Terminal.indexOf(root.op_str) >= 0)
		{
			return root.op_str;
		}
		else if (root.op_str.equals("e"))
		{
			return "exp(1)";
		}
		else if (root.op_str.equals("\\pi"))
		{
			return "Pi";
		}
		else if (root.op_str.equals("i"))
		{
			return "I";
		}
		else if (root.op_str.equals("#"))
		{
			boolean includeParens = false;
			if (!ParentOp.equals(""))
				if (Operator.indexOf(ParentOp) >= 0)
				{
					if (root.num.hasTwoTerms()) 
						includeParens = true;
					else if (root.num.isReal() && (root.num.getReal().approx() < 0))
						includeParens = true;
					else if (root.num.isImag() && (root.num.getImag().approx() < 0))
						includeParens = true;				
				}
			
			if (includeParens)
				return "\\left("+ root.num.toLaTeXString() + "\\right)";
			else
				return root.num.toLaTeXString();
		}
		else if (!FindFct(root.op_str).equals(""))
		{
			rightstr = toLaTeXCodeStringRec(root.right, root.op_str, true);

			String LaTeXFctStr = "";
			
			//  Chenge the ones that are not the same.

			if (root.op_str.equals("exp")) LaTeXFctStr = "e^{"+ rightstr + "}";
			if (root.op_str.equals("ln")) LaTeXFctStr = "\\ln\\left("+ rightstr + "\\right)";
			if (root.op_str.equals("log")) LaTeXFctStr = "\\ln\\left("+ rightstr + "\\right)";
			if (root.op_str.equals("log10")) LaTeXFctStr = "\\log\\left("+ rightstr + "\\right)";
			if (root.op_str.equals("sin")) LaTeXFctStr = "\\sin\\left("+ rightstr + "\\right)";
			if (root.op_str.equals("cos")) LaTeXFctStr = "\\cos\\left("+ rightstr + "\\right)";
			if (root.op_str.equals("tan")) LaTeXFctStr = "\\tan\\left("+ rightstr + "\\right)";
			if (root.op_str.equals("cot")) LaTeXFctStr = "\\cot\\left("+ rightstr + "\\right)";
			if (root.op_str.equals("sec")) LaTeXFctStr = "\\sec\\left("+ rightstr + "\\right)";
			if (root.op_str.equals("csc")) LaTeXFctStr = "\\csc\\left("+ rightstr + "\\right)";
			if (root.op_str.equals("sinh")) LaTeXFctStr = "{\\rm sinh}\\left("+ rightstr + "\\right)";
			if (root.op_str.equals("cosh")) LaTeXFctStr = "{\\rm cosh}\\left("+ rightstr + "\\right)";
			if (root.op_str.equals("tanh")) LaTeXFctStr = "{\\rm tanh}\\left("+ rightstr + "\\right)";
			if (root.op_str.equals("coth")) LaTeXFctStr = "{\\rm coth}\\left("+ rightstr + "\\right)";
			if (root.op_str.equals("sech")) LaTeXFctStr = "{\\rm sech}\\left("+ rightstr + "\\right)";
			if (root.op_str.equals("csch")) LaTeXFctStr = "{\\rm csch}\\left("+ rightstr + "\\right)";
			if (root.op_str.equals("abs")) LaTeXFctStr = "\\left|"+ rightstr + "\\right|";
			if (root.op_str.equals("mod")) LaTeXFctStr = "\\left|"+ rightstr + "\\right|";
			if (root.op_str.equals("arg")) LaTeXFctStr = "{\\rm arg}\\left("+ rightstr + "\\right)";
			if (root.op_str.equals("carg")) LaTeXFctStr = "{\\rm arg}\\left("+ rightstr + "\\right)";
			if (root.op_str.equals("sign")) LaTeXFctStr = "{\\rm sign}\\left("+ rightstr + "\\right)";
			if (root.op_str.equals("zeron")) LaTeXFctStr = "{\\rm zeron}\\left("+ rightstr + "\\right)";
			if (root.op_str.equals("sqrt")) LaTeXFctStr = "\\sqrt{"+ rightstr + "}";

			if (root.op_str.equals("asin")) LaTeXFctStr = "\\sin^{-1}\\left("+ rightstr + "\\right)";
			if (root.op_str.equals("acos")) LaTeXFctStr = "\\cos^{-1}\\left("+ rightstr + "\\right)";
			if (root.op_str.equals("atan")) LaTeXFctStr = "\\tan^{-1}\\left("+ rightstr + "\\right)";
			if (root.op_str.equals("acot")) LaTeXFctStr = "\\cot^{-1}\\left("+ rightstr + "\\right)";
			if (root.op_str.equals("asec")) LaTeXFctStr = "\\sec^{-1}\\left("+ rightstr + "\\right)";
			if (root.op_str.equals("acsc")) LaTeXFctStr = "\\csc^{-1}\\left("+ rightstr + "\\right)";

			if (root.op_str.equals("asinh")) LaTeXFctStr = "{\\rm sinh}^{-1}\\left("+ rightstr + "\\right)";
			if (root.op_str.equals("acosh")) LaTeXFctStr = "{\\rm cosh}^{-1}\\left("+ rightstr + "\\right)";
			if (root.op_str.equals("atanh")) LaTeXFctStr = "{\\rm tanh}^{-1}\\left("+ rightstr + "\\right)";
			if (root.op_str.equals("acoth")) LaTeXFctStr = "{\\rm coth}^{-1}\\left("+ rightstr + "\\right)";
			if (root.op_str.equals("asech")) LaTeXFctStr = "{\\rm sech}^{-1}\\left("+ rightstr + "\\right)";
			if (root.op_str.equals("acsch")) LaTeXFctStr = "{\\rm csch}^{-1}\\left("+ rightstr + "\\right)";

			return LaTeXFctStr;

		}
		return "";  //  Should not happen.
	}

	private ComplexNumber evaluateNumericSubtree(ExpTreenode t)
	{
		simplified = true;
		while (simplified) 
		{
			simplified = false;
			t = simplifyNumbers(t);
		}
		
		if (t != null)
			if (t.op_str.equals("#"))
				return t.num.clone();

		return null;
	}

/**
 * Simplifies the current expression.	 These simplification routines just evaluate 
 * numeric portions of the expression and do a little simplification of 
 * polynomial expressions.
 */
	public void simplify()
	{
		simplify(null);
	}

/**
 * Simplifies the current expression using the given NumericTolerances.	 
 * These simplification routines just evaluate 
 * numeric portions of the expression and do a little simplification of 
 * polynomial expressions.
 * @param tol the given NumericTolerances.
 */
	public void simplify(NumericTolerances tol)
	{
		if (tol == null)
			tol = NumericTolerances.setForExactNumber();
			
		simplified = true;
		while (simplified) 
		{
			while (simplified) 
			{
				simplified = false;
				root = simplifyRec(root);
			}
			
			if (root.op_str.equals("#"))
				root = new ExpTreenode(root.num.adjust(tol));
			else
				root = simplifyPolynomialsRec(root, tol);
	
			root = simplifyRec(root);
		}
	}
	
	private ExpTreenode simplifyRec(ExpTreenode t)
	{
		if (t == null) return null;
		
		ExpTreenode tempnode = new ExpTreenode(t.op_str, t.num, simplifyRec(t.left), simplifyRec(t.right));
		tempnode = simplifyNumbers(tempnode);
		tempnode = simplifyZeroOne(tempnode);
		return tempnode;
	}

	private ExpTreenode simplifyZeroOne(ExpTreenode tempnode)
	{
		//  Simple things: 1*?, ?*1, 0*?, ?*0, 0+?, ?+0, 0-?, ?-0, ?^0, ?^1, 1^?
		if ((tempnode.left != null) && (tempnode.right != null))
		{
			if (tempnode.op_str.equals("*"))
			{
				if (tempnode.left.op_str.equals("#"))
				{
					if (tempnode.left.num.isZero())
					{
						tempnode = new ExpTreenode(ComplexNumber.makeZero());
						simplified = true;
					}
					else if (tempnode.left.num.isOne())
					{
						tempnode = tempnode.right;
						simplified = true;
					}
				}
				else if (tempnode.right.op_str.equals("#"))
				{
					if (tempnode.right.num.isZero())
					{
						tempnode = new ExpTreenode(ComplexNumber.makeZero());
						simplified = true;
					} 
					else if (tempnode.right.num.isOne())
					{
						tempnode = tempnode.left;
						simplified = true;
					}
				}
			}
			else if (tempnode.op_str.equals("+"))
			{
				if (tempnode.left.op_str.equals("#"))
				{
					if (tempnode.left.num.isZero())
					{
						tempnode = tempnode.right;
						simplified = true;
					}
				}
				else if (tempnode.right.op_str.equals("#"))
				{
					if (tempnode.right.num.isZero())
					{
						tempnode = tempnode.left;
						simplified = true;
					}
				}
			}
			else if (tempnode.op_str.equals("-"))
			{
				if (tempnode.left.op_str.equals("#"))
				{
					if (tempnode.left.num.isZero())
					{
						tempnode = new ExpTreenode("-", null, null, tempnode.right);
						simplified = true;
					}
				}
				else if (tempnode.right.op_str.equals("#"))
				{
					if (tempnode.right.num.isZero())
					{
						tempnode = tempnode.left;
						simplified = true;
					}
				}
			}
			else if (tempnode.op_str.equals("^"))
			{
				if (tempnode.right.op_str.equals("#"))
				{
					if (tempnode.right.num.isZero())
					{
						tempnode = new ExpTreenode("#", ComplexNumber.makeOne(), null, null);
						simplified = true;
					}
					else if (tempnode.right.num.isOne())
					{
						tempnode = tempnode.left;
						simplified = true;
					}
				}
				else if (tempnode.left.op_str.equals("#"))
				{
					if (tempnode.left.num.isOne())
					{
						tempnode = new ExpTreenode("#", ComplexNumber.makeOne(), null, null);
						simplified = true;
					}
				}
			}
		}
		return tempnode;
	}

	private ExpTreenode simplifyNumbers(ExpTreenode tempnode)
	{
		
		if (tempnode == null) return null;
		if (tempnode.op_str.equals("#")) return tempnode;
		
		ExpTreenode leftExp = simplifyNumbers(tempnode.left);
		ExpTreenode rightExp = simplifyNumbers(tempnode.right);
		
		//  Simplify Numbers

		//  Binary Operations
		
		if ((Operator.indexOf(tempnode.op_str) >= 0) && (leftExp != null) && (rightExp != null))
		{
			if (leftExp.op_str.equals("#") && rightExp.op_str.equals("#"))
			{
				ComplexNumber c1 = leftExp.num;
				ComplexNumber c2 = rightExp.num;				
				ComplexNumber c = new ComplexNumber();
				
				if (tempnode.op_str.equals("+"))
					c = c1.add(c2);
				if (tempnode.op_str.equals("-"))
					c = c1.subtract(c2);
				if (tempnode.op_str.equals("*"))
					c = c1.multiply(c2);
				if (tempnode.op_str.equals("/"))
					c = c1.divide(c2);
				if (tempnode.op_str.equals("^"))
					c = c1.pow(c2);
					
				tempnode = new ExpTreenode(c);
				
				simplified = true;
			}
		}
		
		//  Unary -
		
		if (tempnode.op_str.equals("-") && (leftExp == null) && (rightExp != null))
		{
			if (rightExp.op_str.equals("#"))
			{
				ComplexNumber c2 = rightExp.num;
				tempnode = new ExpTreenode(c2.multiply(ComplexNumber.makeMinusOne()));
				
				simplified = true;
			}
		}

		//  Functions
				
		if ((leftExp == null) && (rightExp != null))
			if (rightExp.op_str.equals("#"))
			{
				ComplexNumber c = rightExp.num;
				
				if (tempnode.op_str.equals("ln") || tempnode.op_str.equals("log"))
				{
					tempnode = new ExpTreenode(c.log());
					simplified = true;
				}

				if (tempnode.op_str.equals("exp"))
				{
					tempnode = new ExpTreenode(c.exp());
					simplified = true;
				}

				if (tempnode.op_str.equals("log10"))
				{
					ComplexNumber ln10 = new ComplexNumber(Math.log(10), 0.0);
					tempnode = new ExpTreenode((c.log()).divide(ln10));
					simplified = true;
				}

				if (tempnode.op_str.equals("sin"))
				{
					tempnode = new ExpTreenode(c.sin());
					simplified = true;
				}

				if (tempnode.op_str.equals("cos"))
				{
					tempnode = new ExpTreenode(c.cos());
					simplified = true;
				}

				if (tempnode.op_str.equals("tan"))
				{
					tempnode = new ExpTreenode(c.tan());
					simplified = true;
				}

				if (tempnode.op_str.equals("cot"))
				{
					tempnode = new ExpTreenode(c.cot());
					simplified = true;
				}

				if (tempnode.op_str.equals("sec"))
				{
					tempnode = new ExpTreenode(c.sec());
					simplified = true;
				}

				if (tempnode.op_str.equals("csc"))
				{
					tempnode = new ExpTreenode(c.csc());
					simplified = true;
				}
		
				if (tempnode.op_str.equals("sinh"))
				{
					tempnode = new ExpTreenode(c.sinh());
					simplified = true;
				}

				if (tempnode.op_str.equals("cosh"))
				{
					tempnode = new ExpTreenode(c.cosh());
					simplified = true;
				}

				if (tempnode.op_str.equals("tanh"))
				{
					tempnode = new ExpTreenode(c.tanh());
					simplified = true;
				}

				if (tempnode.op_str.equals("coth"))
				{
					tempnode = new ExpTreenode(c.coth());
					simplified = true;
				}

				if (tempnode.op_str.equals("sech"))
				{
					tempnode = new ExpTreenode(c.sech());
					simplified = true;
				}

				if (tempnode.op_str.equals("csch"))
				{
					tempnode = new ExpTreenode(c.csch());
					simplified = true;
				}

				if (tempnode.op_str.equals("asin"))
				{
					tempnode = new ExpTreenode(c.asin());
					simplified = true;
				}

				if (tempnode.op_str.equals("acos"))
				{
					tempnode = new ExpTreenode(c.acos());
					simplified = true;
				}

				if (tempnode.op_str.equals("atan"))
				{
					tempnode = new ExpTreenode(c.atan());
					simplified = true;
				}

				if (tempnode.op_str.equals("acot"))
				{
					tempnode = new ExpTreenode(c.acot());
					simplified = true;
				}

				if (tempnode.op_str.equals("asec"))
				{
					tempnode = new ExpTreenode(c.asec());
					simplified = true;
				}

				if (tempnode.op_str.equals("acsc"))
				{
					tempnode = new ExpTreenode(c.acsc());
					simplified = true;
				}

				if (tempnode.op_str.equals("asinh"))
				{
					tempnode = new ExpTreenode(c.asinh());
					simplified = true;
				}

				if (tempnode.op_str.equals("acosh"))
				{
					tempnode = new ExpTreenode(c.acosh());
					simplified = true;
				}

				if (tempnode.op_str.equals("atanh"))
				{
					tempnode = new ExpTreenode(c.atanh());
					simplified = true;
				}

				if (tempnode.op_str.equals("acoth"))
				{
					tempnode = new ExpTreenode(c.acoth());
					simplified = true;
				}

				if (tempnode.op_str.equals("asech"))
				{
					tempnode = new ExpTreenode(c.asech());
					simplified = true;
				}

				if (tempnode.op_str.equals("acsch"))
				{
					tempnode = new ExpTreenode(c.acsch());
					simplified = true;
				}

				if (tempnode.op_str.equals("abs"))
				{
					tempnode = new ExpTreenode(new ComplexNumber(c.mod(), 0.0));
					simplified = true;
				}

				if (tempnode.op_str.equals("sign"))
				{
					tempnode = new ExpTreenode(c.sign());
					simplified = true;
				}

				if (tempnode.op_str.equals("zeron"))
				{
					tempnode = new ExpTreenode(c.zeron());
					simplified = true;
				}

				if (tempnode.op_str.equals("mod"))
				{
					tempnode = new ExpTreenode(new ComplexNumber(c.mod(), 0.0));
					simplified = true;
				}

				if (tempnode.op_str.equals("arg") || tempnode.op_str.equals("carg"))
				{
					tempnode = new ExpTreenode(new ComplexNumber(c.arg(), 0.0));
					simplified = true;
				}
				
				if (tempnode.op_str.equals("sqrt"))
				{
					tempnode = new ExpTreenode(c.pow(new ComplexNumber(1, 2, 0, 1)));
					simplified = true;
				}

			}  //  End numeric valued functions

		return tempnode;
	}

/**
 * Determines if the expression represents a polynomial function.	
 * @return true if the expression is a polynomial and false otherwise.
 */
	public boolean isPolynomial()
	{
		return isPolynomial(root);
	}

	private boolean isPolynomial(ExpTreenode tempnode)
	{
		if (tempnode == null) return true;
		
		if (tempnode.op_str.equals("#")) return true;
		if (Terminal.indexOf(tempnode.op_str.toLowerCase()) >= 0) return true;
		
		if (tempnode.op_str.equals("+") || tempnode.op_str.equals("-") || tempnode.op_str.equals("*"))
			return isPolynomial(tempnode.left) && isPolynomial(tempnode.right);
		else if (tempnode.op_str.equals("/"))
			return isPolynomial(tempnode.left) && isNumericTree(tempnode.right);
		else if (tempnode.op_str.equals("^"))
		{
			boolean poly = isPolynomial(tempnode.left);
			boolean num = isNumericTree(tempnode.right);
			boolean intYN = false;
			
			if (num && (tempnode.right != null))
			{
				ExpTreenode t = tempnode.right.clone();
				simplified = true;
				while (simplified)
				{
					simplified = false;
					t = simplifyNumbers(t);
				}
				
				ComplexNumber rightVal = null;
				if (t != null)
					if (t.op_str.equals("#"))
						rightVal = t.num.clone();

				if (rightVal != null)
					if (rightVal.isRealInteger())
						intYN = true;
			}
			return poly && intYN;
		}
		
		return false;	
	}

	public boolean isPolynomialNoPower()
	{
		return isPolynomialNoPower(root);
	}

	private boolean isPolynomialNoPower(ExpTreenode tempnode)
	{
		if (tempnode == null) return true;
		
		if (tempnode.op_str.equals("#")) return true;
		if (Terminal.indexOf(tempnode.op_str.toLowerCase()) >= 0) return true;
		
		if (tempnode.op_str.equals("+") || tempnode.op_str.equals("-") || tempnode.op_str.equals("*"))
			return isPolynomialNoPower(tempnode.left) && isPolynomialNoPower(tempnode.right);
		else if (tempnode.op_str.equals("/"))
			return isPolynomialNoPower(tempnode.left) && isNumericTree(tempnode.right);
		
		return false;	
	}

/**
 * Determines if the polynomial has no quantities to powers.
 * @return true if the polynomial expression has no quantities to powers and 
 * false otherwise.
 */
	public boolean isPolynomialNoQuantityPower()
	{
		return isPolynomialNoQuantityPower(root);
	}

	private boolean isPolynomialNoQuantityPower(ExpTreenode tempnode)
	{
		if (tempnode == null) return true;
		
		if (tempnode.op_str.equals("#")) return true;
		if (Terminal.indexOf(tempnode.op_str.toLowerCase()) >= 0) return true;
		
		if (tempnode.op_str.equals("+") || tempnode.op_str.equals("-") || tempnode.op_str.equals("*"))
			return isPolynomialNoQuantityPower(tempnode.left) && isPolynomialNoQuantityPower(tempnode.right);
		else if (tempnode.op_str.equals("/"))
			return isPolynomialNoQuantityPower(tempnode.left) && isNumericTree(tempnode.right);
		else if (tempnode.op_str.equals("^"))
			return (Terminal.indexOf(tempnode.left.op_str.toLowerCase()) >= 0) && isNumericTree(tempnode.right);

		return false;	
	}

/**
 * Determines if the expression is a single term of a polynomial in the variable
 * var.
 * @param var a string containing a single character of the variable.
 * @return true if the expression is single term of a polynomial and false otherwise.
 */
	public boolean isSinglePolyTerm(String var)
	{
		return isSinglePolyTerm(root, var);
	}
	
	private boolean isSinglePolyTerm(ExpTreenode tempnode, String var)
	{
		if (tempnode == null) return true;
		if (tempnode.op_str.equals("#")) return true;
		if (var.equalsIgnoreCase(tempnode.op_str)) return true;
		
		boolean termOK = false;
		try
		{
			if (tempnode.op_str.equals("*"))
			{
				if ((tempnode.left != null) && (tempnode.right != null))
				{
					if (tempnode.left.op_str.equals("#") && var.equalsIgnoreCase(tempnode.right.op_str))
						termOK = true;

					if (tempnode.right.op_str.equals("#") && var.equalsIgnoreCase(tempnode.left.op_str))
						termOK = true;
						
					if (tempnode.left.op_str.equals("#") && tempnode.right.op_str.equals("^"))
						if (tempnode.right.right.op_str.equals("#") && tempnode.right.left.op_str.equalsIgnoreCase(var))
							termOK = true;
				
					if (tempnode.right.op_str.equals("#") && tempnode.left.op_str.equals("^"))
						if (tempnode.left.right.op_str.equals("#") && tempnode.left.left.op_str.equalsIgnoreCase(var))
							termOK = true;
				}
			}
			else if (tempnode.op_str.equals("^"))
			{
				if ((tempnode.left != null) && (tempnode.right != null))
					if (tempnode.right.op_str.equals("#") && tempnode.left.op_str.equalsIgnoreCase(var))
						termOK = true;
			}
		}
		catch (Exception ex) {}
				
		return termOK;
	}

/**
 * Determines if the expression is a polynomial in standard form in the variable var.	
 * @param var a string containing a single character of the variable.
 * @return true if the expression is a polynomial in standard form and false 
 * otherwise.
 */
	public boolean isPolynomialStandardForm(String var)
	{
		return isPolynomialStandardForm(root, var);
	}
	
	private boolean isPolynomialStandardForm(ExpTreenode tempnode, String var)
	{
		//  Do polynomial check before invoking this method.
		
		if (tempnode == null) return true;
		if (isSinglePolyTerm(tempnode, var)) return true;
		if (tempnode.op_str.equals("#")) return true;
		if (var.equalsIgnoreCase(tempnode.op_str)) return true;
		
		boolean termOK = false;
		if (tempnode.op_str.equals("+") || tempnode.op_str.equals("-"))
			if (isSinglePolyTerm(tempnode.right, var))
				if (getPolynomialDegree(tempnode.left, var) < getPolynomialDegree(tempnode.right, var))
					termOK = true;

		return termOK && isPolynomialStandardForm(tempnode.left, var);
	}

/**
 * Returns the degree of a polynomial in a single variable var. 	
 * @param var a string containing a single character of the variable.
 * @return The degree of the polynomial.
 */
	public long getPolynomialDegree(String var)
	{
		return getPolynomialDegree(root, var);
	}
	
	private long getPolynomialDegree(ExpTreenode tempnode, String var)
	{
		//  Do polynomial check before invoking this method.
		
		if (tempnode == null) return 0;
		if (tempnode.op_str.equals("#")) return 0;
		if (var.equalsIgnoreCase(tempnode.op_str)) return 1;
		if (Terminal.indexOf(tempnode.op_str.toLowerCase()) >= 0) return 0;
		
		if (tempnode.op_str.equals("+") || tempnode.op_str.equals("-"))
		{
			long max = getPolynomialDegree(tempnode.left, var);
			long rightdeg = getPolynomialDegree(tempnode.right, var);
			
			if (max < rightdeg)
				max = rightdeg;
				
			return max;
		}
		else if (tempnode.op_str.equals("*"))
			return getPolynomialDegree(tempnode.left, var) + getPolynomialDegree(tempnode.right, var);
		else if (tempnode.op_str.equals("/"))
			return getPolynomialDegree(tempnode.left, var);
		else if (tempnode.op_str.equals("^"))
		{
			long leftdeg = getPolynomialDegree(tempnode.left, var);
			boolean num = isNumericTree(tempnode.right);
			
			if (num && (tempnode.right != null))
			{
				ExpTreenode t = tempnode.right.clone();
				simplified = true;
				while (simplified)
				{
					simplified = false;
					t = simplifyNumbers(t);
				}
				
				ComplexNumber rightVal = null;
				if (t != null)
					if (t.op_str.equals("#"))
						rightVal = t.num.clone();

				if (rightVal != null)
					if (rightVal.isRealInteger())
					{
						return leftdeg * ((long)rightVal.getReal().approx());
					}
			}
		}
		
		return 0;  //  Should not happen.
	}
	
	private ExpTreenode simplifyPolynomialsRec(ExpTreenode tempnode, NumericTolerances tol)
	{
		if (tempnode == null) return null;
		ExpTreenode t = tempnode.clone();

		if (isPolynomialNoQuantityPower(t) && (!t.op_str.equalsIgnoreCase("#")))  //  Process the polynomial
		{
			
			ArrayList<String> varlist = getVariableList(t);
			boolean OneVar = (varlist.size() == 1);
			
			if (OneVar)
			{
				String var = ((String)varlist.get(0)).toLowerCase();
				if (!isPolynomialStandardForm(t, var))
					return simplifyPoly(t, var, tol);  //  Process single variable polynomial.
				else
					return t;	
			}
			else
				return new ExpTreenode(t.op_str, null, simplifyPolynomialsRec(t.left, tol), simplifyPolynomialsRec(t.right, tol));
		}
		else     //  The subtree is not a polynomial, recurse.
		{
			if (t.num != null)
				return new ExpTreenode(t.op_str, t.num.clone(), simplifyPolynomialsRec(t.left, tol), simplifyPolynomialsRec(t.right, tol));
			else
				return new ExpTreenode(t.op_str, null, simplifyPolynomialsRec(t.left, tol), simplifyPolynomialsRec(t.right, tol));
		}
	}

	private ExpTreenode simplifyPoly(ExpTreenode tempnode, String var, NumericTolerances tol)
	{
		if (tempnode == null) return null;
		ExpTreenode t = tempnode.clone();

		//  Expand
		
		simplified = true;
		while (simplified) 
		{
			simplified = false;
			t = expand(t, var);
		}
		
		//  Combine single terms

		simplified = true;
		while (simplified) 
		{
			simplified = false;
			t = combineSingleTerms(t, var);
		}

		//  Combine like terms into an ArrayList
		
		ArrayList<Object> PolyTerms = new ArrayList<Object>();
		buildPolyTerms(t, var, PolyTerms, 1);

//		for (int i = 0; i < PolyTerms.size(); i++)
//		{
//			ComplexNumber coeff = (ComplexNumber)((ArrayList)PolyTerms.get(i)).get(1);
//			long deg = ((Long)((ArrayList)PolyTerms.get(i)).get(0)).longValue();
//		}
		
		//  Sort list
		
		for (int i = 0; i < PolyTerms.size()-1; i++)
			for (int j = 0; j < PolyTerms.size()-1-i; j++)
				if (((Long)((ArrayList<?>)PolyTerms.get(j+1)).get(0)).longValue() < ((Long)((ArrayList<?>)PolyTerms.get(j)).get(0)).longValue())
				{
					Object e1 = PolyTerms.get(j+1);
					Object e2 = PolyTerms.get(j);
					PolyTerms.set(j, e1);
					PolyTerms.set(j+1, e2);
				}
		
		//  Build polynomial
		
		t = null;		
		for (int i = 0; i < PolyTerms.size(); i++)
		{
			ComplexNumber coeff = (ComplexNumber)((ArrayList<?>)PolyTerms.get(i)).get(1);
			long deg = ((Long)((ArrayList<?>)PolyTerms.get(i)).get(0)).longValue();
			
			if (!coeff.isZero())
			{
				if (deg == 0)
				{
					if (t == null)
						t = new ExpTreenode(coeff);
					else
					{
						if (coeff.hasNegativeFirstTerm())
							t = new ExpTreenode("-", null, t, new ExpTreenode(coeff.negate()));
						else
							t = new ExpTreenode("+", null, t, new ExpTreenode(coeff));
					}
				}
				else if (deg == 1)
				{
					if (t == null)
					{
						if (coeff.isOne())
							t = new ExpTreenode(var);
						else 
							t = new ExpTreenode("*", null, new ExpTreenode(coeff), new ExpTreenode(var));
					}
					else
					{
						ExpTreenode newTerm = null;
						
						if (coeff.hasNegativeFirstTerm())
						{
							newTerm = new ExpTreenode("*", null, new ExpTreenode(coeff.negate()), new ExpTreenode(var));
							t = new ExpTreenode("-", null, t, newTerm);
						}
						else
						{
							newTerm = new ExpTreenode("*", null, new ExpTreenode(coeff), new ExpTreenode(var));
							t = new ExpTreenode("+", null, t, newTerm);							
						}
					}
				}
				else  //  deg > 1
				{
					if (t == null)
					{
						if (coeff.isOne())
							t = new ExpTreenode("^", null, new ExpTreenode(var), new ExpTreenode(new ComplexNumber(deg, 0)));
						else 
						{
							ExpTreenode varpow = new ExpTreenode("^", null, new ExpTreenode(var), new ExpTreenode(new ComplexNumber(deg, 0)));
							t = new ExpTreenode("*", null, new ExpTreenode(coeff),varpow);
						}
					}
					else
					{
						ExpTreenode newTerm = null;
						
						if (coeff.hasNegativeFirstTerm())
						{
							ExpTreenode varpow = new ExpTreenode("^", null, new ExpTreenode(var), new ExpTreenode(new ComplexNumber(deg, 0)));
							newTerm = new ExpTreenode("*", null, new ExpTreenode(coeff.negate()), varpow);
							t = new ExpTreenode("-", null, t, newTerm);
						}
						else
						{
							ExpTreenode varpow = new ExpTreenode("^", null, new ExpTreenode(var), new ExpTreenode(new ComplexNumber(deg, 0)));
							newTerm = new ExpTreenode("*", null, new ExpTreenode(coeff), varpow);
							t = new ExpTreenode("+", null, t, newTerm);							
						}
					}
				}
			}
		}
		
		if (t == null)
			t = new ExpTreenode(ComplexNumber.makeZero());

		return t;
	}

	private void buildPolyTerms(ExpTreenode tempnode, String var, ArrayList<Object> PolyTerms, final int sign)
	{
		if (tempnode == null) return;
		
		if (tempnode.op_str.equals("+"))
		{
			buildPolyTerms(tempnode.left, var, PolyTerms, sign);
			buildPolyTerms(tempnode.right, var, PolyTerms, sign);
		}
		else if (tempnode.op_str.equals("-"))		
		{
			buildPolyTerms(tempnode.left, var, PolyTerms, sign);
			buildPolyTerms(tempnode.right, var, PolyTerms, -1*sign);
		}
		else
		{
			ComplexNumber coeff = getSingleTermCoefficient(tempnode, var);
			long deg = getSingleTermDegree(tempnode, var);
			
			if (PolyTerms.size() == 0)
			{
				ArrayList<Object> entry = new ArrayList<Object>();
				entry.add(Long.valueOf(deg));
				if (sign > 0)
					entry.add(coeff);
				else
					entry.add(coeff.negate());
				PolyTerms.add(entry);
			}
			else
			{
				int pos = -1;
				for (int i = 0; i < PolyTerms.size(); i++)
					if (((Long)((ArrayList<?>)PolyTerms.get(i)).get(0)).longValue() == deg)
						pos = i;
						
				if (pos >= 0)
				{
					ComplexNumber oldval = (ComplexNumber)((ArrayList<?>)PolyTerms.get(pos)).get(1);
					ArrayList<Object> entry = new ArrayList<Object>();
					entry.add(Long.valueOf(deg));
					if (sign > 0)
						entry.add(oldval.add(coeff));
					else
						entry.add(oldval.subtract(coeff));
						
					PolyTerms.set(pos, entry);
				}
				else
				{
					ArrayList<Object> entry = new ArrayList<Object>();
					entry.add(Long.valueOf(deg));
					if (sign > 0)
						entry.add(coeff);
					else
						entry.add(coeff.negate());
					PolyTerms.add(entry);
				}
			}
		}
	}
	
	private ComplexNumber getSingleTermCoefficient(ExpTreenode tempnode, String var)
	{
		//  Only works on single terms.
		
		if (tempnode == null) return ComplexNumber.makeZero();
		if (tempnode.op_str.equals("#")) return tempnode.num.clone();
		if (var.equalsIgnoreCase(tempnode.op_str)) return ComplexNumber.makeOne();

		try
		{
			if (tempnode.op_str.equals("*"))
			{
				if (tempnode.left.op_str.equals("#"))
					return tempnode.left.num.clone();

				if (tempnode.right.op_str.equals("#"))
					return tempnode.right.num.clone();						
			}
			else if (tempnode.op_str.equals("^"))
				return ComplexNumber.makeOne();
		}
		catch (Exception ex) {}
		
		return ComplexNumber.makeZero();  //  Should not happen.
	}
	
	private long getSingleTermDegree(ExpTreenode tempnode, String var)
	{
		if (tempnode == null) return 0;
		if (tempnode.op_str.equals("#")) return 0;
		if (var.equalsIgnoreCase(tempnode.op_str)) return 1;
		
		try
		{
			if (tempnode.op_str.equals("*"))
			{
				if ((tempnode.left != null) && (tempnode.right != null))
				{
					if (var.equalsIgnoreCase(tempnode.right.op_str))
						return 1;
						
					if (var.equalsIgnoreCase(tempnode.left.op_str))
						return 1;
						
					if (tempnode.right.op_str.equals("^"))
						return tempnode.right.right.num.clone().getReal().getNum().longValue();

					if (tempnode.left.op_str.equals("^"))
						return tempnode.left.right.num.clone().getReal().getNum().longValue();
				}
			}
			else if (tempnode.op_str.equals("^"))
				return tempnode.right.num.clone().getReal().getNum().longValue();
		}
		catch (Exception ex) {}
				
		return 0;  //  Should not happen
	}

	private ExpTreenode combineSingleTerms(ExpTreenode tempnode, String var)
	{
		if (tempnode == null) return null;
		ExpTreenode t = tempnode.clone();
		if (isSinglePolyTerm(t, var)) return t;
		
		if (t.op_str.equals("*"))
		{
			if ((t.left != null) && (t.right != null))  //  Should happen if the tree is well formed.
				if (isSinglePolyTerm(t.left, var) && isSinglePolyTerm(t.right, var))
				{
					simplified = true;
					ComplexNumber leftCoeff = getSingleTermCoefficient(t.left, var);
					ComplexNumber rightCoeff = getSingleTermCoefficient(t.right, var);
					long leftDeg = getSingleTermDegree(t.left, var);
					long rightDeg = getSingleTermDegree(t.right, var);
					
					long newDegree = leftDeg + rightDeg;
					ComplexNumber newCoeff = leftCoeff.multiply(rightCoeff);
					
					if (newCoeff.isZero())
						return new ExpTreenode(ComplexNumber.makeZero());
					else
					{
						if (newDegree == 0)
							return new ExpTreenode(newCoeff);
						else if (newDegree == 1)
							return new ExpTreenode("*", null, new ExpTreenode(newCoeff), new ExpTreenode(var));
						else
						{
							ExpTreenode varpow = new ExpTreenode("^", null, new ExpTreenode(var), new ExpTreenode(new ComplexNumber(newDegree, 0)));
							return new ExpTreenode("*", null, new ExpTreenode(newCoeff), varpow);
						}
					}
				}
		}
		
		if (t.num != null)
			return new ExpTreenode(t.op_str, t.num.clone(), combineSingleTerms(t.left, var), combineSingleTerms(t.right, var));
		else
			return new ExpTreenode(t.op_str, null, combineSingleTerms(t.left, var), combineSingleTerms(t.right, var));
	}
	
	private ExpTreenode expand(ExpTreenode tempnode, String var)
	{
		if (tempnode == null) return null;
		if (isSinglePolyTerm(tempnode, var)) return tempnode;
		
		ExpTreenode t = tempnode.clone();
		ExpTreenode leftTree = null;
		ExpTreenode rightTree = null;

		if (t.op_str.equals("*"))
		{
			//  Distribute over left quantity, binary op.
			if (t.left != null)
				if ((t.left.left != null) && (t.left.right != null) && (t.right != null))  //  should happen if the tree is well formed and operation is binary.
					if (t.left.op_str.equals("+") || t.left.op_str.equals("-"))
					{
						simplified = true;
						leftTree = new ExpTreenode("*", null, t.left.left.clone(), t.right.clone());
						rightTree = new ExpTreenode("*", null, t.left.right.clone(), t.right.clone());
						return new ExpTreenode(t.left.op_str, null, leftTree, rightTree);
					}
					
			//  Distribute over right quantity, binary op.
			if (t.right != null)
				if ((t.right.left != null) && (t.right.right != null) && (t.left != null))  //  should happen if the tree is well formed and operation is binary.
					if (t.right.op_str.equals("+") || t.right.op_str.equals("-"))
					{
						simplified = true;
						leftTree = new ExpTreenode("*", null, t.right.left.clone(), t.left.clone());
						rightTree = new ExpTreenode("*", null, t.right.right.clone(), t.left.clone());
						return new ExpTreenode(t.right.op_str, null, leftTree, rightTree);
					}
			
			//  Rearrange over a unary -, left.
			if (t.left != null)
				if ((t.left.right != null) && (t.right != null))  //  should happen if the tree is well formed and operation is unary.
					if (t.left.op_str.equals("-"))
					{
						simplified = true;
						rightTree = new ExpTreenode("*", null, t.left.right.clone(), t.right.clone());
						return new ExpTreenode("-", null, null, rightTree);
					}
			
			//  Rearrange over a unary -, right.
			if (t.right != null)
				if ((t.right.right != null) && (t.left != null))  //  should happen if the tree is well formed and operation is unary.
					if (t.right.op_str.equals("-"))
					{
						simplified = true;
						rightTree = new ExpTreenode("*", null, t.right.right.clone(), t.left.clone());
						return new ExpTreenode("-", null, null, rightTree);
					}
		}

		if (t.op_str.equals("/"))   //  right subtree is numeric.
			if (t.right != null)
			{
				ExpTreenode numRight = t.right.clone();
				
				numRight = new ExpTreenode("/", null, new ExpTreenode(ComplexNumber.makeOne()), numRight);
				simplified = true;
				while (simplified) 
				{
					simplified = false;
					numRight = simplifyNumbers(numRight);
				}
				simplified = true;
				return new ExpTreenode("*", null, t.left.clone(), numRight);
			}
		
		if (t.num != null)
			return new ExpTreenode(t.op_str, t.num.clone(), expand(t.left, var), expand(t.right, var));
		else
			return new ExpTreenode(t.op_str, null, expand(t.left, var), expand(t.right, var));
	}
	
/**
 * Evaluates the current expression at the given values stored in the ArrayList
 * VarVals.  The VarVals ArrayList has the following form, it is a list of 
 * ArrayLists containing two entries each.  The first is a one character string
 * containing the variable to be substituted and the second is an ExpFormula object
 * that contains the expression to be used in the substitution.  For example,
 * if the current expression is x^2+xy+z and your VarVals ArrayList looks like
 * {{"x", sin(t)},{"y", 3}} then the result on evaluation is
 * sin(t)^2+3sin(t)+z.  Note that in this example the sin(t) and 3 are actually
 * ExpFormula objects containing these expressions.       
 * @param VarVals ArrayList which is a list of 
 * ArrayLists containing two entries each.  The first is a one character string
 * containing the variable to be substituted and the second is an ExpFormula object
 * that contains the expression to be used in the substitution. 
 * @return The substituted formula. 
 */
	public ExpFormula evaluate(ArrayList<?> VarVals)
	{
		ExpTreenode newForm = root.clone();
		ExpFormula retform = new ExpFormula();
		newForm = evaluateRec(newForm, VarVals);
		
		retform.setRoot(newForm);
		retform.simplify();
		return retform;
	}
	
	private ExpTreenode evaluateRec(ExpTreenode t, ArrayList<?> VarVals)
	{
		if (t == null) return null;
		int pos = -1;
		
		for (int i = 0; i < VarVals.size(); i++)
		{
			String var = (String)((ArrayList<?>)VarVals.get(i)).get(0);
			if (t.op_str.equalsIgnoreCase(var))
				pos = i;
		}
		
		if (pos > -1)
			return ((ExpFormula)((ArrayList<?>)VarVals.get(pos)).get(1)).root.clone();
		else
			return new ExpTreenode(t.op_str, t.num, evaluateRec(t.left, VarVals), evaluateRec(t.right, VarVals));
	}

/**
 * This method approximates any part of the expression it can.	
 * @return The function with all possible approximations. 
 */
	public ExpFormula approximate()
	{
		ExpTreenode newForm = root.clone();
		ExpFormula retform = new ExpFormula();
		newForm = approximateRec(newForm);
		
		retform.setRoot(newForm);
		retform.simplify();
		return retform;
	}
	
	private ExpTreenode approximateRec(ExpTreenode t)
	{
		if (t == null) return null;
		
		if (t.op_str.equals("#"))
			return new ExpTreenode("#", t.num.approx(), approximateRec(t.left), approximateRec(t.right));
		else
			return new ExpTreenode(t.op_str, null, approximateRec(t.left), approximateRec(t.right));
	}

/**
 * This method finds the symbolic derivative of the function with respect to the
 * variable var.  Formally, this is the partial derivative of the function with
 * respect to var.  Note that the derivative will not be completely simplified
 * but some simplifications will be done. 	
 * @param var string containing the variable which the derivative will be with
 * respect to.
 * @return The partial derivative of the current function with respect to var.
 */
	public ExpFormula diff(String var)  //  Will not completely simplify the expression.
	{
		ExpTreenode newForm = root.clone();
		ExpFormula retform = new ExpFormula();
		try
		{
			newForm = diffRec(newForm, var);
		} 
		catch (Exception ex) 
		{
			throw new SyntaxErrorException("The function is not differentiable.");
		}
		
		simplified = true;
		while (simplified) 
		{
			simplified = false;
			newForm = simplifyZeroOne(newForm);
		}		

		retform.setRoot(newForm);
		return retform;
	}
	
	private ExpTreenode diffRec(ExpTreenode t, String var)
	{
		if (t == null) return null;
		String TestString = t.op_str.toLowerCase();

		if (isNumericTree(t))
		{
			return new ExpTreenode(ComplexNumber.makeZero());
		}
		else if (Terminal.indexOf(TestString) >= 0)
		{
			if (TestString.equalsIgnoreCase(var))
				return new ExpTreenode(ComplexNumber.makeOne());
			else
				return new ExpTreenode(ComplexNumber.makeZero());
		}
		else if (TestString.equalsIgnoreCase("+"))
		{
			return new ExpTreenode("+", null, diffRec(t.left, var), diffRec(t.right, var));
		}
		else if (TestString.equalsIgnoreCase("-"))
		{
			return new ExpTreenode("-", null, diffRec(t.left, var), diffRec(t.right, var));
		}
		else if (TestString.equalsIgnoreCase("*"))
		{
			ExpTreenode leftTree = t.left.clone();
			ExpTreenode rightTree = t.right.clone();
			ExpTreenode diffLeft = new ExpTreenode("*", null, leftTree, diffRec(t.right, var));
			ExpTreenode diffRight = new ExpTreenode("*", null, diffRec(t.left, var), rightTree);
			
			return new ExpTreenode("+", null, diffLeft, diffRight);
		}
		else if (TestString.equalsIgnoreCase("/"))
		{
			ExpTreenode leftTree = t.left.clone();
			ExpTreenode rightTree = t.right.clone();
			ExpTreenode DemSqTree = new ExpTreenode("^", null, t.right.clone(), new ExpTreenode(new ComplexNumber(2, 0)));
			ExpTreenode NumRight = new ExpTreenode("*", null, leftTree, diffRec(t.right, var));
			ExpTreenode NumLeft = new ExpTreenode("*", null, diffRec(t.left, var), rightTree);
			ExpTreenode NumTree = new ExpTreenode("-", null, NumLeft, NumRight);
			
			return new ExpTreenode("/", null, NumTree, DemSqTree);
		}
		else if (TestString.equalsIgnoreCase("^"))  //  Note: #^# already taken care of.
		{
			if (isNumericTree(t.right))  //  f(x)^n
			{
				ExpTreenode leftTree = t.left.clone();
				ComplexNumber cexp = evaluateNumericSubtree(t.right);
				ComplexNumber newExp = cexp.subtract(ComplexNumber.makeOne());
				ExpTreenode fct_pow = new ExpTreenode("^", null, leftTree.clone(), new ExpTreenode(newExp));
				ExpTreenode coeff_fct_pow = new ExpTreenode("*", null, new ExpTreenode(cexp), fct_pow);
				
				return new ExpTreenode("*", null, coeff_fct_pow, diffRec(leftTree, var));
			}
			else if (isNumericTree(t.left))  //  n^f(x)
			{
				ComplexNumber cbase = evaluateNumericSubtree(t.left);
				ExpTreenode expTree = t.right.clone();
				
				ExpTreenode fct_pow = new ExpTreenode("^", null, new ExpTreenode(cbase), expTree);
				ExpTreenode log_base = new ExpTreenode("ln", null, null, new ExpTreenode(cbase));
				ExpTreenode fct_log = new ExpTreenode("*", null, log_base, fct_pow);
				
				return new ExpTreenode("*", null, fct_log, diffRec(t.right.clone(), var));
			}
			else  //  h(x) = f(x)^g(x)
			{
				ExpTreenode f = t.left.clone();
				ExpTreenode g = t.right.clone();
				ExpTreenode h = t.clone();
				ExpTreenode fder = diffRec(f, var);
				ExpTreenode gder = diffRec(g, var);
				ExpTreenode fder_f = new ExpTreenode("/", null, fder, f.clone());
				ExpTreenode g_fder_f = new ExpTreenode("*", null, g, fder_f);
				ExpTreenode log_f = new ExpTreenode("ln", null, null, f);
				ExpTreenode gder_log_f = new ExpTreenode("*", null, gder, log_f);
				ExpTreenode rightFact = new ExpTreenode("+", null, g_fder_f, gder_log_f);
				
				return new ExpTreenode("*", null, h, rightFact);
			}
		}
		else if (TestString.equalsIgnoreCase("ln") || TestString.equalsIgnoreCase("log"))
		{
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			return new ExpTreenode("/", null, fder, f);			
		}
		else if (TestString.equalsIgnoreCase("log10"))
		{
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			ExpTreenode fder_f = new ExpTreenode("/", null, fder, f.clone());
			ComplexNumber ln10 = new ComplexNumber(Math.log(10.0), 0.0);
			
			return new ExpTreenode("/", null, fder_f, new ExpTreenode(ln10));
		}
		else if (TestString.equalsIgnoreCase("sin")) 
		{
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			ExpTreenode cos_f = new ExpTreenode("cos", null, null, f);
			
			return new ExpTreenode("*", null, cos_f, fder);
		}
		else if (TestString.equalsIgnoreCase("cos")) 
		{
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			ExpTreenode sin_f = new ExpTreenode("sin", null, null, f);
			ExpTreenode sin_f_fder = new ExpTreenode("*", null, sin_f, fder);
			
			return new ExpTreenode("-", null, null, sin_f_fder);
		}
		else if (TestString.equalsIgnoreCase("tan")) 
		{
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			ExpTreenode sec_f = new ExpTreenode("sec", null, null, f);
			ExpTreenode sec_f_sq = new ExpTreenode("^", null, sec_f, new ExpTreenode(new ComplexNumber(2, 1, 0, 1)));
			
			return new ExpTreenode("*", null, sec_f_sq, fder);
		}
		else if (TestString.equalsIgnoreCase("cot")) 
		{
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			ExpTreenode csc_f = new ExpTreenode("csc", null, null, f);
			ExpTreenode csc_f_sq = new ExpTreenode("^", null, csc_f, new ExpTreenode(new ComplexNumber(2, 1, 0, 1)));
			ExpTreenode neg_csc_f_sq = new ExpTreenode("-", null, null, csc_f_sq);
			
			return new ExpTreenode("*", null, neg_csc_f_sq, fder);
		}
		else if (TestString.equalsIgnoreCase("sec")) 
		{
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			ExpTreenode sec_f = new ExpTreenode("sec", null, null, f.clone());
			ExpTreenode tan_f = new ExpTreenode("tan", null, null, f);
			ExpTreenode sec_f_tan_f = new ExpTreenode("*", null, sec_f, tan_f);
			
			return new ExpTreenode("*", null, sec_f_tan_f, fder);
		}
		else if (TestString.equalsIgnoreCase("csc")) 
		{
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			ExpTreenode csc_f = new ExpTreenode("csc", null, null, f.clone());
			ExpTreenode cot_f = new ExpTreenode("cot", null, null, f);
			ExpTreenode csc_f_cot_f = new ExpTreenode("*", null, csc_f, cot_f);
			ExpTreenode neg_csc_f_cot_f = new ExpTreenode("-", null, null, csc_f_cot_f);
			
			return new ExpTreenode("*", null, neg_csc_f_cot_f, fder);
		}
		else if (TestString.equalsIgnoreCase("exp")) 
		{
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			ExpTreenode exp_f = new ExpTreenode("exp", null, null, f);
			
			return new ExpTreenode("*", null, exp_f, fder);
		}
		else if (TestString.equalsIgnoreCase("sinh")) 
		{
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			ExpTreenode cosh_f = new ExpTreenode("cosh", null, null, f);
			
			return new ExpTreenode("*", null, cosh_f, fder);
		}
		else if (TestString.equalsIgnoreCase("cosh")) 
		{
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			ExpTreenode sinh_f = new ExpTreenode("sinh", null, null, f);
			
			return new ExpTreenode("*", null, sinh_f, fder);
		}
		else if (TestString.equalsIgnoreCase("tanh"))
		{
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			ExpTreenode sech_f = new ExpTreenode("sech", null, null, f);
			ExpTreenode sech_f_sq = new ExpTreenode("^", null, sech_f, new ExpTreenode(new ComplexNumber(2, 1, 0, 1)));
			
			return new ExpTreenode("*", null, sech_f_sq, fder);
		}
		else if (TestString.equalsIgnoreCase("coth"))
		{
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			ExpTreenode csch_f = new ExpTreenode("csch", null, null, f);
			ExpTreenode csch_f_sq = new ExpTreenode("^", null, csch_f, new ExpTreenode(new ComplexNumber(2, 1, 0, 1)));
			ExpTreenode neg_csch_f_sq = new ExpTreenode("-", null, null, csch_f_sq);
			
			return new ExpTreenode("*", null, neg_csch_f_sq, fder);
		}
		else if (TestString.equalsIgnoreCase("sech"))
		{
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			ExpTreenode sech_f = new ExpTreenode("sech", null, null, f.clone());
			ExpTreenode tanh_f = new ExpTreenode("tanh", null, null, f);
			ExpTreenode sech_f_tanh_f = new ExpTreenode("*", null, sech_f, tanh_f);
			ExpTreenode neg_sech_f_tanh_f = new ExpTreenode("-", null, null, sech_f_tanh_f);
			
			return new ExpTreenode("*", null, neg_sech_f_tanh_f, fder);
		}
		else if (TestString.equalsIgnoreCase("csch"))
		{
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			ExpTreenode csch_f = new ExpTreenode("csch", null, null, f.clone());
			ExpTreenode coth_f = new ExpTreenode("coth", null, null, f);
			ExpTreenode csch_f_coth_f = new ExpTreenode("*", null, csch_f, coth_f);
			ExpTreenode neg_csch_f_coth_f = new ExpTreenode("-", null, null, csch_f_coth_f);
			
			return new ExpTreenode("*", null, neg_csch_f_coth_f, fder);
		}
		else if (TestString.equalsIgnoreCase("asin"))
		{
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			ExpTreenode f_sq = new ExpTreenode("^", null, f, new ExpTreenode(new ComplexNumber(2, 1, 0, 1)));
			ExpTreenode one_minus_f_sq = new ExpTreenode("-", null, new ExpTreenode(ComplexNumber.makeOne()), f_sq);
			ExpTreenode sqrt_one_minus_f_sq = new ExpTreenode("^", null, one_minus_f_sq, new ExpTreenode(new ComplexNumber(1, 2, 0, 1)));
			ExpTreenode one_div_sqrt_one_minus_f_sq = new ExpTreenode("/", null, new ExpTreenode(ComplexNumber.makeOne()), sqrt_one_minus_f_sq);
			
			return new ExpTreenode("*", null, one_div_sqrt_one_minus_f_sq, fder);
		}
		else if (TestString.equalsIgnoreCase("acos"))
		{
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			ExpTreenode f_sq = new ExpTreenode("^", null, f, new ExpTreenode(new ComplexNumber(2, 1, 0, 1)));
			ExpTreenode one_minus_f_sq = new ExpTreenode("-", null, new ExpTreenode(ComplexNumber.makeOne()), f_sq);
			ExpTreenode sqrt_one_minus_f_sq = new ExpTreenode("^", null, one_minus_f_sq, new ExpTreenode(new ComplexNumber(1, 2, 0, 1)));
			ExpTreenode one_div_sqrt_one_minus_f_sq = new ExpTreenode("/", null, new ExpTreenode(ComplexNumber.makeOne()), sqrt_one_minus_f_sq);
			ExpTreenode neg_one_div_sqrt_one_minus_f_sq = new ExpTreenode("-", null, null, one_div_sqrt_one_minus_f_sq);
			
			return new ExpTreenode("*", null, neg_one_div_sqrt_one_minus_f_sq, fder);
		}
		else if (TestString.equalsIgnoreCase("atan"))
		{
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			ExpTreenode f_sq = new ExpTreenode("^", null, f, new ExpTreenode(new ComplexNumber(2, 1, 0, 1)));
			ExpTreenode one_plus_f_sq = new ExpTreenode("+", null, new ExpTreenode(ComplexNumber.makeOne()), f_sq);
			ExpTreenode one_div_one_plus_f_sq = new ExpTreenode("/", null, new ExpTreenode(ComplexNumber.makeOne()), one_plus_f_sq);
			
			return new ExpTreenode("*", null, one_div_one_plus_f_sq, fder);
		}
		else if (TestString.equalsIgnoreCase("acot"))
		{
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			ExpTreenode f_sq = new ExpTreenode("^", null, f, new ExpTreenode(new ComplexNumber(2, 1, 0, 1)));
			ExpTreenode one_plus_f_sq = new ExpTreenode("+", null, new ExpTreenode(ComplexNumber.makeOne()), f_sq);
			ExpTreenode one_div_one_plus_f_sq = new ExpTreenode("/", null, new ExpTreenode(ComplexNumber.makeOne()), one_plus_f_sq);
			ExpTreenode neg_one_div_one_plus_f_sq = new ExpTreenode("-", null, null, one_div_one_plus_f_sq);
			
			return new ExpTreenode("*", null, neg_one_div_one_plus_f_sq, fder);
		}
		else if (TestString.equalsIgnoreCase("asec"))
		{
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			ExpTreenode f_sq = new ExpTreenode("^", null, f, new ExpTreenode(new ComplexNumber(2, 1, 0, 1)));
			ExpTreenode one_div_f_sq = new ExpTreenode("/", null, new ExpTreenode(ComplexNumber.makeOne()), f_sq.clone());
			ExpTreenode one_minus_one_div_f_sq = new ExpTreenode("-", null, new ExpTreenode(ComplexNumber.makeOne()), one_div_f_sq);
			ExpTreenode sqrt_one_minus_one_div_f_sq = new ExpTreenode("^", null, one_minus_one_div_f_sq, new ExpTreenode(new ComplexNumber(1, 2, 0, 1)));
			ExpTreenode f_sq_sqrt_one_minus_one_div_f_sq = new ExpTreenode("*", null, f_sq, sqrt_one_minus_one_div_f_sq);
			ExpTreenode one_div_f_sq_sqrt_one_minus_one_div_f_sq = new ExpTreenode("/", null, new ExpTreenode(ComplexNumber.makeOne()), f_sq_sqrt_one_minus_one_div_f_sq);
			
			return new ExpTreenode("*", null, one_div_f_sq_sqrt_one_minus_one_div_f_sq, fder);
		}
		else if (TestString.equalsIgnoreCase("acsc"))
		{
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			ExpTreenode f_sq = new ExpTreenode("^", null, f, new ExpTreenode(new ComplexNumber(2, 1, 0, 1)));
			ExpTreenode one_div_f_sq = new ExpTreenode("/", null, new ExpTreenode(ComplexNumber.makeOne()), f_sq.clone());
			ExpTreenode one_minus_one_div_f_sq = new ExpTreenode("-", null, new ExpTreenode(ComplexNumber.makeOne()), one_div_f_sq);
			ExpTreenode sqrt_one_minus_one_div_f_sq = new ExpTreenode("^", null, one_minus_one_div_f_sq, new ExpTreenode(new ComplexNumber(1, 2, 0, 1)));
			ExpTreenode f_sq_sqrt_one_minus_one_div_f_sq = new ExpTreenode("*", null, f_sq, sqrt_one_minus_one_div_f_sq);
			ExpTreenode one_div_f_sq_sqrt_one_minus_one_div_f_sq = new ExpTreenode("/", null, new ExpTreenode(ComplexNumber.makeOne()), f_sq_sqrt_one_minus_one_div_f_sq);
			ExpTreenode neg_one_div_f_sq_sqrt_one_minus_one_div_f_sq = new ExpTreenode("-", null, null, one_div_f_sq_sqrt_one_minus_one_div_f_sq);
			
			return new ExpTreenode("*", null, neg_one_div_f_sq_sqrt_one_minus_one_div_f_sq, fder);
		}
		else if (TestString.equalsIgnoreCase("asinh"))
		{
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			ExpTreenode f_sq = new ExpTreenode("^", null, f, new ExpTreenode(new ComplexNumber(2, 1, 0, 1)));
			ExpTreenode one_plus_f_sq = new ExpTreenode("+", null, new ExpTreenode(ComplexNumber.makeOne()), f_sq);
			ExpTreenode sqrt_one_plus_f_sq = new ExpTreenode("^", null, one_plus_f_sq, new ExpTreenode(new ComplexNumber(1, 2, 0, 1)));
			ExpTreenode one_div_sqrt_one_plus_f_sq = new ExpTreenode("/", null, new ExpTreenode(ComplexNumber.makeOne()), sqrt_one_plus_f_sq);
			
			return new ExpTreenode("*", null, one_div_sqrt_one_plus_f_sq, fder);
		}
		else if (TestString.equalsIgnoreCase("acosh"))
		{
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			ExpTreenode f_minus_one = new ExpTreenode("-", null, f.clone(), new ExpTreenode(ComplexNumber.makeOne()));
			ExpTreenode f_plus_one = new ExpTreenode("+", null, f.clone(), new ExpTreenode(ComplexNumber.makeOne()));
			ExpTreenode sqrt_f_minus_one = new ExpTreenode("^", null, f_minus_one, new ExpTreenode(new ComplexNumber(1, 2, 0, 1)));
			ExpTreenode sqrt_f_plus_one = new ExpTreenode("^", null, f_plus_one, new ExpTreenode(new ComplexNumber(1, 2, 0, 1)));
			ExpTreenode sqrt_f_minus_one_sqrt_f_plus_one = new ExpTreenode("*", null, sqrt_f_minus_one, sqrt_f_plus_one);
			ExpTreenode one_div_sqrt_f_minus_one_sqrt_f_plus_one = new ExpTreenode("/", null, new ExpTreenode(ComplexNumber.makeOne()), sqrt_f_minus_one_sqrt_f_plus_one);
			
			return new ExpTreenode("*", null, one_div_sqrt_f_minus_one_sqrt_f_plus_one, fder);
		}
		else if (TestString.equalsIgnoreCase("atanh") || TestString.equalsIgnoreCase("acoth"))
		{
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			ExpTreenode f_sq = new ExpTreenode("^", null, f, new ExpTreenode(new ComplexNumber(2, 1, 0, 1)));
			ExpTreenode one_minus_f_sq = new ExpTreenode("-", null, new ExpTreenode(ComplexNumber.makeOne()), f_sq);
			ExpTreenode one_div_one_minus_f_sq = new ExpTreenode("/", null, new ExpTreenode(ComplexNumber.makeOne()), one_minus_f_sq);
			
			return new ExpTreenode("*", null, one_div_one_minus_f_sq, fder);
		}
		else if (TestString.equalsIgnoreCase("asech"))
		{			
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			ExpTreenode f_sq = new ExpTreenode("^", null, f, new ExpTreenode(new ComplexNumber(2, 1, 0, 1)));
			ExpTreenode one_div_f = new ExpTreenode("/", null, new ExpTreenode(ComplexNumber.makeOne()), f);
			ExpTreenode one_div_f_minus_one = new ExpTreenode("-", null, one_div_f.clone(), new ExpTreenode(ComplexNumber.makeOne()));
			ExpTreenode one_div_f_plus_one = new ExpTreenode("+", null, one_div_f.clone(), new ExpTreenode(ComplexNumber.makeOne()));
			ExpTreenode sqrt_one_div_f_minus_one = new ExpTreenode("^", null, one_div_f_minus_one, new ExpTreenode(new ComplexNumber(1, 2, 0, 1)));
			ExpTreenode sqrt_one_div_f_plus_one = new ExpTreenode("^", null, one_div_f_plus_one, new ExpTreenode(new ComplexNumber(1, 2, 0, 1)));
			ExpTreenode sqrt_one_div_f_minus_one_sqrt_one_div_f_plus_one = new ExpTreenode("*", null, sqrt_one_div_f_minus_one, sqrt_one_div_f_plus_one);
			ExpTreenode f_sq_sqrt_one_div_f_minus_one_sqrt_one_div_f_plus_one = new ExpTreenode("*", null, f_sq, sqrt_one_div_f_minus_one_sqrt_one_div_f_plus_one);
			ExpTreenode one_div_f_sq_sqrt_one_div_f_minus_one_sqrt_one_div_f_plus_one = new ExpTreenode("/", null, new ExpTreenode(ComplexNumber.makeOne()), f_sq_sqrt_one_div_f_minus_one_sqrt_one_div_f_plus_one);
			ExpTreenode neg_one_div_f_sq_sqrt_one_div_f_minus_one_sqrt_one_div_f_plus_one = new ExpTreenode("-", null, null, one_div_f_sq_sqrt_one_div_f_minus_one_sqrt_one_div_f_plus_one);

			return new ExpTreenode("*", null, neg_one_div_f_sq_sqrt_one_div_f_minus_one_sqrt_one_div_f_plus_one, fder);
		}
		else if (TestString.equalsIgnoreCase("acsch"))
		{
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			ExpTreenode f_sq = new ExpTreenode("^", null, f, new ExpTreenode(new ComplexNumber(2, 1, 0, 1)));
			ExpTreenode one_div_f_sq = new ExpTreenode("/", null, new ExpTreenode(ComplexNumber.makeOne()), f_sq.clone());
			ExpTreenode one_plus_one_div_f_sq = new ExpTreenode("+", null, new ExpTreenode(ComplexNumber.makeOne()), one_div_f_sq);
			ExpTreenode sqrt_one_plus_one_div_f_sq = new ExpTreenode("^", null, one_plus_one_div_f_sq, new ExpTreenode(new ComplexNumber(1, 2, 0, 1)));
			ExpTreenode f_sq_sqrt_one_plus_one_div_f_sq = new ExpTreenode("*", null, f_sq, sqrt_one_plus_one_div_f_sq);
			ExpTreenode one_div_f_sq_sqrt_one_plus_one_div_f_sq = new ExpTreenode("/", null, new ExpTreenode(ComplexNumber.makeOne()), f_sq_sqrt_one_plus_one_div_f_sq);
			ExpTreenode neg_one_div_f_sq_sqrt_one_plus_one_div_f_sq = new ExpTreenode("-", null, null, one_div_f_sq_sqrt_one_plus_one_div_f_sq);
			
			return new ExpTreenode("*", null, neg_one_div_f_sq_sqrt_one_plus_one_div_f_sq, fder);
		}
		else if (TestString.equalsIgnoreCase("sqrt"))
		{
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			ExpTreenode two_t = new ExpTreenode("*", null, new ExpTreenode(new ComplexNumber(2, 1, 0, 1)), t.clone());
			ExpTreenode one_div_two_t = new ExpTreenode("/", null, new ExpTreenode(ComplexNumber.makeOne()), two_t);

			return new ExpTreenode("*", null, one_div_two_t, fder);
		}
		else if (TestString.equalsIgnoreCase("abs") || TestString.equalsIgnoreCase("mod"))
		{
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			ExpTreenode sign_f = new ExpTreenode("sign", null, null, f);

			return new ExpTreenode("*", null, sign_f, fder);
		}
		else if (TestString.equalsIgnoreCase("sign"))
		{
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			ExpTreenode zeron_f = new ExpTreenode("zeron", null, null, f);

			return new ExpTreenode("*", null, zeron_f, fder);
		}
		else if (TestString.equalsIgnoreCase("zeron"))
		{
			ExpTreenode f = t.right.clone();
			ExpTreenode fder = diffRec(f, var);
			ExpTreenode zeron_f = new ExpTreenode("zeron", null, null, f);

			return new ExpTreenode("*", null, zeron_f, fder);
		}
		else if (TestString.equalsIgnoreCase("arg") || TestString.equalsIgnoreCase("carg"))
		{
			throw new SyntaxErrorException("The arg(z) and carg(z) functions are not differentiable.");
		}
		else  //  Should not happen
		{ }
		
		return null;  //  Should not happen
	}
	
}

