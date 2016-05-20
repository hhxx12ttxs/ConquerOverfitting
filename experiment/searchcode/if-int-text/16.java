package eclihx.core.haxe.model;

/**
 * haXe code simple formatter.
 */
public final class CodeFormatter {
	
	/**
	 *  Don't allow to instantiate this class.
	 */
	private CodeFormatter() {}
	
	/**
	 * Storage for format options. 
	 */
	public static class FormatOptions {
		
		/**
		 * @return the bracketNewLines
		 */
		public boolean isBracketNewLines() {
			return bracketNewLines;
		}

		/**
		 * @param bracketNewLines the bracketNewLines to set
		 */
		public void setBracketNewLines(boolean bracketNewLines) {
			this.bracketNewLines = bracketNewLines;
		}

		/**
		 * @return the insertTabs
		 */
		public boolean isInsertTabs() {
			return insertTabs;
		}

		/**
		 * @param insertTabs the insertTabs to set
		 */
		public void setInsertTabs(boolean insertTabs) {
			this.insertTabs = insertTabs;
		}

		/**
		 * @return the intendWidth
		 */
		public int getIntendWidth() {
			return intendWidth;
		}

		/**
		 * @param intendWidth the intendWidth to set
		 */
		public void setIntendWidth(int intendWidth) {
			this.intendWidth = intendWidth;
		}

		/**
		 * @return the oneOperatorOnLine
		 */
		public boolean isOneOperatorOnLine() {
			return oneOperatorOnLine;
		}

		/**
		 * @param oneOperatorOnLine the oneOperatorOnLine to set
		 */
		public void setOneOperatorOnLine(boolean oneOperatorOnLine) {
			this.oneOperatorOnLine = oneOperatorOnLine;
		}

		/**
		 * @return the indentOnEmptyLines
		 */
		public boolean isIndentOnEmptyLines() {
			return indentOnEmptyLines;
		}

		/**
		 * @param indentOnEmptyLines the indentOnEmptyLines to set
		 */
		public void setIndentOnEmptyLines(boolean indentOnEmptyLines) {
			this.indentOnEmptyLines = indentOnEmptyLines;
		}

		/**
		 * Move curly bracket to new line.
		 */
		private boolean bracketNewLines = false;
		
		/**
		 * Use tabs for indentation.
		 */
		private boolean insertTabs = false;
		
		/**
		 * Number of spaces in indentation.
		 */
		private int intendWidth = 4;
		
		/**
		 * Allows only one operator on the line.
		 * Will make forced new line after the <code>;</code> char.
		 */
		private boolean oneOperatorOnLine = true;

		/**
		 * Make indent on empty lines.
		 */
		private boolean indentOnEmptyLines = false;
	}
	
	/**
	 * Format text.
	 * @param text text to format.
	 * @param options set of options for the formatter.
	 * @return formatted text.
	 */
	public static String formatAll(
			final String text, 
			final FormatOptions options) {
		
		return format(text, options, 0, false);
	}
	
	/**
	 * Format text.
	 * 
	 * @param text text to format.
	 * @param options set of options for the formatter.
	 * @param lineIndentation pattern for line indentation.
	 * @return formatted text.
	 */
	public static String formatSelection(
			final String text, 
			final FormatOptions options,
			final String lineIndentation) {
		
		return format(text, options, getIndentationLevel(lineIndentation, singleIndentStr(options)), true);
	}
	
