/* Problem 24: Lexicographic Permutations
A permutation is an ordered arrangement of objects. For example, 3124 is one possible permutation of the digits 1, 2, 3 and 4. 
If all of the permutations are listed numerically or alphabetically, we call it lexicographic order. The lexicographic permutations of 0, 1 and 2 are:
012   021   102   120   201   210

What is the millionth lexicographic permutation of the digits 0, 1, 2, 3, 4, 5, 6, 7, 8 and 9?
*/

import java.io.*;
import java.util.*;

public class Problem24 {
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		
		ArrayList<Integer> digits = new ArrayList<Integer>(Arrays.asList(0,1,2,3,4,5,6,7,8,9));
		ArrayList<Integer> lexico = new ArrayList<Integer>();
		int remainder = 999999;

		for (int i = 9; i >= 0; i--) {
			int permutation = factorial(i); // Calculates the permuatation of the remaining possible digits (rather than calling it twice)
			int index = remainder / permutation; // Calculates the current lexicographic digit
			lexico.add(digits.remove(index)); // Both adds and removes the digit to/from the respective arrays
			remainder = remainder - (index * permutation); // Calculates the new remainder
		}

		for (Integer i : lexico)
			System.out.print(i);
		System.out.println();

		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Total run time was " + totalTime + " milliseconds.");
	}

	public static int factorial(int n) {
		if (n == 0) return 1;
		else return n * factorial(n - 1);
	}
}
