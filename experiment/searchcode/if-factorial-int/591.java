package com.slgerkamp.mkjavaalgorithm.recursive;

/**
 * 階乗値を再帰を使って求める
 *
 */
public class Factorial {

	/**
	 * 階乗値を再帰を使って求める
	 * @param i
	 * @return
	 */
	public static int factorial(int i){
		if(i > 0){
			return i * factorial(i - 1);
		} else {
			return 1;
		}		
	}
}

