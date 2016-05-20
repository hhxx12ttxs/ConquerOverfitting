/*  
 * Copyright (c) 2009  Christian Riekoff <info@texone.org>  
 *  
 *  This file is free software: you may copy, redistribute and/or modify it  
 *  under the terms of the GNU General Public License as published by the  
 *  Free Software Foundation, either version 2 of the License, or (at your  
 *  option) any later version.  
 *  
 *  This file is distributed in the hope that it will be useful, but  
 *  WITHOUT ANY WARRANTY; without even the implied warranty of  
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU  
 *  General Public License for more details.  
 *  
 *  You should have received a copy of the GNU General Public License  
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.  
 *  
 * This file incorporates work covered by the following copyright and  
 * permission notice:  
 */
package cc.creativecomputing.util;

/**
 * @author info
 * 
 */
public class CCParseUtil {
	/**
	 * <p>
	 * Convert an integer to a boolean. Because of how Java handles upgrading numbers, this will also cover byte and
	 * char (as they will upgrade to an int without any sort of explicit cast).
	 * </p>
	 * <p>
	 * The preprocessor will convert boolean(what) to parseBoolean(what).
	 * </p>
	 * 
	 * @return false if 0, true if any other number
	 */
	static final public boolean parseBoolean(int what) {
		return (what != 0);
	}

	/*
	 * // removed because this makes no useful sense static final public boolean parseBoolean(float what) { return (what
	 * != 0); }
	 */

