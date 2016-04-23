/**
 * 
 */
package com.parsa.service;

/**
 * @author ramesh
 *
 */
public class FactorialService {
	/**
	 * @param number
	 * @return
	 */
	public int getFactorialOfNumber(int number) {
		int factorial = 0;
		if (number == 0) {
			factorial = 1;
		} else if (number == 1) {
			factorial = 1;
		} else {
			factorial = number * (getFactorialOfNumber(number - 1));
		}
		return factorial;
	}
}

