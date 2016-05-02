/*******************************************************************************
 *
 *==============================================================================
 *
 * Copyright (c) 2008-2011 ayound@gmail.com 
 * This program and the accompanying materials
 * are made available under the terms of the Apache License 2.0 
 * which accompanies this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 * All rights reserved.
 * 
 * Created on 2008-10-26
 *******************************************************************************/
package org.ayound.js.debug.script;

/**
 * 
 * the class is used to compile js line to debug line by add $jsd line before
 * the javascript line.
 * 
 */
public class ScriptCompileUtil {

	/**
	 * 
	 * @param lines
	 * @param resourcePath
	 * @param index
	 * @return
	 */
	public static String compileJsLine(String lines[], String resourcePath,
			int index) {

		String jsLine = lines[index];
		return compileOneLine(jsLine, lines, resourcePath, index);

	}

	/**
	 * compile one javascript line, if the line has ":" it maybe a json Object
	 * line or case,default
	 * 
	 * @param jsLine
	 * @param lines
	 * @param resourcePath
	 * @param index
	 * @return
	 */
	private static String compileOneLine(String jsLine, String lines[],
			String resourcePath, int index) {
		int firstColon = jsLine.indexOf(':');
		if (firstColon > 0 && firstColon < jsLine.length() - 2) {
			if (jsLine.trim().startsWith("case ")) {
				int caseOffset = jsLine.indexOf(':');
				if (caseOffset > 0) {
					String caseLine = jsLine.substring(0, caseOffset + 1)
							+ getdebugString(lines, resourcePath, index) + ";";
					if (caseOffset < jsLine.length()) {
						caseLine = caseLine + jsLine.substring(caseOffset + 1);
					}
					return caseLine;
				}
				return jsLine;
			}
			String beforeColon = jsLine.substring(0, firstColon).trim();
			boolean isColonLine = true;
			if (beforeColon.startsWith("'") && beforeColon.endsWith("'")) {
				isColonLine = true;
			} else if (beforeColon.startsWith("\"")
					&& beforeColon.endsWith("\"")) {
				isColonLine = true;
			} else if (beforeColon.equals("default")) {
				String defaultLine = jsLine.substring(0, firstColon + 1)
						+ getdebugString(lines, resourcePath, index) + ";";
				return defaultLine;
			} else {
				for (int i = 0; i < beforeColon.length(); i++) {
					char ch = beforeColon.charAt(i);
					if (Character.isLetterOrDigit(ch) || ch == '_' || ch == '$') {
						continue;
					} else {
						isColonLine = false;
					}
				}
			}
			if (isColonLine) {
				return "$jsd:" + getdebugString(lines, resourcePath, index)
						+ "," + jsLine;
			}
		} else {
			if (index > 1) {
				String lastLine = lines[index - 1];
				lastLine = lastLine.replaceAll("//.*", "").replaceAll("/\\*.*\\*/",
						"").trim();
				if ((lastLine.startsWith("if") || lastLine.startsWith("else if")) && (!lastLine.endsWith("{"))
						&& (!jsLine.startsWith("{"))) {
					boolean isIfLine = true;
					int bracket = 0;
					for (int i = 0; i < lastLine.length() - 1; i++) {
						char ch = lastLine.charAt(i);
						if (ch == '(') {
							bracket--;
						} else if (ch == ')') {
							bracket++;
							if (bracket == 0) {
								isIfLine = false;
							}
						} else {
							continue;
						}
					}
					if (isIfLine) {
						return "if(" + getdebugString(lines, resourcePath, index) + "||true)"
								+ jsLine;
					}
				} else if (lastLine.equals("else")) {
					return "if(" + getdebugString(lines, resourcePath, index) + "||true)"
							+ jsLine;
				}
			}
		}
		return getdebugString(lines, resourcePath, index) + ";" + jsLine;
	}

	/**
	 * get debug string insert before every javascript line.
	 * 
	 * @param lines
	 * @param resourcePath
	 * @param index
	 * @return
	 */
	private static String getdebugString(String lines[], String resourcePath,
			int index) {
		String debugStr = "$jsd('"
				+ resourcePath
				+ "',"
				+ (index + 1)
				+ ",this,((typeof(arguments)!=\"undefined\"?arguments:null)),function(__text){return eval(__text);})";
		return debugStr;
	}

	/**
	 * insert $jsd after <script> tag
	 * 
	 * @param lines
	 * @param resourcePath
	 * @param index
	 * @return
	 */
	public static String compileHtmlLine(String[] lines, String resourcePath,
			int index) {
		StringBuffer buffer = new StringBuffer();
		String htmlLine = lines[index];
		int scriptOffset = htmlLine.toLowerCase().indexOf("<script");
		String preLine = "";
		String lastLine = htmlLine;
		if(scriptOffset>=0){
			preLine = htmlLine.substring(0, scriptOffset + 7);
			lastLine = htmlLine.substring(scriptOffset + 7);
			buffer.append(preLine);
		}
		if(lastLine.trim().startsWith("<")){
			return htmlLine;
		}
		int offset = lastLine.indexOf('>');
		if (offset >= 0) {
			buffer.append(lastLine
					.substring(0, offset));
			if (!buffer.toString().toLowerCase().contains("<script")) {
				for (int i = index - 1; i >= 0; i--) {
					String line = lines[i];
					int offsetScript = line.toLowerCase().indexOf("<script");
					if (offsetScript > -1) {
						buffer.append(line.substring(offsetScript));
						break;
					} else {
						buffer.append(line);
					}
				}
			}
			if (buffer.toString().contains(">")) {
				if(scriptOffset>=0){
					htmlLine = preLine +  lastLine.substring(0, offset + 1)
					+ getdebugString(lines, resourcePath, index) + ";"
					+ lastLine.substring(offset + 1);
				}else{					
					htmlLine = preLine + compileOneLine(lastLine, lines, resourcePath, index);
				}
			} else {
				htmlLine = preLine +  lastLine.substring(0, offset + 1)
						+ getdebugString(lines, resourcePath, index) + ";"
						+ lastLine.substring(offset + 1);
			}

		} else {
			htmlLine = compileOneLine(htmlLine, lines, resourcePath, index);
		}
		return htmlLine;
	}
}

