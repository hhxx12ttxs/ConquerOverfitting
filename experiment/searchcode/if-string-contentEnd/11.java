/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package org.eclipse.dltk.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.dltk.compiler.CharOperation;
import org.eclipse.dltk.core.ISourceRange;
import org.eclipse.dltk.core.SourceRange;
import org.eclipse.dltk.core.builder.ISourceLineTracker;

public abstract class TextUtils {

	private TextUtils() {
		throw new AssertionError("Cannot instantiate utility class"); //$NON-NLS-1$
	}

	/**
	 * (Copied from <code>Pattern</code> class, JRE 5.) Returns a literal
	 * pattern <code>String</code> for the specified <code>String</code>.
	 * 
	 * <p>
	 * This method produces a <code>String</code> that can be used to create a
	 * <code>Pattern</code> that would match the string <code>s</code> as if it
	 * were a literal pattern.
	 * </p>
	 * Metacharacters or escape sequences in the input sequence will be given no
	 * special meaning.
	 * 
	 * @param s
	 *            The string to be literalized
	 * @return A literal string replacement
	 */
	public static String Pattern_quote(String s) {
		int slashEIndex = s.indexOf("\\E"); //$NON-NLS-1$
		if (slashEIndex == -1)
			return "\\Q" + s + "\\E"; //$NON-NLS-1$ //$NON-NLS-2$

		StringBuffer sb = new StringBuffer(s.length() * 2);
		sb.append("\\Q"); //$NON-NLS-1$
		slashEIndex = 0;
		int current = 0;
		while ((slashEIndex = s.indexOf("\\E", current)) != -1) { //$NON-NLS-1$
			sb.append(s.substring(current, slashEIndex));
			current = slashEIndex + 2;
			sb.append("\\E\\\\E\\Q"); //$NON-NLS-1$
		}
		sb.append(s.substring(current, s.length()));
		sb.append("\\E"); //$NON-NLS-1$
		return sb.toString();
	}

	/**
	 * Split this string around line boundaries (handles any line boundaries -
	 * "\n", "\r", "\r\n" so it is not equivalent to String#split("\n"))
	 * 
	 * @param content
	 * @return
	 */
	public static String[] splitLines(CharSequence content) {
		if (content == null) {
			return null;
		}
		final LineSplitter splitter = new LineSplitter(content);
		return splitter.split();
	}

	/**
	 * Split this string around line boundaries (handles any line boundaries -
	 * "\n", "\r", "\r\n" so it is not equivalent to String#split("\n"))
	 * 
	 * @param content
	 * @param limit
	 *            the maximal number of lines to return
	 * @return
	 */
	public static String[] splitLines(CharSequence content, int limit) {
		if (content == null) {
			return null;
		}
		final LineSplitter splitter = new LineSplitter(content);
		return splitter.split(limit);
	}

	/**
	 * Counts the number of lines in the specified string. Lines are counter by
	 * the separators ("\n", "\r", "\r\n")
	 * 
	 * @param content
	 * @return
	 */
	public static int countLines(CharSequence content) {
		return new LineSplitter(content).countLines();
	}

	/**
	 * @param content
	 * @param lines
	 * @return
	 */
	public static CharSequence selectHeadLines(CharSequence content, int lines) {
		return new LineSplitter(content).selectHeadLines(lines);
	}

	private static class LineSplitter {

		private final CharSequence content;
		protected final int contentEnd;
		protected int contentPos;
		protected String lastLineDelimiter = null;

		private static final String DELIMITER_WINDOWS = "\r\n"; //$NON-NLS-1$
		private static final String DELIMITER_UNIX = "\n"; //$NON-NLS-1$
		private static final String DELIMITER_MAC = "\r"; //$NON-NLS-1$

		public LineSplitter(CharSequence content) {
			this.content = content;
			this.contentEnd = content.length();
		}

		/**
		 * @param lines
		 * @return
		 */
		public CharSequence selectHeadLines(int lines) {
			contentPos = 0;
			while (lines > 0 && contentPos < contentEnd) {
				findEndOfLine();
				--lines;
			}
			return content.subSequence(0, contentPos);
		}

