package javacommon.util;

import java.util.Random;

/**
 * ??? ?? ??
 * 
 * @author bzq 2010.10.28
 * 
 */
public class MyRandomNum {

	public static final String allChar = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static final String letterChar = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static final String numberChar = "0123456789";
	public static final String numberValue = "01234567890123456789012345678901234567890123456789";
	/**
	 * * ????????????(???????????) * *
	 * 
	 * @param length
	 *            ??????? *
	 * @return ?????
	 */
	public static String generateString(int length) {
		StringBuffer sb = new StringBuffer();
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			sb.append(allChar.charAt(random.nextInt(allChar.length())));
		}
		return sb.toString();
	}

	/**
	 * * ???????????????(????????) * *
	 * 
	 * @param length
	 *            ??????? *
	 * @return ?????
	 */
	public static String generateMixString(int length) {
		StringBuffer sb = new StringBuffer();
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			sb.append(allChar.charAt(random.nextInt(letterChar.length())));
		}
		return sb.toString();
	}

	/**
	 * * ?????????????????(????????) * *
	 * 
	 * @param length
	 *            ??????? *
	 * @return ?????
	 */
	public static String generateLowerString(int length) {
		return generateMixString(length).toLowerCase();
	}

	/**
	 * * ?????????????????(????????) * *
	 * 
	 * @param length
	 *            ??????? *
	 * @return ?????
	 */
	public static String generateUpperString(int length) {
		return generateMixString(length).toUpperCase();
	}

	/**
	 * * ????????0??? * *
	 * 
	 * @param length
	 *            ????? *
	 * @return ?0???
	 */
	public static String generateZeroString(int length) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			sb.append('0');
		}
		return sb.toString();
	}

	/**
	 * * ??????????????????????0 * *
	 * 
	 * @param num
	 *            ?? *
	 * @param fixdlenth
	 *            ????? *
	 * @return ??????
	 */
	public static String toFixdLengthString(long num, int fixdlenth) {
		StringBuffer sb = new StringBuffer();
		String strNum = String.valueOf(num);
		if (fixdlenth - strNum.length() >= 0) {
			sb.append(generateZeroString(fixdlenth - strNum.length()));
		} else {
			throw new RuntimeException("???" + num + "??????" + fixdlenth
					+ "?????????");
		}
		sb.append(strNum);
		return sb.toString();
	}

	/**
	 * * ??????????????????????0 * *
	 * 
	 * @param num
	 *            ?? *
	 * @param fixdlenth
	 *            ????? *
	 * @return ??????
	 */
	public static String toFixdLengthString(int num, int fixdlenth) {
		StringBuffer sb = new StringBuffer();
		String strNum = String.valueOf(num);
		if (fixdlenth - strNum.length() >= 0) {
			sb.append(generateZeroString(fixdlenth - strNum.length()));
		} else {
			throw new RuntimeException("???" + num + "??????" + fixdlenth
					+ "?????????");
		}
		sb.append(strNum);
		return sb.toString();
	}
	/**
	 * ??????????
	 * @param length
	 * @return
	 */
	public static String password(int length) {
		StringBuffer sb = new StringBuffer();
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			sb.append(numberValue.charAt(random.nextInt(numberValue.length())));
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		System.out.println(generateString(4));
		System.out.println(generateMixString(15));
		System.out.println(generateLowerString(15));
		System.out.println(generateUpperString(15));
		System.out.println(generateZeroString(6));
		System.out.println(toFixdLengthString(123, 15));
		System.out.println(toFixdLengthString(123L, 15));
		System.out.println(password(6));
		
	}

}

