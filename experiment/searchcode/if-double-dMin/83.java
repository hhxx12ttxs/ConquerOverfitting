package lipstone.joshua.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lipstone.joshua.parser.backend.StepSolveThread;
import lipstone.joshua.parser.exceptions.ParserException;
import lipstone.joshua.parser.exceptions.UndefinedResultException;
import lipstone.joshua.parser.types.BigDec;
import lipstone.joshua.parser.types.BigInt;

public class NumericalSolver {
	protected BigDec accuracy = new BigDec(0.000001);
	public final BigDec defaultDelta = new BigDec(5.0);
	public BigDec xMin = BigDec.ZERO, xMax = new BigDec(2000), domainMax = new BigDec(2000), domainMin = new BigDec(-2000), deltaX = defaultDelta;
	public String method = "domain", previousInput = "";
	public boolean configWindowExists = false;
	public int maxIterations = 200;
	protected String var = "", initialInput = "";
	public String[] substitutions = {"sin", "cos", "tan", "sec", "csc", "cot", "arcsin", "arccos", "arctan", "arcsec", "arccsc", "arccot",
			"ln", "e^", "sinh", "cosh", "tanh", "sech", "csch", "coth", "("};
	public String[] reverseSubstitutions = {"arcsin(z)", "arccos(z)", "arctan(z)", "arcsec(z)", "arccsc(z)", "arccot(z)", "sin(z)",
			"cos(z)", "tan(z)", "sec(z)", "csc(z)", "cot(z)", "e^(z)", "log(z, e)", "arcsinh(z)", "arccosh(z)", "arctanh(z)", "arcsech(z)", "arccsch(z)", "arccoth(z)", "z"};
	public String[] functionDerivatives = {"sin({S})", "cos({S})", "cos({S})", "-sin({S})", "tan({S})", "(sec(({S})))^2",
			"sec({S})", "sec({S})*tan({S})", "csc({S})", "-csc({S})*cot({S})", "cot({S})", "-(csc(({S})))^2"};
	
	public NumericalSolver() {}
	
	public String solve(String input, Parser parser) throws ParserException {
		ArrayList<String> parts = parser.fromCommaList(input);
		if (parts.size() >= 4)
			deltaX = new BigDec(parser.run(parts.get(3)));
		if (parts.size() >= 3) {
			setDomain(new BigDec(parser.run(parts.get(1))), new BigDec(parser.run(parts.get(2))));
			if (parts.get(1).toLowerCase().contains("infinity") || parts.get(2).toLowerCase().contains("infinity"))
				method = "infinite";
		}
		return solve(parts.get(0), true, true, parser);
	}
	
	public String solve(String input, boolean useSub, boolean useOneTerm, Parser parser) throws ParserException {
		input = input.replaceAll("E", "*10^");
		if (useSub)
			initialInput = new String(input);
		String solutions = "";
		if (input.split("=").length != 2)
			return "inp input";
		parser.vars = parser.getVariables(input);
		var = parser.vars.get(0);
		if (parser.vars.size() == 0 || (parser.vars.size() == 1 && parser.vars.get(0).equals("")) || !parser.containsVariable(input, parser.vars)) {
			if (new BigDec(parser.run(input.split("=")[0])).eq(new BigDec(parser.run(input.split("=")[1]))))
				return "true";
			else
				return "false";
		}
		ArrayList<BigDec> solutionsList = new ArrayList<BigDec>();
		input = parser.preProcess(input, parser.vars);
		if (useOneTerm)
			solutionsList = oneTermSolve(factor(input, parser), parser);
		if (solutionsList.size() == 0 && useSub)
			solutionsList = substitutionSolve(input, parser);
		if (useSub)
			input = new String(initialInput);
		input = parser.seekOperations(input);
		if (solutionsList.size() == 0)
			solutionsList = rationalRootsSolve(input, parser);
		if (solutionsList.size() == 0)
			solutionsList = formSolve(input, parser);
		if (solutionsList.size() == 0 && useSub && useOneTerm)
			solutionsList = stepSolve(input, parser);
		String[] temp = input.split("=");
		if (temp[0].charAt(0) == '(' && parser.getEndIndex(temp[0], 0) == temp[0].length() - 1)
			temp[0] = temp[0].substring(1, temp[0].length() - 1);
		if (temp[1].charAt(0) == '(' && parser.getEndIndex(temp[1], 0) == temp[1].length() - 1)
			temp[1] = temp[1].substring(1, temp[1].length() - 1);
		String equation = temp[0] + invertSign(temp[1], parser);
		
		if (solutionsList.size() < orderOfEquation(sortTerms(simplifyTerms(getTerms(equation, parser), parser), parser), parser).abs().doubleValue()) {
			solutionsList.add(BigDec.ZERO);
			if (!isAnswer(equation, solutionsList.get(solutionsList.size() - 1), BigDec.ZERO, accuracy, parser))
				solutionsList.remove(solutionsList.size() - 1);
		}
		
		Collections.sort(solutionsList);
		for (BigDec d : solutionsList) {
			if (solutions.equals(""))
				solutions = d.toString();
			else
				solutions = solutions + ", " + d.toString();
		}
		if (solutions.length() < 1)
			solutions = "no solution in (" + domainMin + ", " + domainMax + ")";
		previousInput = input;
		method = "domain";
		return solutions;
	}
	
	private ArrayList<BigDec> checkSolutions(ArrayList<BigDec> solutionsList, String equation, BigDec accuracy, Parser parser) throws ParserException {
		for (int i = 0; i < solutionsList.size(); i++) {
			if (!isAnswer(equation, solutionsList.get(i), BigDec.ZERO, accuracy, parser)) {
				solutionsList.remove(i);
				i--;
			}
		}
		return solutionsList;
	}
	
	public ArrayList<BigDec> stepSolve(String equation, Parser parser) throws UndefinedResultException {
		if (method.equalsIgnoreCase("infinite"))
			return stepSolveInfinite(equation, parser);
		else
			return stepSolveDomain(equation, xMin, xMax, deltaX, parser);
	}
	
	public ArrayList<BigDec> stepSolveDomain(String equation, BigDec xMin, BigDec xMax, BigDec deltaX, Parser parser) throws UndefinedResultException {
		int numCores = Runtime.getRuntime().availableProcessors();
		BigDec range = xMax.subtract(xMin).divide(new BigDec(numCores));
		System.out.println(range);
		ArrayList<BigDec> output = new ArrayList<BigDec>();
		
		ArrayList<StepSolveThread> threads = new ArrayList<StepSolveThread>();
		for (int i = 0; i < numCores; i++) {	//Initializes and starts the threads
			System.out.println(xMin.add(new BigDec(i).multiply(range)) + " : " + xMin.add(new BigDec(i + 1).multiply(range)));
			threads.add(new StepSolveThread(equation, parser, deltaX, accuracy, xMin.add(new BigDec(i).multiply(range)), xMin.add(new BigDec(i + 1).multiply(range)), parser.vars, i));
			threads.get(i).start();
		}
		for (int i = 0; i < threads.size(); i++) {
			try {
				threads.get(i).join();
			}
			catch (InterruptedException e) {
				//Nothing should happen here
			}
			output.addAll(threads.get(i).answer);
		}
		return output;
	}
	
	public ArrayList<BigDec> stepSolveInfinite(String equation, Parser parser) throws UndefinedResultException {
		BigDec xMin = BigDec.ZERO, xMax = new BigDec(1000);
		ArrayList<BigDec> answers = new ArrayList<BigDec>();
		while (answers.size() == 0) {
			for (BigDec answer : stepSolveDomain(equation, xMin, xMax, deltaX, parser))
				answers.add(answer);
			if (xMin.gteq(BigDec.ZERO))
				xMin = xMin.add(xMax.multiply(new BigDec(2)));
			xMin = xMin.multiply(BigDec.MINUSONE);
		}
		return answers;
	}
	