		public String[] split() {
			final List<String> result = new ArrayList<String>();
			contentPos = 0;
			while (contentPos < contentEnd) {
				final int begin = contentPos;
				final int end = findEndOfLine();
				result.add(content.subSequence(begin, end).toString());
			}
			return result.toArray(new String[result.size()]);
		}

		public String[] split(int lines) {
			final List<String> result = new ArrayList<String>(lines);
			contentPos = 0;
			while (lines > 0 && contentPos < contentEnd) {
				final int begin = contentPos;
				final int end = findEndOfLine();
				result.add(content.subSequence(begin, end).toString());
				--lines;
			}
			return result.toArray(new String[result.size()]);
		}

		public int countLines() {
			contentPos = 0;
			int count = 0;
			while (contentPos < contentEnd) {
				findEndOfLine();
				++count;
			}
			return count;
		}

		protected final int findEndOfLine() {
			while (contentPos < contentEnd) {
				char charAt1 = content.charAt(contentPos);
				if (charAt1 == '\r') {
					final int endLine = contentPos;
					++contentPos;
					if (contentPos < contentEnd
							&& content.charAt(contentPos) == '\n') {
						++contentPos;
						lastLineDelimiter = DELIMITER_WINDOWS;
					} else {
						lastLineDelimiter = DELIMITER_MAC;
					}
					return endLine;
				} else if (charAt1 == '\n') {
					final int endLine = contentPos;
					++contentPos;
					lastLineDelimiter = DELIMITER_UNIX;
					return endLine;
				} else {
					++contentPos;
				}
			}
			lastLineDelimiter = null;
			return contentPos;
		}

	}

	public static ISourceLineTracker createLineTracker(char[] content) {
		final LineTrackerBuilder builder = new LineTrackerBuilder(
				new CharArraySequence(content));
		return builder.buildLineTracker();
	}

	public static ISourceLineTracker createLineTracker(String content) {
		final LineTrackerBuilder builder = new LineTrackerBuilder(content);
		return builder.buildLineTracker();
	}

	private static class LineTrackerBuilder extends LineSplitter {

		public LineTrackerBuilder(CharSequence content) {
			super(content);
		}

		public ISourceLineTracker buildLineTracker() {
			final List<String> delimiters = new ArrayList<String>();
			IntList lineOffsets = new IntList(256);
			contentPos = 0;
			while (contentPos < contentEnd) {
				final int begin = contentPos;
				findEndOfLine();
				lineOffsets.add(begin);
				delimiters.add(lastLineDelimiter);
			}
			return new DefaultSourceLineTracker(contentEnd,
					lineOffsets.toArray(),
					delimiters.toArray(new String[delimiters.size()]));
		}

	}

	public static class DefaultSourceLineTracker implements ISourceLineTracker {

		private final int contentLength;
		private final int[] lineOffsets;
		private final String[] delimiters;

		public DefaultSourceLineTracker(int contentLength, int[] lineOffsets,
				String[] delimiters) {
			this.contentLength = contentLength;
			this.lineOffsets = lineOffsets;
			this.delimiters = delimiters;
		}

		public int getLength() {
			return contentLength;
		}

		public String getLineDelimiter(int line) {
			if (line < delimiters.length) {
				return delimiters[line];
			} else {
				return null;
			}
		}

		public ISourceRange getLineInformation(int line) {
			if (line < lineOffsets.length) {
				int length;
				if (line == lineOffsets.length - 1) {
					length = contentLength - lineOffsets[line];
				} else {
					length = lineOffsets[line + 1] - lineOffsets[line];
				}
				if (delimiters[line] != null) {
					length -= delimiters[line].length();
				}
				return new SourceRange(lineOffsets[line], length);
			} else {
				return NULL_RANGE;
			}
		}

		public ISourceRange getLineInformationOfOffset(int offset) {
			if (offset < contentLength) {
				return getLineInformation(findLine(offset));
			}
			return NULL_RANGE;
		}

