/*
 * $Id: TrueTypeFont.java,v 1.6 2009/03/15 20:47:39 tomoke Exp $
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */
package com.sun.pdfview.font.ttf;


import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import com.nu.art.software.pdf.core.PDF_BufferWrapper;


/**
 *
 * @author jkaplan
 * @author Ferenc Hechler (ferenc@hechler.de)
 * @author Joerg Jahnke (joergjahnke@users.sourceforge.net)
 */
public class TrueTypeFont {

	private int type;

	private SortedMap<String, Object> tables;

	/** Creates a new instance of TrueTypeParser */
	public TrueTypeFont(int type) {
		this.type = type;

		tables = Collections.synchronizedSortedMap(new TreeMap<String, Object>());
	}

	/**
	 * Get the type of this font
	 */
	public int getType() {
		return type;
	}

	/**
	 * Add a table to the font
	 *
	 * @param tagString the name of this table, as a 4 character string (i.e. cmap or head)
	 * @param data the data for this table, as a byte buffer
	 */
	public void addTable(String tagString, PDF_BufferWrapper data) {
		tables.put(tagString, data);
	}

	/**
	 * Add a table to the font
	 *
	 * @param tagString the name of this table, as a 4 character string (i.e. cmap or head)
	 * @param table the table
	 */
	public void addTable(String tagString, TrueTypeTable table) {
		tables.put(tagString, table);
	}

	/**
	 * Get a table by name. This command causes the table in question to be parsed, if it has not already been parsed.
	 *
	 * @param tagString the name of this table, as a 4 character string (i.e. cmap or head)
	 */
	public synchronized TrueTypeTable getTable(String tagString) {
		Object tableObj = tables.get(tagString);

		if (tableObj instanceof TrueTypeTable)
			return (TrueTypeTable) tableObj;

		// the table has not yet been parsed. Parse it, and add the
		// parsed version to the map of tables.
		PDF_BufferWrapper data = (PDF_BufferWrapper) tableObj;
		TrueTypeTable table = TrueTypeTable.createTable(this, tagString, data);
		addTable(tagString, table);
		return table;
	}

	/**
	 * Remove a table by name
	 *
	 * @param tagString the name of this table, as a 4 character string (i.e. cmap or head)
	 */
	public void removeTable(String tagString) {
		tables.remove(tagString);
	}

	/**
	 * Get the number of tables
	 */
	public short getNumTables() {
		return (short) tables.size();
	}

	/**
	 * Get the search range
	 */
	public short getSearchRange() {
		double pow2 = Math.floor(Math.log(getNumTables()) / Math.log(2));
		double maxPower = Math.pow(2, pow2);

		return (short) (16 * maxPower);
	}

	/**
	 * Get the entry selector
	 */
	public short getEntrySelector() {
		double pow2 = Math.floor(Math.log(getNumTables()) / Math.log(2));
		double maxPower = Math.pow(2, pow2);

		return (short) (Math.log(maxPower) / Math.log(2));
	}

	/**
	 * Get the range shift
	 */
	public short getRangeShift() {
		double pow2 = Math.floor(Math.log(getNumTables()) / Math.log(2));
		double maxPower = Math.pow(2, pow2);

		return (short) ((maxPower * 16) - getSearchRange());
	}
}