	/**
	 * Format text.
	 * @param text text to format.
	 * @param options set of options for the formatter
	 * @param initIndentationLevel level of text selection indentation
	 * @param firstLineAlreadyIndented is first line indented (useful for formatting selections)
	 * @return formatted text.
	 */
	public static String format(
			final String text, 
			final FormatOptions options,
			final int initIndentationLevel,
			final boolean firstLineAlreadyIndented) {
		
		// Global variable o count number of indentation for the current code.
		int numberOfIndentation = initIndentationLevel;
		
		// This variable is a buffer for the method result		
		final StringBuilder outputBuilder = new StringBuilder();
		
		// This variable will accumulate one line of the output.
		final StringBuilder outputLineBuffer = new StringBuilder();
		
		for (int index = 0; index < text.length(); ++index) {
			
			char currentChar = text.charAt(index); 
			
			switch (currentChar) {
				case '{':
					// Nothing in the current line or we shouldn't place forced line break
					if (outputLineBuffer.toString().trim().isEmpty() || !options.bracketNewLines) {
						
						// Insert additional space before the brace
						if (!checkChar(text, index-1, ' ')) {
							outputLineBuffer.append(' ');
						}
						
						outputLineBuffer.append('{');
						
						// Check {> construction
						if (checkChar(text, index+1, '>')) {
							outputLineBuffer.append('>');
							++index;
						}						
						
						appendNewLine(outputLineBuffer, index, text);
						
					} else {
						// Write line content first
						outputLineBuffer.append('\n');
						numberOfIndentation = flushLineBuffer(outputBuilder, outputLineBuffer, index == (text.length() - 1), options, 
								numberOfIndentation, firstLineAlreadyIndented);
						
						outputLineBuffer.append('{');
						
						appendNewLine(outputLineBuffer, index, text);
					}
					
					numberOfIndentation = flushLineBuffer(outputBuilder, outputLineBuffer, index == (text.length() - 1), options, 
							 numberOfIndentation, firstLineAlreadyIndented);
					
					break;
					
				case '}':
					if (!outputLineBuffer.toString().trim().isEmpty()) {
						appendNewLine(outputLineBuffer, index, text);
						numberOfIndentation = flushLineBuffer(outputBuilder, outputLineBuffer, index == (text.length() - 1), options, 
								numberOfIndentation, firstLineAlreadyIndented);
					}
					
					outputLineBuffer.append('}');
					
					if (options.bracketNewLines || !followedBy(text, index + 1, "else")) {
						appendNewLine(outputLineBuffer, index, text);
					}
					
					numberOfIndentation = flushLineBuffer(outputBuilder, outputLineBuffer, index == (text.length() - 1), options, 
							numberOfIndentation, firstLineAlreadyIndented);
					
					break;
					
				case ';':					
					outputLineBuffer.append(currentChar);
					
					if (options.oneOperatorOnLine) {	
						appendNewLine(outputLineBuffer, index, text);
					}
					
					numberOfIndentation = flushLineBuffer(outputBuilder, outputLineBuffer, index == (text.length() - 1), options, 
							numberOfIndentation, firstLineAlreadyIndented);				
					break;
					
				default: 
					outputLineBuffer.append(currentChar);
					numberOfIndentation = flushLineBuffer(outputBuilder, outputLineBuffer, index == (text.length() - 1), options, 
							numberOfIndentation, firstLineAlreadyIndented);
					break;
			}
			

		}
		
		return outputBuilder.toString();
	}
	
	static private void appendNewLine(StringBuilder lineBuilder, int index, String text) {
		if ((index != text.length() - 1) && !existValidNewLine(text, index + 1)) {
			lineBuilder.append('\n');
		}
	}
	
	static private int flushLineBuffer(StringBuilder outputBuilder, StringBuilder lineBuilder, boolean forced, FormatOptions options, 
			int indentLevel, boolean firstLineIsIndented) {
		// Flush line buffer if necessary
		if (finishedWithEndLine(lineBuilder) || forced) {
			final String line = lineBuilder.toString().trim();
			
			if (line.length() != 0 || options.isIndentOnEmptyLines()) {
				if (outputBuilder.length() != 0 || !firstLineIsIndented) {
					outputBuilder.append(multiply(singleIndentStr(options), newIndentLevelBefore(line, indentLevel)));
				}
			}
			
			outputBuilder.append(line);
			
			if (finishedWithEndLine(lineBuilder)) {
				outputBuilder.append('\n');
				
				if (forced) {
					outputBuilder.append(multiply(singleIndentStr(options), newIndentLevelAfter(line, indentLevel)));
				}
			}
			
			lineBuilder.delete(0, lineBuilder.length());
			
			return newIndentLevelAfter(line, indentLevel);
		}
		
		return indentLevel;
	}
	