		public int getLineLength(int line) {
			if (line < lineOffsets.length) {
				if (line == lineOffsets.length - 1) {
					return contentLength - lineOffsets[line];
				} else {
					return lineOffsets[line + 1] - lineOffsets[line];
				}
			} else {
				return 0;
			}
		}

		public int getLineNumberOfOffset(int offset) {
			if (offset < contentLength) {
				return findLine(offset);
			}
			return 0;
		}

		private int findLine(int offset) {
			// TODO use binary search
			for (int i = lineOffsets.length; --i >= 0;) {
				if (offset >= lineOffsets[i]) {
					return i;
				}
			}
			return 0;
		}

		public int getLineOffset(int line) {
			if (line < 0 || line > lineOffsets.length) {
				return WRONG_OFFSET;
			}
			if (line == lineOffsets.length) {
				if (delimiters[line - 1] != null) {
					return contentLength;
				}
				return WRONG_OFFSET;
			}
			return lineOffsets[line];
		}

		public int getNumberOfLines() {
			return lineOffsets.length;
		}

		public String[] getDelimeters() {
			return delimiters;
		}

		public int[] getLineOffsets() {
			return lineOffsets;
		}
	}

	public static String replace(String text, char c, String s) {

		int previous = 0;
		int current = text.indexOf(c, previous);

		if (current == -1)
			return text;

		StringBuffer buffer = new StringBuffer();
		while (current > -1) {
			buffer.append(text.substring(previous, current));
			buffer.append(s);
			previous = current + 1;
			current = text.indexOf(c, previous);
		}
		buffer.append(text.substring(previous));

		return buffer.toString();
	}

	public static String escapeHTML(String content) {
		content = replace(content, '&', "&amp;"); //$NON-NLS-1$
		content = replace(content, '"', "&quot;"); //$NON-NLS-1$
		content = replace(content, '<', "&lt;"); //$NON-NLS-1$
		return replace(content, '>', "&gt;"); //$NON-NLS-1$
	}

	/**
	 * <p>
	 * Joins the elements of the provided <code>Collection</code> into a single
	 * String containing the provided elements.
	 * </p>
	 * 
	 * <p>
	 * No delimiter is added before or after the list. A <code>null</code>
	 * separator is the same as an empty String ("").
	 * </p>
	 * 
	 * @param collection
	 *            the <code>Collection</code> of values to join together, may be
	 *            null
	 * @param separator
	 *            the separator character to use, null treated as ""
	 * @return the joined String, <code>null</code> if null collection input
	 */
	public static String join(Collection<?> collection, String separator) {
		// handle null, zero and one elements before building a buffer
		if (collection == null) {
			return null;
		}
		if (collection.isEmpty()) {
			return ""; //$NON-NLS-1$
		}
		final Iterator<?> iterator = collection.iterator();
		final Object first = iterator.next();
		if (!iterator.hasNext()) {
			return first != null ? first.toString() : ""; //$NON-NLS-1$
		}
		// two or more elements
		final StringBuffer buf = new StringBuffer(256);
		if (first != null) {
			buf.append(first);
		}
		while (iterator.hasNext()) {
			if (separator != null) {
				buf.append(separator);
			}
			final Object obj = iterator.next();
			if (obj != null) {
				buf.append(obj);
			}
		}
		return buf.toString();
	}

	/**
	 * <p>
	 * Joins the elements of the provided <code>Collection</code> into a single
	 * String containing the provided elements.
	 * </p>
	 * 
	 * <p>
	 * No delimiter is added before or after the list. A <code>null</code>
	 * separator is the same as an empty String ("").
	 * </p>
	 * 
	 * @param collection
	 *            the <code>Collection</code> of values to join together, may be
	 *            null
	 * @param separator
	 *            the separator character to use, null treated as ""
	 * @return the joined String, <code>null</code> if null collection input
	 */
	public static String join(Collection<?> collection, char separator) {
		// handle null, zero and one elements before building a buffer
		if (collection == null) {
			return null;
		}
		if (collection.isEmpty()) {
			return ""; //$NON-NLS-1$
		}
		final Iterator<?> iterator = collection.iterator();
		final Object first = iterator.next();
		if (!iterator.hasNext()) {
			return first != null ? first.toString() : ""; //$NON-NLS-1$
		}
		// two or more elements
		final StringBuffer buf = new StringBuffer(256);
		if (first != null) {
			buf.append(first);
		}
		while (iterator.hasNext()) {
			buf.append(separator);
			final Object obj = iterator.next();
			if (obj != null) {
				buf.append(obj);
			}
		}
		return buf.toString();
	}

