package lrg.common.abstractions.plugins.conformities.creation.parsing;

import java.lang.reflect.Field;
import java.util.Stack;

import lrg.common.abstractions.plugins.conformities.creation.InvalidExpressionException;
import lrg.common.abstractions.plugins.conformities.creation.wrappers.Filter;
import lrg.common.abstractions.plugins.conformities.creation.wrappers.Rule;
import lrg.common.abstractions.plugins.conformities.creation.wrappers.operators.AndOperator;
import lrg.common.abstractions.plugins.conformities.creation.wrappers.operators.Bracket;
import lrg.common.abstractions.plugins.conformities.creation.wrappers.operators.ComparableOperator;
import lrg.common.abstractions.plugins.conformities.creation.wrappers.operators.OrOperator;
import lrg.common.abstractions.plugins.conformities.thresholds.Threshold;
import lrg.common.abstractions.plugins.conformities.weights.Weight;

public class RuleParser {
	private Stack<ParsingItemWrapper> postfixStack = new Stack<ParsingItemWrapper>();
	private Stack<ComparableOperator> operatorStack = new Stack<ComparableOperator>();
	private String targetEntityType;
	private String ruleName;
	
	public Stack<ParsingItemWrapper> getStack() {
		Stack<ParsingItemWrapper> result = new Stack<ParsingItemWrapper>();
		result.addAll(postfixStack);
		return result;
	}
	
	public String getTargetEntityType() {
		return targetEntityType;
	}
	
	public String getRuleName() {
		return ruleName;
	}
	
	public Stack<ParsingItemWrapper> startParse(String rawRule) throws InvalidExpressionException {
		if (isComment(rawRule))
			return null;
		postfixStack.clear();
		operatorStack.clear();
		
		targetEntityType = new String();
		ruleName = new String();
		char c;
		int i;
		for (i = 0; (c = rawRule.charAt(i)) != ':'; i++)
			ruleName += c;
		rawRule = rawRule.substring(i + 2);
		for (i = 0; (c = rawRule.charAt(i)) != ':'; i++)
			targetEntityType += c;
		
		rawRule = rawRule.substring(i + 2);
		if (isInvalidTargetEntityType(targetEntityType))
			throw new InvalidExpressionException();

		parse(rawRule);
		
		return getStack();
	}

	private boolean isComment(String rawRule) {
		return rawRule.startsWith("//");
	}

	private void parse(String s) throws InvalidExpressionException {
		String remaining = null;
		if (s.startsWith("["))
			remaining = parseRule(s);
		else
			remaining = parseOperator(s);
		if (s.equals(remaining))
			remaining = parseWeight(s);
		if (s.equals(remaining))
			remaining = parseFilter(s);
		if (s.equals(remaining)) {
			//nu a fost un weight
			throw new InvalidExpressionException();
		}
		
		System.out.println("remaining: " + remaining);
		if (remaining != null && remaining.length() != 0)
			parse(remaining);
		else 
			while (operatorStack.isEmpty() == false) {
				ComparableOperator o = operatorStack.pop();
				if (o instanceof Bracket == false)
					postfixStack.push((ParsingItemWrapper)o);
			}
	}
	
	private String parseOperator(String s) {
		ComparableOperator op = null;
		if (s.charAt(0) == '&')
			op = new AndOperator();
		else if (s.charAt(0) == '|')
			op = new OrOperator();
		else if (s.charAt(0) == '(')
			op = new Bracket(true);
		else if (s.charAt(0) == ')')
			op = new Bracket(false);
		else
			return s;
		System.out.println(op);
		push(op);
		if (s.length() <= 2)
			return null;
		return s.substring(2);
	}

	private String parseWeight(String s) {
		Weight result = null;
		for (Weight w : Weight.values())
			if (s.startsWith(w.toString())) {
				result = w;
				postfixStack.push(w);
				System.out.println(result);
				int pos = s.indexOf(w.toString()) + w.toString().length();
				if (pos + 1 >= s.length())
					return null;
				return s.substring(pos + 1);
			}
				
		return s;
	}
	
	private String parseRule(String s) throws InvalidExpressionException {
		int pos = s.indexOf(']');
		String rawRule = s.substring(1, pos);
		
		String items[] = rawRule.split(" ");
		if (items.length == 3) {
			//Rule result = new Rule(items[0], items[1], Double.parseDouble(items[2]), targetEntityType);
			Rule result = new Rule(items[0], items[1], getThresholdValue(items[2]), targetEntityType);
			System.out.println(result);
			postfixStack.push(result);
		}
		
		if (pos + 2 >= s.length())
			return null;
		return s.substring(pos + 2);
	}
	
	private String parseFilter(String s) throws InvalidExpressionException {
		int pos = s.indexOf(' ');
		String filter;
		boolean expectedFilterValue = true;
		if (s.charAt(0) == '!') {
			expectedFilterValue = false;
			filter = s.substring(1, pos);
		} else 
			filter = s.substring(0, pos);
		
		Filter result = new Filter(filter, expectedFilterValue, targetEntityType);
		System.out.println(result);
		postfixStack.push(result);
		
		if (pos + 1 >= s.length())
			return null;
		return s.substring(pos + 1);
	}
	
	private double getThresholdValue(String threshold) throws InvalidExpressionException {
		double multiply = 1;
		boolean numerical = true;
		if (Character.isDigit(threshold.charAt(0))) {
			String nr = new String();
			int i;
			for (i = 0; i < threshold.length() && threshold.charAt(i) != '*'; i++)
				nr += threshold.charAt(i);
			multiply = Double.parseDouble(nr);
			for (i = 0; i < threshold.length() && isStartOfThresholdName(threshold.charAt(i)) == false; i++)
				if (threshold.charAt(i) == '*')
					numerical = false;
			System.out.println(threshold + " " + i);
			threshold = threshold.substring(i);
		} else {
			numerical = false;
		}
		if (numerical)
			return multiply;
			
		for (Field f : Threshold.class.getFields())
			if (f.getName().equals(threshold))
				try {
					return f.getDouble(new Threshold()) * multiply;
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					throw new InvalidExpressionException();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					throw new InvalidExpressionException();
				}
		throw new InvalidExpressionException();
	}
	
	private boolean isStartOfThresholdName(Character c) {
		if (c == '.' || c == ' ' || c == '*')
			return false;
		return Character.isLetter(c);
	}

	private boolean isInvalidTargetEntityType(String target) {
		if ("method".equals(target))
			return false;
		if ("class".equals(target))
			return false;
		return true;
	}
	
	private void push(ComparableOperator op) {
		boolean pushed = false;
		do {
			if (operatorStack.isEmpty() || op instanceof Bracket) { //open
				operatorStack.push(op);
				pushed = true;
			}
			else {
				if (op.compareTo(operatorStack.peek()) > 0) {
					operatorStack.push(op);
					pushed = true;
				} else {
					ComparableOperator popped = operatorStack.pop();
					if (popped instanceof Bracket) //closed
						pushed = true;
					else
						postfixStack.push((ParsingItemWrapper)popped);					
				}
			}
		} while (pushed == false);
	}
}
