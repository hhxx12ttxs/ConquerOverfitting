	// ========================================================================================= //
	//		Author: Pluto Chan, Ka Hou															 //
	//		E-mail: jesic01@jesic-tech.com														 //
	//		Date: 2012/2/23																		 //
	//		Last-Mod: 2012/2/25																	 //
	//		Description: Assignment I for course CE1002 - Introduction to Computer Science II	 //
	//					 A simple calculator with basic operation function, using techniques	 //
	//					 like compiler to catch all the token in a input of string format		 //
	// ========================================================================================= //

package a1.s985003038;

import java.util.*;
import java.text.*;

public class A11 {
	public static void main(String[] argv){
		String username, studentNo, result = "";
        boolean newline = true;									// a flag to determine whether we keep calculating using the previous result
        Scanner input = new Scanner(System.in);					// a scanner for receiving your input
        
        System.out.print("Please enter your username: ");
        username = input.nextLine();							// input your username
        
        System.out.print("Please enter your student no: ");
        while(true){											// here is a infinite loop, so you can input your student No until your it is in correct format
        	studentNo = input.nextLine();						// input your student No
	        if(isStudentNo(studentNo)){							// send it to the method and check whether it is in correct format, returning true if it is correct, otherwise, false is returned
		        break;											// if the student No is correct, exit the infinite loop
	        } else {											// otherwise, show error message and let you input again
	        	System.out.println("Error: student no. should be an integer with 9 digits!!");
	        	System.out.print("Please enter your student no again: ");
	        }
        }
        
		System.out.println("Welcome " + username + " <" + studentNo + ">. Here is the rules and introduction to my program.");
		System.out.println("======================================================");
		System.out.println("1. You should input a complete formula, with all the numbers and symbols, in order to get an answer. For example: -1 + 2 * ( 3 - 4 ) * 5");
		System.out.println("2. You can input any integer numbers or floating-point numbers, and both can be mixed together. For example: -1 + 2.3 * -4 + 5.6");
		System.out.println("3. You can use the following symbols in your formula: +, -, *, /, %, (, )");
		System.out.println("4. You can add SPACE(s) wherever you want.");
		System.out.println("5. You can use the previous result in the next formula, for example, if the previous result is = 1, you can keep writing after it, that is, = 1 + 2");
		System.out.println("6. If you want to start a new calculation, that is, not using the previous result, just press ENTER");
		System.out.println("7. If you want to exit the program, enter \"EXIT\", what matter it is in uppercase or lowercase letter, or even in both");
		System.out.println("======================================================");
		System.out.println("Here, you can input your formula: ");
		
		while(input.hasNextLine()){											// here is a infinite loop so you can keep using the calculator until you want to exit
			String value = input.nextLine();								// get your formula
			if(newline && value.toLowerCase().equals("exit")) break;		// if you enter "EXIT", break out of the infinite loop so you can exit the program
			else if(value.isEmpty()){										// if you enter nothing, it means you want to start with a new formula
				if(!newline) System.out.println("Now, you can input your new formula <input EXIT to exit the program>: ");
				newline = true;
			} else {
				if(newline)
					result = scanForToken(value);							// if you are starting with a new formula, just send the formula you have input to the processing method
		        else
		        	result = scanForToken(result + value);					// otherwise, send the formula, with the previous result to the processing method
		        		
		        if(result.contains("ERROR")){								// if the processing method return an error message
		        	System.out.println(result);								// print out the error message, and let you start with a new formula
		        	System.out.println("Please input your new formula <input EXIT to exit the program>: ");
		        	newline = true;
		        } else {													// otherwise, just show out the result
		        	System.out.print("= " + result);
		        	newline = false;
		        }
			}
		}
		
		System.out.println("Bye, " + username + ".");						// before leaving the program, print out a leaving message
		return;
    }
	
