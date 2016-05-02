	// ========================================================================================= //
	//		Author: Pluto Chan, Ka Hou															 //
	//		E-mail: jesic01@jesic-tech.com														 //
	//		Date: 2012/2/23																		 //
	//		Last-Mod: 2012/4/13																	 //
	//		Description: Assignment VIII for course CE1002 - Introduction to Computer Science II //
	//					 A simple calculator with basic operation function, using techniques	 //
	//					 like compiler to catch all the token in a input of string format,		 //
	//					 adding GUI and exception handler in the last edition					 //
	// ========================================================================================= //

package a8.s985003038;

import java.text.*;

public class Calculator {
	private FrameWork frame;
	
	public Calculator(){
		frame = new FrameWork();
	}
	
	// ========================================================================================= //
	//		Name: showCalculator																 //
	//		Input: none																			 //
	//		Output: none																		 //
	//		Description: show the calculator framework											 //
	// ========================================================================================= //
	public void showCalculator(){
		frame.setVisible(true);
	}
	
	// ========================================================================================= //
	//		Name: hideCalculator																 //
	//		Input: none																			 //
	//		Output: none																		 //
	//		Description: hide the calculator framework											 //
	// ========================================================================================= //
	public void hideCalculator(){
		frame.setVisible(false);
	}
	
