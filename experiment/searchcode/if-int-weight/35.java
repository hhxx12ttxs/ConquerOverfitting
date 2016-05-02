package com.aamend.dsa.examples;

/**
 * Generates an instance of the 0/1 knapsack problem with N items and maximum
 * weight W and solves it in time and space proportional to N * W using dynamic
 * programming.
 */
public class KnapSackProfit {

	public static int NUMBER_ITEMS = 5;
	public static int TOTAL_ALLOWED_WEIGHT = 10;

	public static void main(String[] args) {

		int[] profit = new int[NUMBER_ITEMS + 1];
		int[] weight = new int[NUMBER_ITEMS + 1];

		// ------------------------------------------------
		// generate random instance, items 1..N
		for (int n = 1; n <= NUMBER_ITEMS; n++) {
			profit[n] = (int) (Math.random() * 10 + 1);
			weight[n] = (int) (Math.random() * 5 + 1);
			System.out.println(n + " p:"
					+ profit[n] + ", w:" + weight[n]);
		}
		
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");

		// ------------------------------------------------
		// opt[n][w] = max profit of packing items 1..n with weight limit w
		// sol[n][w] = does opt solution to pack items 1..n with weight limit w
		// include item n?
		int[][] opt = new int[NUMBER_ITEMS + 1][TOTAL_ALLOWED_WEIGHT + 1];
		boolean[][] sol = new boolean[NUMBER_ITEMS + 1][TOTAL_ALLOWED_WEIGHT + 1];

		for (int n = 1; n <= NUMBER_ITEMS; n++) {
			for (int w = 1; w <= TOTAL_ALLOWED_WEIGHT; w++) {

				int option1 = opt[n - 1][w];
				int option2 = Integer.MIN_VALUE;
				if (weight[n] <= w) {
					option2 = profit[n] + opt[n - 1][w - weight[n]];
				}

				// select better of two options
				opt[n][w] = Math.max(option1, option2);
				sol[n][w] = (option2 > option1);
			}
		}

		// determine which items to take
		boolean[] take = new boolean[NUMBER_ITEMS + 1];
		int w = TOTAL_ALLOWED_WEIGHT;
		for (int n = NUMBER_ITEMS; n > 0; n--) {
			if (sol[n][w]) {
				take[n] = true;
				w = w - weight[n];
			} else {
				take[n] = false;
			}
		}

		// print results
		System.out.println("item" + "\t" + "profit" + "\t" + "weight" + "\t"
				+ "take");
		for (int n = 1; n <= NUMBER_ITEMS; n++) {
			System.out.println(n + "\t" + profit[n] + "\t" + weight[n] + "\t"
					+ take[n]);
		}
	}
}

