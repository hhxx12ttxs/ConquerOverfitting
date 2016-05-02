/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

/* 
 * 
 * Created on 4-apr-2003
 * 
 */

package com.inwiss.springcrud.support.excelinput;

import com.inwiss.springcrud.util.Consts;

/**
 * Meta data for the Excel step.
 */
public class ExcelImportMeta {


	public static final String[] RequiredFilesCode = new String[] { "N", "Y" };
	private static final String NO = "N";
	private static final String YES = "Y";
	public final static int TYPE_TRIM_NONE = 0;
	public final static int TYPE_TRIM_LEFT = 1;
	public final static int TYPE_TRIM_RIGHT = 2;
	public final static int TYPE_TRIM_BOTH = 3;

	public final static String type_trim_code[] = { "none", "left", "right","both" };

	public static final String STRING_SEPARATOR = " \t --> ";

	/**
	 * The filenames to load or directory in case a filemask was set.
	 */
	private String fileName;

	/**
	 * The fieldname that holds the name of the file
	 */
	private String fileField;

	/**
	 * The names of the sheets to load. Null means: all sheets...
	 */
	private String sheetName[];

	/**
	 * The row-nr where we start processing.
	 */
	private int startRow = 0;
	
	private int endRow;
	
	


	/**
	 * 行结束的范围，需要做变量替换.
	 */
	private String maskEndRow;

	/**
	 * The column-nr where we start processing.
	 */
	private int startColumn;

	/**
	 * The fieldname that holds the name of the sheet
	 */
	private String sheetField;

	/**
	 * The cell-range starts with a header-row
	 */
	private boolean startsWithHeader;

	/**
	 * Stop reading when you hit an empty row.
	 */
	private boolean stopOnEmpty;

	/**
	 * Avoid empty rows in the result.
	 */
	private boolean ignoreEmptyRows;

	/**
	 * The fieldname containing the row number. An empty (null) value means that
	 * no row number is included in the output. This is the rownumber of all
	 * written rows (not the row in the sheet).
	 */
	private String rowNumberField;

	/**
	 * The fieldname containing the sheet row number. An empty (null) value
	 * means that no sheet row number is included in the output. Sheet row
	 * number is the row number in the sheet.
	 */
	private String sheetRowNumberField;

	/**
	 * The maximum number of rows that this step writes to the next step.
	 */
	private long rowLimit;

	/**
	 * The fields to read in the range. Note: the number of columns in the range
	 * has to match field.length
	 */
	private ExcelInputField field[];


	/** Ignore error : turn into warnings */
	private boolean errorIgnored;

	/** If error line are skipped, you can replay without introducing doubles. */
	private boolean errorLineSkipped;

	/**
	 * The encoding to use for reading: null or empty string means system
	 * default encoding
	 */
	private String encoding;

	/** The add filenames to result filenames flag */
	private boolean isaddresult;

	private String spreadSheetType;

	public ExcelImportMeta() {
		super(); // allocate BaseStepMeta
	}

	
		/**
	 * @return Returns the fieldLength.
	 */
	public ExcelInputField[] getField() {
		return field;
	}

	/**
	 * @param fields
	 *            The excel input fields to set.
	 */
	public void setField(ExcelInputField[] fields) {
		this.field = fields;
	}

	/**
	 * @return Returns the fileField.
	 */
	public String getFileField() {
		return fileField;
	}

	/**
	 * @param fileField
	 *            The fileField to set.
	 */
	public void setFileField(String fileField) {
		this.fileField = fileField;
	}




	/**
	 * @return Returns the fileName.
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName
	 *            The fileName to set.
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return Returns the ignoreEmptyRows.
	 */
	public boolean ignoreEmptyRows() {
		return ignoreEmptyRows;
	}

	/**
	 * @param ignoreEmptyRows
	 *            The ignoreEmptyRows to set.
	 */
	public void setIgnoreEmptyRows(boolean ignoreEmptyRows) {
		this.ignoreEmptyRows = ignoreEmptyRows;
	}

	/**
	 * @return Returns the rowLimit.
	 */
	public long getRowLimit() {
		return rowLimit;
	}

	/**
	 * @param rowLimit
	 *            The rowLimit to set.
	 */
	public void setRowLimit(long rowLimit) {
		this.rowLimit = rowLimit;
	}

	/**
	 * @return Returns the rowNumberField.
	 */
	public String getRowNumberField() {
		return rowNumberField;
	}

	/**
	 * @param rowNumberField
	 *            The rowNumberField to set.
	 */
	public void setRowNumberField(String rowNumberField) {
		this.rowNumberField = rowNumberField;
	}

	/**
	 * @return Returns the sheetRowNumberField.
	 */
	public String getSheetRowNumberField() {
		return sheetRowNumberField;
	}