	// ========================================================================================= //
	//		Name: scanForToken																	 //
	//		Input: a complete formula in string													 //
	//		Output: a solution to the input formula in string									 //
	//		Description: catch all the tokens in the input formula, and find out the solution	 //
	//					 to that formula														 //
	// ========================================================================================= //
	public static String scanForToken(String formula) throws CalculatorException {
		String leftNumber = "", rightNumber = "";							// a method for scanning out all the numbers and symbols, and return a final answer to the input formula
		boolean leftDotFlag = false, rightDotFlag = false;
		int tempSymbol = 0;
		int state = 0;
		boolean exitLoopFlag = false;
		int blanketStartIndex, blanketEndIndex;
		
		while(true){														// find out all the couple blankets 
			blanketStartIndex = formula.indexOf('(');						// find the nearest open blanket
			blanketEndIndex = formula.indexOf(')');							// and also find the nearest close blanket
			if(blanketStartIndex != -1){
				int lastBlanketStartIndex = blanketStartIndex, nextBlanketStartIndex = blanketStartIndex;
				while(true){												// matching the open blanket with the close blanket
					nextBlanketStartIndex = nextBlanketStartIndex + formula.substring(nextBlanketStartIndex + 1, formula.length()).indexOf('(') + 1;
					if(lastBlanketStartIndex != nextBlanketStartIndex && nextBlanketStartIndex < blanketEndIndex){
						blanketEndIndex = blanketEndIndex + formula.substring(blanketEndIndex + 1, formula.length()).indexOf(')') + 1;
						lastBlanketStartIndex = nextBlanketStartIndex;
					} else break;
				}
			}
			if(blanketStartIndex == -1 && blanketEndIndex == -1)			// if none of them are found, there is no blankets anymore, so leave the loop
				break;
			else if((blanketStartIndex == -1 && blanketEndIndex != -1) || (blanketStartIndex != -1 && blanketEndIndex == -1))
				throw new CalculatorException(2);							// if there are any single blanket, there should be a mistake, throw an exception
			else if((blanketStartIndex > 0 && isDigit(formula, blanketStartIndex-1)) || (blanketEndIndex < formula.length()-1 && isDigit(formula, blanketEndIndex+1)))
				throw new CalculatorException(3);							// if there is no operators outside the blanket, two continuous symbol are found
			else															// otherwise, send the blanketed part to the recursive function itself, and replace the returned answer to the formula
				formula = formula.substring(0, blanketStartIndex) + scanForToken(formula.substring(blanketStartIndex + 1, blanketEndIndex)) + formula.substring(blanketEndIndex + 1, formula.length());
		}
		
		for(int i = 0; i < formula.length() && !exitLoopFlag; i++){			// now, scan for all the tokens, including all the numbers and symbols
			if(formula.charAt(i) == ' ')									// if the coming character is a space, just forget it
				continue;
			else if(isDigit(formula, i) || formula.charAt(i) == '.'){
				if(state == 0 || state == 1){								// state 0 means nothing has found yet, state 1 means a number or decimal point has already found
					leftNumber = leftNumber + formula.charAt(i);			// the reason to separate these states is because we have to determine if the formula start with a number or a symbol, that is, a positive and negative sign
					state = 1;												// if it is a number or decimal point and no operators are found, we keep it as a left-hand-side number
					if(formula.charAt(i) == '.')							// otherwise, a operator is found, so the coming number is keep as a right-hand-side number
						if(leftDotFlag) throw new CalculatorException(1);	// state 2 stands for a operator is just found, but no numbers are after it are found yet
						else leftDotFlag = true;							// state 3 stands for a operator is found, and a number after it is also found, it is used to consider if the formula end with a symbol
				} else if(state == 2 || state == 3){
					rightNumber = rightNumber + formula.charAt(i);
					state = 3;
					if(formula.charAt(i) == '.')							// to prevent a number with more than one dot
						if(rightDotFlag) throw new CalculatorException(1);	// we use a flag to check it
						else rightDotFlag = true;
				}
			} else if(isOperator(formula, i)){
				if(state == 1){												// if state is 1, it means the first symbol is found				
					tempSymbol = i;											// we keep the index of the symbol
					state = 2;												
				} else if(state == 3){										// if state is 3, it means at least one operator is found
					if(formula.charAt(tempSymbol) == '*' || formula.charAt(tempSymbol) == '/' || formula.charAt(tempSymbol) == '%' || formula.charAt(i) == '+' || formula.charAt(i) == '-'){
																			// and now, the second operator is also found
																			// according to the rule of multiplication or division before addition or subtraction, there are three cases we have to consider
																			// if the first operator is multiplied or divided, we don't care whatever the second operator is, we can just finish the first operation
																			// if both the first and the second operator are plus or minus, we can still don't care about that, and just finish the first operation
						leftNumber = calculate(leftNumber, rightNumber, formula.charAt(tempSymbol));
						tempSymbol = i;										// we call the calculate function, put both side numbers and the operator as a parameter, and get the returned answer as a new left-hand-side number
						rightNumber = "";									// then set the current symbol to be the first symbol and reset the right-hand-side number
					} else {												// otherwise, it is the third case, the first operator is plus or minus, but the second is multiplied or divided
																			// so we recall the recursive function itself, cut the formula after the plus or minus operator
																			// put the right-hand-side formula as a parameter so we can calculate the right-hand-side multiplied or divided first 
						rightNumber = scanForToken(formula.substring(tempSymbol + 1, formula.length()));
						leftNumber = calculate(leftNumber, rightNumber, formula.charAt(tempSymbol));
						exitLoopFlag = true;								// then we get a returned solution to the right-hand-side part, so we can just calculate with the left-hand-side number and the right-hand-side solution
																			// and so, we finish scanning the whole formula, so we set the exit flag to be true and leave the scanning loop
					}
					state = 2;
				} else {													// if state is 0, it means it is the beginning of the formula and the coming character should be a number
																			// if state is 2, it means the previous character is symbol, so the coming character should be a number
					if(formula.charAt(i) == '-'){							// however, if the coming character is a minus symbol, actually it can be a negative sign
						if(state == 0) leftNumber = "-";					// so we consider it as a part of the number
						else if(state == 2) rightNumber = "-";
					} else if(state == 0)									// if it is the beginning of the formula and the coming character is not a negative sign, there must be a mistake
						throw new CalculatorException(5);
					else if(state == 2)										// if both the previous and coming character are operators, there must be a mistake
						throw new CalculatorException(4);
				}
			} else throw new CalculatorException(0);						// if it is neither a number nor a specified symbol, an unknown symbol is found
		}
		if(!exitLoopFlag)													// if no second operators are found anymore, the loop will also exit since the length of the formula
			if(state == 3)													// here, we calculate the leaving first operation cause by the exiting loop
				leftNumber = calculate(leftNumber, rightNumber, formula.charAt(tempSymbol));
			else if(state == 2)															// if state is not equal to 3, it means the formula is not ending with a number, but a symbol
				throw new CalculatorException(6);							// therefore, throw an exception
		if(leftNumber == "-")												// if the formula has only a minus operator, throw an exception
			throw new CalculatorException(6);
		if(leftNumber.charAt(leftNumber.length()-1) == '.')					// otherwise, we reduce all the solutions of the calculation to the leftNumber, now, we return the leftNumber as a final solution to the input formula
			return leftNumber + '0';										// if a dot is at the end of the formula, add a zero to the decimal place
		else
			return leftNumber;												// otherwise, return leftNumber
	}

