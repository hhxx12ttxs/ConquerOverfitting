package org.autoimpl.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.autoimpl.cst.Identifier;
import org.autoimpl.cst.Position;
import org.autoimpl.parser.jay.Input;

public class Scanner implements Input {

	static private class Cursor {
		private int column = 1;
		private int row = 1;

		public void next(int n) {
			column += n;
		}

		void newLine() {
			column = 1;
			++row;
		}

		int column() {
			return column;
		}

		int row() {
			return row;
		}

		Position getPosition() {
			return new Position(row(), column());
		}
	}

	private static class LAReader {
		private Reader reader;
		private int next = -1;

		public LAReader(Reader reader) {
			this.reader = reader;
		}

		public int lookAhead() throws IOException {
			prepareLookAhead();
			return crToLf(next);
		}

		private int crToLf(int ch) {
			if (ch == '\r')
				return '\n';
			return ch;
		}

		private void prepareLookAhead() throws IOException {
			if (next == -1)
				next = reader.read();
		}

		public int read() throws IOException {
			prepareLookAhead();
			int ch = next;
			next = reader.read();
			if ((ch == '\r' && next == '\n') || (ch == '\n' && next == '\r'))
				next = reader.read();
			return crToLf(ch);
		}
	}

	private LAReader reader;
	private final int identifierToken;
	private HashMap<String, Integer> keywords;
	private Map<Character, Integer> punctuation;
	private Cursor cursor = new Cursor();
	private int currentToken = 0;
	private Object currentValue = null;
	private int tokenLen = 0;

	public Scanner(Reader reader, int identifierToken,
			Map<String, Integer> keywords, Map<Character, Integer> punctuation) {
		this.reader = new LAReader(reader);
		this.identifierToken = identifierToken;
		this.keywords = new HashMap<String, Integer>(keywords);
		this.punctuation = new HashMap<Character, Integer>(punctuation);
	}

	@Override
	public boolean advance() throws IOException {
		movePastCurrentToken();
		currentToken = getToken();
		return currentToken != 0;
	}

	private void movePastCurrentToken() {
		if (currentToken == '\n') {
			cursor.newLine();
		} else {
			cursor.next(tokenLen);
		}
	}

	private int getToken() throws IOException {
		currentValue = null;
		skipSpace();
		return extractToken();
	}

	private int extractToken() throws IOException {
		int lookAhead = reader.lookAhead();
		if (lookAhead == -1) {
			tokenLen = 0;
			return 0;
		}
		if (lookAhead == '\n')
			return extractChar();
		Integer p = getPunctuation(lookAhead);
		if (p != null) {
			extractChar();
			return p;
		}
		return getKeywordOrIdentifierToken(extractIdentifier());
	}

	private Integer getPunctuation(int lookAhead) {
		return punctuation.get((char) lookAhead);
	}

	private int extractChar() throws IOException {
		tokenLen = 1;
		currentValue = cursor.getPosition();
		return reader.read();
	}

	private int getKeywordOrIdentifierToken(String id) {
		tokenLen = id.length();
		Integer keywordToken = keywords.get(id);
		if (keywordToken != null) {
			currentValue = cursor.getPosition();
			return keywordToken;
		}
		currentValue = new Identifier(id, cursor.getPosition());
		return identifierToken;
	}

	private void skipSpace() throws IOException {
		while (reader.lookAhead() == ' ') {
			reader.read();
			cursor.next(1);
		}
	}

	private String extractIdentifier() throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append((char) reader.read());
		while (isIdentifierChar(reader.lookAhead())) {
			sb.append((char) reader.read());
		}
		return sb.toString();
	}

	private boolean isIdentifierChar(int lookAhead) throws IOException {
		return lookAhead != -1 && lookAhead != ' ' && lookAhead != '\n'
				&& !punctuation.containsKey((char) lookAhead);
	}

	@Override
	public int token() {
		return currentToken;
	}

	@Override
	public Object value() {
		return currentValue;
	}

	@Override
	public int column() {
		return cursor.column();
	}

	@Override
	public int row() {
		return cursor.row();
	}

}

