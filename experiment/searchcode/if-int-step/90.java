package com.leetcode;

public class ClimbingStairs {
	public int climbStairs(int n) {

		if (n <= 2) {
			return n;
		} else {
			int[] step = new int[n];

			step[0] = 1;
			step[1] = 2;

			for (int i = 2; i < n; i++) {
				step[i] = step[i - 1] + step[i - 2];
			}
			return step[n - 1];
		}
	}

	public static void main(String[] args) {
		ClimbingStairs c = new ClimbingStairs();
		int x = c.climbStairs(4);
		System.out.println(x);
	}
}