	// ========================================================================================= //
	//		Name: calculate																		 //
	//		Input: left-hand-side number in string												 //
	// 			   right-hand-side number in string												 //
	// 			   operator in character														 //
	//		Output: a solution in string														 //
	//		Description: calculating with two numbers and one operator							 //
	// ========================================================================================= //
	public static String calculate(String left, String right, char operator) throws CalculatorException {
		DecimalFormat doubleFormat = new DecimalFormat("#.##");				// a format standard to all the solution with decimal places
		if(left.charAt(left.length()-1) == '.') left += '0';				// if a dot is at the end of an operand, add a zero to the decimal place
		if(right.charAt(right.length()-1) == '.') right += '0';				// if a dot is at the end of an operand, add a zero to the decimal place
		switch(operator){													// switch for the operator, do the corresponding operation and return the solution
			case '+':
				return doubleFormat.format(Double.valueOf(left) + Double.valueOf(right));
			case '-':
				return doubleFormat.format(Double.valueOf(left) - Double.valueOf(right));
			case '*':
				return doubleFormat.format(Double.valueOf(left) * Double.valueOf(right));
			case '/':														// check the divisor if it is zero
				if(Double.valueOf(right) == 0) throw new CalculatorException(7);
				return doubleFormat.format(Double.valueOf(left) / Double.valueOf(right));
			case '%':														// check the divisor if it is zero
				if(Double.valueOf(right) == 0) throw new CalculatorException(7);
				return doubleFormat.format(Double.valueOf(left) % Double.valueOf(right));
			default:
				throw new CalculatorException(0);							// undefined symbol
		}
	}
	
	// ========================================================================================= //
	//		Name: isOperator																	 //
	//		Input: a formula as string															 //
	// 			   the position of the character which we want to check in the formula			 //
	//		Output: the result as boolean														 //
	//		Description: check if a character in the formula is an operator						 //
	// ========================================================================================= //
	private static boolean isOperator(String formula, int position){
		if(formula.charAt(position) == '+' || formula.charAt(position) == '-' || formula.charAt(position) == '*' || formula.charAt(position) == '/' || formula.charAt(position) == '%')
			return true;													// if it is a defined symbol, return true
		else
			return false;													// otherwise, return false
	}
	
	// ========================================================================================= //
	//		Name: isDigit																		 //
	//		Input: a formula as string															 //
	// 			   the position of the character which we want to check in the formula			 //
	//		Output: the result as boolean														 //
	//		Description: check if a character in the formula is a digit							 //
	// ========================================================================================= //
	private static boolean isDigit(String formula, int position){
		if(formula.charAt(position) >= '0' && formula.charAt(position) <= '9')
			return true;													// if it is a digit, return true
		else
			return false;													// otherwise, return false
	}
}