	// ========================================================================================= //
	//		Name: scanForToken																	 //
	//		Input: a complete formula in string													 //
	//		Output: a solution to the input formula in string									 //
	//		Description: catch all the tokens in the input formula, and find out the solution	 //
	//					 to that formula														 //
	// ========================================================================================= //
	public static String scanForToken(String formula){						// a method for scanning out all the numbers and symbols, and return a final answer to the input formula
		String leftNumber = "", rightNumber = "";
		int tempSymbol = 0;
		int state = 0;
		boolean exitLoopFlag = false;
		int blanketStartIndex, blanketEndIndex;
		
		while(true){														// find out all the couple blankets 
			blanketStartIndex = formula.indexOf('(');						// find the nearest open blanket
			blanketEndIndex = formula.indexOf(')');							// and also find the nearest close blanket
			if(blanketStartIndex == -1 && blanketEndIndex == -1)			// if none of them are found, there is no blankets anymore, so leave the loop
				break;
			else if((blanketStartIndex == -1 && blanketEndIndex != -1) || (blanketStartIndex != -1 && blanketEndIndex == -1))
				return "ERROR: blanket doesn't match";						// if there are any single blanket, there should be a mistake, so return a error message
			else															// otherwise, send the blanketed part to the recursive function itself, and replace the returned answer to the formula
				formula = formula.substring(0, blanketStartIndex) + scanForToken(formula.substring(blanketStartIndex + 1, blanketEndIndex)) + formula.substring(blanketEndIndex + 1, formula.length());
		}
		
		for(int i = 0; i < formula.length() && !exitLoopFlag; i++){			// now, scan for all the tokens, including all the numbers and symbols
			if(formula.charAt(i) == ' ')									// if the coming character is a space, just forget it
				continue;
			else if((formula.charAt(i) >= '0' && formula.charAt(i) <= '9') || formula.charAt(i) == '.' || formula.charAt(i) == 'i'){
				if(state == 0 || state == 1){								// state 0 means nothing has found yet, state 1 means a number or decimal point has already found
					leftNumber = leftNumber + formula.charAt(i);			// the reason to separate these states is because we have to determine if the formula start with a number or a symbol, that is, a positive and negative sign
					state = 1;												// if it is a number or decimal point and no operators are found, we keep it as a left-hand-side number
				} else if(state == 2 || state == 3){						// otherwise, a operator is found, so the coming number is keep as a right-hand-side number
					rightNumber = rightNumber + formula.charAt(i);			// state 2 stands for a operator is just found, but no numbers are after it are found yet
					state = 3;												// state 3 stands for a operator is found, and a number after it is also found, it is used to consider if the formula end with a symbol 
				}
			} else if(formula.charAt(i) == '+' || formula.charAt(i) == '-' || formula.charAt(i) == '*' || formula.charAt(i) == '/' || formula.charAt(i) == '%'){
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
					if(formula.charAt(i) == '-'){					// however, if the coming character is a minus symbol, actually it can be a negative sign
						if(state == 0) leftNumber = "-";					// so we consider it as a part of the number
						else if(state == 2) rightNumber = "-";
					} else if(state == 0)									// if it is the beginning of the formula and the coming character is not a negative sign, there must be a mistake
						return "ERROR: formula cannot start with +, *, / or %";
					else if(state == 2)										// if both the previous and coming character are operators, there must be a mistake
						return "ERROR: two continuous symbols";
				}
			} else return "ERROR: unknown symbol";							// if it is neither a number nor a specified symbol, an unknown symbol is found
		}
		if(!exitLoopFlag)													// if no second operators are found anymore, the loop will also exit since the length of the formula
			if(state == 3)													// here, we calculate the leaving first operation cause by the exiting loop
				leftNumber = calculate(leftNumber, rightNumber, formula.charAt(tempSymbol));
			else if(state == 2)															// if state is not equal to 3, it means the formula is not ending with a number, but a symbol
				return "ERROR: end with symbol";							// therefore, we show the error message
		return leftNumber;													// we reduce all the solutions of the calculation to the leftNumber, now, we return the leftNumber as a final solution to the input formula
	}

	// ========================================================================================= //
	//		Name: calculate																		 //
	//		Input: left-hand-side number in string												 //
	// 			   right-hand-side number in string												 //
	// 			   operator in character														 //
	//		Output: a solution in string														 //
	//		Description: calculating with two numbers and one operator							 //
	// ========================================================================================= //
	public static String calculate(String left, String right, char operator){
		DecimalFormat doubleFormat = new DecimalFormat("#.##");				// a format standard to all the solution with decimal places
		switch(operator){													// switch for the operator, do the corresponding operation and return the solution
			case '+':
				return doubleFormat.format(Double.valueOf(left) + Double.valueOf(right));
			case '-':
				return doubleFormat.format(Double.valueOf(left) - Double.valueOf(right));
			case '*':
				return doubleFormat.format(Double.valueOf(left) * Double.valueOf(right));
			case '/':
				return doubleFormat.format(Double.valueOf(left) / Double.valueOf(right));
			case '%':
				return doubleFormat.format(Double.valueOf(left) % Double.valueOf(right));
			default:
				return "ERROR: unknown symbol";
		}
	}
	
	// ========================================================================================= //
	//		Name: isStudentNo																		 //
	//		Input: studentNo in string															 //
	//		Output: analyzing result in boolean													 //
	//		Description: analysis whether the input is a student no. or not						 //
	// ========================================================================================= //
	public static boolean isStudentNo(String input){							// a method to consider if the input student No is correct or not
		if(input.length() != 9) return false;								// if the length of the input is not equal to 9, it should not be a student No
		for(int i = 0; i < input.length(); i++){							// if any digit of the input is not a number, it should not be a student No
			if(input.charAt(i) < '0' || input.charAt(i) > '9')
				return false;
		}
		return true;														// otherwise, it is a student No
	}
}
