package com.leetcode;

public class NextPermutation {

	public static void main(String[] args) {
		int[] num = {1,1,5 };
		NextPermutation np = new NextPermutation();
		np.nextPermutation(num);
	}

	void nextPermutation(int[] num) {

		if (num.length > 1) {
			int i = num.length - 1;
			for (; i >= 1; i--) {
				if (num[i - 1] < num[i]) {
					break;
				}
			}
			if (i == 0) {
				for (int k = 0; k < num.length / 2; k++) {
					swap(num, k, num.length - 1 - k);
				}
			} else {
				int ii = i;
				i -= 1;
				int j = num.length - 1;
				for (; j >= 0; j--) {
					if (num[i] < num[j]) {
						break;
					}
				}
				swap(num, i, j);
				for (int k = 0; k < (num.length - ii) / 2; k++) {
					swap(num, ii + k, num.length - 1 - k);
				}
			}
		}
		for (int i = 0; i < num.length; i++) {
			System.out.print(num[i]);
		}
	}

	void swap(int[] num, int i, int j) {
		int t = num[i];
		num[i] = num[j];
		num[j] = t;
	}
}

