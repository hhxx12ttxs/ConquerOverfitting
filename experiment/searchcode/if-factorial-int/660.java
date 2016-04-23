package com.rationalcoding.combinatorics.tests;

/**
 * Utils for combination functions
 * @author yarlagadda
 *
 */
public class CombinationUtils {
	
	public static int factorial(int number) {
		if(number <= 0){
			return 0;
		}
		int factorial = 1;
		for (int index = number; index > 0 ; index--) {
			factorial *= index;
		}
		return factorial;
	}

}