	static private boolean finishedWithEndLine(StringBuilder builder) {
		return builder.charAt(builder.length() - 1) == '\n';
	}
	
	static private int newIndentLevelBefore(String trimmedLine, int indentLevel) {
		if (trimmedLine.startsWith("}")) {
			indentLevel--;
		}
		
		if ((trimmedLine.startsWith("case") || trimmedLine.startsWith("default")) && trimmedLine.endsWith(":")) {
			indentLevel--;
		}
		
		return indentLevel;
	}
	
	static private int newIndentLevelAfter(String trimmedLine, int indentLevel) {
		if (trimmedLine.startsWith("}")) {
			indentLevel--;
		}
		
		if (trimmedLine.endsWith("{")) {
			indentLevel++;
		}
		
		return indentLevel;
	}

	/**
	 * Method checks there is exist new-line char separated from the 
	 * specified place only with spaces and tab chars.
	 * @param str the string for searching.
	 * @param index index of the start point.
	 * @return <code>true</code> if search was success.
	 */
	static private boolean existValidNewLine(String str, int index) {
		int newLineIndex = str.indexOf('\n', index);
		return (newLineIndex != -1 && 
				str.substring(index, newLineIndex).trim().isEmpty());
	}
	
	
	/**
	 * Method returns number of char occurrences in the string.
	 * @param text the string to inspect.
	 * @param ch the char for searching.
	 * @return Number of char entries to the string. 
	 */
	/*
	static private int countNumberOfOccurrences(String text, char ch) {
		
		int number = 0;
		
		// ++offset to start search for the next occurrence. If we won't 
		// increment indexOf will be finding the same char again and again. 
		for (int offset = 0; 
			 (offset = text.indexOf(ch, offset)) != -1; 
			 ++number, ++offset) {
		}
			
		return number;
	}*/
	
	/**
	 * Checks if char on the specified place is equal to another char.
	 * @param str the string where the check should be done.
	 * @param index the position of the char.
	 * @param ch the char to compare with.
	 * @return <code>true</code> if char is exist and is equal to the specified.
	 *  
	 */
	static private boolean checkChar(String str, int index, char ch) {
		// Check that index is valid and equality of the chars.
		return index >= 0 && index < str.length() && str.charAt(index) == ch;			
	}
	
	static private boolean followedBy(String str, int index, String pattern) {
		while (index < str.length() && Character.isWhitespace(str.charAt(index))) {
			++index;
		}
		
		return str.startsWith(pattern, index);
	}
		
	/**
	 * This method multiplies the strings.
     *
	 * @param str the string to multiply.
	 * @param number number of times to repeat the string. You can get valid
	 *        result only if number >= 0. In other cases you'll get an 
	 *        empty string.
	 *        
	 * @return Multiplied string.
	 */
	static public String multiply(String str, int number) {
		
		StringBuilder newStrBuilder = new StringBuilder();
		
		for (int i = 0; i < number; ++i) {
			newStrBuilder.append(str);
		}
		
		return newStrBuilder.toString();
	}
	
	/**
	 * Get line indent pattern from options.
	 * @param options format options
	 * @return Line indent pattern from options.
	 */
	public static String singleIndentStr(final FormatOptions options) {
		return options.insertTabs ? "\t" : 
								   multiply(" ", options.intendWidth);
	} 
	
	private static int getIndentationLevel(String lineIndentation, String singleIndentStr) {
		int offset = 0;
		int level = 0;
		
		while (lineIndentation.startsWith(singleIndentStr, offset)) {
			offset += singleIndentStr.length();
			level++;
		}

		return level;
	}
}