	/**
	 * Convert the string "true" or "false" to a boolean.
	 * 
	 * @return true if 'what' is "true" or "TRUE", false otherwise
	 */
	static final public boolean parseBoolean(String what) {
		return new Boolean(what).booleanValue();
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	/*
	 * // removed, no need to introduce strange syntax from other languages static final public boolean[]
	 * parseBoolean(char what[]) { boolean outgoing[] = new boolean[what.length]; for (int i = 0; i < what.length; i++)
	 * { outgoing[i] = ((what[i] == 't') || (what[i] == 'T') || (what[i] == '1')); } return outgoing; }
	 */

	/**
	 * Convert a byte array to a boolean array. Each element will be evaluated identical to the integer case, where a
	 * byte equal to zero will return false, and any other value will return true.
	 * 
	 * @return array of boolean elements
	 */
	static final public boolean[] parseBoolean(byte what[]) {
		boolean outgoing[] = new boolean[what.length];
		for (int i = 0; i < what.length; i++) {
			outgoing[i] = (what[i] != 0);
		}
		return outgoing;
	}

	/**
	 * Convert an int array to a boolean array. An int equal to zero will return false, and any other value will return
	 * true.
	 * 
	 * @return array of boolean elements
	 */
	static final public boolean[] parseBoolean(int what[]) {
		boolean outgoing[] = new boolean[what.length];
		for (int i = 0; i < what.length; i++) {
			outgoing[i] = (what[i] != 0);
		}
		return outgoing;
	}

	/*
	 * // removed, not necessary... if necessary, convert to int array first static final public boolean[]
	 * parseBoolean(float what[]) { boolean outgoing[] = new boolean[what.length]; for (int i = 0; i < what.length; i++)
	 * { outgoing[i] = (what[i] != 0); } return outgoing; }
	 */

	static final public boolean[] parseBoolean(String what[]) {
		boolean outgoing[] = new boolean[what.length];
		for (int i = 0; i < what.length; i++) {
			outgoing[i] = new Boolean(what[i]).booleanValue();
		}
		return outgoing;
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	static final public byte parseByte(boolean what) {
		return what ? (byte) 1 : 0;
	}

	static final public byte parseByte(char what) {
		return (byte) what;
	}

	static final public byte parseByte(int what) {
		return (byte) what;
	}

	static final public byte parseByte(float what) {
		return (byte) what;
	}

	/*
	 * // nixed, no precedent static final public byte[] parseByte(String what) { // note: array[] return
	 * what.getBytes(); }
	 */

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
	static final public byte[] parseByte(boolean what[]) {
		byte outgoing[] = new byte[what.length];
		for (int i = 0; i < what.length; i++) {
			outgoing[i] = what[i] ? (byte) 1 : 0;
		}
		return outgoing;
	}

	static final public byte[] parseByte(char what[]) {
		byte outgoing[] = new byte[what.length];
		for (int i = 0; i < what.length; i++) {
			outgoing[i] = (byte) what[i];
		}
		return outgoing;
	}

	static final public byte[] parseByte(int what[]) {
		byte outgoing[] = new byte[what.length];
		for (int i = 0; i < what.length; i++) {
			outgoing[i] = (byte) what[i];
		}
		return outgoing;
	}

	static final public byte[] parseByte(float what[]) {
		byte outgoing[] = new byte[what.length];
		for (int i = 0; i < what.length; i++) {
			outgoing[i] = (byte) what[i];
		}
		return outgoing;
	}

	/*
	 * static final public byte[][] parseByte(String what[]) { // note: array[][] byte outgoing[][] = new
	 * byte[what.length][]; for (int i = 0; i < what.length; i++) { outgoing[i] = what[i].getBytes(); } return outgoing;
	 * }
	 */

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
	/*
	 * static final public char parseChar(boolean what) { // 0/1 or T/F ? return what ? 't' : 'f'; }
	 */

	static final public char parseChar(byte what) {
		return (char) (what & 0xff);
	}

	static final public char parseChar(int what) {
		return (char) what;
	}

	/*
	 * static final public char parseChar(float what) { // nonsensical return (char) what; }
	 * 
	 * static final public char[] parseChar(String what) { // note: array[] return what.toCharArray(); }
	 */

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
	/*
	 * static final public char[] parseChar(boolean what[]) { // 0/1 or T/F ? char outgoing[] = new char[what.length];
	 * for (int i = 0; i < what.length; i++) { outgoing[i] = what[i] ? 't' : 'f'; } return outgoing; }
	 */

	static final public char[] parseChar(byte what[]) {
		char outgoing[] = new char[what.length];
		for (int i = 0; i < what.length; i++) {
			outgoing[i] = (char) (what[i] & 0xff);
		}
		return outgoing;
	}

	static final public char[] parseChar(int what[]) {
		char outgoing[] = new char[what.length];
		for (int i = 0; i < what.length; i++) {
			outgoing[i] = (char) what[i];
		}
		return outgoing;
	}

	/*
	 * static final public char[] parseChar(float what[]) { // nonsensical char outgoing[] = new char[what.length]; for
	 * (int i = 0; i < what.length; i++) { outgoing[i] = (char) what[i]; } return outgoing; }
	 * 
	 * static final public char[][] parseChar(String what[]) { // note: array[][] char outgoing[][] = new
	 * char[what.length][]; for (int i = 0; i < what.length; i++) { outgoing[i] = what[i].toCharArray(); } return
	 * outgoing; }
	 */

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
	static final public int parseInt(boolean what) {
		return what ? 1 : 0;
	}

	/**
	 * Note that parseInt() will un-sign a signed byte value.
	 */
	static final public int parseInt(byte what) {
		return what & 0xff;
	}

	/**
	 * Note that parseInt('5') is unlike String in the sense that it won't return 5, but the ascii value. This is
	 * because ((int) someChar) returns the ascii value, and parseInt() is just longhand for the cast.
	 */
	static final public int parseInt(char what) {
		return what;
	}

	/**
	 * Same as floor(), or an (int) cast.
	 */
	static final public int parseInt(float what) {
		return (int) what;
	}

	/**
	 * Parse a String into an int value. Returns 0 if the value is bad.
	 */
	static final public int parseInt(String what) {
		return parseInt(what, 0);
	}

	/**
	 * Parse a String to an int, and provide an alternate value that should be used when the number is invalid.
	 */
	static final public int parseInt(String what, int otherwise) {
		try {
			int offset = what.indexOf('.');
			if (offset == -1) {
				return Integer.parseInt(what);
			} else {
				return Integer.parseInt(what.substring(0, offset));
			}
		} catch (NumberFormatException e) {
		}
		return otherwise;
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	static final public int[] parseInt(boolean what[]) {
		int list[] = new int[what.length];
		for (int i = 0; i < what.length; i++) {
			list[i] = what[i] ? 1 : 0;
		}
		return list;
	}

	static final public int[] parseInt(byte what[]) { // note this unsigns
		int list[] = new int[what.length];
		for (int i = 0; i < what.length; i++) {
			list[i] = (what[i] & 0xff);
		}
		return list;
	}

	static final public int[] parseInt(char what[]) {
		int list[] = new int[what.length];
		for (int i = 0; i < what.length; i++) {
			list[i] = what[i];
		}
		return list;
	}

	static public int[] parseInt(float what[]) {
		int inties[] = new int[what.length];
		for (int i = 0; i < what.length; i++) {
			inties[i] = (int) what[i];
		}
		return inties;
	}

	/**
	 * Make an array of int elements from an array of String objects. If the String can't be parsed as a number, it will
	 * be set to zero.
	 * 
	 * String s[] = { "1", "300", "44" }; int numbers[] = parseInt(s);
	 * 
	 * numbers will contain { 1, 300, 44 }
	 */
	static public int[] parseInt(String what[]) {
		return parseInt(what, 0);
	}

	/**
	 * Make an array of int elements from an array of String objects. If the String can't be parsed as a number, its
	 * entry in the array will be set to the value of the "missing" parameter.
	 * 
	 * String s[] = { "1", "300", "apple", "44" }; int numbers[] = parseInt(s, 9999);
	 * 
	 * numbers will contain { 1, 300, 9999, 44 }
	 */
	static public int[] parseInt(String what[], int missing) {
		int output[] = new int[what.length];
		for (int i = 0; i < what.length; i++) {
			try {
				output[i] = Integer.parseInt(what[i]);
			} catch (NumberFormatException e) {
				output[i] = missing;
			}
		}
		return output;
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	/*
	 * static final public float parseFloat(boolean what) { return what ? 1 : 0; }
	 */

	/**
	 * Convert an int to a float value. Also handles bytes because of Java's rules for upgrading values.
	 */
	static final public float parseFloat(int what) { // also handles byte
		return (float) what;
	}

	static final public float parseFloat(String what) {
		return parseFloat(what, Float.NaN);
	}

	static final public float parseFloat(String what, float otherwise) {
		try {
			return new Float(what).floatValue();
		} catch (NumberFormatException e) {
		}

		return otherwise;
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	/*
	 * static final public float[] parseFloat(boolean what[]) { float floaties[] = new float[what.length]; for (int i =
	 * 0; i < what.length; i++) { floaties[i] = what[i] ? 1 : 0; } return floaties; }
	 * 
	 * static final public float[] parseFloat(char what[]) { float floaties[] = new float[what.length]; for (int i = 0;
	 * i < what.length; i++) { floaties[i] = (char) what[i]; } return floaties; }
	 */

	static final public float[] parseByte(byte what[]) {
		float floaties[] = new float[what.length];
		for (int i = 0; i < what.length; i++) {
			floaties[i] = what[i];
		}
		return floaties;
	}

	static final public float[] parseFloat(int what[]) {
		float floaties[] = new float[what.length];
		for (int i = 0; i < what.length; i++) {
			floaties[i] = what[i];
		}
		return floaties;
	}

	static final public float[] parseFloat(String what[]) {
		return parseFloat(what, Float.NaN);
	}

	static final public float[] parseFloat(String what[], float missing) {
		float output[] = new float[what.length];
		for (int i = 0; i < what.length; i++) {
			try {
				output[i] = new Float(what[i]).floatValue();
			} catch (NumberFormatException e) {
				output[i] = missing;
			}
		}
		return output;
	}
}

