package com.leetcode;

public class MaximumSubArray {

	public static void main(String[] args) {
		int[] A = { -2, 1, -3, 4, -1, 2, 1, -5, 4 };
		int x = maxSubArray2(A);
		System.out.println(x);
	}

	public static int maxSubArray(int[] A) {
		int tmp = 0;
		int max = 0;
		for (int i = 0; i < A.length; i++) {
			for (int j = i; j < A.length; j++) {
				tmp += A[j];
				if (tmp > max) {
					max = tmp;
				}
			}
			tmp = 0;
		}
		return max;
	}

	public static int maxSubArray2(int[] A) {

		int max = A[0];
		int sum = A[0];
		for (int i = 1; i < A.length; i++) {
			sum = sum < 0 ? A[i] : A[i] + sum;
			if (sum > max)
				max = sum;
		}
		return max;
	}
}

