/*
 * Copyright (c) 2006, 2007 Andy Armstrong, Kelsey Grant and other contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * The names of contributors may not
 *       be used to endorse or promote products derived from this software without
 *       specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.andya.confluence.plugins.metadata.model;

import com.atlassian.renderer.v2.macro.MacroException;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * A data structure representing a list of metadata name/value pairs.
 */
public class MetadataList {
	private final List<String> names = new ArrayList<String>();
	private final List<String> values = new ArrayList<String>();

	public MetadataList() {
	}

	public List<String> getNames() {
		return names;
	}

	public List<String> getValues() {
		return values;
	}

	public void addNameAndValue(String name, String value) {
		names.add(name);
		values.add(value);
	}

	public void addRow(String row) throws MacroException {
		row = row.trim();
		if (row.length() == 0) return;    // ignore blank rows
		int startIndex = 0;
		int endIndex = row.length();
		if (row.charAt(0) == '|') {
			if (endIndex > 1 && row.charAt(1) == '|')
				startIndex = 2;
			else
				startIndex = 1;
		}
		if (startIndex == endIndex)
			throw new MacroException("Metadata list entry missing name: \"" + row + "\"");
		while (endIndex > startIndex &&
				(row.charAt(endIndex - 1) == '|'
				 || row.charAt(endIndex - 1) == '\\'
			     || row.charAt(endIndex - 1) == ' '))
		{
			// ignore double antislash
			if(endIndex >= 2 && row.charAt(endIndex - 1) == '\\' && row.charAt(endIndex - 2) == '\\')
				endIndex--;
			else if(row.charAt(endIndex - 1) == '\\')
				break;
			endIndex--;
		}
		int nameEndIndex = row.indexOf('|', startIndex);
		String name = row.substring(startIndex, nameEndIndex);
		// carriage returns are not allowed in names
		name = name.replace("\\\\", "").trim();
		if (name.length() == 0)
			throw new MacroException("Metadata list entry missing name: \"" + row + "\"");
		startIndex = nameEndIndex + 1;
		String value;
		if (startIndex >= endIndex)
			value = "";
		else
			value = row.substring(startIndex, endIndex).trim();
		addNameAndValue(name, value);
	}

	/** Parse a metadata list body returning a data structure describing the list. */
	public static MetadataList parseMetadataList(String body)  throws MacroException {
		MetadataList list = new MetadataList();
		StringTokenizer tokenizer = new StringTokenizer(body, "\n");
		if (tokenizer.countTokens() > 0)	{
		    String currentRow = tokenizer.nextToken();
		    while (tokenizer.hasMoreTokens()) {
		        String s = tokenizer.nextToken();
		        if (s.charAt(0) == '|') {
		            list.addRow(currentRow);
		            currentRow = s;
		        } else {
		            currentRow += "\n" + s;
		        }
		    }
		    list.addRow(currentRow);
		}
		return list;
	}
}