	/**
	 * @param lines
	 * @param separator
	 * @return
	 */
	public static String join(String[] lines, char separator) {
		final StringBuffer sb = new StringBuffer(256);
		for (int i = 0; i < lines.length; ++i) {
			if (i != 0) {
				sb.append(separator);
			}
			sb.append(lines[i]);
		}
		return sb.toString();
	}

	/**
	 * @param lines
	 * @param separator
	 * @return
	 * @since 2.0
	 */
	public static String join(String[] lines, String separator) {
		final StringBuffer sb = new StringBuffer(256);
		for (int i = 0; i < lines.length; ++i) {
			if (i != 0) {
				sb.append(separator);
			}
			sb.append(lines[i]);
		}
		return sb.toString();
	}

	public static String[] split(String str, char separatorChar) {
		if (str == null) {
			return null;
		}
		int len = str.length();
		if (len == 0) {
			return CharOperation.NO_STRINGS;
		}
		int i = str.indexOf(separatorChar);
		if (i == -1) {
			return new String[] { str };
		}
		final List<String> list = new ArrayList<String>();
		int start = 0;
		boolean match = i != 0;
		while (i < len) {
			if (str.charAt(i) == separatorChar) {
				if (match) {
					list.add(str.substring(start, i));
					match = false;
				}
				start = ++i;
				continue;
			}
			match = true;
			i++;
		}
		if (match) {
			list.add(str.substring(start, i));
		}
		return list.toArray(new String[list.size()]);
	}

	private static int indexOfWhitespace(String str) {
		for (int i = 0; i < str.length(); ++i) {
			if (Character.isWhitespace(str.charAt(i))) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * @since 2.0
	 */
	public static String[] splitWords(String str) {
		if (str == null) {
			return null;
		}
		int len = str.length();
		if (len == 0) {
			return CharOperation.NO_STRINGS;
		}
		int i = indexOfWhitespace(str);
		if (i == -1) {
			return new String[] { str };
		}
		final List<String> list = new ArrayList<String>();
		int start = 0;
		boolean match = i != 0;
		while (i < len) {
			if (Character.isWhitespace(str.charAt(i))) {
				if (match) {
					list.add(str.substring(start, i));
					match = false;
				}
				start = ++i;
				continue;
			}
			match = true;
			i++;
		}
		if (match) {
			list.add(str.substring(start, i));
		}
		return list.toArray(new String[list.size()]);
	}

	/**
	 * Creates a new <code>ISourceRange</code> object whose offset and length
	 * are a subset of the original and do not include any leading or trailing
	 * whitespace.
	 * 
	 * <p>
	 * This method is most useful when calculating the start and end offsets
	 * required to create an <code>IProblem</code>,
	 * <code>IValidatorProblem</code>, etc
	 * </p>
	 * 
	 * @param source
	 *            source contents
	 * @param range
	 *            position in the document whitespace should be stripped from
	 * 
	 * @return source range minus leading/trailing whitespace
	 */
	public static ISourceRange trimWhitespace(String source, ISourceRange range) {
		int sOffset = range.getOffset();
		int eOffset = sOffset + range.getLength();

		String line = source.substring(sOffset, eOffset);
		char[] bytes = line.toCharArray();

		int start = 0;
		while (start < bytes.length) {
			if ((bytes[start] != '\t') && (bytes[start] != ' ')) {
				break;
			}

			start++;
		}

		sOffset += start;

		return createSourceRange(sOffset, sOffset + line.trim().length());
	}

	private static ISourceRange createSourceRange(final int start, final int end) {
		return new ISourceRange() {
			public int getLength() {
				return end - start;
			}

			public int getOffset() {
				return start;
			}
		};
	}
}