	public ArrayList<BigDec> formSolve(String equation, Parser parser) throws ParserException {
		equation = distributeExponenets(equation, parser);
		ArrayList<BigDec> solutions = new ArrayList<BigDec>();
		String parts[] = equation.split("=");
		if (parts[0].charAt(0) == '(' && parser.getEndIndex(parts[0], 0) == parts[0].length() - 1)
			parts[0] = parts[0].substring(1, parts[0].length() - 1);
		if (parts[1].charAt(0) == '(' && parser.getEndIndex(parts[1], 0) == parts[1].length() - 1)
			parts[1] = parts[1].substring(1, parts[1].length() - 1);
		equation = parts[0] + invertSign(parts[1], parser);
		ArrayList<String> eqn = distributiveProperty(getTerms(equation, parser), parser);
		BigDec modifier = BigDec.ZERO;
		for (int i = 1; i > -2; i -= 2) {
			if (containsTerms(new BigDec[]{new BigDec(i), BigDec.ZERO}, eqn, parser) && solutions.size() == 0) {
				BigDec a = BigDec.ZERO, b = BigDec.ZERO;
				modifier = getModifier(new BigDec[]{new BigDec(i), BigDec.ZERO}, eqn, parser);
				for (String str : eqn) {
					if (orderOfTerm(str, parser)[0].add(modifier).eq(new BigDec(i)))
						a = a.add(getCoefficient(str, parser));
					if (orderOfTerm(str, parser)[0].add(modifier).eq(BigDec.ZERO))
						b = b.add(getCoefficient(str, parser));
				}
				for (BigDec d : linearSolve(equation, a, b, parser))
					solutions.add(d);
			}
			if (containsTerms(new BigDec[]{new BigDec(i).multiply(new BigDec(2)), new BigDec(i), BigDec.ZERO}, eqn, parser) && solutions.size() == 0) {
				BigDec a = BigDec.ZERO, b = BigDec.ZERO, c = BigDec.ZERO;
				modifier = getModifier(new BigDec[]{new BigDec(i).multiply(new BigDec(2)), new BigDec(i), BigDec.ZERO}, eqn, parser);
				for (String str : eqn) {
					if (orderOfTerm(str, parser)[0].add(modifier).eq(new BigDec(i).multiply(new BigDec(2))))
						a = a.add(getCoefficient(str, parser));
					if (orderOfTerm(str, parser)[0].add(modifier).eq(new BigDec(i)))
						b = b.add(getCoefficient(str, parser));
					if (orderOfTerm(str, parser)[0].add(modifier).eq(BigDec.ZERO))
						c = c.add(getCoefficient(str, parser));
				}
				for (BigDec d : quadraticSolve(equation, a, b, c, parser))
					solutions.add(d);
			}
		}
		
		if (containsTerms(new BigDec[]{BigDec.ONE, BigDec.ZERO, BigDec.MINUSONE}, eqn, parser) && solutions.size() == 0) {
			BigDec a = BigDec.ZERO, b = BigDec.ZERO, c = BigDec.ZERO;
			modifier = getModifier(new BigDec[]{BigDec.ONE, BigDec.ZERO, BigDec.MINUSONE}, eqn, parser);
			for (String str : eqn) {
				if (orderOfTerm(str, parser)[0].add(modifier).eq(BigDec.ONE))
					a = a.add(getCoefficient(str, parser));
				if (orderOfTerm(str, parser)[0].add(modifier).eq(BigDec.MINUSONE))
					b = b.add(getCoefficient(str, parser));
				if (orderOfTerm(str, parser)[0].add(modifier).eq(BigDec.ZERO))
					c = c.add(getCoefficient(str, parser));
			}
			for (BigDec d : quadraticSolve(equation, a, c, b, parser))
				solutions.add(d);
		}
		if (eqn.size() == 2 && solutions.size() == 0) {
			BigDec m = orderOfTerm(eqn.get(0), parser)[0], a = getCoefficient(eqn.get(0), parser), n = orderOfTerm(eqn.get(1), parser)[0], b = getCoefficient(eqn.get(1), parser);
			solutions.add(new BigDec(parser.run("((0-" + b + ")/(0+" + a + "))^(1/(" + m + "-" + n + "))")));
			if (!isAnswer(equation, solutions.get(solutions.size() - 1), BigDec.ZERO, accuracy, parser))
				solutions.remove(solutions.size() - 1);
			solutions.add(BigDec.ZERO);
			if (!isAnswer(equation, solutions.get(solutions.size() - 1), BigDec.ZERO, accuracy, parser))
				solutions.remove(solutions.size() - 1);
		}
		return checkSolutions(solutions, equation, accuracy, parser);
	}
	
	private BigDec getModifier(BigDec orders[], ArrayList<String> eqn, Parser parser) throws ParserException {
		BigDec largest = BigDec.ZERO;
		for (int i = 0; i < orders.length; i++) {
			if (orders[i].abs().gt(largest.abs()))
				largest = orders[i];
		}
		return largest.subtract(orderOfEquation(eqn, parser));
	}
	
	public ArrayList<BigDec> linearSolve(String equation, BigDec a, BigDec b, Parser parser) throws ParserException {
		ArrayList<BigDec> solutions = new ArrayList<BigDec>();
		solutions.add(new BigDec(parser.run(parser.preProcess("(0-" + b + ")/(" + a + ")", parser.vars))));
		if (!isAnswer(equation, solutions.get(solutions.size() - 1), BigDec.ZERO, accuracy, parser))
			solutions.remove(solutions.size() - 1);
		return solutions;
	}
	
	public ArrayList<BigDec> quadraticSolve(String equation, BigDec a, BigDec b, BigDec c, Parser parser) throws ParserException {
		ArrayList<BigDec> solutions = new ArrayList<BigDec>();
		solutions.add(new BigDec(parser.run(parser.preProcess("(0-(" + b + ")+((" + b + ")^2-4*(" + a + ")*(" + c + "))^(1/2))/(2*(" + a + "))", parser.vars))));
		if (!isAnswer(equation, solutions.get(solutions.size() - 1), BigDec.ZERO, accuracy, parser))
			solutions.remove(solutions.size() - 1);
		solutions.add(new BigDec(parser.run(parser.preProcess("(0-(" + b + ")-((" + b + ")^2-4*(" + a + ")*(" + c + "))^(1/2))/(2*(" + a + "))", parser.vars))));
		if (!isAnswer(equation, solutions.get(solutions.size() - 1), BigDec.ZERO, accuracy, parser))
			solutions.remove(solutions.size() - 1);
		return solutions;
	}
	
	public ArrayList<BigDec> rationalRootsSolve(String equation, Parser parser) throws ParserException {
		ArrayList<BigDec> solutions = getRoots(equation, parser);
		String[] parts = equation.split("=");
		if (parts.length != 2)
			return solutions;
		String eqn = parts[0] + invertSign(parts[1], parser);
		for (int i = 0; i < solutions.size(); i++) {
			if (!isAnswer(eqn, solutions.get(i), BigDec.ZERO, accuracy, parser)) {
				solutions.remove(i);
				i--;
			}
		}
		return checkSolutions(solutions, equation, accuracy, parser);
	}
	
	public ArrayList<BigDec> getRoots(String equation, Parser parser) throws ParserException {
		ArrayList<BigDec> roots = new ArrayList<BigDec>();
		String[] parts = equation.split("=");
		if (parts.length != 2)
			parts = new String[]{equation, "0"};
		ArrayList<String> eqn = sortTerms(simplifyTerms(getTerms(parts[0] + invertSign(parts[1], parser), parser), parser), parser);
		if (eqn.size() < 1)
			return roots;
		for (String str : eqn)
			if (orderOfTerm(str, parser)[0].lt(BigDec.ZERO))
				return roots;
		BigDec coefficient = getCoefficient(eqn.get(0), parser);
		if (coefficient.isInt())
			for (int i = 0; i < eqn.size(); i++)
				eqn.set(i, eqn.get(i) + "/(" + coefficient + ")");
		else if (getCoefficient(eqn.get(eqn.size() - 1), parser).isInt())
			for (int i = 0; i < eqn.size(); i++)
				eqn.set(i, eqn.get(i) + "/(" + getCoefficient(eqn.get(eqn.size() - 1), parser) + ")");
		eqn = sortTerms(simplifyTerms(eqn, parser), parser);
		coefficient = getCoefficient(eqn.get(0), parser);
		if (coefficient.isInt())
			return roots;
		ArrayList<BigInt> aN = findFactors(coefficient.toBigInt(), parser), a0 = findFactors(getCoefficient(eqn.get(eqn.size() - 1), parser).toBigInt(), parser);
		for (BigInt i : a0)
			for (BigInt a : aN) {
				roots.add(new BigDec(i).divide(new BigDec(a)));
				roots.add(new BigDec(i).divide(new BigDec(a)).multiply(BigDec.MINUSONE));
			}
		return roots;
	}
	