	/**
	 * @param rowNumberField
	 *            The rowNumberField to set.
	 */
	public void setSheetRowNumberField(String rowNumberField) {
		this.sheetRowNumberField = rowNumberField;
	}

	/**
	 * @return Returns the sheetField.
	 */
	public String getSheetField() {
		return sheetField;
	}

	/**
	 * @param sheetField
	 *            The sheetField to set.
	 */
	public void setSheetField(String sheetField) {
		this.sheetField = sheetField;
	}

	/**
	 * @return Returns the sheetName.
	 */
	public String[] getSheetName() {
		return sheetName;
	}

	/**
	 * @param sheetName
	 *            The sheetName to set.
	 */
	public void setSheetName(String[] sheetName) {
		this.sheetName = sheetName;
	}

	/**
	 * @return Returns the startColumn.
	 */
	public int getStartColumn() {
		return startColumn;
	}

	/**
	 * @param startColumn
	 *            The startColumn to set.
	 */
	public void setStartColumn(int startColumn) {
		this.startColumn = startColumn;
	}

	/**
	 * @return Returns the startRow.
	 */
	public int getStartRow() {
		return startRow;
	}

	/**
	 * @param startRow
	 *            The startRow to set.
	 */
	public void setStartRow(int startRow) {
		this.startRow = startRow;
	}

	/**
	 * @return Returns the startsWithHeader.
	 */
	public boolean startsWithHeader() {
		return startsWithHeader;
	}

	/**
	 * @param startsWithHeader
	 *            The startsWithHeader to set.
	 */
	public void setStartsWithHeader(boolean startsWithHeader) {
		this.startsWithHeader = startsWithHeader;
	}

	/**
	 * @return Returns the stopOnEmpty.
	 */
	public boolean stopOnEmpty() {
		return stopOnEmpty;
	}

	/**
	 * @param stopOnEmpty
	 *            The stopOnEmpty to set.
	 */
	public void setStopOnEmpty(boolean stopOnEmpty) {
		this.stopOnEmpty = stopOnEmpty;
	}



	public void setDefault() {
		startsWithHeader = true;
		ignoreEmptyRows = true;
		rowNumberField = "";
		sheetRowNumberField = "";
		isaddresult = true;
		int nrfiles = 0;
		int nrfields = 0;
		int nrsheets = 0;

		//allocate(nrfiles, nrsheets, nrfields);


		/**
		for (int i = 0; i < nrfields; i++) {
			field[i] = new ExcelInputField();
			field[i].setName("field" + i);
			field[i].setType(Consts.TYPE_NUMBER);
			field[i].setLength(9);
			field[i].setPrecision(2);
			field[i].setTrimType(TYPE_TRIM_NONE);
			field[i].setRepeated(false);
		}*/

		rowLimit = 0L;
		errorIgnored = false;
		errorLineSkipped = false;
		spreadSheetType = Consts.JXL; // default.
	}

	public final static int getTrimTypeByCode(String tt) {
		if (tt != null) {
			for (int i = 0; i < type_trim_code.length; i++) {
				if (type_trim_code[i].equalsIgnoreCase(tt))
					return i;
			}
		}
		return 0;
	}

	public final static String getTrimTypeCode(int i) {
		if (i < 0 || i >= type_trim_code.length)
			return type_trim_code[0];
		return type_trim_code[i];
	}

	

	public boolean isErrorIgnored() {
		return errorIgnored;
	}

	public void setErrorIgnored(boolean errorIgnored) {
		this.errorIgnored = errorIgnored;
	}

	

	public boolean isErrorLineSkipped() {
		return errorLineSkipped;
	}

	public void setErrorLineSkipped(boolean errorLineSkipped) {
		this.errorLineSkipped = errorLineSkipped;
	}

	


	public String[] getUsedLibraries() {
		return new String[] { "jxl.jar", };
	}

	/**
	 * @return the encoding
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * @param encoding
	 *            the encoding to set
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * @param isaddresult
	 *            The isaddresult to set.
	 */
	public void setAddResultFile(boolean isaddresult) {
		this.isaddresult = isaddresult;
	}

	/**
	 * @return Returns isaddresult.
	 */
	public boolean isAddResultFile() {
		return isaddresult;
	}

	public String getSpreadSheetType() {
		return Consts.JXL;
	}

	public void setSpreadSheetType(String spreadSheetType) {
		this.spreadSheetType = spreadSheetType;
	}
	
	/**
	 * @return the endRow
	 */
	public int getEndRow() {
		return endRow;
	}


	/**
	 * @param endRow the endRow to set
	 */
	public void setEndRow(int endRow) {
		this.endRow = endRow;
	}


	/**
	 * @return the maskEndRow
	 */
	public String getMaskEndRow() {
		return maskEndRow;
	}


	/**
	 * @param maskEndRow the maskEndRow to set
	 */
	public void setMaskEndRow(String maskEndRow) {
		this.maskEndRow = maskEndRow;
	}
}
