package com.emathias.projecteuler;

import java.util.ArrayList;
import java.util.List;

public class EulerUtils {
	
	public static double slope(double x1, double y1, double x2, double y2) {
		return ((y1 - y2) / (x1 - x2));
	}
	
	/**
	 * find the distance between two points
	 */
	public static double distance(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
	}
	
	/**
	 * Check the number to see if it is perfect, and return 0 if so.
	 * If not return negative number for deficient number and
	 * positive number for abundant number.
	 * @param num
	 * @return
	 */
	public static int isPerfect(int num) {
		int[] factors = findFactors(num);
		
		int sum = 0;
		for (int i = 0; i < factors.length; i++) {
			sum += factors[i];
		}
		sum -= num;
		
		if (sum == num) {
			return 0;
		} else if (sum < num) {
			return -1;
		} else {
			return 1;
		}
	}
	
	public static int factorialSmall(int num) {
		
		int answer = 1;
		
		for (int i = 2; i <= num; i++) {
			// int[] numArray = new int[(int) Math.log10(i) + 1];
			// for (int j = numArray.length; j > 0; j--)
			// numArray[numArray.length - j] = (int) (i / Math.pow(10, j - 1)) %
			// 10;
			
			answer = answer * i;
		}
		
		return answer;
	}
	
	/**
	 * compute a factorial and return the result as separate digits
	 * in an array of ints (this allows arbitrarily long answers)
	 * @param num
	 * @return
	 */
	public static int[] factorial(int num) {
		
		int[] answer = { 1 };
		
		for (int i = 2; i <= num; i++) {
			int[] numArray = new int[(int) Math.log10(i) + 1];
			for (int j = numArray.length; j > 0; j--)
				numArray[numArray.length - j] = (int) (i / Math.pow(10, j - 1)) % 10;
			
			answer = multiply(answer, numArray);
		}
		
		return answer;
	}
	
	public static int[] oneDividedBy(int n, int digits) {
		int[] answer = new int[digits];
		int numerator = 1;
		for (int i = 0; i < digits; i++) {
			answer[i] = numerator / n;
			numerator -= answer[i] * n;
			numerator *= 10;
		}
		return answer;
	}
	
	/**
	 * You may get an incorrect answer unless each array slot contains exactly
	 * one digit
	 * 
	 * @param n1
	 * @param n2
	 * @return
	 */
	public static int[] multiply(int[] n1, int[] n2) {
		
		int[] answer = new int[n1.length + n2.length];
		
		for (int i = 0; i < n1.length; i++) {
			for (int j = 0; j < n2.length; j++) {
				answer[i + j] += (n1[i] * n2[j]) / 10;
				answer[i + j + 1] += (n1[i] * n2[j]) % 10;
			}
			for (int k = answer.length - 1; k > 0; k--) {
				if (answer[k] > 9) {
					answer[k - 1] += answer[k] / 10;
					answer[k] = answer[k] % 10;
				}
			}
		}
		
		return answer;
	}
	
	public static long[] findCollatzChain(long num) {
		
		List<Long> chain = new ArrayList<Long>();
		long[] numbers;
		
		while (num > 1) {
			chain.add(num);
			if (num % 2 == 0) {
				num >>= 1;
			} else {
				num *= 3;
				num++;
			}
		}
		chain.add(num); // finish off with the final '1'
		
		numbers = new long[chain.size()];
		for (int i = 0; i < chain.size(); i++) {
			numbers[i] = chain.get(i);
		}
		
		return numbers;
	}
	
	public static int[] findFactors(int num) {
		
		List<Integer> tmp = new ArrayList<Integer>();
		int[] factors;
		
		for (int i = 1; i <= Math.sqrt(num); i++) {
			if (num % i == 0) {
				tmp.add(i);
				if (i != num / i)
					tmp.add(num / i);
			}
		}
		
		factors = new int[tmp.size()];
		for (int i = 0; i < tmp.size(); i++) {
			factors[i] = tmp.get(i);
		}
		
		return factors;
	}
	
	public static boolean isPrime(int num) {
		
		if (num < 0 || num % 2 == 0 || num == 1) {
			return false;
		}
		
		if ((num - 1) % 6 == 0 || (num + 1) % 6 == 0) {
			for (int i = 2; i <= Math.sqrt(num); i++) {
				if (num % i == 0) {
					return false;
				}
			}
			return true;
		}
		
		return false;
	}
}

