package com.leetcode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LongestConsecutiveSequence {

	public static void main(String[] args) {
		int[] num = {4,0,-4,-2,2,5,2,0,-8,-8,-8,-8,-1,7,4,5,5,-4,6,6,-3};
		int x = longestConsecutive2(num);
		System.out.println(x);
	}

	public static int longestConsecutive2(int[] num) {
		int max = 0;
		Map<Integer, Boolean> map = new HashMap<>();
		for (int i = 0; i < num.length; i++) {
			map.put(num[i], true);
		}
		for (int i = 0; i < num.length; i++) {
			int curr = num[i];
			int dec = curr - 1;
			int add = curr + 1;
			int len = 1;
			while (map.get(dec)!=null) {
				map.remove(dec);
				len++;
				dec--;
			}
			while (map.get(add)!=null) {
				map.remove(add);
				len++;
				add++;
			}
			max = len > max ? len : max;
		}

		return max;

	}

	public static int longestConsecutive(int[] num) {
		int max = 1;
		Arrays.sort(num);
		for (int i = 0; i < num.length; i++) {
			System.out.print(num[i] + " ");
		}
		System.out.println();
		int last = num[0];
		int tempMax = 1;
		for (int i = 1; i < num.length; i++) {
			int curr = num[i];
			if (curr == last) {
				continue;
			}
			if (curr - last == 1) {
				tempMax++;
				if (tempMax > max) {
					max = tempMax;
				}
			} else {
				tempMax = 1;
			}
			last = curr;
		}
		return max;

	}

}

