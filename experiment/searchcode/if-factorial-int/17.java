package net.euler.problem024;

import java.util.Arrays;

/**
 * A permutation is an ordered arrangement of objects. For example, 3124 is one
 * possible permutation of the digits 1, 2, 3 and 4. If all of the permutations
 * are listed numerically or alphabetically, we call it lexicographic order. The
 * lexicographic permutations of 0, 1 and 2 are:
 * 
 * 012 021 102 120 201 210
 * 
 * What is the millionth lexicographic permutation of the digits 0, 1, 2, 3, 4,
 * 5, 6, 7, 8 and 9?
 */
public class Problem {

	public static void main(String[] args) {
		System.out.println(Arrays.toString(Problem.getNPermutation(1_000_000, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9})));
	}

	static int[] getNPermutation(int n, int[] digits) {
		int[] permutation = new int[digits.length];
		Arrays.sort(digits);

		int factorial;
		int pos = n;
		int index;
		for (int i = permutation.length - 1; i >= 0; i--) {
			factorial = factorial(i);
			index = 0;
			while (pos > factorial) {
				pos -= factorial;
				index++;
			}
			permutation[permutation.length - 1 - i] = digits[index];
			digits = delete(index, digits);
		}

		return permutation;
	}
	
	static int[] delete(int index, int[] array) {
		if(index < 0 || index >= array.length) return array;
		
		int[] newArray = new int[array.length - 1];
		int pos = 0;
		for(int i = 0; i < array.length; i++) {
			if(i != index) {
				newArray[pos] = array[i];
				pos++;
			}
		}
		
		
		return newArray;
	}

	static int factorial(int n) {
		int factorial = 1;
		while (n > 1)
			factorial *= n--;
		return factorial;
	}
}

