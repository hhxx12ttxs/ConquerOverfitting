package com.parsa.app;

import java.util.Scanner;

;

	/**
	 * This class calculates factorial of a number.
	 * 
	 * @author ramesh
	 *
	 */
	public class Factorial {

		/**
		 * This method returns factorial of provided number.
		 * 
		 * @param n - num
		 * @return int - factorial
		 */
		
		long getFactorial(long n) {

			long output;

			if (n == 0)
				return 1;

		    if (n == 1)
				return 1;
			if(n<0)
				return output=getFactorial(n-1)*(-n);
				

			else 

				output = getFactorial(n - 1) * n;

			return output;
		}

	}