	public String factor(String equation, Parser parser) throws ParserException {
		String initial = "", coefficient = "1.0", substitution = "";
		if (!parser.containsVariable(equation, parser.getVariables(equation))) {
			ArrayList<BigInt> factors = findFactors((new BigDec(parser.run(parser.preProcess(equation, parser.vars))).toBigInt()), parser);
			equation = factors.get(0).toString();
			for (int i = 1; i < factors.size(); i++)
				equation = equation + ", " + factors.get(i).toString();
			return equation;
		}
		boolean containsEquals = false;
		if (equation.contains("="))
			containsEquals = true;
		equation = parser.preProcess(equation, parser.getVariables(equation));
		String parts[] = seekSubstitutions(equation, parser);
		equation = parts[0];
		substitution = parts[1];
		parser.vars = parser.getVariables(equation);
		equation = factoring(equation, parser);
		if (equation.indexOf('(') < 0)
			return equation;
		while (!initial.equalsIgnoreCase(equation)) {
			initial = new String(equation);
			ArrayList<String> eqn = new ArrayList<String>();
			if (equation.charAt(0) != '(') {
				coefficient = parser.run(coefficient + "*" + equation.substring(0, equation.indexOf('('))).toString();
				equation = equation.substring(equation.indexOf('('));
			}
			Matcher m = Pattern.compile("\\)\\(").matcher(equation);
			while (m.find()) {
				eqn.add(removeExcessParentheses(equation.substring(1, m.start()), parser));
				equation = equation.substring(m.end() - 1);
				m = Pattern.compile("\\)\\(").matcher(equation);
			}
			if (equation.length() > 2)
				eqn.add(equation);
			equation = "";
			boolean done = true;
			for (int i = 0; i < eqn.size(); i++) {
				String temp = factoring(eqn.get(i), parser).replaceAll("=0", "");
				if (temp.length() >= 3) {
					if (!temp.equalsIgnoreCase(eqn.get(i)))
						done = false;
					if (temp.charAt(0) != '(')
						temp = "(" + temp;
					if (temp.charAt(temp.length() - 1) != ')')
						temp = temp + ")";
					equation = equation + temp;
				}
			}
			if (done)
				break;
		}
		if (containsEquals)
			equation = equation + "=0";
		if (substitution.length() > 2)
			equation = removeExcessParentheses(equation.replaceAll(parser.vars.get(0), "(" + substitution + ")"), parser);
		equation = (coefficient + "*" + equation).replaceAll("1.0\\*", "");
		parser.vars = parser.getVariables(equation);
		return equation;
	}
	
	public String factoring(String equation, Parser parser) throws ParserException {
		String output = "";
		ArrayList<String> factored = new ArrayList<String>();
		String[] parts = equation.split("=");
		if (parts.length != 2)
			parts = new String[]{equation, "0"};
		for (int i = 0; i < parts.length; i++)
			parts[i] = removeExcessParentheses(parts[i], parser);
		ArrayList<String> eqn = sortTerms(simplifyTerms(getTerms(parts[0] + invertSign(parts[1], parser), parser), parser), parser);
		if (eqn.size() < 2)
			return equation;
		else if (eqn.size() == 2 && orderOfTerm(eqn.get(0), parser)[0].lteq(BigDec.ONE))
			return equation;
		for (String str : eqn)
			if (orderOfTerm(str, parser)[0].lt(BigDec.ZERO))
				return equation;
		BigDec coefficient = getCoefficient(eqn.get(0), parser);
		//attempt to make coefficients into integers
		BigDec div = BigDec.ONE;
		if (coefficient.isInt())
			div = new BigDec(coefficient);
		else if (getCoefficient(eqn.get(eqn.size() - 1), parser).isInt())
			div = new BigDec(getCoefficient(eqn.get(eqn.size() - 1), parser));
		if (div.neq(BigDec.ONE))
			for (int i = 0; i < eqn.size(); i++)
				eqn.set(i, eqn.get(i) + "/(" + div + ")");
		coefficient = getCoefficient(eqn.get(0), parser);
		if (coefficient.isInt())
			return equation;
		equation = "";
		for (String str : eqn)
			equation = equation + "+" + str;
		if (equation.length() > 1)
			equation = parser.removeDoubles(equation.substring(1));
		ArrayList<BigDec> roots = getRoots(equation + "=0", parser);
		for (BigDec d : roots) {
			boolean isGood = true;
			String divisor = parser.removeDoubles(parser.vars.get(0) + "+" + d.toString());
			String temp = "(" + divisor + ")" + polynomialDivision(equation, divisor, parser);
			if (temp.contains("/(" + divisor + ")"))
				isGood = false;
			if (!(new BigDec(parser.run(parser.preProcess(temp.replaceAll(parser.vars.get(0), "1")))).eq(new BigDec(parser.run(parser.preProcess(equation.replaceAll(parser.vars.get(0), "1")))))) ||
					!(new BigDec(parser.run(parser.preProcess(temp.replaceAll(parser.vars.get(0), "0")))).eq(new BigDec(parser.run(parser.preProcess(equation.replaceAll(parser.vars.get(0), "0")))))) ||
					!(new BigDec(parser.run(parser.preProcess(temp.replaceAll(parser.vars.get(0), "(-1)")))).eq(new BigDec(parser.run(parser.preProcess(equation.replaceAll(parser.vars.get(0), "(-1)")))))) ||
					temp.contains("()") || temp.equalsIgnoreCase("1.0") || temp.equalsIgnoreCase(""))
				isGood = false;
			if (isGood) {
				output = temp;
				factored.add(output);
			}
		}
		if (output.equalsIgnoreCase("1.0") || output.equalsIgnoreCase(""))
			return equation;
		if (equation.contains("="))
			output = output + "=0";
		if (div.neq(BigDec.ONE))
			output = div + "*" + output;
		return output;
	}
	
	public String polynomialDivision(String equation, String divis, Parser parser) throws ParserException {
		String output = new String(equation), remainder = "", substitution = "";
		String parts[] = seekSubstitutions(equation, parser);
		equation = parts[0];
		substitution = parts[1].trim();
		if (!substitution.equals("")) {
			parser.vars = parser.getVariables(equation);
			divis = seekSubstitutions(divis, substitution, parser)[0];
		}
		ArrayList<String> eqn = sortTerms(simplifyTerms(getTerms(equation, parser), parser), parser);
		if (eqn.size() < 2)
			return equation;
		else if (eqn.size() == 2 && orderOfTerm(eqn.get(0), parser)[0].lteq(BigDec.ONE))
			return equation;
		for (String str : eqn)
			if (orderOfTerm(str, parser)[0].lt(BigDec.ZERO))
				return output;
		BigDec coefficient = getCoefficient(eqn.get(0), parser);
		//attempt to make coefficients into integers
		BigDec div = BigDec.ONE;
		if (coefficient.isInt())
			div = new BigDec(coefficient);
		else if (getCoefficient(eqn.get(eqn.size() - 1), parser).isInt())
			div = new BigDec(getCoefficient(eqn.get(eqn.size() - 1), parser));
		if (div.neq(BigDec.ONE))
			for (int i = 0; i < eqn.size(); i++)
				eqn.set(i, eqn.get(i) + "/(" + div + ")");
		coefficient = getCoefficient(eqn.get(0), parser);
		if (coefficient.isInt())
			return output;
		equation = "";
		for (String str : eqn)
			equation = equation + "+" + str;
		if (equation.length() > 1)
			equation = parser.removeDoubles(equation.substring(1));
		boolean isGood = true;
		ArrayList<String> divisor = sortTerms(simplifyTerms(getTerms(divis, parser), parser), parser), dividend = new ArrayList<String>();
		//add in missing orders
		for (int i = 1; i < eqn.size(); i++) {
			if (orderOfTerm(eqn.get(i), parser)[0].toBigInt().lt(orderOfTerm(eqn.get(i - 1), parser)[0].subtract(BigDec.ONE).toBigInt())) {
				eqn.add(i, "0*" + parser.vars.get(0) + "^" + orderOfTerm(eqn.get(i - 1), parser)[0].subtract(BigDec.ONE));
				i--;
			}
		}
		while (eqn.size() >= divisor.size() && isGood) {
			BigDec order = orderOfTerm(eqn.get(0), parser)[0].subtract(orderOfTerm(divisor.get(0), parser)[0]);
			BigDec co = getCoefficient(eqn.get(0), parser).divide(getCoefficient(divisor.get(0), parser));
			String temp = "*" + parser.vars.get(0) + "^(" + order + ")";
			if (order.eq(BigDec.ONE))
				temp = "*" + parser.vars.get(0);
			else if (order.gt(BigDec.ZERO))
				temp = "*" + parser.vars.get(0) + "^" + order;
			else if (order.eq(BigDec.ZERO))
				temp = "";
			else {
				isGood = false;
				break;
			}
			dividend.add(co + temp);
			for (int i = 1; i < divisor.size() && i < eqn.size(); i++) {
				order = orderOfTerm(eqn.get(i), parser)[0];
				if (order.eq(BigDec.ONE))
					temp = "*" + parser.vars.get(0);
				else if (order.gt(BigDec.ZERO))
					temp = "*" + parser.vars.get(0) + "^" + order;
				else if (order.eq(BigDec.ZERO))
					temp = "";
				else {
					isGood = false;
					break;
				}
				eqn.set(i, (getCoefficient(eqn.get(i), parser).subtract(co.multiply(getCoefficient(divisor.get(i), parser))) + temp));
			}
			eqn.remove(0);
			if (eqn.size() == 1 && getCoefficient(eqn.get(0), parser).neq(BigDec.ZERO))
				isGood = false;
			if (!isGood)
				break;
		}
		if (eqn.size() > 0 && !(eqn.get(0).equals("0.0") || eqn.get(0).equals("0"))) {
			remainder = "(" + eqn.get(0);
			for (int i = 1; i < eqn.size(); i++)
				remainder = remainder + "+" + eqn.get(i);
			remainder = remainder + ")/(" + divis + ")";
		}
		String temp = "";
		for (int i = 0; i < dividend.size(); i++)
			if (dividend.get(i).contains("0.0*") || dividend.get(i).contains("0*")) {
				dividend.remove(i);
				i--;
			}
		for (String str : dividend)
			temp = temp + "+" + str;
		if (!remainder.equals(""))
			temp = temp + "+" + remainder;
		if (temp.length() > 1)
			temp = parser.removeDoubles(temp.substring(1));
		temp = ")(" + temp + ")";
		String di = "";
		for (String str : divisor)
			di = di + "+" + str;
		temp = ("(" + di.substring(1) + temp).replaceAll("\\=0", "");
		temp = parser.removeDoubles(temp);
		if (!(new BigDec(parser.run(parser.preProcess(temp.replaceAll(parser.vars.get(0), "1")))).eq(new BigDec(parser.run(parser.preProcess(equation.replaceAll(parser.vars.get(0), "1")))))) ||
				!(new BigDec(parser.run(parser.preProcess(temp.replaceAll(parser.vars.get(0), "0")))).eq(new BigDec(parser.run(parser.preProcess(equation.replaceAll(parser.vars.get(0), "0")))))) ||
				!(new BigDec(parser.run(parser.preProcess(temp.replaceAll(parser.vars.get(0), "(-1)")))).eq(new BigDec(parser.run(parser.preProcess(equation.replaceAll(parser.vars.get(0), "(-1)")))))) ||
				temp.contains("()") || temp.equalsIgnoreCase("1.0") || temp.equalsIgnoreCase(""))
			isGood = false;
		if (substitution.length() > 1)
			temp = temp.replaceAll(parser.vars.get(0), substitution);
		parser.vars = parser.getVariables(output);
		if (isGood)
			output = temp.substring(temp.indexOf(")") + 1);
		else
			return "(" + equation + ")/(" + di + ")";
		if (equation.contains("="))
			output = output + "=0";
		if (div.neq(BigDec.ONE))
			output = div + "*" + output;
		return output;
	}
	
