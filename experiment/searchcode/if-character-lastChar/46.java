/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package biz.bidi.archivee.commons.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;

import biz.bidi.archivee.commons.exceptions.ArchiveeException;
import biz.bidi.archivee.commons.model.mongodb.Pattern;
import biz.bidi.archivee.commons.model.mongodb.PatternChild;
import biz.bidi.archivee.commons.model.mongodb.PatternPath;
import biz.bidi.archivee.commons.model.mongodb.PatternPathEntry;

/**
 * Generic class which contains generic pattern rules/processes
 * @author Andrey Bidinotto
 * @email andreymoser@bidi.biz
 * @since Sep 14, 2012
 */
public class ArchiveePatternUtils {

	/**
	 * Returns true if the given regex is valid for the given string (str)
	 * @param str - the string to verify
	 * @param regex - the regex expression
	 * @return true if is valid or false otherwise
	 */
	public static boolean matchRegex(String str, String regex) {
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		
		return matcher.find();
	}
	
	/**
	 * Returns true if the given regex is valid for the given string (str)
	 * @param str - the string to verify
	 * @param regex - the regex expression
	 * @return true if is valid or false otherwise
	 */
	public static Integer[] getDigitValues(String str, int min, int max) throws ArchiveeException {
		String[] strArray = getRegexValues(str, "\\d{" + min + "," + max + "}");
		
		Integer[] values = new Integer[strArray.length];
		
		for(String value : strArray) {
			Integer.parseInt(value);
		}
		
		return values;
	}
	
	/**
	 * Returns true if the given regex is valid for the given string (str)
	 * @param str - the string to verify
	 * @param regex - the regex expression
	 * @return true if is valid or false otherwise
	 */
	public static String[] getRegexValues(String str, String regex) throws ArchiveeException {
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		
		ArrayList<String> values = new ArrayList<String>();
		
		while(matcher.find()) {
			values.add(matcher.group());
		}
		
		return values.toArray(new String[values.size()]);
	}
	
	/**
	 * Converts a simple regex string to a standard Java Regex string
	 * @param simpleRegex
	 * @return regex string
	 */
	public static String convertSimpleRegexToRegex(String simpleRegex) {
		String regex = "";
		
		char currentChar = '\0';
		
		for(int i = 0; i < simpleRegex.length(); i++) {
			currentChar = simpleRegex.charAt(i);
			
			if(currentChar == 'W') {
				regex += "[A-Za-z]+"; 
			} else if(currentChar == 'N') {
				regex += "(-|\\+)?[0-9]+(\\.[0-9]+)?"; 
			} else if(currentChar == 'M') {
				regex += "([A-Za-z]+|(-|\\+)?[0-9]+(\\.[0-9]+)?)+"; 
			} else if("\\.^$|?*+[]{}()-".indexOf(currentChar) != -1) {
				regex += "\\" + Character.toString(currentChar);
			} else {
				regex += Character.toString(currentChar);
			}
		}
		
		return regex;
	}
	
	/**
	 * Converts the string to a simple regex where words are converted to 'W', 
	 * numbers to 'N', words and numbers together to 'M'. The other are characthers aren't converted
	 * or replaced.
	 * 
	 * The string "Hey, there are 2 books on the table!!!" becomes "W, W W N W W W W!!!"  
	 *   
	 * @param str
	 * @return
	 */
	public static String convertToSimpleRegex(String str) {
		String sr = "";

		char lastChar = '\0';
		char currentChar = '\0';
		int i = 0;
		
		for(i = 0; i < str.length(); i++) {
		
			currentChar = str.charAt(i);
			
			sr = convertCharToSimpleRegex(sr, lastChar, currentChar);
			
			lastChar = currentChar;
		}
		
		if(i > 0 && str.length() > 0) {
			sr = convertCharToSimpleRegex(sr, lastChar, '\0');
		}
		
		sr = sr.replaceAll("N\\.N", "N");
		sr = sr.replaceAll("-N", "N");
		sr = sr.replaceAll("\\+N", "N");
		sr = sr.replaceAll("N+", "N");
		
		sr = sr.replaceAll("WN", "M");
		sr = sr.replaceAll("NW", "M");
		sr = sr.replaceAll("NM", "M");
		sr = sr.replaceAll("MN", "M");
		sr = sr.replaceAll("WM", "M");
		sr = sr.replaceAll("MW", "M");
		sr = sr.replaceAll("M+", "M");
		
		return sr;
	}

	/**
	 * 
	 * @param str
	 * @param lastChar
	 * @param currentChar
	 */
	private static String convertCharToSimpleRegex(String str, char lastChar, char currentChar) {
		if(Character.isLetter(currentChar)) {
			if(Character.isLetter(lastChar)) {
				//nothing, just go ahead
			} else if(Character.isDigit(lastChar)) {
				str += "N";
			} else {
				if(lastChar != '\0') {
					str += Character.toString(lastChar);
				}
			}
		} else if(Character.isDigit(currentChar)) {
			if(Character.isLetter(lastChar)) {
				str += "W";
			} else if(Character.isDigit(lastChar)) {
				//nothing, just go ahead
			} else {
				if(lastChar != '\0') {
					str += Character.toString(lastChar);
				}
			}
		} else {
			if(Character.isLetter(lastChar)) {
				str += "W";
			} else if(Character.isDigit(lastChar)) {
				str += "N";
			} else {
				if(lastChar != '\0') {
					str += Character.toString(lastChar);
				}
			}
		}
		return str;
	}
	
	public static PatternChild getLeafPattern(Pattern pattern, String simpleRegex) throws ArchiveeException {
		PatternChild leafPattern = null;
		
		for(PatternChild p : pattern.getPatterns()) {
			if(simpleRegex.startsWith(p.getValue(),p.getOffset())) {
				leafPattern = getLeafPatternChild(p, simpleRegex);
				if(leafPattern == null) {
					leafPattern = p;
				}
				break;
			}
		}
		
		return leafPattern;
	}
		
