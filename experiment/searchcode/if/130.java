package org.fusionide.builder.parsers.cfml.tags;

import java.util.Vector;

import org.fusionide.builder.parsers.IParserHandler;
import org.fusionide.builder.parsers.ParserException;
import org.fusionide.builder.parsers.cfml.CFMLTag;
import org.fusionide.builder.parsers.cfml.Variable;

public class If extends CFMLTag {

	public If(String attributes, int startOffset) {
		super(attributes, startOffset);
	}

	@Override
	public boolean requiresClosingTag() {
		return true;
	}

	@Override
	public boolean validate(IParserHandler parserHandler) {
		if (super.validate(parserHandler)) {
			if (attributes.trim().equalsIgnoreCase("")) {
				if (parserHandler != null)
					parserHandler.addMarker(new ParserException("<cfif> Need a compairison", ParserException.SEVERITY_ERROR, startLine));
				return false;
			}
		}
		return true;
	}

	@Override
	public Vector<Variable> getVariables(int offset) {
		return getChildVariables(offset);
	}

	@Override
	public Vector<Variable> getChildVariables(int offset) {
		return super.getChildVariables(offset);
	}
}

