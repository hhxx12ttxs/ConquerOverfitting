package org.fierry.roots.parser;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

import org.fierry.build.utils.Lines;


public class Tokenizer {

	private int line;
	private int step;
	private int spaces;
	
	private Path path;
	private Collection<Token> tokens;
	
	public static Collection<Token> getTokens(Path path, String file) {
		Tokenizer tokenizer = new Tokenizer(path);
		
		for(String line : file.split(Lines.separator)) {
			tokenizer.parse(line);
		}
		tokenizer.eof();
		
		return tokenizer.tokens;
	}

	private Tokenizer(Path path) {
		this.line   = 0;
		this.step   = 0;
		this.spaces = 0;
		
		this.path   = path;
		this.tokens = new ArrayList<Token>();
	}

	public void parse(String s) {
		line++;
		
		// Reporting error lines, ignoring empty lines.
		if(s.matches(".*\t.*")) { error("Illegal character (\\t) found."); }
		if(s.matches(" *| *#.*")) { return; }
		
		// Tokenizing indentation.
		Integer chars  = getSpaces(s);
		Integer indent = getIndent(chars);
		
		for(int i = 0; i < indent; i++) {
			tokens.add(getIndentToken(chars));
		}
		spaces = chars;
		
		// Stripping leading spaces.
		s = s.substring(spaces);
		
		// Action expression found.
		if(s.matches("\\/[^\\/].*")) {
			
			// Action expression wrapped into inline if.
			if(s.matches("\\/[^\\/].* \\/if.+")) {
				int idx = s.indexOf("/if");
				
				tokens.add(Token.ACTION(s.substring(idx)));
				tokens.add(Token.BEGIN);
				tokens.add(Token.ACTION(s.substring(0, idx - 1)));
				tokens.add(Token.END);
			} else {
				tokens.add(Token.ACTION(s));
			}
			
		// Value expression.
		} else {
			tokens.add(Token.VALUE(s));
		}
	}
	
	public void eof() {
		Integer indent = getIndent(0);
		
		for(int i = 0; i < indent; i++) {
			tokens.add(Token.END);
		}
	}

	private int getSpaces(String s) {
		int chars = 0;
		while(s.charAt(chars) == ' ') chars++;
		
		// Setting indentation step if not set.
		if(step == 0 && chars > 0) { step = chars; }
		return chars;
	}

	private int getIndent(int chars) {
		int difference = chars - spaces;
		
		if(difference != 0) {
			if(difference % step != 0) { error("Wrong indentation encountered, indent: /1, step: /2", chars, step); }
			return Math.abs(difference / step);
		}
		return 0;
	}

	private Token getIndentToken(int chars) {
		return (chars - spaces) > 0 ? Token.BEGIN : Token.END;
	}

	private void error(String s, Object... args ) {
		for(int i = 0; i < args.length; i++) {
			s = s.replace("/" + i, args[i].toString());
		}
		throw new IllegalStateException(path + "(" + line + "): " + s);
	}
}

