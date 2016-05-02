package org.pentaho.di.ui.core.widget;

/**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

import java.io.IOException;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class SqlLineStyle implements LineStyleListener {
	JavaScanner scanner = new JavaScanner();
	int[] tokenColors;
	Color[] colors;
	Vector<int[]> blockComments = new Vector<int[]>();

	public static final int EOF = -1;
	public static final int EOL = 10;

	public static final int WORD = 0;
	public static final int WHITE = 1;
	public static final int KEY = 2;
	public static final int COMMENT = 3; // single line comment: //
	public static final int STRING = 5;
	public static final int OTHER = 6;
	public static final int NUMBER = 7;
	public static final int FUNCTIONS = 8;

	public static final int MAXIMUM_TOKEN = 9;

	public SqlLineStyle() {
		this.initializeColors();
		this.scanner = new JavaScanner();
	}

	public SqlLineStyle(final String[] strArrSQLFunctions) {
		this.initializeColors();
		this.scanner = new JavaScanner();
		this.scanner.setSQLKeywords(strArrSQLFunctions);
		this.scanner.initializeSQLFunctions();
	}

	Color getColor(final int type) {
		if (type < 0 || type >= this.tokenColors.length) {
			return null;
		}
		return this.colors[this.tokenColors[type]];
	}

	boolean inBlockComment(final int start, final int end) {
		for (int i = 0; i < this.blockComments.size(); i++) {
			final int[] offsets = this.blockComments.elementAt(i);
			// start of comment in the line
			if ((offsets[0] >= start) && (offsets[0] <= end)) {
				return true;
			}
			// end of comment in the line
			if ((offsets[1] >= start) && (offsets[1] <= end)) {
				return true;
			}
			if ((offsets[0] <= start) && (offsets[1] >= end)) {
				return true;
			}
		}
		return false;
	}

	void initializeColors() {
		final Display display = Display.getDefault();
		this.colors = new Color[] { new Color(display, new RGB(0, 0, 0)), // black
				new Color(display, new RGB(255, 0, 0)), // red
				new Color(display, new RGB(63, 127, 95)), // green
				new Color(display, new RGB(0, 0, 255)), // blue
				new Color(display, new RGB(255, 0, 255)) // SQL Functions / Rose

		};
		this.tokenColors = new int[MAXIMUM_TOKEN];
		this.tokenColors[WORD] = 0;
		this.tokenColors[WHITE] = 0;
		this.tokenColors[KEY] = 3;
		this.tokenColors[COMMENT] = 2;
		this.tokenColors[STRING] = 1;
		this.tokenColors[OTHER] = 0;
		this.tokenColors[NUMBER] = 0;
		this.tokenColors[FUNCTIONS] = 4;
	}

	void disposeColors() {
		for (int i = 0; i < this.colors.length; i++) {
			this.colors[i].dispose();
		}
	}

	/**
	 * Event.detail line start offset (input) Event.text line text (input)
	 * LineStyleEvent.styles Enumeration of StyleRanges, need to be in order.
	 * (output) LineStyleEvent.background line background color (output)
	 */
	@Override
	public void lineGetStyle(final LineStyleEvent event) {
		final Vector<StyleRange> styles = new Vector<StyleRange>();
		int token;
		StyleRange lastStyle;

		if (this.inBlockComment(event.lineOffset, event.lineOffset
				+ event.lineText.length())) {
			styles.addElement(new StyleRange(event.lineOffset, event.lineText
					.length() + 4, this.colors[1], null));
			event.styles = new StyleRange[styles.size()];
			styles.copyInto(event.styles);
			return;
		}
		this.scanner.setRange(event.lineText);
		final String xs = ((StyledText) event.widget).getText();
		if (xs != null) {
			this.parseBlockComments(xs);
		}
		token = this.scanner.nextToken();
		while (token != EOF) {
			if (token == OTHER) {
				// do nothing
			} else if ((token == WHITE) && (!styles.isEmpty())) {
				final int start = this.scanner.getStartOffset()
						+ event.lineOffset;
				lastStyle = styles.lastElement();
				if (lastStyle.fontStyle != SWT.NORMAL) {
					if (lastStyle.start + lastStyle.length == start) {
						// have the white space take on the style before it to
						// minimize font style
						// changes
						lastStyle.length += this.scanner.getLength();
					}
				}
			} else {
				final Color color = this.getColor(token);
				if (color != this.colors[0]) { // hardcoded default foreground
												// color, black
					final StyleRange style = new StyleRange(
							this.scanner.getStartOffset() + event.lineOffset,
							this.scanner.getLength(), color, null);
					if (token == KEY) {
						// style.fontStyle = SWT.BOLD;
					}
					if (styles.isEmpty()) {
						styles.addElement(style);
					} else {
						lastStyle = styles.lastElement();
						if (lastStyle.similarTo(style)
								&& (lastStyle.start + lastStyle.length == style.start)) {
							lastStyle.length += style.length;
						} else {
							styles.addElement(style);
						}
					}
				}
			}
			token = this.scanner.nextToken();
		}
		event.styles = new StyleRange[styles.size()];
		styles.copyInto(event.styles);
	}

	public void parseBlockComments(final String text) {
		this.blockComments = new Vector<int[]>();
		final StringReader buffer = new StringReader(text);
		int ch;
		boolean blkComment = false;
		int cnt = 0;
		int[] offsets = new int[2];
		boolean done = false;

		try {
			while (!done) {
				switch (ch = buffer.read()) {
				case -1: {
					if (blkComment) {
						offsets[1] = cnt;
						this.blockComments.addElement(offsets);
					}
					done = true;
					break;
				}
				case '/': {
					ch = buffer.read();
					if ((ch == '*') && (!blkComment)) {
						offsets = new int[2];
						offsets[0] = cnt;
						blkComment = true;
						cnt++;
					} else {
						cnt++;
					}
					cnt++;
					break;
				}
				case '*': {
					if (blkComment) {
						ch = buffer.read();
						cnt++;
						if (ch == '/') {
							blkComment = false;
							offsets[1] = cnt;
							this.blockComments.addElement(offsets);
						}
					}
					cnt++;
					break;
				}
				default: {
					cnt++;
					break;
				}
				}
			}
		} catch (final IOException e) {
			// ignore errors
		}
	}

	/**
	 * A simple fuzzy scanner for Java
	 */
	public class JavaScanner {
		protected Map<String, Integer> fgKeys = null;
		protected Map<?, ?> fgFunctions = null;
		protected Map<String, Integer> kfKeys = null;
		protected Map<?, ?> kfFunctions = null;

		protected StringBuffer fBuffer = new StringBuffer();
		protected String fDoc;
		protected int fPos;
		protected int fEnd;
		protected int fStartToken;
		protected boolean fEofSeen = false;

		private String[] kfKeywords = { "getdate", "case", "convert", "left", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"right", "isnumeric", "isdate", "isnumber", "number", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"finally", "cast", "var", "fetch_status", "isnull", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"charindex", "difference", "len", "nchar", "quotename", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"replicate", "reverse", "str", "stuff", "unicode", "ascii", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"char", "to_char", "to_date", "to_number", "nvl", "sysdate", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"corr", "count", "grouping", "max", "min", "stdev", "sum", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"concat", "length", "locate", "ltrim", "posstr", "repeat", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"replace", "rtrim", "soundex", "space", "substr", "substring", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"trunc", "nextval", "currval", "getclobval", "char_length", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"compare", "patindex", "sortkey", "uscalar", "current_date", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"current_time", "current_timestamp", "current_user", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"session_user", "system_user", "curdate", "curtime", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"database", "now", "sysdate", "today", "user", "version", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"coalesce", "nullif", "octet_length", "datalength", "decode", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"greatest", "ifnull", "least", "||", "char_length", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"character_length", "collate", "concatenate", "like", "lower", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"position", "translate", "upper", "char_octet_length", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"character_maximum_length", "character_octet_length", "ilike", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"initcap", "instr", "lcase", "lpad", "patindex", "rpad", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"ucase", "bit_length", "&", "|", "^", "%", "+", "-", "*", "/", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
				"(", ")", "abs", "asin", "atan", "ceiling", "cos", "cot", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				"exp", "floor", "ln", "log", "log10", "mod", "pi", "power", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				"rand", "round", "sign", "sin", "sqrt", "tan", "trunc", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"extract", "interval", "overlaps", "adddate", "age", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"date_add", "dateformat", "date_part", "date_sub", "datediff", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"dateadd", "datename", "datepart", "day", "dayname", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"dayofmonth", "dayofweek", "dayofyear", "hour", "last_day", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"minute", "month", "month_between", "monthname", "next_day", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"second", "sub_date", "week", "year", "dbo", "log", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"objectproperty" }; //$NON-NLS-1$

		private final String[] fgKeywords = { "create", "procedure", "as", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"set", "nocount", "on", "declare", "varchar", "print", "table", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"int", "tintytext", "select", "from", "where", "and", "or", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"insert", "into", "cursor", "read_only", "for", "open", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"fetch", "next", "end", "deallocate", "table", "drop", "exec", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"begin", "close", "update", "delete", "truncate", "inner", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"outer", "join", "union", "all", "float", "when", "nolock", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"with", "false", "datetime", "dare", "time", "hour", "array", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"minute", "second", "millisecond", "view", "function", "catch", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"const", "continue", "compute", "browse", "option", "date", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"default", "do", "raw", "auto", "explicit", "xmldata", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"elements", "binary", "base64", "read", "outfile", "asc", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"desc", "else", "eval", "escape", "having", "limit", "offset", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"of", "intersect", "except", "using", "variance", "specific", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"language", "body", "returns", "specific", "deterministic", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"not", "external", "action", "reads", "static", "inherit", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"called", "order", "group", "by", "natural", "full", "exists", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"between", "some", "any", "unique", "match", "value", "limite", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"minus", "references", "grant", "on", "top", "index", "bigint", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"text", "char", "use", "move", "exec", "init", "name", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"noskip", "skip", "noformat", "format", "stats", "disk", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"from", "to", "rownum", "alter", "add", "remove", "move", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$//$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"alter", "add", "remove", "lineno", "modify", "if", "else", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"in", "is", "new", "Number", "null", "string", "switch", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"this", "then", "throw", "true", "false", "try", "return", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"with", "while", "start", "connect", "optimize", "first", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"only", "rows", "sequence", "blob", "clob", "image", "binary", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"column", "decimal", "distinct", "primary", "key", "timestamp", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"varbinary", "nvarchar", "nchar", "longnvarchar", "nclob", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"numeric", "constraint", "dbcc", "backup", "bit", "clustered", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"pad_index", "off", "statistics_norecompute", "ignore_dup_key", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"allow_row_locks", "allow_page_locks", "textimage_on", "double" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		public JavaScanner() {
			this.initialize();
			this.initializeSQLFunctions();
		}

		/**
		 * Returns the ending location of the current token in the document.
		 */
		public final int getLength() {
			return this.fPos - this.fStartToken;
		}

		/**
		 * Initialize the lookup table.
		 */
		void initialize() {
			this.fgKeys = new Hashtable<String, Integer>();
			final Integer k = new Integer(KEY);
			for (int i = 0; i < this.fgKeywords.length; i++) {
				this.fgKeys.put(this.fgKeywords[i], k);
			}
		}

		public void setSQLKeywords(final String[] kfKeywords) {
			this.kfKeywords = kfKeywords;
		}

		void initializeSQLFunctions() {
			this.kfKeys = new Hashtable<String, Integer>();
			final Integer k = new Integer(FUNCTIONS);
			for (int i = 0; i < this.kfKeywords.length; i++) {
				this.kfKeys.put(this.kfKeywords[i], k);
			}
		}

		/**
		 * Returns the starting location of the current token in the document.
		 */
		public final int getStartOffset() {
			return this.fStartToken;
		}

		/**
		 * Returns the next lexical token in the document.
		 */
		public int nextToken() {
			int c;
			this.fStartToken = this.fPos;
			while (true) {
				switch (c = this.read()) {
				case EOF:
					return EOF;
				case '/': // comment
					c = this.read();
					if (c == '/') {
						while (true) {
							c = this.read();
							if ((c == EOF) || (c == EOL)) {
								this.unread(c);
								return COMMENT;
							}
						}
					} else {
						this.unread(c);
					}
					return OTHER;
				case '-': // comment
					c = this.read();
					if (c == '-') {
						while (true) {
							c = this.read();
							if ((c == EOF) || (c == EOL)) {
								this.unread(c);
								return COMMENT;
							}
						}
					} else {
						this.unread(c);
					}
					return OTHER;
				case '\'': // char const
					for (;;) {
						c = this.read();
						switch (c) {
						case '\'':
							return STRING;
						case EOF:
							this.unread(c);
							return STRING;
						case '\\':
							c = this.read();
							break;
						}
					}

				case '"': // string
					for (;;) {
						c = this.read();
						switch (c) {
						case '"':
							return STRING;
						case EOF:
							this.unread(c);
							return STRING;
						case '\\':
							c = this.read();
							break;
						}
					}

				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					do {
						c = this.read();
					} while (Character.isDigit((char) c));
					this.unread(c);
					return NUMBER;
				default:
					if (Character.isWhitespace((char) c)) {
						do {
							c = this.read();
						} while (Character.isWhitespace((char) c));
						this.unread(c);
						return WHITE;
					}
					if (Character.isJavaIdentifierStart((char) c)) {
						this.fBuffer.setLength(0);
						do {
							this.fBuffer.append((char) c);
							c = this.read();
						} while (Character.isJavaIdentifierPart((char) c));
						this.unread(c);
						Integer i = this.fgKeys.get(this.fBuffer.toString());
						if (i != null) {
							return i.intValue();
						}
						i = this.kfKeys.get(this.fBuffer.toString());
						if (i != null) {
							return i.intValue();
						}
						return WORD;
					}
					return OTHER;
				}
			}
		}

		/**
		 * Returns next character.
		 */
		protected int read() {
			if (this.fPos <= this.fEnd) {
				return this.fDoc.charAt(this.fPos++);
			}
			return EOF;
		}

		public void setRange(final String text) {
			this.fDoc = text.toLowerCase();
			this.fPos = 0;
			this.fEnd = this.fDoc.length() - 1;
		}

		protected void unread(final int c) {
			if (c != EOF) {
				this.fPos--;
			}
		}
	}
}
