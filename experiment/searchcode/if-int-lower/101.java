package com.elecfant.lib;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.elecfant.lib.logger.Logger;

public final class Utils {
	
	public final static float DEG_RAD = 0.01745329251994329576923690768489f;
	public final static float RAD_DEG = 57.295779513082320876798154814105f;
	public final static float PI_HALF = 1.5707963267948966192313216916398f;
	public final static float PI_DOUBLE = 6.283185307179586476925286766559f;
	
	public static float coTangent(float angle) {
		return (float) (1 / Math.tan(angle));
	}
	
	public static float clamp(float value, float min, float max) {
		if (value > max) return max;
		if (value < min) return min;
		return value;
	}
	
	public static double clamp(double value, double min, double max) {
		if (value > max) return max;
		if (value < min) return min;
		return value;
	}
	
	public static int clamp(int value, int min, int max) {
		if (value > max) return max;
		if (value < min) return min;
		return value;
	}
	
	public static long clamp(long value, long min, long max) {
		if (value > max) return max;
		if (value < min) return min;
		return value;
	}
	
	public static short clamp(short value, short min, short max) {
		if (value > max) return max;
		if (value < min) return min;
		return value;
	}
	
	public static byte clamp(byte value, byte min, byte max) {
		if (value > max) return max;
		if (value < min) return min;
		return value;
	}
	
	public static double lerp(double start, double end, double value) {
		return start*(1-value)+end*value;
	}
	public static float lerp(float start, float end, float value) {
		return start*(1-value)+end*value;
	}
	
	private final static String LATIN_CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmonpqrstuvwxyz";
	private final static String NUMBER_CHARSET = "0123456789";
	private final static String COMMON_PUNCTUATION_CHARSET = ".,;:-+=_()<>[]{}!?@#$%^&*/\\|~`'\"";
	private final static String DEFAULT_CHARSET = LATIN_CHARSET+NUMBER_CHARSET+COMMON_PUNCTUATION_CHARSET;
	
	public static String generateRandomString(int length) {
		return generateRandomString(length, DEFAULT_CHARSET);
	}
	
	public static String generateRandomString(int length, String sourceCharset) {
		String result = "";
		int charID = -1;
		for (int i = 0; i < length; ++i) {
			charID = (int)Math.round(Math.random()*(sourceCharset.length()-1));
			result += sourceCharset.charAt(charID);
		}
		return result;
	}
	
	public static byte[] getMD5Bytes(String argument) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			Logger.e("Hashing algorithm MD5 is not available in current environment/system.");
			e.printStackTrace();
			return new byte[0];
		}
		md.update(argument.getBytes());
		return md.digest();
	}
	
	public static String getMD5String(String argument) {
		return bytesToHex(getMD5Bytes(argument));
	}
	
	final protected static char[] HEX_CHARSET = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = HEX_CHARSET[v >>> 4];
	        hexChars[j * 2 + 1] = HEX_CHARSET[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	/**
	 * Wrapper method for generating random integer number with uniform distribution.
	 * @param upper bound (included)
	 * @return random integer number in range [<i>0</i>; <i>upper</i>].
	 */
	public static int randomInt(int upper) {
		return (int)Math.round(Math.random()*upper);
	}
	
	/**
	 * Wrapper method for generating random integer number with uniform distribution.
	 * @param lower bound (included)
	 * @param upper bound (included)
	 * @return random integer number in range [<i>lower</i>; <i>upper</i>].
	 */
	public static int randomInt(int lower, int upper) {
		return lower+(int)Math.round(Math.random()*(upper-lower));
	}
}