	public String primeFactor(String equation, Parser parser) throws ParserException {
		ArrayList<BigInt> factors = findFactors(new BigDec(parser.run(parser.preProcess(equation))).toBigInt(), false, parser);
		String output = factors.get(1).toString();
		for (int i = 2; i < factors.size() - 1; i++)
			output = output + ", " + factors.get(i).toString();
		return output;
	}
	
	public ArrayList<BigInt> findFactors(BigInt num, Parser parser) throws UndefinedResultException {
		return findFactors(num, true, parser);
	}
	
	public ArrayList<BigInt> findFactors(BigInt num, boolean allFactors, Parser parser) throws UndefinedResultException {
		ArrayList<BigInt> output = new ArrayList<BigInt>();
		int primes[] = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71,
				73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173,
				179, 181, 191, 193, 197, 199, 211, 223, 227, 229, 233, 239, 241, 251, 257, 263, 269, 271, 277, 281,
				283, 293, 307, 311, 313, 317, 331, 337, 347, 349, 353, 359, 367, 373, 379, 383, 389, 397, 401, 409,
				419, 421, 431, 433, 439, 443, 449, 457, 461, 463, 467, 479, 487, 491, 499, 503, 509, 521, 523, 541,
				547, 557, 563, 569, 571, 577, 587, 593, 599, 601, 607, 613, 617, 619, 631, 641, 643, 647, 653, 659,
				661, 673, 677, 683, 691, 701, 709, 719, 727, 733, 739, 743, 751, 757, 761, 769, 773, 787, 797, 809,
				811, 821, 823, 827, 829, 839, 853, 857, 859, 863, 877, 881, 883, 887, 907, 911, 919, 929, 937, 941,
				947, 953, 967, 971, 977, 983, 991, 997, 1009, 1013, 1019, 1021, 1031, 1033, 1039, 1049, 1051, 1061, 1063, 1069,
				1087, 1091, 1093, 1097, 1103, 1109, 1117, 1123, 1129, 1151, 1153, 1163, 1171, 1181, 1187, 1193, 1201, 1213, 1217, 1223,
				1229, 1231, 1237, 1249, 1259, 1277, 1279, 1283, 1289, 1291, 1297, 1301, 1303, 1307, 1319, 1321, 1327, 1361, 1367, 1373,
				1381, 1399, 1409, 1423, 1427, 1429, 1433, 1439, 1447, 1451, 1453, 1459, 1471, 1481, 1483, 1487, 1489, 1493, 1499, 1511,
				1523, 1531, 1543, 1549, 1553, 1559, 1567, 1571, 1579, 1583, 1597, 1601, 1607, 1609, 1613, 1619, 1621, 1627, 1637, 1657,
				1663, 1667, 1669, 1693, 1697, 1699, 1709, 1721, 1723, 1733, 1741, 1747, 1753, 1759, 1777, 1783, 1787, 1789, 1801, 1811,
				1823, 1831, 1847, 1861, 1867, 1871, 1873, 1877, 1879, 1889, 1901, 1907, 1913, 1931, 1933, 1949, 1951, 1973, 1979, 1987,
				1993, 1997, 1999, 2003, 2011, 2017, 2027, 2029, 2039, 2053, 2063, 2069, 2081, 2083, 2087, 2089, 2099, 2111, 2113, 2129,
				2131, 2137, 2141, 2143, 2153, 2161, 2179, 2203, 2207, 2213, 2221, 2237, 2239, 2243, 2251, 2267, 2269, 2273, 2281, 2287,
				2293, 2297, 2309, 2311, 2333, 2339, 2341, 2347, 2351, 2357, 2371, 2377, 2381, 2383, 2389, 2393, 2399, 2411, 2417, 2423,
				2437, 2441, 2447, 2459, 2467, 2473, 2477, 2503, 2521, 2531, 2539, 2543, 2549, 2551, 2557, 2579, 2591, 2593, 2609, 2617,
				2621, 2633, 2647, 2657, 2659, 2663, 2671, 2677, 2683, 2687, 2689, 2693, 2699, 2707, 2711, 2713, 2719, 2729, 2731, 2741,
				2749, 2753, 2767, 2777, 2789, 2791, 2797, 2801, 2803, 2819, 2833, 2837, 2843, 2851, 2857, 2861, 2879, 2887, 2897, 2903,
				2909, 2917, 2927, 2939, 2953, 2957, 2963, 2969, 2971, 2999, 3001, 3011, 3019, 3023, 3037, 3041, 3049, 3061, 3067, 3079,
				3083, 3089, 3109, 3119, 3121, 3137, 3163, 3167, 3169, 3181, 3187, 3191, 3203, 3209, 3217, 3221, 3229, 3251, 3253, 3257,
				3259, 3271, 3299, 3301, 3307, 3313, 3319, 3323, 3329, 3331, 3343, 3347, 3359, 3361, 3371, 3373, 3389, 3391, 3407, 3413,
				3433, 3449, 3457, 3461, 3463, 3467, 3469, 3491, 3499, 3511, 3517, 3527, 3529, 3533, 3539, 3541, 3547, 3557, 3559, 3571};
		ArrayList<BigInt> factors = new ArrayList<BigInt>();
		factors.add(BigInt.ONE);
		factors.add(num);
		for (int i : primes) {
			if (new BigDec(num).divide(new BigDec(2)).lt(new BigDec(i)))
				break;
			BigDec tempNum = new BigDec(num);
			double a = 1;
			while (tempNum.divide(new BigDec(i)).isInt()) {
				if (allFactors) {
					if (tempNum.toBigInt().lteq(new BigInt((int) Math.pow(i, a))))
						break;
					factors.add(new BigInt((int) Math.pow(i, a)));
					factors.add(tempNum.divide(new BigDec(i)).toBigInt());
				}
				else
					factors.add(new BigInt(i));
				tempNum = tempNum.divide(new BigInt(i));
				a++;
			}
		}
		output = new ArrayList<BigInt>(factors);
		for (int i = 0; i < output.size(); i++)
			if (output.get(i).gt(num)) {
				output.remove(i);
				i--;
			}
		if (allFactors)
			if (output.size() >= 2)
				for (int i = 0; i < output.size(); i++)
					for (int a = i + 1; a < output.size(); a++)
						if (output.get(a).equals(output.get(i))) {
							output.remove(a);
							a--;
						}
		Collections.sort(output);
		return output;
	}
	
	public ArrayList<BigDec> substitutionSolve(String equation, Parser parser) throws ParserException {
		parser.setVars(parser.getVariables(equation));
		ArrayList<String> initialVars = new ArrayList<String>(parser.vars);
		ArrayList<BigDec> tempSolutions = new ArrayList<BigDec>(), solutions = new ArrayList<BigDec>();
		String substitution = "";
		String parts[] = seekSubstitutions(equation, parser);
		equation = parts[0];
		substitution = parts[1];
		String temp = solve(equation, false, false, parser);
		if (temp.contains("no"))
			return solutions;
		for (String str : temp.split(",")) {
			try {
				tempSolutions.add(new BigDec(str));
			}
			catch (NumberFormatException e) {};
		}
		if (substitution.equals("") || substitution.equals(" ")) {
			for (int i = 0; i < solutions.size(); i++)
				if (!isAnswer(equation, solutions.get(i), BigDec.ZERO, accuracy, parser)) {
					solutions.remove(i);
					i--;
				}
			return tempSolutions;
		}
		else {
			parser.vars = initialVars;
			for (BigDec d : tempSolutions) {
				String deSubEqn = reverseSubstitution(substitution, d.toString(), parser);
				for (BigDec solutin : substitutionSolve(deSubEqn, parser))
					solutions.add(solutin);
			}
		}
		return checkSolutions(solutions, equation, accuracy, parser);
	}
	
	private String reverseSubstitution(String substitution, String z, Parser parser) throws ParserException {
		int subID = subID(substitution);
		String deSubEqn = reverseSubstitutions[subID].replaceAll("z", z);
		deSubEqn = deSubEqn.replaceAll("substitution", substitution);
		String equation = substitution.substring(substitution.indexOf("(") + 1, substitution.length() - 1);
		if (subID == 14)
			equation = substitution.substring(1, substitution.length() - 1);
		equation = equation + "=" + parser.run(parser.preProcess(deSubEqn)).toString();
		return equation;
	}
	
	public int subID(String substitution) {
		int ID = 0;
		for (int i = 0; i < substitutions.length; i++)
			if (substitution.substring(0, substitution.indexOf("(")).equalsIgnoreCase(substitutions[i]))
				ID = new Integer(i);
		if (substitution.charAt(0) == '(')
			ID = 20;
		return ID;
	}
	
	public String[] seekSubstitutions(String equation, Parser parser) throws ParserException {
		return seekSubstitutions(equation, "", parser);
	}
	
	public String[] seekSubstitutions(String equation, String substitution, Parser parser) throws ParserException {
		return seekSubstitutions(equation, substitution, "y", parser);
	}
	
	public String[] seekSubstitutions(String equation, String substitution, String u, Parser parser) throws ParserException {
		String operation = "";
		int i = 0;
		while (i < equation.length()) {
			//Find a valid substitution by scanning for an operation in the substitution list, and then
			//determining if it contains the appropriate variable.
			boolean uSub = false;
			int endIndex = 0;
			if (i <= equation.length() - 2) {
				if (equation.indexOf('(', i) >= i && equation.charAt(i) != '(')
					operation = parser.isOperation(equation.substring(i, equation.indexOf('(', i + 1)));
				else if (equation.charAt(i) == '(')
					operation = "(";
				if (isSubstitution(operation)) {
					int startIndex = equation.indexOf('(', i);
					if (operation.equals("("))
						startIndex = i;
					endIndex = parser.getEndIndex(equation, startIndex);
					if (endIndex >= i) {
						String replacement = "";
						String component = equation.substring(startIndex + 1, endIndex);
						if (parser.containsVariable(component, parser.vars)) {
							if (!operation.equals("("))
								replacement = operation;
							replacement = removeExcessParentheses(replacement + "(" + component + ")", parser);
							if (substitution.equals("")) {
								substitution = replacement;
								uSub = true;
							}
							else if (replacement.equals(substitution) || replacement.equals(substitution.substring(1, substitution.length() - 1)))
								uSub = true;
						}
						if (uSub)
							replacement = u;
						else
							replacement = parser.operation(parser.run(component), operation).toString();
						if (parser.operators.contains(new Character(replacement.charAt(0)).toString()))
							replacement = "(" + replacement + ")";
						equation = equation.substring(0, i) + replacement + equation.substring(endIndex + 1);
						if (uSub)
							i = -1;
					}
					operation = "";
				}
			}
			i++;
		}
		if (substitution.length() > 0 && !substitution.contains("("))
			substitution = "(" + substitution + ")";
		equation = removeExcessParentheses(equation, parser);
		return new String[]{equation, substitution};
	}
	
	private boolean isSubstitution(String operation) {
		boolean isSub = false;
		for (String str : substitutions)
			if (str.equalsIgnoreCase(operation))
				isSub = true;
		return isSub;
	}
	
	public int specialCase(String form, String specialCases[]) {
		int specialCase = 0, termBefore = 0, termAfter = 0;
		for (int a = 0; a < form.length(); a++) {
			if (form.charAt(a) == ',')
				if (a < form.indexOf('='))
					termBefore++;
				else
					termAfter++;
		}
		for (int i = 0; i < specialCases.length; i++) {
			int specBefore = 0, specAfter = 0;
			for (int a = 0; a < specialCases[i].length(); a++) {
				if (specialCases[i].charAt(a) == ',')
					if (a < specialCases[i].indexOf('='))
						specBefore++;
					else
						specAfter++;
			}
			if ((termBefore == specBefore) && (termAfter == specAfter)) {
				specialCase = -i - 1;
				break;
			}
		}
		if (specialCase == -1 && form.equals("0.0, =, 0.0"))
			specialCase = 0;
		if (specialCase == -2 && form.equals("0.0, 0.0, =, 0.0"))
			specialCase = 0;
		return specialCase;
	}
	
	public boolean containsTerms(BigDec orders[], ArrayList<String> eqn, Parser parser) throws ParserException {
		boolean contains = true;
		BigDec modifier = getModifier(orders, eqn, parser);
		for (String str : eqn) {
			BigDec orderOfTerm = orderOfTerm(str, parser)[0].add(modifier);
			boolean isValid = false;
			for (BigDec d : orders)
				if (orderOfTerm.eq(d))
					isValid = true;
			if (!isValid)
				contains = false;
		}
		return contains;
	}
	
	/**
	 * Solves a fully factored homogeneous equation
	 * 
	 * @param equation
	 *            the equation to be solved
	 * @param parser
	 *            the parser that the caller is plugged in to
	 * @return the solutions to the equation if there are any via this method
	 */
	public ArrayList<BigDec> oneTermSolve(String equation, Parser parser) throws ParserException {
		if (equation.split(",").length != 0)
			return new ArrayList<BigDec>();
		return checkSolutions(oneTermSolve(equation.split(","), parser), equation, accuracy, parser);
	}
	
	private ArrayList<BigDec> oneTermSolve(String parts[], Parser parser) throws ParserException {
		String equation = parts[0] + invertSign(parts[1], parser);
		ArrayList<BigDec> solutions = new ArrayList<BigDec>();
		int p = 0;
		for (int i = 0; i < parts.length; i++) {
			p = i;
			for (int a = 0; a < parts[i].length(); a++) {
				if (parts[i].charAt(a) == '(')
					a = parser.getEndIndex(parts[i], a);
				if (parts[i].charAt(a) == '+' || parts[i].charAt(a) == '-')
					p = 2;
			}
			if (p == i && !parser.containsVariable(parts[(i + 1) % 2], parser.vars) && new BigDec(parser.run(parts[(i + 1) % 2])).eq(BigDec.ZERO)) {
				p = i;
				break;
			}
			else
				p = 2;
		}
		if (p == 2)
			return solutions;
		ArrayList<String> segments = getTermSegments(parts[p], parser);
		for (int i = 0; i < segments.size(); i += 2) {
			if (i >= 1 && segments.get(i - 1).equals("/")) {}
			else {
				String segment = segments.get(i);
				if (segment.charAt(0) == '(' && parser.getEndIndex(segment, 0) == segment.length() - 1)
					segment = segment.substring(1, segment.length() - 1);
				String temp = solve(equation, true, false, parser);
				if (temp.contains("no"))
					return solutions;
				for (String str : temp.split(",")) {
					try {
						solutions.add(new BigDec(str));
					}
					catch (NumberFormatException e) {};
				}
			}
		}
		for (int i = 0; i < solutions.size(); i++) {
			if (!isAnswer(equation, solutions.get(i), BigDec.ZERO, accuracy, parser)) {
				solutions.remove(i);
				i--;
			}
		}
		if (solutions.size() == 0) {
			String temp = solve(equation, true, false, parser);
			if (temp.contains("no"))
				return solutions;
			for (String str : temp.split(",")) {
				try {
					solutions.add(new BigDec(str));
				}
				catch (NumberFormatException e) {};
			}
		}
		return solutions;
	}
	
	public String getDirection(BigDec slope, BigDec y, BigDec answer, int sign, Parser parser) throws UndefinedResultException {
		String direction = "";
		slope = slope.multiply(new BigDec(sign));
		if ((slope.lt(BigDec.ZERO) && y.gt(answer)) || (slope.gt(BigDec.ZERO) && y.lt(answer)))
			direction = "towards";
		else if ((slope.lt(BigDec.ZERO) && y.lt(answer)) || (slope.gt(BigDec.ZERO) && y.gt(answer)))
			direction = "away";
		else if (y.eq(answer))
			direction = "answer";
		else
			direction = "neutral";
		return direction;
	}
	
	private BigDec getY(String equation, BigDec x, Parser parser) throws ParserException {
		return new BigDec(parser.getValue(equation.replaceAll(var, "(" + x.toString() + ")")));
	}
	
	public BigDec getSlope(BigDec point1[], BigDec point2[], Parser parser) throws ParserException {
		return new BigDec(parser.run("(" + point2[1] + "-" + point1[1] + ")/(" + point2[0] + "-" + point1[0] + ")"));
	}
	
	public boolean isAnswer(String equation, BigDec point, BigDec answer, BigDec accuracy, Parser parser) throws ParserException {
		boolean isAnswer = false;
		if (getY(equation, point, parser).subtract(answer).abs().lt(accuracy))
			isAnswer = true;
		return isAnswer;
	}
	
	public void setDomain(BigDec dMin, BigDec dMax) throws UndefinedResultException {
		if (dMin.gt(dMax)) {
			BigDec temp = new BigDec(dMax);
			dMax = new BigDec(dMin);
			dMin = temp;
		}
		xMin = (dMax.add(dMin)).divide(new BigDec(2));
		xMax = dMax.subtract(xMin);
		domainMax = dMax;
		domainMin = dMin;
		method = "domain";
	}
	
	public BigDec getDomain()[] throws UndefinedResultException {
		return new BigDec[]{xMin.subtract(xMax), xMin.add(xMax)};
	}
	
	public ArrayList<String> getTerms(String section, Parser parser) throws ParserException {
		ArrayList<String> terms = new ArrayList<String>();
		section = parser.preProcess(section, parser.vars).trim();
		boolean cont = true;
		while (cont) {
			cont = false;
			int i = 0;
			for (i = 0; i < section.length(); i++) {
				if (section.charAt(i) == '(')
					i = parser.getEndIndex(section, i);
				if (i > 0 && (section.charAt(i) == '+' || section.charAt(i) == '-')) {
					cont = true;
					break;
				}
			}
			if (cont) {
				terms.add(section.substring(0, i));
				if (i < section.length()) {
					terms.add(new Character(section.charAt(i)).toString());
					section = section.substring(i + 1);
				}
				else {
					section = section.substring(i);
					break;
				}
			}
		}
		if (section.trim().length() > 0)
			terms.add(section);
		if (terms.size() > 1)
			terms = parser.removeBlanks(terms, true);
		return terms;
	}
	
	public BigDec getCoefficient(String term, Parser parser) throws ParserException {
		return getCoefficient(term, parser.vars, parser);
	}
	
	public BigDec getCoefficient(String term, ArrayList<String> vars, Parser parser) throws ParserException {
		BigDec coefficient = BigDec.ZERO;
		ArrayList<String> termSegments = getTermSegments(term, parser);
		if (termSegments.size() == 1) {
			if (!parser.containsVariable(termSegments.get(0), vars))
				return new BigDec(parser.run(termSegments.get(0)));
		}
		termSegments.add(0, "1");
		termSegments.add(1, "*");
		ArrayList<String> coefficientSegments = new ArrayList<String>();
		coefficientSegments.add("1");
		for (int i = 2; i < termSegments.size(); i += 2) {
			if (!parser.containsVariable(termSegments.get(i), vars)) {
				coefficientSegments.add(termSegments.remove(i - 1));
				coefficientSegments.add(termSegments.remove(i - 1));
				i -= 2;
			}
			else if (termSegments.get(i).substring(0, 1).equals("-")) {
				coefficientSegments.add("*");
				coefficientSegments.add("-1");
			}
		}
		if (coefficientSegments.size() > 1)
			coefficientSegments = parser.removeBlanks(coefficientSegments, true);
		if (coefficientSegments.size() < 1)
			return coefficient;
		String coEqn = "";
		for (String str : coefficientSegments)
			coEqn = coEqn + str;
		coefficient = new BigDec(parser.run(parser.preProcess(coEqn, vars)));
		return coefficient;
	}
	
	public String[] getExponents(String term, Parser parser) throws ParserException {
		return getExponents(term, parser.vars, parser);
	}
	
	public String[] getExponents(String term, ArrayList<String> vars, Parser parser) throws ParserException {
		ArrayList<String> exponents = new ArrayList<String>();
		for (int i = 0; i < term.length(); i++) {
			if (term.charAt(i) == '(')
				i = parser.getEndIndex(term, i);
			else if (term.charAt(i) == '^') {
				int end = term.length();
				if (term.charAt(i + 1) == '(')
					end = parser.getEndIndex(term, i + 1);
				else
					for (end = i + 1; end < term.length(); end++)
						if (parser.isOperator(term.charAt(end)) || term.charAt(end) == ')' || term.charAt(end) == '(' || term.charAt(end) == '=')
							break;
				if (end >= term.length() - 1)
					exponents.add(term.substring(i + 1));
				else
					exponents.add(term.substring(i + 1, end));
				i = end;
			}
		}
		String[] output = new String[exponents.size()];
		for (int i = 0; i < exponents.size(); i++)
			output[i] = new String(exponents.get(i));
		return output;
	}
	
	public ArrayList<String> getTermSegments(String term, Parser parser) throws ParserException {
		ArrayList<String> output = new ArrayList<String>();
		term = parser.preProcess(term, parser.vars);
		while (term.contains("*") || term.contains("/")) {
			int i = 0;
			for (i = 0; i < term.length(); i++) {
				if (term.charAt(i) == '*' || term.charAt(i) == '/')
					break;
				if (term.charAt(i) == '(')
					i = parser.getEndIndex(term, i);
			}
			if (i == term.length())
				break;
			output.add(term.substring(0, i));
			output.add(new Character(term.charAt(i)).toString());
			term = term.substring(i + 1);
		}
		if (term.length() > 0)
			output.add(term);
		if (output.size() > 1)
			output = parser.removeBlanks(output, true);
		return output;
	}
	
	public BigDec[] orderOfTerm(String term, Parser parser) throws ParserException {
		return orderOfTerm(term, parser.vars, parser);
	}
	
	public BigDec[] orderOfTerm(String term, ArrayList<String> vars, Parser parser) throws ParserException {
		String base = "0";
		for (int i = 1; i < vars.size(); i++)
			base = base + ", 0";
		BigDec[] id = toDoubleArray(base);
		for (int i = 0; i < vars.size(); i++) {
			id[i] = id[i].add(orderOfTermLoop(term, vars.get(i), parser));
		}
		return id;
	}
	
	public BigDec orderOfTermLoop(String term, String var, Parser parser) throws ParserException {
		BigDec order = BigDec.ZERO;
		term = parser.preProcess(term, parser.vars);
		ArrayList<String> termSegments = getTermSegments(term, parser);
		if (parser.vars.size() >= 1)
			for (int i = 0; i < termSegments.size(); i += 2) {
				int a = 0, sign = 1;
				if (i >= 2 && termSegments.get(i - 1).contains("/"))
					sign = -1;
				term = termSegments.get(i);
				Matcher m = Pattern.compile("\\Q" + var + "^\\E").matcher(term);
				while (m.find()) {
					if (term.charAt(m.end()) == '(') {
						a = parser.getEndIndex(term, m.end()) + 1;
					}
					else {
						for (a = m.end(); a < term.length(); a++)
							if (parser.isOperator(term.charAt(a)) || term.charAt(a) == '(' || term.charAt(a) == ')')
								break;
					}
					String ran = parser.run(parser.preProcess(term.substring(m.end(), a)));
					try {
						order = order.add(new BigDec(ran).multiply(new BigInt(sign)));
					}
					catch (NumberFormatException e) {
						order = order.add(parser.processEquation(ran, true).multiply(new BigInt(sign)));
					}
				}
				m = Pattern.compile("\\Q" + var + "\\E[a-zA-Z0-9\\(\\)]").matcher(term);
				while (m.find())
					order = order.add(new BigInt(sign));
				if (var.length() >= 1 && term.length() >= var.length() && term.substring(term.length() - var.length()).equals(var))
					order = order.add(new BigInt(sign));
			}
		return order;
	}
	
	public int[] getSectionLimits(String section, int mid, Parser parser) throws ParserException {
		int start = 0, end = section.length() - 1;
		if (section.charAt(mid - 1) == ')')
			start = parser.getStartIndex(section, mid - 1);
		else
			for (start = mid - 1; start >= 0; start--)
				if (parser.isOperator(section.charAt(start)) || section.charAt(start) == ')' || section.charAt(start) == '(' || section.charAt(start) == '=')
					break;
		if (section.charAt(mid + 1) == '(')
			end = parser.getEndIndex(section, mid + 1);
		else
			for (end = mid + 1; end < section.length(); end++)
				if (parser.isOperator(section.charAt(end)) || section.charAt(end) == ')' || section.charAt(end) == '(' || section.charAt(end) == '=')
					break;
		return new int[]{start, end};
	}
	
	public ArrayList<String> simplifyTerms(ArrayList<String> terms, Parser parser) throws ParserException {
		ArrayList<String> output = new ArrayList<String>(), ids = new ArrayList<String>(), subs = new ArrayList<String>();
		for (int i = 0; i < terms.size(); i++) {
			ArrayList<String> tempExps = getExponentSections(terms.get(i), parser.vars, true, parser), tempFunctionSubs = getFunctionSections(terms.get(i), parser.vars, true, parser);
			for (String str : tempFunctionSubs)
				tempExps.add(str);
			if (tempExps.size() > 0) {
				for (int b = 0; b < terms.size(); b++) {
					String term = terms.get(b);
					for (int a = 0; a < tempExps.size(); a++)
						term = term.replaceAll("\\Q" + tempExps.get(a) + "\\E", "{S" + (subs.size() + a) + "}");
					terms.set(b, term);
				}
				for (String str : tempExps)
					subs.add(str);
			}
		}
		ArrayList<String> tempVars = new ArrayList<String>();
		for (int i = 0; i < subs.size(); i++)
			tempVars.add("{S" + i + "}");
		for (String var : parser.vars)
			tempVars.add(var);
		for (int i = 0; i < terms.size() - 1; i++) {
			if (terms.get(i).equals("+")) {
				terms.remove(i);
				i--;
			}
			if (terms.get(i).equals("-")) {
				terms.remove(i);
				terms.set(i, "-" + terms.get(i));
				i--;
			}
		}
		for (int i = 0; i < terms.size(); i++)
			ids.add(toIdString(orderOfTerm(terms.get(i), tempVars, parser)));
		for (int i = 0; i < terms.size(); i++) {
			BigDec coefficient = getCoefficient(terms.get(i), tempVars, parser);
			String idi = ids.get(i);
			for (int a = i + 1; a < terms.size(); a++) {
				if (idi.equalsIgnoreCase(ids.get(a))) {
					coefficient = coefficient.add(getCoefficient(terms.get(a), parser));
					ids.remove(a);
					terms.remove(a);
					a--;
				}
			}
			if (coefficient.neq(BigDec.ZERO)) {
				BigDec[] IDs = toDoubleArray(idi);
				String[] ID = new String[IDs.length];
				for (int a = 0; a < IDs.length; a++)
					ID[a] = IDs[a].toString();
				String newTerm = "";
				newTerm = coefficient.toString();
				for (int a = 0; a < tempVars.size(); a++) {
					if (IDs[a].neq(BigDec.ZERO) && IDs[a].neq(BigDec.ONE))
						if (IDs[a].gt(BigDec.ZERO))
							newTerm = newTerm + "*" + tempVars.get(a) + "^" + ID[a];
						else
							newTerm = newTerm + "*" + tempVars.get(a) + "^(" + ID[a] + ")";
					else if (IDs[a].neq(BigDec.ZERO))
						newTerm = newTerm + "*" + tempVars.get(a);
				}
				if (newTerm.length() > 2 && newTerm.substring(0, 2).equals("1*"))
					newTerm = newTerm.substring(2);
				if (!parser.containsVariable(newTerm, tempVars))
					newTerm = parser.run(newTerm).toString();
				if ((newTerm.equals("0") || newTerm.equals("0.0")) && coefficient.neq(BigDec.ZERO))
					newTerm = coefficient.toString();
				output.add(newTerm);
			}
		}
		for (int i = 0; i < output.size(); i++)
			for (int a = 0; a < subs.size(); a++)
				output.set(i, output.get(i).replaceAll("\\Q" + "{S" + a + "}" + "\\E", subs.get(a)));
		return sortTerms(output, parser);
	}
	
	public ArrayList<String> getExponentSections(String term, ArrayList<String> vars, boolean varsOnly, Parser parser) throws ParserException {
		ArrayList<String> exps = new ArrayList<String>();
		for (int i = 0; i < term.length(); i++) {
			if (term.charAt(i) == '(')
				i = parser.getEndIndex(term, i);
			else if (term.charAt(i) == '^') {
				int end = term.length();
				if (term.charAt(i + 1) == '(')
					end = parser.getEndIndex(term, i + 1);
				else
					for (end = i + 1; end < term.length(); end++)
						if (parser.isOperator(term.charAt(end)) || term.charAt(end) == ')' || term.charAt(end) == '(' || term.charAt(end) == '=')
							break;
				String exp;
				if (end >= term.length() - 1)
					exp = term.substring(i + 1);
				else
					exp = term.substring(i + 1, end);
				if ((varsOnly && parser.containsVariable(exp, vars)) || !varsOnly) {
					int start = 0;
					if (term.charAt(i - 1) == ')')
						start = parser.getStartIndex(term, i - 1);
					else
						for (start = i - 1; start > 0; start--)
							if (parser.isOperator(term.charAt(start)) || term.charAt(start) == ')' || term.charAt(start) == '(' || term.charAt(start) == '=') {
								start += 1;
								break;
							}
					if (end >= term.length() - 1)
						exps.add(term.substring(start));
					else
						exps.add(term.substring(start, end));
				}
				i = end;
			}
		}
		return exps;
	}
	
	public ArrayList<String> getFunctionSections(String term, ArrayList<String> vars, boolean varsOnly, Parser parser) throws ParserException {
		ArrayList<String> subs = new ArrayList<String>();
		for (int i = 0; i < term.length(); i++) {
			if (term.charAt(i) == '(')
				i = parser.getEndIndex(term, i);
			else {
				int start = term.indexOf("(", i + 1);
				if (start > i) {
					if (!parser.isOperation(term.substring(i, start)).equals("") && term.charAt(i) != '(') {
						int end = parser.getEndIndex(term, start);
						if (parser.containsVariable(term.substring(start + 1, end), vars) || !varsOnly)
							subs.add(term.substring(i, end + 1));
						else
							i = end;
					}
				}
			}
		}
		return subs;
	}
	
	public ArrayList<String> distributiveProperty(ArrayList<String> terms, Parser parser) throws ParserException {
		return distributiveProperty(terms, true, parser);
	}
	
	public ArrayList<String> distributiveProperty(ArrayList<String> terms, boolean simplify, Parser parser) throws ParserException {
		int initLength = 0;
		boolean needsDistribution = false;
		terms = distributeExponenets(terms, parser);
		for (int i = 0; i < terms.size(); i++) {
			if (!parser.containsVariable(terms.get(i), parser.vars) && !(terms.get(i).equals("+") || terms.get(i).equals("-")))
				terms.set(i, parser.run(parser.preProcess(terms.get(i), parser.vars)).toString());
		}
		for (String str : terms)
			if (str.contains("(") || str.contains(")"))
				needsDistribution = true;
		if (needsDistribution) {
			while (initLength != terms.size()) {
				initLength = terms.size();
				int t = 0;
				while (t < initLength && t < terms.size()) {
					String term = terms.get(t);
					while (term.length() < 2 && t < terms.size() - 1) {
						t++;
						term = terms.get(t);
					}
					term = parser.seekOperations(term);
					term = distributeExponenets(term, parser);
					if (term.charAt(0) == '(' && parser.getEndIndex(term, 0) == term.length() - 1)
						term = term.substring(1, term.length() - 1);
					term = removeExcessParentheses(term, parser);
					term = distributeExponenets(term, parser);
					//Convert all sections of the term into parenthetic statements
					for (int a = 0; a < term.length(); a++) {
						if (term.charAt(a) == '(')
							a = parser.getEndIndex(term, a);
						if (term.charAt(a) == '*' || term.charAt(a) == '/') {
							term = term.substring(0, a) + ")" + term.substring(a);
							a++;
							int b = 0;
							for (b = a - 2; b > 0; b--) {
								if (term.charAt(b) == ')')
									b = parser.getStartIndex(term, b);
								if (term.charAt(b) == '*' || term.charAt(b) == '/')
									break;
							}
							if (b > 0)
								term = term.substring(0, b + 1) + "(" + term.substring(b + 1);
							else
								term = "(" + term;
							a++;
							term = term.substring(0, a + 1) + "(" + term.substring(a + 1);
							for (b = a + 2; b < term.length(); b++) {
								if (term.charAt(b) == '(')
									b = parser.getEndIndex(term, b);
								if (term.charAt(b) == '*' || term.charAt(b) == '/')
									break;
							}
							a++;
							term = term.substring(0, b) + ")" + term.substring(b);
						}
					}
					term = removeExcessParentheses(term, parser);
					//Find the segments without the variable, and convert them to one number
					for (int i = 0; i < term.length(); i++) {
						if (term.charAt(i) == '(') {
							String component = term.substring(i + 1, parser.getEndIndex(term, i)), replacement = "";
							if (!parser.containsVariable(component, parser.vars)) {
								replacement = parser.run(component).toString();
								term = term.substring(0, i + 1) + replacement + term.substring(parser.getEndIndex(term, i));
							}
							i = parser.getEndIndex(term, i);
						}
					}
					//Now for the distributive property
					term = term.replaceAll("\\Q-(\\E", "(-1)*(");
					for (int i = 0; i < term.length(); i++) {
						int start = 0, end = 0;
						String p1 = "", sign = "", p2 = "";
						ArrayList<String> p1T = new ArrayList<String>(), p2T = new ArrayList<String>(), expansion = new ArrayList<String>();
						if (term.charAt(i) == '(') {
							i = parser.getEndIndex(term, i);
						}
						if (i > 0 && i < term.length() - 1 && (term.charAt(i) == '*' || term.charAt(i) == '/') && (term.charAt(i - 1) == ')' || term.charAt(i + 1) == '(')) {
							sign = new Character(term.charAt(i)).toString();
							for (start = i - 1; start > 0; start--) {
								if (term.charAt(start) == ')')
									start = parser.getStartIndex(term, start);
								if (parser.isOperator(term.charAt(start)) || term.charAt(start) == ')' || term.charAt(start) == '=')
									break;
							}
							for (end = i + 1; end < term.length(); end++) {
								if (term.charAt(end) == '(')
									end = parser.getEndIndex(term, end);
								if (parser.isOperator(term.charAt(end)) || term.charAt(end) == ')' || term.charAt(end) == '=')
									break;
							}
							p1 = term.substring(start, i);
							if (p1.charAt(0) == '(' && parser.getEndIndex(p1, 0) == p1.length() - 1)
								p1 = p1.substring(1, p1.length() - 1);
							p2 = term.substring(i + 1, end + 1);
							if (p1.charAt(0) == ')' && parser.getEndIndex(p2, 0) == p2.length() - 1)
								p2 = p2.substring(1, p2.length() - 1);
							if (sign.equals("/")) {
								p1 = polynomialDivision(p1, p2, parser);
								if (parser.getEndIndex(p1, 0) == p1.length() - 1)
									p1 = p1.substring(1, p1.length() - 1);
							}
							else if (sign.equals("*")) {
								p1T = sortTerms(simplifyTerms(getTerms(p1, parser), parser), parser);
								p2T = sortTerms(simplifyTerms(getTerms(p2, parser), parser), parser);
								for (int p1I = 0; p1I < p1T.size(); p1I++) {
									for (int p2I = 0; p2I < p2T.size(); p2I++)
										expansion.add(p1T.get(p1I) + sign + p2T.get(p2I));
								}
								p1 = expansion.get(0);
								for (int a = 1; a < expansion.size(); a++)
									p1 = p1 + "+" + expansion.get(a);
							}
							if (sign.equals("*") || sign.equals("/")) {
								if (start == 0)
									term = removeExcessParentheses("(" + p1 + ")" + term.substring(end + 1), parser);
								else
									term = removeExcessParentheses(term.substring(0, start + 1) + "(" + p1 + ")" + term.substring(end + 1), parser);
							}
							if (term.charAt(0) == '(' && parser.getEndIndex(term, 0) == term.length() - 1)
								term = term.substring(1, term.length() - 1);
							i = -1;
						}
					}
					terms.remove(t);
					terms.addAll(t, getTerms(term, parser));
					t++;
				}
			}
		}
		if (simplify)
			return simplifyTerms(terms, parser);
		else
			return terms;
	}
	
	public ArrayList<String> distributeExponenets(ArrayList<String> terms, Parser parser) throws ParserException {
		for (int i = 0; i < terms.size(); i++)
			terms.set(i, distributeExponenets(terms.get(i), parser));
		return terms;
	}
	
	public String distributeExponenets(String term, Parser parser) throws ParserException {
		int start = 0, end = 0, exponent = 0;
		String parenthesis = "";
		for (int i = 0; i < term.length(); i++) {
			if (term.charAt(i) == '(')
				i = parser.getEndIndex(term, i);
			if (i >= 1 && term.charAt(i) == '^' && term.charAt(i - 1) == ')') {
				start = i;
				for (end = i + 1; end < term.length(); end++) {
					if (term.charAt(end) == '(')
						end = parser.getEndIndex(term, end);
					if (parser.isOperator(term.charAt(end)) || term.charAt(end) == ')' || term.charAt(end) == '=')
						break;
				}
				if (end >= term.length() - 1)
					end = term.length() - 1;
				exponent = (int) Math.round(new BigDec(parser.run(term.substring(start + 1, end + 1))).doubleValue());
				int b = i - 1;
				if (i >= 1 && term.charAt(i - 1) == ')')
					b = parser.getStartIndex(term, i - 1);
				else
					for (b += 0; b > 0; b--)
						if (parser.isOperator(term.charAt(b)) || term.charAt(b) == '(' || term.charAt(b) == ')' || term.charAt(b) == '=')
							break;
				String sector = term.substring(b, i);
				if (!parser.containsVariable(sector, parser.vars)) {
					sector = parser.run(sector + "^" + term.substring(start + 1, end + 1)).toString();
					term = term.substring(0, b + 1) + sector + term.substring(end);
					i = -1;
				}
				else {
					for (int a = 0; a < ((int)Math.abs(exponent)) - 1; a++)
						parenthesis = parenthesis + "*" + sector;
					if (exponent < 0) {
						term = term.substring(0, b) + "/(" + term.substring(b);
						parenthesis = parenthesis + ")";
					}
					term = term.substring(0, start) + parenthesis + term.substring(end);
					i = -1;
					parenthesis = "";
				}
				start = 0;
				end = 0;
				exponent = 0;
			}
		}
		return removeExcessParentheses(term, parser);
	}
	
	public String removeExcessParentheses(String term, Parser parser) throws ParserException {
		if (term.length() < 3)
			return term;
		if (term.charAt(0) == '+')
			term = term.substring(1);
		while (term.length() >= 3 && parser.getEndIndex(term, 0) == term.length() - 1)
			term = term.substring(1, term.length() - 1);
		for (int i = 0; i < term.length() - 1; i++) {
			if (term.charAt(i) == '(' && term.charAt(i + 1) == '(') {
				if (parser.getEndIndex(term, i + 1) == parser.getEndIndex(term, i) - 1) {
					term = term.substring(0, i) + term.substring(i + 1, parser.getEndIndex(term, i)) + term.substring(parser.getEndIndex(term, i) + 1);
					i--;
				}
			}
		}
		for (int i = 0; i < term.length(); i++) {
			if (term.charAt(i) == '(')
				i = parser.getEndIndex(term, i) - 1;
			else if (term.charAt(i) == ')') {
				int a = parser.getStartIndex(term, i);
				boolean valid = false;
				if (a >= 1 && parser.isOperator(term.charAt(a - 1)))
					valid = true;
				else if (a == 0)
					valid = true;
				if (a >= 1 && (term.charAt(a - 1) == '^' || term.charAt(a - 1) == '/'))
					valid = false;
				String component = removeExcessParentheses(term.substring(a + 1, i), parser);
				if (valid && (isOneTerm(component.substring(0), parser) && !(i < term.length() - 1 && term.charAt(i + 1) == '^' && (term.contains("*") || term.contains("/"))) || component.length() == 1)) {
					term = term.substring(0, a) + component + term.substring(i + 1);
					i = -1;
				}
				else
					term = term.substring(0, a + 1) + component + term.substring(i);
			}
			if (i < -1)
				break;
		}
		return term;
	}
	
	public boolean isOneTerm(String term, Parser parser) throws ParserException {
		boolean isOneTerm = true;
		for (int i = 0; i < term.length(); i++) {
			if (term.charAt(i) == '(')
				i = parser.getEndIndex(term, i);
			if (term.charAt(i) == '+' || term.charAt(i) == '-')
				isOneTerm = false;
		}
		return isOneTerm;
	}
	
	public ArrayList<String> sortTerms(ArrayList<String> terms, Parser parser) throws ParserException {
		if (terms.size() > 1)
			for (int a = 0; a < terms.size(); a++) {
				for (int b = a + 1; b < terms.size(); b++) {
					if (orderOfTerm(terms.get(a), parser)[0].lt(orderOfTerm(terms.get(b), parser)[0])) {
						String temp = new String(terms.get(a));
						terms.set(a, new String(terms.get(b)));
						terms.set(b, temp);
					}
				}
			}
		return terms;
	}
	
	public BigDec orderOfEquation(ArrayList<String> terms, Parser parser) throws ParserException {
		BigDec order = BigDec.ZERO;
		for (String term : terms)
			if (orderOfTerm(term, parser)[0].abs().gt(order.abs()))
				order = orderOfTerm(term, parser)[0];
		return order;
	}
	
	public String invertSign(String section, Parser parser) throws ParserException {
		for (int i = 0; i < section.length() - 1; i++) {
			if (section.charAt(i) == '(')
				i = parser.getEndIndex(section, i);
			if (section.charAt(i) == '+' || section.charAt(i) == '-') {
				section = section.substring(0, i) + "-" + section.substring(i);
				i++;
			}
		}
		if (section.charAt(0) != '+' && section.charAt(0) != '-')
			section = "-" + section;
		return parser.removeDoubles(section).trim();
	}
	
	public BigDec[] toDoubleArray(String id) {
		BigDec output[] = new BigDec[id.split(",").length];
		String ids[] = id.split(",");
		for (int i = 0; i < ids.length; i++)
			output[i] = new BigDec(ids[i]);
		return output;
	}
	
	public String toIdString(BigDec array[]) {
		if (array.length < 1)
			return "";
		String output = new BigDec(array[0]).toString();
		for (int i = 1; i < array.length; i++)
			output = output + ", " + array[i];
		return output;
	}
}