	private static PatternChild getLeafPatternChild(PatternChild pattern, String simpleRegex) throws ArchiveeException {
		PatternChild leafPattern = null;
		
		for(PatternChild p : pattern.getPatterns()) {
			if(simpleRegex.startsWith(p.getValue(),p.getOffset())) {
				leafPattern = getLeafPatternChild(p, simpleRegex);
				if(leafPattern == null) {
					leafPattern = p;
				}
				break;
			}
		}
		
		return leafPattern;
	}
	
	/**
	 * @param simpleRegexLogLine
	 * @param pattern
	 * @return
	 */
	public static boolean validatePatternForLogLine(String logLine, Pattern rootPattern) throws ArchiveeException {
		String simpleRegexLogLine = convertToSimpleRegex(logLine);
		
		if(simpleRegexLogLine.equals(rootPattern.getValue())) {
			return true;
		}
		
		boolean foundPattern = false;
		
		if(rootPattern.getPatterns() != null) {
			for(PatternChild p : rootPattern.getPatterns()) {
				if(p.getOffset() >= simpleRegexLogLine.length()) {
					continue;
				}
				
				if(simpleRegexLogLine.equals(rootPattern.getValue() + p.getValue())) {
					return true;
				}
				
				if(!simpleRegexLogLine.startsWith(p.getValue(),p.getOffset())) {
					continue;
				}
				
				if(validatePatternForLogLine(simpleRegexLogLine, logLine, p, rootPattern, rootPattern.getValue() + p.getValue())) {
					foundPattern = true;
				}
			}
		}
		
		return foundPattern;
	}
	
	/**
	 * @param simpleRegexLogLine
	 * @param pattern
	 * @return
	 */
	private static boolean validatePatternForLogLine(String simpleRegexLogLine, String logLine,PatternChild pattern, Pattern rootPattern, String fullPattern) throws ArchiveeException {
		
//		if(pattern == null) {
//			return false;
//		}
		
//		if(pattern.isRoot()) {
//			if(!simpleRegexLogLine.startsWith(pattern.getValue())) {
//				return false;
//			}
//			fullPattern = pattern.getValue();
//		}
		if(fullPattern == null || fullPattern.length() == 0) {
			fullPattern = rootPattern.getValue();
		}
		
		boolean foundPattern = false;
		
		if(pattern.getPatterns() != null) {
			for(PatternChild p : pattern.getPatterns()) {
				if(p.getOffset() >= simpleRegexLogLine.length()) {
					continue;
				}
				
				if(simpleRegexLogLine.equals(fullPattern + p.getValue())) {
					return true;
				}
				
				if(!simpleRegexLogLine.startsWith(p.getValue(),p.getOffset())) {
					continue;
				}
				
				if(validatePatternForLogLine(simpleRegexLogLine,logLine, p, rootPattern, fullPattern + p.getValue())) {
					foundPattern = true;
				}
			}
		}
		
		return foundPattern;
	}
	
	/**
	 * Returns all the pattern values for the given message
	 * @param message
	 * @return
	 * @throws ArchiveeException
	 */
	public static ArrayList<String> getPatternValues(String message) throws ArchiveeException {
		ArrayList<String> values;
		values = new ArrayList<String>(Arrays.asList(getRegexValues(message, convertSimpleRegexToRegex("M"))));
		return values;
	}
	
	/**
	 * Builds the pattern path based on the log message given and its pattern data 
	 * @param message
	 * @param pattern
	 * @return
	 * @throws ArchiveeException
	 */
	public static PatternPath findPatternPath(String message, Pattern pattern) throws ArchiveeException {
		PatternPath patternPath = new PatternPath();
		String simpleRegex = convertToSimpleRegex(message);
		
		int i = 0;
		
		PatternPathEntry value = new PatternPathEntry();
		value.setIndex(0);
		value.setWords(getPatternValues(pattern.getValue()).size());
		patternPath.getValues().add(value);
		
		for(PatternChild p : pattern.getPatterns()) {
			if(simpleRegex.startsWith(p.getValue(), p.getOffset())) {
				value = new PatternPathEntry();
				value.setIndex(i);
				value.setWords(getPatternValues(p.getValue()).size());
				patternPath.getValues().add(value);
				
				findPatternPath(simpleRegex,p,patternPath);
				break;
			}
			i++;
		}
		
		return patternPath;
	}
	
	private static boolean findPatternPath(String simpleRegex, PatternChild patternChild, PatternPath patternPath) throws ArchiveeException {
		boolean found = false; 
		
		int i = 0;
		for(PatternChild p : patternChild.getPatterns()) {
			if(simpleRegex.startsWith(p.getValue(), p.getOffset())) {
				PatternPathEntry value = new PatternPathEntry();
				value.setIndex(i);
				value.setWords(getPatternValues(p.getValue()).size());
				patternPath.getValues().add(value);
				
				findPatternPath(simpleRegex,p,patternPath);
				break;
			}
			i++;
		}
		if(patternChild.getPatterns().size() == 0) {
			found = true;
		}
		
		return found;
	}

	/**
	 * @param message
	 * @return
	 */
	public static String convertToRegex(String message) {
		String regex = "";
		
		char currentChar = '\0';
		
		for(int i = 0; i < message.length(); i++) {
			currentChar = message.charAt(i);
			
			if("\\.^$|?*+[]{}()-".indexOf(currentChar) != -1) {
				regex += "\\" + Character.toString(currentChar);
			} else {
				regex += Character.toString(currentChar);
			}
		}
		
		return regex;
	}

}

