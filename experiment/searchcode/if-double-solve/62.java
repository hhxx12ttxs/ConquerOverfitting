/*
 * Copyright Â 2009 Gerald Isaac Schwarz
 * LICENSE
 * Permission to use, copy, modify and distribute this software and its documentation is hereby granted, provided that both the copyright notice and this permission notice appear in all copies of the software, derivative works or modified versions, and any portions thereof, and that both notices appear in supporting documentation.
 * 
 * DISCLAIMER
 * This software is provided "as is" with no warranty, liability, or any implication thereof.
 */

package sysCalculator;

import java.util.ArrayList;

/**
 * The calculator's backend.
 * @author Gerald Isaac Schwarz
 * @version 0.1
 * @since 0.1
 */

public class Calculator {

	/**
	 * Check whether or not the given character is a supported operator.
	 * @since 0.1
	 * @param operator The character that is to be evaluated.
	 * @return Whether or not the given character represents a supported character.
	 */
	public static boolean isOperator(char operator) {
		if (isAdder(operator) || isMultiplier(operator)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Solves any given mathematical problem.
	 * @since 0.1
	 * @param problem A string that represents a mathematical problem. Can contain numbers and any operator for which isOperator(char) returns true. No non-numerical characters or whitespace allowed. The string must begin with a number.
	 * @return The solution to the problem.
	 */
	
	public static double solve(String problem) {
		return solve(compile(extract(problem)));
	}
	
	/**
	 * Check whether or not the given character is a plus or minus.
	 * @since 0.1
	 * @param operator The character that is to be evaluated.
	 * @return Whether or not the given character a plus or minus sign.
	 */
	private static boolean isAdder(char operator) {
		String operators = "+-";
		if (operators.indexOf(operator) > -1) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Check whether or not the given character is a times, divide, or modulus.
	 * @since 0.1
	 * @param operator The character that is to be evaluated.
	 * @return Whether or not the given character is times, divide, or modulus sign.
	 */
	private static boolean isMultiplier(char operator) {
		String operators = "*/%";
		if (operators.indexOf(operator) > -1) {
			return true;
		}
		else {
			return false;
		}
	}
	
	
	/**
	 * Converts a String into an array that's ready to be processed by Calculator.compile(ArrayList<String>).
	 * @since 0.1
	 * @param problem A regular string that represents a mathematical problem.
	 * @return A String ArrayList which alternates between numbers and operators.  
	 */
	private static ArrayList<String> extract(String problem) {
		
//		Get an array of all the operands:
		
		String[] numbers = problem.split("[\\+\\-\\*\\/\\%]");
		
//		Go over each character in the display, and if it's an operator then add to an ArrayList:
		
		ArrayList<String> operators = new ArrayList<String>();
		
		for (int i = 0; i < problem.length(); i++) {
			if (Calculator.isOperator(problem.charAt(i))) {
				operators.add(problem.charAt(i) + "");
			}
		}
		
//		Compile the problem into an ArrayList:
		
		ArrayList<String> problemArray = new ArrayList<String>();
		
		for (int i = 0; i < operators.size(); i++) {
			problemArray.add(numbers[i]);
			problemArray.add(operators.get(i));
		}
		
		problemArray.add(numbers[numbers.length - 1]);
		
		return problemArray;
	}
	
	/**
	 * Converts a String into an array that's ready to be processed by Calculator.solve(ArrayList<ArrayList<String>>).
	 * @since 0.1
	 * @param problem An ArrayList that alternates between numbers and operators.
	 * @return A 2D String ArrayList in which every every element of the 1st level represents a mathematical element. the strings inside the 2nd level alternate between operators and numbers. 
	 */
	private static ArrayList<ArrayList<String>> compile(ArrayList<String> problem) {
		ArrayList<ArrayList<String>> elements = new ArrayList<ArrayList<String>>();
		
//		Since every problem begins with a positive number, add the plus sign to the beginnig of the first element:
		
		ArrayList<String> firstElement = new ArrayList<String>();
		firstElement.add("+");
		
//		Go over each element in the problem array and add it to the first element, until you reach an adder:
		
		for (int i = 0; i < problem.size(); i++) {
			if (!isAdder(problem.get(i).charAt(0))) {
				firstElement.add(problem.get(i));
			}
			else {
				break;
			}
		}
		
		elements.add(firstElement);
		
//		Look for adders, and for each one of them do as before:
		
		for (int i = 0; i < problem.size(); i++) {
			if (isAdder(problem.get(i).charAt(0))) {
				ArrayList<String> element = new ArrayList<String>();
				element.add(problem.get(i));
				elements.add(element);
				for (int h = i + 1; h < problem.size(); h++) {
					i = h - 1;
					if (!isAdder(problem.get(h).charAt(0))) {
						element.add(problem.get(h));
					}
					else {
						break;
					}
				}
			}
		}
		return elements;
	}

	/**
	 * Solves the given mathematical problem.
	 * @since 0.1
	 * @param problem The problem to be solved.
	 * @return The solution to the problem.
	 */
	private static double solve(ArrayList<ArrayList<String>> problem) {
		
//		In the first stage, calculate the value of each element in the problem array:
		
		ArrayList<String> firstStageSolution = new ArrayList<String>();
		
		for (ArrayList<String> element : problem) {
			double currentResult = Double.parseDouble(element.get(1));
			
			for (int i = 2; i < element.size(); i++) {
				if (i % 2 == 0) {
					currentResult = Calculator.calculate(currentResult, Double.parseDouble(element.get(i + 1)), element.get(i).charAt(0));
				}
			}
			firstStageSolution.add(element.get(0));
			firstStageSolution.add(currentResult + "");
		}
		
//		In the second stage, add up the results of the first stage in order to get the solution to the problem:
		
		double currentResult = Double.parseDouble(firstStageSolution.get(1));
		
		for (int i = 2; i < firstStageSolution.size(); i++) {
			if (i % 2 == 0) {
				currentResult = Calculator.calculate(currentResult, Double.parseDouble(firstStageSolution.get(i + 1)), firstStageSolution.get(i).charAt(0));
			}
		}
		
		return currentResult;
	}

	/**
	 * Perform binary mathematical operations. Currently only addition, subtraction, multiplication, division, and modulus are supported.
	 * @since 0.1
	 * @param operandA One of the two operands of the binary operation. 
	 * @param operandB One of the two operands of the binary operation.
	 * @param operator The character that sybolizes the binary operation that will be performed.
	 * @return The result of the calculation.
	 */
	private static double calculate(double operandA, double operandB, char operator) {
		switch (operator) {
			case '+':
				return operandA + operandB;
			case '-':
				return operandA - operandB;
			case '*':
				return operandA * operandB;
			case '%':
				return operandA % operandB;
			default:
				return operandA / operandB;
		}
			
	}
	
	/**
	 * Calculator is a utility class that offers no instance methods. As such, it has no reason to be instantiated.
	 * @since 0.1
	 */
	private Calculator() {
//		Do nothing.
	}
}

