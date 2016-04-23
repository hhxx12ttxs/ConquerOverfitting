package com.chinaer.util;

//import java.util.HashSet;
import java.util.Random;

//import java.util.Set;

public class NumberUtil {

	/**
	 * ????[begin,end)??????end
	 * 
	 * @param begin
	 * @param end
	 * @param size
	 *            ??????
	 * @return
	 */
	public static int[] generateRandomNumber(int begin, int end, int size) {
		if (size <= 0) {
			return null;
		}
		if (begin >= end || (end - begin) < size) {
			return null;
		}
		int[] seed = new int[end - begin];

		for (int i = begin; i < end; i++) {
			seed[i - begin] = i;
		}
		int[] ranArr = new int[size];
		Random ran = new Random();
		for (int i = 0; i < size; i++) {
			try {
				Thread.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			int j = ran.nextInt(seed.length - i);
			ranArr[i] = seed[j];
			seed[j] = seed[seed.length - 1 - i];
		}
		return ranArr;
	}

	public static int[] generateRandomNumberForZC(int begin, int end, int size) {
		if (size <= 0) {
			return null;
		}
		// if (begin >= end || (end - begin) < size) { ????????(end-begin)<size
		if (begin >= end) {
			return null;
		}

		int[] seed = new int[end - begin];
		for (int i = begin; i < end; i++) {
			seed[i - begin] = i;
		}
		int[] ranArr = new int[size];
		Random ran = new Random();
		for (int i = 0; i < size; i++) {
			try {
				Thread.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// int j = ran.nextInt(seed.length - i);
			// ranArr[i] = seed[j];
			// seed[j] = seed[seed.length - 1 - i];
			int j = ran.nextInt(end);
			ranArr[i] = seed[j];
			// seed[j] = seed[seed.length - 1 - i];
		}
		return ranArr;
	}

	// public Integer[] generateBySet(int begin, int end, int size) {
	// if (begin >= end || (end - begin) < size) {
	// return null;
	// }
	//
	// Random ran = new Random();
	// Set<Integer> set = new HashSet<Integer>();
	// while (set.size() < size) {
	// set.add(begin + ran.nextInt(end - begin));
	// }
	//
	// Integer[] ranArr = new Integer[size];
	// ranArr = set.toArray(new Integer[size]);
	// //ranArr = (Integer[]) set.toArray();
	//
	// return ranArr;
	// }
	// public boolean isInteger(String s){
	// if((s != null)&&(s!=""))
	// return s.matches("^[0-9]*$");
	// else
	// return false;
	// }
	// public boolean isDouble(String value) {
	// try {
	// Double.parseDouble(value);
	// if (value.contains("."))
	// return true;
	// return false;
	// } catch (NumberFormatException e) {
	// return false;
	// }
	// }
	// public boolean isNumber(String value) {
	// return isInteger(value) || isDouble(value);
	// }
	// public static String generateNumber(int length) {
	// if (length < 1)
	// return null;
	// char c[] = { '1', '2', '3', '4', '5', '6', '7', '8', '9' ,'0'};
	// StringBuffer sb = new StringBuffer();
	// int r = (int) Math.round(Math.random() * 100D);
	// for (int i = 0; i < length; i++) {
	// r = (int) Math.round(Math.random() * 100D);
	// sb.append(c[r % c.length]);
	// }
	// return sb.toString();
	// }

	/**
	 * ?????
	 */
	public static boolean isPrimes(int n) {
		for (int i = 2; i <= Math.sqrt(n); i++) {
			if (n % i == 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * ??
	 * 
	 * @param n
	 * @return
	 */
	public static int factorial(int n) {
		if (n == 1) {
			return 1;
		}
		return n * factorial(n - 1);
	}

	// public static int getRandomInt(int min, int max) {
	// // include min, exclude max
	// int result = min + new Double(Math.random() * (max - min)).intValue();
	//
	// return result;
	// }
	// ????
	public static void sort(int[] array) {// ??????
		if (array == null) {
			return;
		}
		int temp = 0;
		for (int i = 0; i < array.length; i++) {
			for (int j = i; j < array.length; j++) {
				if (array[i] > array[j]) {
					temp = array[i];
					array[i] = array[j];
					array[j] = temp;
				}
			}
		}
	}

	/**
	 * ?????
	 * 
	 * @param x
	 * @return
	 */
	public static long sqrt(long x) {
		long y = 0;
		long b = (~Long.MAX_VALUE) >>> 1;
		while (b > 0) {
			if (x >= y + b) {
				x -= y + b;
				y >>= 1;
				y += b;
			} else {
				y >>= 1;
			}
			b >>= 2;
		}
		return y;
	}

	public static void main(String[] args) {
		// NumberUtil util=new NumberUtil();
		// int[] array=new int[]{4,6,1,7,10,40,30};
		// util.sort(array);
		// for(int i=0;i<array.length;i++){
		// System.out.print(array[i]+",");
		// }
		// System.out.println(util.sqrt(144L));
	}
}

