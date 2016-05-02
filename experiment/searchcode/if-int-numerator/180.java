<<<<<<< HEAD
/*
 * #%L
 * Fork of Apache Jakarta POI.
 * %%
 * Copyright (C) 2008 - 2013 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
=======
>>>>>>> 76aa07461566a5976980e6696204781271955163
/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

<<<<<<< HEAD
/*
 * HSSFSheet.java
 *
 * Created on September 30, 2001, 3:40 PM
 */
package loci.poi.hssf.usermodel;

import loci.poi.ddf.EscherRecord;
import loci.poi.hssf.model.Sheet;
import loci.poi.hssf.model.Workbook;
import loci.poi.hssf.record.*;
import loci.poi.hssf.util.Region;
import loci.poi.hssf.util.PaneInformation;
import loci.poi.util.POILogFactory;
import loci.poi.util.POILogger;
=======
package org.apache.poi.hssf.usermodel;
>>>>>>> 76aa07461566a5976980e6696204781271955163

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
<<<<<<< HEAD
import java.text.AttributedString;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.awt.font.TextLayout;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;

import java.awt.geom.AffineTransform;
=======

import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.model.InternalSheet;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.record.aggregates.DataValidityTable;
import org.apache.poi.hssf.record.aggregates.FormulaRecordAggregate;
import org.apache.poi.hssf.record.aggregates.WorksheetProtectionBlock;
import org.apache.poi.ss.formula.FormulaShifter;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.Area3DPtg;
import org.apache.poi.hssf.util.PaneInformation;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellRange;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.SSCellRange;
import org.apache.poi.ss.util.SheetUtil;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
>>>>>>> 76aa07461566a5976980e6696204781271955163

/**
 * High level representation of a worksheet.
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author  Glen Stampoultzis (glens at apache.org)
 * @author  Libin Roman (romal at vistaportal.com)
 * @author  Shawn Laubach (slaubach at apache dot org) (Just a little)
 * @author  Jean-Pierre Paris (jean-pierre.paris at m4x dot org) (Just a little, too)
 * @author  Yegor Kozlov (yegor at apache.org) (Autosizing columns)
<<<<<<< HEAD
 */

public class HSSFSheet
{
    private static final int DEBUG = POILogger.DEBUG;

    /* Constants for margins */
    public static final short LeftMargin = Sheet.LeftMargin;
    public static final short RightMargin = Sheet.RightMargin;
    public static final short TopMargin = Sheet.TopMargin;
    public static final short BottomMargin = Sheet.BottomMargin;

    public static final byte PANE_LOWER_RIGHT = (byte)0;
    public static final byte PANE_UPPER_RIGHT = (byte)1;
    public static final byte PANE_LOWER_LEFT = (byte)2;
    public static final byte PANE_UPPER_LEFT = (byte)3;


=======
 * @author  Josh Micich
 * @author  Petr Udalau(Petr.Udalau at exigenservices.com) - set/remove array formulas
 */
public final class HSSFSheet implements org.apache.poi.ss.usermodel.Sheet {
    private static final POILogger log = POILogFactory.getLogger(HSSFSheet.class);
    private static final int DEBUG = POILogger.DEBUG;

>>>>>>> 76aa07461566a5976980e6696204781271955163
    /**
     * Used for compile-time optimization.  This is the initial size for the collection of
     * rows.  It is currently set to 20.  If you generate larger sheets you may benefit
     * by setting this to a higher number and recompiling a custom edition of HSSFSheet.
     */
<<<<<<< HEAD

    public final static int INITIAL_CAPACITY = 20;

    /**
     * reference to the low level Sheet object
     */

    private Sheet sheet;
    private TreeMap rows;
    protected Workbook book;
    protected HSSFWorkbook workbook;
    private int firstrow;
    private int lastrow;
    private static POILogger log = POILogFactory.getLogger(HSSFSheet.class);
=======
    public final static int INITIAL_CAPACITY = 20;

    /**
     * reference to the low level {@link InternalSheet} object
     */
    private final InternalSheet _sheet;
    /** stores rows by zero-based row number */
    private final TreeMap<Integer, HSSFRow> _rows;
    protected final InternalWorkbook _book;
    protected final HSSFWorkbook _workbook;
    private HSSFPatriarch _patriarch;
    private int _firstrow;
    private int _lastrow;
>>>>>>> 76aa07461566a5976980e6696204781271955163

    /**
     * Creates new HSSFSheet   - called by HSSFWorkbook to create a sheet from
     * scratch.  You should not be calling this from application code (its protected anyhow).
     *
     * @param workbook - The HSSF Workbook object associated with the sheet.
<<<<<<< HEAD
     * @see loci.poi.hssf.usermodel.HSSFWorkbook#createSheet()
     */

    protected HSSFSheet(HSSFWorkbook workbook)
    {
        sheet = Sheet.createSheet();
        rows = new TreeMap();   // new ArrayList(INITIAL_CAPACITY);
        this.workbook = workbook;
        this.book = workbook.getWorkbook();
=======
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#createSheet()
     */
    protected HSSFSheet(HSSFWorkbook workbook) {
        _sheet = InternalSheet.createSheet();
        _rows = new TreeMap<Integer, HSSFRow>();
        this._workbook = workbook;
        this._book = workbook.getWorkbook();
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * Creates an HSSFSheet representing the given Sheet object.  Should only be
     * called by HSSFWorkbook when reading in an exisiting file.
     *
     * @param workbook - The HSSF Workbook object associated with the sheet.
     * @param sheet - lowlevel Sheet object this sheet will represent
<<<<<<< HEAD
     * @see loci.poi.hssf.usermodel.HSSFWorkbook#createSheet()
     */

    protected HSSFSheet(HSSFWorkbook workbook, Sheet sheet)
    {
        this.sheet = sheet;
        rows = new TreeMap();
        this.workbook = workbook;
        this.book = workbook.getWorkbook();
=======
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#createSheet()
     */
    protected HSSFSheet(HSSFWorkbook workbook, InternalSheet sheet) {
        this._sheet = sheet;
        _rows = new TreeMap<Integer, HSSFRow>();
        this._workbook = workbook;
        this._book = workbook.getWorkbook();
>>>>>>> 76aa07461566a5976980e6696204781271955163
        setPropertiesFromSheet(sheet);
    }

    HSSFSheet cloneSheet(HSSFWorkbook workbook) {
<<<<<<< HEAD
      return new HSSFSheet(workbook, sheet.cloneSheet());
    }

=======
      return new HSSFSheet(workbook, _sheet.cloneSheet());
    }

    /**
     * Return the parent workbook
     *
     * @return the parent workbook
     */
    public HSSFWorkbook getWorkbook(){
        return _workbook;
    }
>>>>>>> 76aa07461566a5976980e6696204781271955163

    /**
     * used internally to set the properties given a Sheet object
     */
<<<<<<< HEAD

    private void setPropertiesFromSheet(Sheet sheet)
    {
        int sloc = sheet.getLoc();
        RowRecord row = sheet.getNextRow();

        while (row != null)
        {
=======
    private void setPropertiesFromSheet(InternalSheet sheet) {

        RowRecord row = sheet.getNextRow();
        boolean rowRecordsAlreadyPresent = row!=null;

        while (row != null) {
>>>>>>> 76aa07461566a5976980e6696204781271955163
            createRowFromRecord(row);

            row = sheet.getNextRow();
        }
<<<<<<< HEAD
        sheet.setLoc(sloc);
        CellValueRecordInterface cval = sheet.getNextValueRecord();
=======

        Iterator<CellValueRecordInterface> iter = sheet.getCellValueIterator();
>>>>>>> 76aa07461566a5976980e6696204781271955163
        long timestart = System.currentTimeMillis();

        if (log.check( POILogger.DEBUG ))
            log.log(DEBUG, "Time at start of cell creating in HSSF sheet = ",
<<<<<<< HEAD
                new Long(timestart));
        HSSFRow lastrow = null;

        while (cval != null)
        {
            long cellstart = System.currentTimeMillis();
            HSSFRow hrow = lastrow;

            if ( ( lastrow == null ) || ( lastrow.getRowNum() != cval.getRow() ) )
            {
                hrow = getRow( cval.getRow() );
            }
            if ( hrow != null )
            {
                lastrow = hrow;
                if (log.check( POILogger.DEBUG ))
                    log.log( DEBUG, "record id = " + Integer.toHexString( ( (Record) cval ).getSid() ) );
                hrow.createCellFromRecord( cval );
                cval = sheet.getNextValueRecord();
                if (log.check( POILogger.DEBUG ))
                    log.log( DEBUG, "record took ",
                        new Long( System.currentTimeMillis() - cellstart ) );
            }
            else
            {
                cval = null;
            }
        }
        if (log.check( POILogger.DEBUG ))
            log.log(DEBUG, "total sheet cell creation took ",
                new Long(System.currentTimeMillis() - timestart));
=======
                Long.valueOf(timestart));
        HSSFRow lastrow = null;

        // Add every cell to its row
        while (iter.hasNext()) {
            CellValueRecordInterface cval = iter.next();

            long cellstart = System.currentTimeMillis();
            HSSFRow hrow = lastrow;

            if (hrow == null || hrow.getRowNum() != cval.getRow()) {
                hrow = getRow( cval.getRow() );
                lastrow = hrow;
                if (hrow == null) {
                    // Some tools (like Perl module Spreadsheet::WriteExcel - bug 41187) skip the RowRecords
                    // Excel, OpenOffice.org and GoogleDocs are all OK with this, so POI should be too.
                    if (rowRecordsAlreadyPresent) {
                        // if at least one row record is present, all should be present.
                        throw new RuntimeException("Unexpected missing row when some rows already present");
                    }
                    // create the row record on the fly now.
                    RowRecord rowRec = new RowRecord(cval.getRow());
                    sheet.addRow(rowRec);
                    hrow = createRowFromRecord(rowRec);
                }
            }
            if (log.check( POILogger.DEBUG ))
                log.log( DEBUG, "record id = " + Integer.toHexString( ( (Record) cval ).getSid() ) );
            hrow.createCellFromRecord( cval );
            if (log.check( POILogger.DEBUG ))
                log.log( DEBUG, "record took ",
                    Long.valueOf( System.currentTimeMillis() - cellstart ) );

        }
        if (log.check( POILogger.DEBUG ))
            log.log(DEBUG, "total sheet cell creation took ",
                Long.valueOf(System.currentTimeMillis() - timestart));
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * Create a new row within the sheet and return the high level representation
     *
     * @param rownum  row number
     * @return High level HSSFRow object representing a row in the sheet
<<<<<<< HEAD
     * @see loci.poi.hssf.usermodel.HSSFRow
     * @see #removeRow(HSSFRow)
     */
    public HSSFRow createRow(int rownum)
    {
        HSSFRow row = new HSSFRow(book, sheet, rownum);
=======
     * @see org.apache.poi.hssf.usermodel.HSSFRow
     * @see #removeRow(org.apache.poi.ss.usermodel.Row)
     */
    public HSSFRow createRow(int rownum)
    {
        HSSFRow row = new HSSFRow(_workbook, this, rownum);
        // new rows inherit default height from the sheet
        row.setHeight(getDefaultRowHeight());
>>>>>>> 76aa07461566a5976980e6696204781271955163

        addRow(row, true);
        return row;
    }

    /**
     * Used internally to create a high level Row object from a low level row object.
     * USed when reading an existing file
     * @param row  low level record to represent as a high level Row and add to sheet
     * @return HSSFRow high level representation
     */

    private HSSFRow createRowFromRecord(RowRecord row)
    {
<<<<<<< HEAD
        HSSFRow hrow = new HSSFRow(book, sheet, row);
=======
        HSSFRow hrow = new HSSFRow(_workbook, this, row);
>>>>>>> 76aa07461566a5976980e6696204781271955163

        addRow(hrow, false);
        return hrow;
    }

    /**
     * Remove a row from this sheet.  All cells contained in the row are removed as well
     *
     * @param row   representing a row to remove.
     */
<<<<<<< HEAD

    public void removeRow(HSSFRow row)
    {
        sheet.setLoc(sheet.getDimsLoc());
        if (rows.size() > 0)
        {
            rows.remove(row);
            if (row.getRowNum() == getLastRowNum())
            {
                lastrow = findLastRow(lastrow);
            }
            if (row.getRowNum() == getFirstRowNum())
            {
                firstrow = findFirstRow(firstrow);
            }
            Iterator iter = row.cellIterator();

            while (iter.hasNext())
            {
                HSSFCell cell = (HSSFCell) iter.next();

                sheet.removeValueRecord(row.getRowNum(),
                        cell.getCellValueRecord());
            }
            sheet.removeRow(row.getRowRecord());
=======
    public void removeRow(Row row) {
        HSSFRow hrow = (HSSFRow) row;
        if (row.getSheet() != this) {
            throw new IllegalArgumentException("Specified row does not belong to this sheet");
        }
        for(Cell cell : row) {
            HSSFCell xcell = (HSSFCell)cell;
            if(xcell.isPartOfArrayFormulaGroup()){
                String msg = "Row[rownum="+row.getRowNum()+"] contains cell(s) included in a multi-cell array formula. You cannot change part of an array.";
                xcell.notifyArrayFormulaChanging(msg);
            }
        }

        if (_rows.size() > 0) {
            Integer key = Integer.valueOf(row.getRowNum());
            HSSFRow removedRow = _rows.remove(key);
            if (removedRow != row) {
                //should not happen if the input argument is valid
                throw new IllegalArgumentException("Specified row does not belong to this sheet");
            }
            if (hrow.getRowNum() == getLastRowNum())
            {
                _lastrow = findLastRow(_lastrow);
            }
            if (hrow.getRowNum() == getFirstRowNum())
            {
                _firstrow = findFirstRow(_firstrow);
            }
            _sheet.removeRow(hrow.getRowRecord());
>>>>>>> 76aa07461566a5976980e6696204781271955163
        }
    }

    /**
     * used internally to refresh the "last row" when the last row is removed.
     */
<<<<<<< HEAD

    private int findLastRow(int lastrow)
    {
        int rownum = lastrow - 1;
        HSSFRow r = getRow(rownum);

        while (r == null && rownum > 0)
        {
            r = getRow(--rownum);
        }
        if (r == null)
          return -1;
=======
    private int findLastRow(int lastrow) {
        if (lastrow < 1) {
            return 0;
        }
        int rownum = lastrow - 1;
        HSSFRow r = getRow(rownum);

        while (r == null && rownum > 0) {
            r = getRow(--rownum);
        }
        if (r == null) {
            return 0;
        }
>>>>>>> 76aa07461566a5976980e6696204781271955163
        return rownum;
    }

    /**
     * used internally to refresh the "first row" when the first row is removed.
     */

    private int findFirstRow(int firstrow)
    {
        int rownum = firstrow + 1;
        HSSFRow r = getRow(rownum);

        while (r == null && rownum <= getLastRowNum())
        {
            r = getRow(++rownum);
        }

        if (rownum > getLastRowNum())
<<<<<<< HEAD
            return -1;
=======
            return 0;
>>>>>>> 76aa07461566a5976980e6696204781271955163

        return rownum;
    }

    /**
     * add a row to the sheet
     *
     * @param addLow whether to add the row to the low level model - false if its already there
     */

    private void addRow(HSSFRow row, boolean addLow)
    {
<<<<<<< HEAD
        rows.put(row, row);
        if (addLow)
        {
            sheet.addRow(row.getRowRecord());
        }
        if (row.getRowNum() > getLastRowNum())
        {
            lastrow = row.getRowNum();
        }
        if (row.getRowNum() < getFirstRowNum())
        {
            firstrow = row.getRowNum();
=======
        _rows.put(Integer.valueOf(row.getRowNum()), row);
        if (addLow)
        {
            _sheet.addRow(row.getRowRecord());
        }
        boolean firstRow = _rows.size() == 1;
        if (row.getRowNum() > getLastRowNum() || firstRow)
        {
            _lastrow = row.getRowNum();
        }
        if (row.getRowNum() < getFirstRowNum() || firstRow)
        {
            _firstrow = row.getRowNum();
>>>>>>> 76aa07461566a5976980e6696204781271955163
        }
    }

    /**
     * Returns the logical row (not physical) 0-based.  If you ask for a row that is not
     * defined you get a null.  This is to say row 4 represents the fifth row on a sheet.
<<<<<<< HEAD
     * @param rownum  row to get
     * @return HSSFRow representing the rownumber or null if its not defined on the sheet
     */

    public HSSFRow getRow(int rownum)
    {
        HSSFRow row = new HSSFRow();

        //row.setRowNum((short) rownum);
        row.setRowNum( rownum);
        return (HSSFRow) rows.get(row);
    }

    /**
     * Returns the number of phsyically defined rows (NOT the number of rows in the sheet)
     */

    public int getPhysicalNumberOfRows()
    {
        return rows.size();
    }

    /**
     * gets the first row on the sheet
     * @return the number of the first logical row on the sheet
     */

    public int getFirstRowNum()
    {
        return firstrow;
    }

    /**
     * gets the last row on the sheet
     * @return last row contained n this sheet.
     */

    public int getLastRowNum()
    {
        return lastrow;
=======
     * @param rowIndex  row to get
     * @return HSSFRow representing the row number or null if its not defined on the sheet
     */
    public HSSFRow getRow(int rowIndex) {
        return _rows.get(Integer.valueOf(rowIndex));
    }

    /**
     * Returns the number of physically defined rows (NOT the number of rows in the sheet)
     */
    public int getPhysicalNumberOfRows() {
        return _rows.size();
    }

    /**
     * Gets the first row on the sheet
     * @return the number of the first logical row on the sheet, zero based
     */
    public int getFirstRowNum() {
        return _firstrow;
    }

    /**
     * Gets the number last row on the sheet.
     * Owing to idiosyncrasies in the excel file
     *  format, if the result of calling this method
     *  is zero, you can't tell if that means there
     *  are zero rows on the sheet, or one at
     *  position zero. For that case, additionally
     *  call {@link #getPhysicalNumberOfRows()} to
     *  tell if there is a row at position zero
     *  or not.
     * @return the number of the last row contained in this sheet, zero based.
     */
    public int getLastRowNum() {
        return _lastrow;
    }

    /**
     * Creates a data validation object
     * @param dataValidation The Data validation object settings
     */
    public void addValidationData(DataValidation dataValidation) {
       if (dataValidation == null) {
           throw new IllegalArgumentException("objValidation must not be null");
       }
       HSSFDataValidation hssfDataValidation = (HSSFDataValidation)dataValidation;
       DataValidityTable dvt = _sheet.getOrCreateDataValidityTable();

       DVRecord dvRecord = hssfDataValidation.createDVRecord(this);
       dvt.addDataValidation(dvRecord);
    }


    /**
     * @deprecated (Sep 2008) use {@link #setColumnHidden(int, boolean)}
     */
    public void setColumnHidden(short columnIndex, boolean hidden) {
        setColumnHidden(columnIndex & 0xFFFF, hidden);
    }

    /**
     * @deprecated (Sep 2008) use {@link #isColumnHidden(int)}
     */
    public boolean isColumnHidden(short columnIndex) {
        return isColumnHidden(columnIndex & 0xFFFF);
    }

    /**
     * @deprecated (Sep 2008) use {@link #setColumnWidth(int, int)}
     */
    public void setColumnWidth(short columnIndex, short width) {
        setColumnWidth(columnIndex & 0xFFFF, width & 0xFFFF);
    }

    /**
     * @deprecated (Sep 2008) use {@link #getColumnWidth(int)}
     */
    public short getColumnWidth(short columnIndex) {
        return (short)getColumnWidth(columnIndex & 0xFFFF);
    }

    /**
     * @deprecated (Sep 2008) use {@link #setDefaultColumnWidth(int)}
     */
    public void setDefaultColumnWidth(short width) {
        setDefaultColumnWidth(width & 0xFFFF);
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * Get the visibility state for a given column.
<<<<<<< HEAD
     * @param column - the column to get (0-based)
     * @param hidden - the visiblity state of the column
     */

    public void setColumnHidden(short column, boolean hidden)
    {
        sheet.setColumnHidden(column, hidden);
=======
     * @param columnIndex - the column to get (0-based)
     * @param hidden - the visiblity state of the column
     */
    public void setColumnHidden(int columnIndex, boolean hidden) {
        _sheet.setColumnHidden(columnIndex, hidden);
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * Get the hidden state for a given column.
<<<<<<< HEAD
     * @param column - the column to set (0-based)
     * @return hidden - the visiblity state of the column
     */

    public boolean isColumnHidden(short column)
    {
        return sheet.isColumnHidden(column);
    }

    /**
     * set the width (in units of 1/256th of a character width)
     * @param column - the column to set (0-based)
     * @param width - the width in units of 1/256th of a character width
     */

    public void setColumnWidth(short column, short width)
    {
        sheet.setColumnWidth(column, width);
=======
     * @param columnIndex - the column to set (0-based)
     * @return hidden - <code>false</code> if the column is visible
     */
    public boolean isColumnHidden(int columnIndex) {
        return _sheet.isColumnHidden(columnIndex);
    }

    /**
     * Set the width (in units of 1/256th of a character width)
     *
     * <p>
     * The maximum column width for an individual cell is 255 characters.
     * This value represents the number of characters that can be displayed
     * in a cell that is formatted with the standard font (first font in the workbook).
     * </p>
     *
     * <p>
     * Character width is defined as the maximum digit width
     * of the numbers <code>0, 1, 2, ... 9</code> as rendered
     * using the default font (first font in the workbook).
     * <br/>
     * Unless you are using a very special font, the default character is '0' (zero),
     * this is true for Arial (default font font in HSSF) and Calibri (default font in XSSF)
     * </p>
     *
     * <p>
     * Please note, that the width set by this method includes 4 pixels of margin padding (two on each side),
     * plus 1 pixel padding for the gridlines (Section 3.3.1.12 of the OOXML spec).
     * This results is a slightly less value of visible characters than passed to this method (approx. 1/2 of a character).
     * </p>
     * <p>
     * To compute the actual number of visible characters,
     *  Excel uses the following formula (Section 3.3.1.12 of the OOXML spec):
     * </p>
     * <code>
     *     width = Truncate([{Number of Visible Characters} *
     *      {Maximum Digit Width} + {5 pixel padding}]/{Maximum Digit Width}*256)/256
     * </code>
     * <p>Using the Calibri font as an example, the maximum digit width of 11 point font size is 7 pixels (at 96 dpi).
     *  If you set a column width to be eight characters wide, e.g. <code>setColumnWidth(columnIndex, 8*256)</code>,
     *  then the actual value of visible characters (the value shown in Excel) is derived from the following equation:
     *  <code>
            Truncate([numChars*7+5]/7*256)/256 = 8;
     *  </code>
     *
     *  which gives <code>7.29</code>.
     *
     * @param columnIndex - the column to set (0-based)
     * @param width - the width in units of 1/256th of a character width
     * @throws IllegalArgumentException if width > 255*256 (the maximum column width in Excel is 255 characters)
     */
    public void setColumnWidth(int columnIndex, int width) {
        _sheet.setColumnWidth(columnIndex, width);
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * get the width (in units of 1/256th of a character width )
<<<<<<< HEAD
     * @param column - the column to set (0-based)
     * @return width - the width in units of 1/256th of a character width
     */

    public short getColumnWidth(short column)
    {
        return sheet.getColumnWidth(column);
=======
     * @param columnIndex - the column to set (0-based)
     * @return width - the width in units of 1/256th of a character width
     */
    public int getColumnWidth(int columnIndex) {
        return _sheet.getColumnWidth(columnIndex);
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * get the default column width for the sheet (if the columns do not define their own width) in
     * characters
     * @return default column width
     */
<<<<<<< HEAD

    public short getDefaultColumnWidth()
    {
        return sheet.getDefaultColumnWidth();
    }

=======
    public int getDefaultColumnWidth() {
        return _sheet.getDefaultColumnWidth();
    }
    /**
     * set the default column width for the sheet (if the columns do not define their own width) in
     * characters
     * @param width default column width
     */
    public void setDefaultColumnWidth(int width) {
        _sheet.setDefaultColumnWidth(width);
    }


>>>>>>> 76aa07461566a5976980e6696204781271955163
    /**
     * get the default row height for the sheet (if the rows do not define their own height) in
     * twips (1/20 of  a point)
     * @return  default row height
     */
<<<<<<< HEAD

    public short getDefaultRowHeight()
    {
        return sheet.getDefaultRowHeight();
=======
    public short getDefaultRowHeight() {
        return _sheet.getDefaultRowHeight();
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * get the default row height for the sheet (if the rows do not define their own height) in
     * points.
     * @return  default row height in points
     */

    public float getDefaultRowHeightInPoints()
    {
<<<<<<< HEAD
        return (sheet.getDefaultRowHeight() / 20);
    }

    /**
     * set the default column width for the sheet (if the columns do not define their own width) in
     * characters
     * @param width default column width
     */

    public void setDefaultColumnWidth(short width)
    {
        sheet.setDefaultColumnWidth(width);
=======
        return ((float)_sheet.getDefaultRowHeight() / 20);
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * set the default row height for the sheet (if the rows do not define their own height) in
     * twips (1/20 of  a point)
     * @param  height default row height
     */

    public void setDefaultRowHeight(short height)
    {
<<<<<<< HEAD
        sheet.setDefaultRowHeight(height);
=======
        _sheet.setDefaultRowHeight(height);
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * set the default row height for the sheet (if the rows do not define their own height) in
     * points
     * @param height default row height
     */

    public void setDefaultRowHeightInPoints(float height)
    {
<<<<<<< HEAD
        sheet.setDefaultRowHeight((short) (height * 20));
=======
        _sheet.setDefaultRowHeight((short) (height * 20));
    }

    /**
     * Returns the HSSFCellStyle that applies to the given
     *  (0 based) column, or null if no style has been
     *  set for that column
     */
    public HSSFCellStyle getColumnStyle(int column) {
        short styleIndex = _sheet.getXFIndexForColAt((short)column);

        if(styleIndex == 0xf) {
            // None set
            return null;
        }

        ExtendedFormatRecord xf = _book.getExFormatAt(styleIndex);
        return new HSSFCellStyle(styleIndex, xf, _book);
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * get whether gridlines are printed.
     * @return true if printed
     */

    public boolean isGridsPrinted()
    {
<<<<<<< HEAD
        return sheet.isGridsPrinted();
=======
        return _sheet.isGridsPrinted();
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * set whether gridlines printed.
     * @param value  false if not printed.
     */

    public void setGridsPrinted(boolean value)
    {
<<<<<<< HEAD
        sheet.setGridsPrinted(value);
    }

    /**
     * adds a merged region of cells (hence those cells form one)
     * @param region (rowfrom/colfrom-rowto/colto) to merge
     * @return index of this region
     */

    public int addMergedRegion(Region region)
    {
        //return sheet.addMergedRegion((short) region.getRowFrom(),
        return sheet.addMergedRegion( region.getRowFrom(),
=======
        _sheet.setGridsPrinted(value);
    }

    /**
     * @deprecated (Aug-2008) use <tt>CellRangeAddress</tt> instead of <tt>Region</tt>
     */
    public int addMergedRegion(org.apache.poi.ss.util.Region region)
    {
        return _sheet.addMergedRegion( region.getRowFrom(),
>>>>>>> 76aa07461566a5976980e6696204781271955163
                region.getColumnFrom(),
                //(short) region.getRowTo(),
                region.getRowTo(),
                region.getColumnTo());
    }
<<<<<<< HEAD
=======
    /**
     * adds a merged region of cells (hence those cells form one)
     * @param region (rowfrom/colfrom-rowto/colto) to merge
     * @return index of this region
     */
    public int addMergedRegion(CellRangeAddress region)
    {
        region.validate(SpreadsheetVersion.EXCEL97);

        // throw IllegalStateException if the argument CellRangeAddress intersects with
        // a multi-cell array formula defined in this sheet
        validateArrayFormulas(region);

        return _sheet.addMergedRegion( region.getFirstRow(),
                region.getFirstColumn(),
                region.getLastRow(),
                region.getLastColumn());
    }

    private void validateArrayFormulas(CellRangeAddress region){
        int firstRow = region.getFirstRow();
        int firstColumn = region.getFirstColumn();
        int lastRow = region.getLastRow();
        int lastColumn = region.getLastColumn();
        for (int rowIn = firstRow; rowIn <= lastRow; rowIn++) {
            for (int colIn = firstColumn; colIn <= lastColumn; colIn++) {
                HSSFRow row = getRow(rowIn);
                if (row == null) continue;

                HSSFCell cell = row.getCell(colIn);
                if(cell == null) continue;

                if(cell.isPartOfArrayFormulaGroup()){
                    CellRangeAddress arrayRange = cell.getArrayFormulaRange();
                    if (arrayRange.getNumberOfCells() > 1 &&
                            ( arrayRange.isInRange(region.getFirstRow(), region.getFirstColumn()) ||
                              arrayRange.isInRange(region.getFirstRow(), region.getFirstColumn()))  ){
                        String msg = "The range " + region.formatAsString() + " intersects with a multi-cell array formula. " +
                                "You cannot merge cells of an array.";
                        throw new IllegalStateException(msg);
                    }
                }
            }
        }

    }

    /**
     * Control if Excel should be asked to recalculate all formulas on this sheet
     * when the workbook is opened.
     *
     *  <p>
     *  Calculating the formula values with {@link org.apache.poi.ss.usermodel.FormulaEvaluator} is the
     *  recommended solution, but this may be used for certain cases where
     *  evaluation in POI is not possible.
     *  </p>
     *
     *  <p>
     *  It is recommended to force recalcuation of formulas on workbook level using
     *  {@link org.apache.poi.ss.usermodel.Workbook#setForceFormulaRecalculation(boolean)}
     *  to ensure that all cross-worksheet formuals and external dependencies are updated.
     *  </p>
     * @param value true if the application will perform a full recalculation of
     * this worksheet values when the workbook is opened
     *
     * @see org.apache.poi.ss.usermodel.Workbook#setForceFormulaRecalculation(boolean)
     */
    public void setForceFormulaRecalculation(boolean value)
    {
        _sheet.setUncalced(value);
    }
    /**
     * Whether a record must be inserted or not at generation to indicate that
     * formula must be recalculated when workbook is opened.
     * @return true if an uncalced record must be inserted or not at generation
     */
    public boolean getForceFormulaRecalculation()
    {
        return _sheet.getUncalced();
    }

>>>>>>> 76aa07461566a5976980e6696204781271955163

    /**
     * determines whether the output is vertically centered on the page.
     * @param value true to vertically center, false otherwise.
     */

    public void setVerticallyCenter(boolean value)
    {
<<<<<<< HEAD
        VCenterRecord record =
                (VCenterRecord) sheet.findFirstRecordBySid(VCenterRecord.sid);

        record.setVCenter(value);
=======
        _sheet.getPageSettings().getVCenter().setVCenter(value);
    }

    /**
     * TODO: Boolean not needed, remove after next release
     * @deprecated (Mar-2008) use getVerticallyCenter() instead
     */
    public boolean getVerticallyCenter(boolean value) {
        return getVerticallyCenter();
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * Determine whether printed output for this sheet will be vertically centered.
     */
<<<<<<< HEAD

    public boolean getVerticallyCenter(boolean value)
    {
        VCenterRecord record =
                (VCenterRecord) sheet.findFirstRecordBySid(VCenterRecord.sid);

        return record.getVCenter();
=======
    public boolean getVerticallyCenter()
    {
        return _sheet.getPageSettings().getVCenter().getVCenter();
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * determines whether the output is horizontally centered on the page.
     * @param value true to horizontally center, false otherwise.
     */

    public void setHorizontallyCenter(boolean value)
    {
<<<<<<< HEAD
        HCenterRecord record =
                (HCenterRecord) sheet.findFirstRecordBySid(HCenterRecord.sid);

        record.setHCenter(value);
=======
        _sheet.getPageSettings().getHCenter().setHCenter(value);
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * Determine whether printed output for this sheet will be horizontally centered.
     */

    public boolean getHorizontallyCenter()
    {
<<<<<<< HEAD
        HCenterRecord record =
                (HCenterRecord) sheet.findFirstRecordBySid(HCenterRecord.sid);

        return record.getHCenter();
    }


=======

        return _sheet.getPageSettings().getHCenter().getHCenter();
    }

    /**
     * Sets whether the worksheet is displayed from right to left instead of from left to right.
     *
     * @param value true for right to left, false otherwise.
     */
    public void setRightToLeft(boolean value)
    {
        _sheet.getWindowTwo().setArabic(value);
    }

    /**
     * Whether the text is displayed in right-to-left mode in the window
     *
     * @return whether the text is displayed in right-to-left mode in the window
     */
    public boolean isRightToLeft()
    {
        return _sheet.getWindowTwo().getArabic();
    }
>>>>>>> 76aa07461566a5976980e6696204781271955163

    /**
     * removes a merged region of cells (hence letting them free)
     * @param index of the region to unmerge
     */

    public void removeMergedRegion(int index)
    {
<<<<<<< HEAD
        sheet.removeMergedRegion(index);
=======
        _sheet.removeMergedRegion(index);
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * returns the number of merged regions
     * @return number of merged regions
     */

    public int getNumMergedRegions()
    {
<<<<<<< HEAD
        return sheet.getNumMergedRegions();
    }

    /**
     * gets the region at a particular index
     * @param index of the region to fetch
     * @return the merged region (simple eh?)
     */

    public Region getMergedRegionAt(int index)
    {
        return new Region(sheet.getMergedRegionAt(index));
=======
        return _sheet.getNumMergedRegions();
    }

    /**
     * @deprecated (Aug-2008) use {@link HSSFSheet#getMergedRegion(int)}
     */
    public org.apache.poi.hssf.util.Region getMergedRegionAt(int index) {
        CellRangeAddress cra = getMergedRegion(index);

        return new org.apache.poi.hssf.util.Region(cra.getFirstRow(), (short)cra.getFirstColumn(),
                cra.getLastRow(), (short)cra.getLastColumn());
    }
    /**
     * @return the merged region at the specified index
     */
    public CellRangeAddress getMergedRegion(int index) {
        return _sheet.getMergedRegionAt(index);
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * @return an iterator of the PHYSICAL rows.  Meaning the 3rd element may not
     * be the third row if say for instance the second row is undefined.
<<<<<<< HEAD
     */

    public Iterator rowIterator()
    {
        return rows.values().iterator();
    }

=======
     * Call getRowNum() on each row if you care which one it is.
     */
    public Iterator<Row> rowIterator() {
        @SuppressWarnings("unchecked") // can this clumsy generic syntax be improved?
        Iterator<Row> result = (Iterator<Row>)(Iterator<? extends Row>)_rows.values().iterator();
        return result;
    }
    /**
     * Alias for {@link #rowIterator()} to allow
     *  foreach loops
     */
    public Iterator<Row> iterator() {
        return rowIterator();
    }


>>>>>>> 76aa07461566a5976980e6696204781271955163
    /**
     * used internally in the API to get the low level Sheet record represented by this
     * Object.
     * @return Sheet - low level representation of this HSSFSheet.
     */
<<<<<<< HEAD

    protected Sheet getSheet()
    {
        return sheet;
=======
    InternalSheet getSheet() {
        return _sheet;
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * whether alternate expression evaluation is on
     * @param b  alternative expression evaluation or not
     */
<<<<<<< HEAD

    public void setAlternativeExpression(boolean b)
    {
        WSBoolRecord record =
                (WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid);
=======
    public void setAlternativeExpression(boolean b) {
        WSBoolRecord record =
                (WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid);
>>>>>>> 76aa07461566a5976980e6696204781271955163

        record.setAlternateExpression(b);
    }

    /**
     * whether alternative formula entry is on
     * @param b  alternative formulas or not
     */
<<<<<<< HEAD

    public void setAlternativeFormula(boolean b)
    {
        WSBoolRecord record =
                (WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid);
=======
    public void setAlternativeFormula(boolean b) {
        WSBoolRecord record =
                (WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid);
>>>>>>> 76aa07461566a5976980e6696204781271955163

        record.setAlternateFormula(b);
    }

    /**
     * show automatic page breaks or not
     * @param b  whether to show auto page breaks
     */
<<<<<<< HEAD

    public void setAutobreaks(boolean b)
    {
        WSBoolRecord record =
                (WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid);
=======
    public void setAutobreaks(boolean b) {
        WSBoolRecord record =
                (WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid);
>>>>>>> 76aa07461566a5976980e6696204781271955163

        record.setAutobreaks(b);
    }

    /**
     * set whether sheet is a dialog sheet or not
     * @param b  isDialog or not
     */
<<<<<<< HEAD

    public void setDialog(boolean b)
    {
        WSBoolRecord record =
                (WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid);
=======
    public void setDialog(boolean b) {
        WSBoolRecord record =
                (WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid);
>>>>>>> 76aa07461566a5976980e6696204781271955163

        record.setDialog(b);
    }

    /**
     * set whether to display the guts or not
     *
     * @param b  guts or no guts (or glory)
     */
<<<<<<< HEAD

    public void setDisplayGuts(boolean b)
    {
        WSBoolRecord record =
                (WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid);
=======
    public void setDisplayGuts(boolean b) {
        WSBoolRecord record =
                (WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid);
>>>>>>> 76aa07461566a5976980e6696204781271955163

        record.setDisplayGuts(b);
    }

    /**
     * fit to page option is on
     * @param b  fit or not
     */
<<<<<<< HEAD

    public void setFitToPage(boolean b)
    {
        WSBoolRecord record =
                (WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid);
=======
    public void setFitToPage(boolean b) {
        WSBoolRecord record =
                (WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid);
>>>>>>> 76aa07461566a5976980e6696204781271955163

        record.setFitToPage(b);
    }

    /**
     * set if row summaries appear below detail in the outline
     * @param b  below or not
     */
<<<<<<< HEAD

    public void setRowSumsBelow(boolean b)
    {
        WSBoolRecord record =
                (WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid);

        record.setRowSumsBelow(b);
=======
    public void setRowSumsBelow(boolean b) {
        WSBoolRecord record =
                (WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid);

        record.setRowSumsBelow(b);
        //setAlternateExpression must be set in conjuction with setRowSumsBelow
        record.setAlternateExpression(b);
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * set if col summaries appear right of the detail in the outline
     * @param b  right or not
     */
<<<<<<< HEAD

    public void setRowSumsRight(boolean b)
    {
        WSBoolRecord record =
                (WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid);
=======
    public void setRowSumsRight(boolean b) {
        WSBoolRecord record =
                (WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid);
>>>>>>> 76aa07461566a5976980e6696204781271955163

        record.setRowSumsRight(b);
    }

    /**
     * whether alternate expression evaluation is on
     * @return alternative expression evaluation or not
     */
<<<<<<< HEAD

    public boolean getAlternateExpression()
    {
        return ((WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid))
=======
    public boolean getAlternateExpression() {
        return ((WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid))
>>>>>>> 76aa07461566a5976980e6696204781271955163
                .getAlternateExpression();
    }

    /**
     * whether alternative formula entry is on
     * @return alternative formulas or not
     */
<<<<<<< HEAD

    public boolean getAlternateFormula()
    {
        return ((WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid))
=======
    public boolean getAlternateFormula() {
        return ((WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid))
>>>>>>> 76aa07461566a5976980e6696204781271955163
                .getAlternateFormula();
    }

    /**
     * show automatic page breaks or not
     * @return whether to show auto page breaks
     */
<<<<<<< HEAD

    public boolean getAutobreaks()
    {
        return ((WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid))
=======
    public boolean getAutobreaks() {
        return ((WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid))
>>>>>>> 76aa07461566a5976980e6696204781271955163
                .getAutobreaks();
    }

    /**
     * get whether sheet is a dialog sheet or not
     * @return isDialog or not
     */
<<<<<<< HEAD

    public boolean getDialog()
    {
        return ((WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid))
=======
    public boolean getDialog() {
        return ((WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid))
>>>>>>> 76aa07461566a5976980e6696204781271955163
                .getDialog();
    }

    /**
     * get whether to display the guts or not
     *
     * @return guts or no guts (or glory)
     */
<<<<<<< HEAD

    public boolean getDisplayGuts()
    {
        return ((WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid))
                .getDisplayGuts();
    }

=======
    public boolean getDisplayGuts() {
        return ((WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid))
                .getDisplayGuts();
    }


    /**
     * Gets the flag indicating whether the window should show 0 (zero) in cells containing zero value.
     * When false, cells with zero value appear blank instead of showing the number zero.
     * <p>
     * In Excel 2003 this option can be changed in the Options dialog on the View tab.
     * </p>
     * @return whether all zero values on the worksheet are displayed
     */
    public boolean isDisplayZeros(){
        return _sheet.getWindowTwo().getDisplayZeros();
    }

    /**
     * Set whether the window should show 0 (zero) in cells containing zero value.
     * When false, cells with zero value appear blank instead of showing the number zero.
     * <p>
     * In Excel 2003 this option can be set in the Options dialog on the View tab.
     * </p>
     * @param value whether to display or hide all zero values on the worksheet
     */
    public void setDisplayZeros(boolean value){
        _sheet.getWindowTwo().setDisplayZeros(value);
    }

>>>>>>> 76aa07461566a5976980e6696204781271955163
    /**
     * fit to page option is on
     * @return fit or not
     */
<<<<<<< HEAD

    public boolean getFitToPage()
    {
        return ((WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid))
=======
    public boolean getFitToPage() {
        return ((WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid))
>>>>>>> 76aa07461566a5976980e6696204781271955163
                .getFitToPage();
    }

    /**
     * get if row summaries appear below detail in the outline
     * @return below or not
     */
<<<<<<< HEAD

    public boolean getRowSumsBelow()
    {
        return ((WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid))
=======
    public boolean getRowSumsBelow() {
        return ((WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid))
>>>>>>> 76aa07461566a5976980e6696204781271955163
                .getRowSumsBelow();
    }

    /**
     * get if col summaries appear right of the detail in the outline
     * @return right or not
     */
<<<<<<< HEAD

    public boolean getRowSumsRight()
    {
        return ((WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid))
=======
    public boolean getRowSumsRight() {
        return ((WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid))
>>>>>>> 76aa07461566a5976980e6696204781271955163
                .getRowSumsRight();
    }

    /**
     * Returns whether gridlines are printed.
     * @return Gridlines are printed
     */
    public boolean isPrintGridlines() {
        return getSheet().getPrintGridlines().getPrintGridlines();
    }

    /**
     * Turns on or off the printing of gridlines.
     * @param newPrintGridlines boolean to turn on or off the printing of
     * gridlines
     */
<<<<<<< HEAD
    public void setPrintGridlines( boolean newPrintGridlines )
    {
        getSheet().getPrintGridlines().setPrintGridlines( newPrintGridlines );
=======
    public void setPrintGridlines(boolean newPrintGridlines) {
        getSheet().getPrintGridlines().setPrintGridlines(newPrintGridlines);
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * Gets the print setup object.
     * @return The user model for the print setup object.
     */
<<<<<<< HEAD
    public HSSFPrintSetup getPrintSetup()
    {
        return new HSSFPrintSetup( getSheet().getPrintSetup() );
    }

    /**
     * Gets the user model for the document header.
     * @return The Document header.
     */
    public HSSFHeader getHeader()
    {
        return new HSSFHeader( getSheet().getHeader() );
    }

    /**
     * Gets the user model for the document footer.
     * @return The Document footer.
     */
    public HSSFFooter getFooter()
    {
        return new HSSFFooter( getSheet().getFooter() );
    }

=======
    public HSSFPrintSetup getPrintSetup() {
        return new HSSFPrintSetup(_sheet.getPageSettings().getPrintSetup());
    }

    public HSSFHeader getHeader() {
        return new HSSFHeader(_sheet.getPageSettings());
    }

    public HSSFFooter getFooter() {
        return new HSSFFooter(_sheet.getPageSettings());
    }

    /**
     * Note - this is not the same as whether the sheet is focused (isActive)
     * @return <code>true</code> if this sheet is currently selected
     */
    public boolean isSelected() {
        return getSheet().getWindowTwo().getSelected();
    }
    /**
     * Sets whether sheet is selected.
     * @param sel Whether to select the sheet or deselect the sheet.
     */
    public void setSelected(boolean sel) {
        getSheet().getWindowTwo().setSelected(sel);
    }
    /**
     * @return <code>true</code> if this sheet is currently focused
     */
    public boolean isActive() {
        return getSheet().getWindowTwo().isActive();
    }
>>>>>>> 76aa07461566a5976980e6696204781271955163
    /**
     * Sets whether sheet is selected.
     * @param sel Whether to select the sheet or deselect the sheet.
     */
<<<<<<< HEAD
    public void setSelected( boolean sel )
    {
        getSheet().setSelected( sel );
=======
    public void setActive(boolean sel) {
        getSheet().getWindowTwo().setActive(sel);
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * Gets the size of the margin in inches.
     * @param margin which margin to get
     * @return the size of the margin
     */
<<<<<<< HEAD
    public double getMargin( short margin )
    {
        return getSheet().getMargin( margin );
=======
    public double getMargin(short margin) {
        switch (margin){
            case FooterMargin:
                return _sheet.getPageSettings().getPrintSetup().getFooterMargin();
            case HeaderMargin:
                return _sheet.getPageSettings().getPrintSetup().getHeaderMargin();
            default:
                return _sheet.getPageSettings().getMargin(margin);
        }
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * Sets the size of the margin in inches.
     * @param margin which margin to get
     * @param size the size of the margin
     */
<<<<<<< HEAD
    public void setMargin( short margin, double size )
    {
        getSheet().setMargin( margin, size );
    }

	/**
	 * Answer whether protection is enabled or disabled
	 * @return true => protection enabled; false => protection disabled
	 */
	public boolean getProtect() {
		return getSheet().isProtected()[0];
	}

	/**
	 * @return hashed password
	 */
	public short getPassword() {
		return getSheet().getPassword().getPassword();
	}

	/**
	 * Answer whether object protection is enabled or disabled
	 * @return true => protection enabled; false => protection disabled
	 */
	public boolean getObjectProtect() {
		return getSheet().isProtected()[1];
	}

	/**
	 * Answer whether scenario protection is enabled or disabled
	 * @return true => protection enabled; false => protection disabled
	 */
	public boolean getScenarioProtect() {
		return getSheet().isProtected()[2];
	}

	/**
	 * Sets the protection on enabled or disabled
	 * @param protect true => protection enabled; false => protection disabled
         * @deprecated use protectSheet(String, boolean, boolean)
	 */
	public void setProtect(boolean protect) {
		getSheet().getProtect().setProtect(protect);
	}

        /**
         * Sets the protection enabled as well as the password
         * @param password to set for protection
         */
        public void protectSheet(String password) {
                getSheet().protectSheet(password, true, true); //protect objs&scenarios(normal)
        }

    /**
     * Sets the zoom magnication for the sheet.  The zoom is expressed as a
=======
    public void setMargin(short margin, double size) {
        switch (margin){
            case FooterMargin:
                _sheet.getPageSettings().getPrintSetup().setFooterMargin(size);
                break;
            case HeaderMargin:
                _sheet.getPageSettings().getPrintSetup().setHeaderMargin(size);
                break;
            default:
                _sheet.getPageSettings().setMargin(margin, size);
        }
    }

    private WorksheetProtectionBlock getProtectionBlock() {
        return _sheet.getProtectionBlock();
    }
    /**
     * Answer whether protection is enabled or disabled
     * @return true => protection enabled; false => protection disabled
     */
    public boolean getProtect() {
        return getProtectionBlock().isSheetProtected();
    }

    /**
     * @return hashed password
     */
    public short getPassword() {
        return (short)getProtectionBlock().getPasswordHash();
    }

    /**
     * Answer whether object protection is enabled or disabled
     * @return true => protection enabled; false => protection disabled
     */
    public boolean getObjectProtect() {
        return getProtectionBlock().isObjectProtected();
    }

    /**
     * Answer whether scenario protection is enabled or disabled
     * @return true => protection enabled; false => protection disabled
     */
    public boolean getScenarioProtect() {
        return getProtectionBlock().isScenarioProtected();
    }
    /**
     * Sets the protection enabled as well as the password
     * @param password to set for protection. Pass <code>null</code> to remove protection
     */
    public void protectSheet(String password) {
        getProtectionBlock().protectSheet(password, true, true); //protect objs&scenarios(normal)
    }

    /**
     * Sets the zoom magnification for the sheet.  The zoom is expressed as a
>>>>>>> 76aa07461566a5976980e6696204781271955163
     * fraction.  For example to express a zoom of 75% use 3 for the numerator
     * and 4 for the denominator.
     *
     * @param numerator     The numerator for the zoom magnification.
     * @param denominator   The denominator for the zoom magnification.
     */
    public void setZoom( int numerator, int denominator)
    {
        if (numerator < 1 || numerator > 65535)
            throw new IllegalArgumentException("Numerator must be greater than 1 and less than 65536");
        if (denominator < 1 || denominator > 65535)
            throw new IllegalArgumentException("Denominator must be greater than 1 and less than 65536");

        SCLRecord sclRecord = new SCLRecord();
        sclRecord.setNumerator((short)numerator);
        sclRecord.setDenominator((short)denominator);
        getSheet().setSCLRecord(sclRecord);
    }
<<<<<<< HEAD
    
    /**
     * The top row in the visible view when the sheet is 
     * first viewed after opening it in a viewer 
     * @return short indicating the rownum (0 based) of the top row
     */
    public short getTopRow() 
    {
    	return sheet.getTopRow();
    }
    
    /**
     * The left col in the visible view when the sheet is 
     * first viewed after opening it in a viewer 
     * @return short indicating the rownum (0 based) of the top row
     */
    public short getLeftCol() 
    {
    	return sheet.getLeftCol();
    }
    
    /**
     * Sets desktop window pane display area, when the 
=======

    /**
     * The top row in the visible view when the sheet is
     * first viewed after opening it in a viewer
     * @return short indicating the rownum (0 based) of the top row
     */
    public short getTopRow() {
        return _sheet.getTopRow();
    }

    /**
     * The left col in the visible view when the sheet is
     * first viewed after opening it in a viewer
     * @return short indicating the rownum (0 based) of the top row
     */
    public short getLeftCol() {
        return _sheet.getLeftCol();
    }

    /**
     * Sets desktop window pane display area, when the
>>>>>>> 76aa07461566a5976980e6696204781271955163
     * file is first opened in a viewer.
     * @param toprow the top row to show in desktop window pane
     * @param leftcol the left column to show in desktop window pane
     */
    public void showInPane(short toprow, short leftcol){
<<<<<<< HEAD
        this.sheet.setTopRow((short)toprow);
        this.sheet.setLeftCol((short)leftcol);
        }

	/**
	 * Shifts the merged regions left or right depending on mode
	 * <p>
	 * TODO: MODE , this is only row specific
	 * @param startRow
	 * @param endRow
	 * @param n
	 * @param isRow
	 */
	protected void shiftMerged(int startRow, int endRow, int n, boolean isRow) {
		List shiftedRegions = new ArrayList();
		//move merged regions completely if they fall within the new region boundaries when they are shifted
		for (int i = 0; i < this.getNumMergedRegions(); i++) {
			 Region merged = this.getMergedRegionAt(i);

			 boolean inStart = (merged.getRowFrom() >= startRow || merged.getRowTo() >= startRow);
			 boolean inEnd =  (merged.getRowTo() <= endRow || merged.getRowFrom() <= endRow);

			 //dont check if it's not within the shifted area
			 if (! (inStart && inEnd)) continue;

			 //only shift if the region outside the shifted rows is not merged too
			 if (!merged.contains(startRow-1, (short)0) && !merged.contains(endRow+1, (short)0)){
				 merged.setRowFrom(merged.getRowFrom()+n);
				 merged.setRowTo(merged.getRowTo()+n);
				 //have to remove/add it back
				 shiftedRegions.add(merged);
				 this.removeMergedRegion(i);
				 i = i -1; // we have to back up now since we removed one

			 }

		}

		//readd so it doesn't get shifted again
		Iterator iterator = shiftedRegions.iterator();
		while (iterator.hasNext()) {
			Region region = (Region)iterator.next();

			this.addMergedRegion(region);
		}

	}
=======
        _sheet.setTopRow(toprow);
        _sheet.setLeftCol(leftcol);
    }

    /**
     * Shifts the merged regions left or right depending on mode
     * <p>
     * TODO: MODE , this is only row specific
     * @param startRow
     * @param endRow
     * @param n
     * @param isRow
     */
    protected void shiftMerged(int startRow, int endRow, int n, boolean isRow) {
        List<CellRangeAddress> shiftedRegions = new ArrayList<CellRangeAddress>();
        //move merged regions completely if they fall within the new region boundaries when they are shifted
        for (int i = 0; i < getNumMergedRegions(); i++) {
             CellRangeAddress merged = getMergedRegion(i);

             boolean inStart= (merged.getFirstRow() >= startRow || merged.getLastRow() >= startRow);
             boolean inEnd  = (merged.getFirstRow() <= endRow   || merged.getLastRow() <= endRow);

             //don't check if it's not within the shifted area
             if (!inStart || !inEnd) {
                continue;
             }

             //only shift if the region outside the shifted rows is not merged too
             if (!SheetUtil.containsCell(merged, startRow-1, 0) &&
                 !SheetUtil.containsCell(merged, endRow+1, 0)){
                 merged.setFirstRow(merged.getFirstRow()+n);
                 merged.setLastRow(merged.getLastRow()+n);
                 //have to remove/add it back
                 shiftedRegions.add(merged);
                 removeMergedRegion(i);
                 i = i -1; // we have to back up now since we removed one
             }
        }

        //read so it doesn't get shifted again
        Iterator<CellRangeAddress> iterator = shiftedRegions.iterator();
        while (iterator.hasNext()) {
            CellRangeAddress region = iterator.next();

            this.addMergedRegion(region);
        }
    }
>>>>>>> 76aa07461566a5976980e6696204781271955163

    /**
     * Shifts rows between startRow and endRow n number of rows.
     * If you use a negative number, it will shift rows up.
     * Code ensures that rows don't wrap around.
     *
     * Calls shiftRows(startRow, endRow, n, false, false);
     *
     * <p>
     * Additionally shifts merged regions that are completely defined in these
     * rows (ie. merged 2 cells on a row to be shifted).
     * @param startRow the row to start shifting
     * @param endRow the row to end shifting
     * @param n the number of rows to shift
     */
    public void shiftRows( int startRow, int endRow, int n ) {
<<<<<<< HEAD
		shiftRows(startRow, endRow, n, false, false);
=======
        shiftRows(startRow, endRow, n, false, false);
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * Shifts rows between startRow and endRow n number of rows.
     * If you use a negative number, it will shift rows up.
     * Code ensures that rows don't wrap around
     *
     * <p>
     * Additionally shifts merged regions that are completely defined in these
     * rows (ie. merged 2 cells on a row to be shifted).
     * <p>
     * TODO Might want to add bounds checking here
     * @param startRow the row to start shifting
     * @param endRow the row to end shifting
     * @param n the number of rows to shift
     * @param copyRowHeight whether to copy the row height during the shift
     * @param resetOriginalRowHeight whether to set the original row's height to the default
     */
<<<<<<< HEAD
    public void shiftRows( int startRow, int endRow, int n, boolean copyRowHeight, boolean resetOriginalRowHeight)
    {
        int s, e, inc;
        if ( n < 0 )
        {
            s = startRow;
            e = endRow;
            inc = 1;
        }
        else
        {
            s = endRow;
            e = startRow;
            inc = -1;
        }

        shiftMerged(startRow, endRow, n, true);
        sheet.shiftRowBreaks(startRow, endRow, n);
			
        for ( int rowNum = s; rowNum >= startRow && rowNum <= endRow && rowNum >= 0 && rowNum < 65536; rowNum += inc )
        {
            HSSFRow row = getRow( rowNum );
=======
    public void shiftRows( int startRow, int endRow, int n, boolean copyRowHeight, boolean resetOriginalRowHeight) {
        shiftRows(startRow, endRow, n, copyRowHeight, resetOriginalRowHeight, true);
    }

    /**
     * Shifts rows between startRow and endRow n number of rows.
     * If you use a negative number, it will shift rows up.
     * Code ensures that rows don't wrap around
     *
     * <p>
     * Additionally shifts merged regions that are completely defined in these
     * rows (ie. merged 2 cells on a row to be shifted).
     * <p>
     * TODO Might want to add bounds checking here
     * @param startRow the row to start shifting
     * @param endRow the row to end shifting
     * @param n the number of rows to shift
     * @param copyRowHeight whether to copy the row height during the shift
     * @param resetOriginalRowHeight whether to set the original row's height to the default
     * @param moveComments whether to move comments at the same time as the cells they are attached to
     */
    public void shiftRows(int startRow, int endRow, int n,
            boolean copyRowHeight, boolean resetOriginalRowHeight, boolean moveComments) {
        int s, inc;
        if (n < 0) {
            s = startRow;
            inc = 1;
        } else if (n > 0) {
            s = endRow;
            inc = -1;
        } else {
           // Nothing to do
           return;
        }
        
        NoteRecord[] noteRecs;
        if (moveComments) {
            noteRecs = _sheet.getNoteRecords();
        } else {
            noteRecs = NoteRecord.EMPTY_ARRAY;
        }

        shiftMerged(startRow, endRow, n, true);
        _sheet.getPageSettings().shiftRowBreaks(startRow, endRow, n);

        for ( int rowNum = s; rowNum >= startRow && rowNum <= endRow && rowNum >= 0 && rowNum < 65536; rowNum += inc ) {
            HSSFRow row = getRow( rowNum );
            // notify all cells in this row that we are going to shift them,
            // it can throw IllegalStateException if the operation is not allowed, for example,
            // if the row contains cells included in a multi-cell array formula
            if(row != null) notifyRowShifting(row);

>>>>>>> 76aa07461566a5976980e6696204781271955163
            HSSFRow row2Replace = getRow( rowNum + n );
            if ( row2Replace == null )
                row2Replace = createRow( rowNum + n );

<<<<<<< HEAD
            HSSFCell cell;




	    // Removes the cells before over writting them.
            for ( short col = row2Replace.getFirstCellNum(); col <= row2Replace.getLastCellNum(); col++ )
            {
                cell = row2Replace.getCell( col );
                if ( cell != null )
                    row2Replace.removeCell( cell );
            }
	    if (row == null) continue; // Nothing to do for this row
	    else {
		if (copyRowHeight) {
		    row2Replace.setHeight(row.getHeight());
		}

		if (resetOriginalRowHeight) {
		    row.setHeight((short)0xff);
		}
	    }
            for ( short col = row.getFirstCellNum(); col <= row.getLastCellNum(); col++ )
            {
                cell = row.getCell( col );
                if ( cell != null )
                {
                    row.removeCell( cell );
                    CellValueRecordInterface cellRecord = cell.getCellValueRecord();
                    cellRecord.setRow( rowNum + n );
                    row2Replace.createCellFromRecord( cellRecord );
                    sheet.addValueRecord( rowNum + n, cellRecord );
                }
            }
        }
        if ( endRow == lastrow || endRow + n > lastrow ) lastrow = Math.min( endRow + n, 65535 );
        if ( startRow == firstrow || startRow + n < firstrow ) firstrow = Math.max( startRow + n, 0 );
    }

    protected void insertChartRecords( List records )
    {
        int window2Loc = sheet.findFirstRecordLocBySid( WindowTwoRecord.sid );
        sheet.getRecords().addAll( window2Loc, records );
=======

            // Remove all the old cells from the row we'll
            //  be writing too, before we start overwriting
            //  any cells. This avoids issues with cells
            //  changing type, and records not being correctly
            //  overwritten
            row2Replace.removeAllCells();

            // If this row doesn't exist, nothing needs to
            //  be done for the now empty destination row
            if (row == null) continue; // Nothing to do for this row

            // Fix up row heights if required
            if (copyRowHeight) {
                row2Replace.setHeight(row.getHeight());
            }
            if (resetOriginalRowHeight) {
                row.setHeight((short)0xff);
            }

            // Copy each cell from the source row to
            //  the destination row
            for(Iterator<Cell> cells = row.cellIterator(); cells.hasNext(); ) {
                HSSFCell cell = (HSSFCell)cells.next();
                row.removeCell( cell );
                CellValueRecordInterface cellRecord = cell.getCellValueRecord();
                cellRecord.setRow( rowNum + n );
                row2Replace.createCellFromRecord( cellRecord );
                _sheet.addValueRecord( rowNum + n, cellRecord );

                HSSFHyperlink link = cell.getHyperlink();
                if(link != null){
                    link.setFirstRow(link.getFirstRow() + n);
                    link.setLastRow(link.getLastRow() + n);
                }
            }
            // Now zap all the cells in the source row
            row.removeAllCells();

            // Move comments from the source row to the
            //  destination row. Note that comments can
            //  exist for cells which are null
            if(moveComments) {
                // This code would get simpler if NoteRecords could be organised by HSSFRow.
                for(int i=noteRecs.length-1; i>=0; i--) {
                    NoteRecord nr = noteRecs[i];
                    if (nr.getRow() != rowNum) {
                        continue;
                    }
                    HSSFComment comment = getCellComment(rowNum, nr.getColumn());
                    if (comment != null) {
                       comment.setRow(rowNum + n);
                    }
                }
            }
        }
        
        // Re-compute the first and last rows of the sheet as needed
        if(n > 0) {
           // Rows are moving down
           if ( startRow == _firstrow ) {
              // Need to walk forward to find the first non-blank row
              _firstrow = Math.max( startRow + n, 0 );
              for( int i=startRow+1; i < startRow+n; i++ ) {
                 if (getRow(i) != null) {
                    _firstrow = i;
                    break;
                 }
              }
           }
           if ( endRow + n > _lastrow ) {
              _lastrow = Math.min( endRow + n, SpreadsheetVersion.EXCEL97.getLastRowIndex() );
           }
        } else {
           // Rows are moving up
           if ( startRow + n < _firstrow ) {
              _firstrow = Math.max( startRow + n, 0 );
           }
           if ( endRow == _lastrow  ) {
              // Need to walk backward to find the last non-blank row
              _lastrow = Math.min( endRow + n, SpreadsheetVersion.EXCEL97.getLastRowIndex() );
              for (int i=endRow-1; i > endRow+n; i++) {
                 if (getRow(i) != null) {
                    _lastrow = i;
                    break;
                 }
              }
           }
        }

        // Update any formulas on this sheet that point to
        //  rows which have been moved
        int sheetIndex = _workbook.getSheetIndex(this);
        short externSheetIndex = _book.checkExternSheet(sheetIndex);
        FormulaShifter shifter = FormulaShifter.createForRowShift(externSheetIndex, startRow, endRow, n);
        _sheet.updateFormulasAfterCellShift(shifter, externSheetIndex);

        int nSheets = _workbook.getNumberOfSheets();
        for(int i=0; i<nSheets; i++) {
            InternalSheet otherSheet = _workbook.getSheetAt(i).getSheet();
            if (otherSheet == this._sheet) {
                continue;
            }
            short otherExtSheetIx = _book.checkExternSheet(i);
            otherSheet.updateFormulasAfterCellShift(shifter, otherExtSheetIx);
        }
        _workbook.getWorkbook().updateNamesAfterCellShift(shifter);
    }

    protected void insertChartRecords(List<Record> records) {
        int window2Loc = _sheet.findFirstRecordLocBySid(WindowTwoRecord.sid);
        _sheet.getRecords().addAll(window2Loc, records);
    }

    private void notifyRowShifting(HSSFRow row){
        String msg = "Row[rownum="+row.getRowNum()+"] contains cell(s) included in a multi-cell array formula. " +
                "You cannot change part of an array.";
        for(Cell cell : row){
            HSSFCell hcell = (HSSFCell)cell;
            if(hcell.isPartOfArrayFormulaGroup()){
                hcell.notifyArrayFormulaChanging(msg);
            }
        }
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * Creates a split (freezepane). Any existing freezepane or split pane is overwritten.
<<<<<<< HEAD
     * @param colSplit      Horizonatal position of split.
     * @param rowSplit      Vertical position of split.
     * @param topRow        Top row visible in bottom pane
     * @param leftmostColumn   Left column visible in right pane.
     */
    public void createFreezePane(int colSplit, int rowSplit, int leftmostColumn, int topRow )
    {
        if (colSplit < 0 || colSplit > 255) throw new IllegalArgumentException("Column must be between 0 and 255");
        if (rowSplit < 0 || rowSplit > 65535) throw new IllegalArgumentException("Row must be between 0 and 65535");
=======
     *
     * <p>
     *     If both colSplit and rowSplit are zero then the existing freeze pane is removed
     * </p>
     *
     * @param colSplit      Horizonatal position of split.
     * @param rowSplit      Vertical position of split.
     * @param leftmostColumn   Left column visible in right pane.
     * @param topRow        Top row visible in bottom pane
     */
    public void createFreezePane(int colSplit, int rowSplit, int leftmostColumn, int topRow) {
        validateColumn(colSplit);
        validateRow(rowSplit);
>>>>>>> 76aa07461566a5976980e6696204781271955163
        if (leftmostColumn < colSplit) throw new IllegalArgumentException("leftmostColumn parameter must not be less than colSplit parameter");
        if (topRow < rowSplit) throw new IllegalArgumentException("topRow parameter must not be less than leftmostColumn parameter");
        getSheet().createFreezePane( colSplit, rowSplit, topRow, leftmostColumn );
    }

    /**
     * Creates a split (freezepane). Any existing freezepane or split pane is overwritten.
<<<<<<< HEAD
     * @param colSplit      Horizonatal position of split.
     * @param rowSplit      Vertical position of split.
     */
    public void createFreezePane( int colSplit, int rowSplit )
    {
        createFreezePane( colSplit, rowSplit, colSplit, rowSplit );
=======
     *
     * <p>
     *     If both colSplit and rowSplit are zero then the existing freeze pane is removed
     * </p>
     *
     * @param colSplit      Horizonatal position of split.
     * @param rowSplit      Vertical position of split.
     */
    public void createFreezePane(int colSplit, int rowSplit) {
        createFreezePane(colSplit, rowSplit, colSplit, rowSplit);
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * Creates a split pane. Any existing freezepane or split pane is overwritten.
     * @param xSplitPos      Horizonatal position of split (in 1/20th of a point).
     * @param ySplitPos      Vertical position of split (in 1/20th of a point).
     * @param topRow        Top row visible in bottom pane
     * @param leftmostColumn   Left column visible in right pane.
     * @param activePane    Active pane.  One of: PANE_LOWER_RIGHT,
     *                      PANE_UPPER_RIGHT, PANE_LOWER_LEFT, PANE_UPPER_LEFT
     * @see #PANE_LOWER_LEFT
     * @see #PANE_LOWER_RIGHT
     * @see #PANE_UPPER_LEFT
     * @see #PANE_UPPER_RIGHT
     */
<<<<<<< HEAD
    public void createSplitPane(int xSplitPos, int ySplitPos, int leftmostColumn, int topRow, int activePane )
    {
        getSheet().createSplitPane( xSplitPos, ySplitPos, topRow, leftmostColumn, activePane );
    }
    
=======
    public void createSplitPane(int xSplitPos, int ySplitPos, int leftmostColumn, int topRow, int activePane) {
        getSheet().createSplitPane( xSplitPos, ySplitPos, topRow, leftmostColumn, activePane );
    }

>>>>>>> 76aa07461566a5976980e6696204781271955163
    /**
     * Returns the information regarding the currently configured pane (split or freeze).
     * @return null if no pane configured, or the pane information.
     */
    public PaneInformation getPaneInformation() {
<<<<<<< HEAD
      return getSheet().getPaneInformation();
=======
        return getSheet().getPaneInformation();
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * Sets whether the gridlines are shown in a viewer.
     * @param show whether to show gridlines or not
     */
    public void setDisplayGridlines(boolean show) {
<<<<<<< HEAD
        sheet.setDisplayGridlines(show);
=======
        _sheet.setDisplayGridlines(show);
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * Returns if gridlines are displayed.
     * @return whether gridlines are displayed
     */
    public boolean isDisplayGridlines() {
<<<<<<< HEAD
	return sheet.isDisplayGridlines();
=======
    return _sheet.isDisplayGridlines();
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * Sets whether the formulas are shown in a viewer.
     * @param show whether to show formulas or not
     */
    public void setDisplayFormulas(boolean show) {
<<<<<<< HEAD
        sheet.setDisplayFormulas(show);
=======
        _sheet.setDisplayFormulas(show);
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * Returns if formulas are displayed.
     * @return whether formulas are displayed
     */
    public boolean isDisplayFormulas() {
<<<<<<< HEAD
    	return sheet.isDisplayFormulas();
=======
        return _sheet.isDisplayFormulas();
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * Sets whether the RowColHeadings are shown in a viewer.
     * @param show whether to show RowColHeadings or not
     */
    public void setDisplayRowColHeadings(boolean show) {
<<<<<<< HEAD
        sheet.setDisplayRowColHeadings(show);
=======
        _sheet.setDisplayRowColHeadings(show);
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * Returns if RowColHeadings are displayed.
     * @return whether RowColHeadings are displayed
     */
    public boolean isDisplayRowColHeadings() {
<<<<<<< HEAD
    	return sheet.isDisplayRowColHeadings();
    }
    
    /**
     * Sets a page break at the indicated row
     * @param row FIXME: Document this!
     */
    public void setRowBreak(int row) {
    	validateRow(row);
    	sheet.setRowBreak(row, (short)0, (short)255);
    }

    /**
     * Determines if there is a page break at the indicated row
     * @param row FIXME: Document this!
     * @return FIXME: Document this!
     */
    public boolean isRowBroken(int row) {
    	return sheet.isRowBroken(row);
    }
    
    /**
     * Removes the page break at the indicated row
     * @param row
     */
    public void removeRowBreak(int row) {
    	sheet.removeRowBreak(row);
    }
    
    /**
     * Retrieves all the horizontal page breaks
     * @return all the horizontal page breaks, or null if there are no row page breaks
     */
    public int[] getRowBreaks(){
    	//we can probably cache this information, but this should be a sparsely used function
    	int count = sheet.getNumRowBreaks();
    	if (count > 0) {
    	  int[] returnValue = new int[count];
    	  Iterator iterator = sheet.getRowBreaks();
    	  int i = 0;
    	  while (iterator.hasNext()) {
    		PageBreakRecord.Break breakItem = (PageBreakRecord.Break)iterator.next();
    		returnValue[i++] = (int)breakItem.main;
    	  }
    	  return returnValue;
    	}
    	return null;
    }

    /**
     * Retrieves all the vertical page breaks
     * @return all the vertical page breaks, or null if there are no column page breaks
     */
    public short[] getColumnBreaks(){
    	//we can probably cache this information, but this should be a sparsely used function 
    	int count = sheet.getNumColumnBreaks();
    	if (count > 0) {
    	  short[] returnValue = new short[count];
    	  Iterator iterator = sheet.getColumnBreaks();
    	  int i = 0;
    	  while (iterator.hasNext()) {
    		PageBreakRecord.Break breakItem = (PageBreakRecord.Break)iterator.next();
    		returnValue[i++] = breakItem.main;
    	  }
    	  return returnValue;
    	}
    	return null;
    }
    
    
    /**
     * Sets a page break at the indicated column
     * @param column
     */
    public void setColumnBreak(short column) {
    	validateColumn(column);
    	sheet.setColumnBreak(column, (short)0, (short)65535);
=======
        return _sheet.isDisplayRowColHeadings();
    }

    /**
     * Sets a page break at the indicated row
     * Breaks occur above the specified row and left of the specified column inclusive.
     *
     * For example, <code>sheet.setColumnBreak(2);</code> breaks the sheet into two parts
     * with columns A,B,C in the first and D,E,... in the second. Simuilar, <code>sheet.setRowBreak(2);</code>
     * breaks the sheet into two parts with first three rows (rownum=1...3) in the first part
     * and rows starting with rownum=4 in the second.
     *
     * @param row the row to break, inclusive
     */
    public void setRowBreak(int row) {
        validateRow(row);
        _sheet.getPageSettings().setRowBreak(row, (short)0, (short)255);
    }

    /**
     * @return <code>true</code> if there is a page break at the indicated row
     */
    public boolean isRowBroken(int row) {
        return _sheet.getPageSettings().isRowBroken(row);
    }

    /**
     * Removes the page break at the indicated row
     */
    public void removeRowBreak(int row) {
        _sheet.getPageSettings().removeRowBreak(row);
    }

    /**
     * @return row indexes of all the horizontal page breaks, never <code>null</code>
     */
    public int[] getRowBreaks() {
        //we can probably cache this information, but this should be a sparsely used function
        return _sheet.getPageSettings().getRowBreaks();
    }

    /**
     * @return column indexes of all the vertical page breaks, never <code>null</code>
     */
    public int[] getColumnBreaks() {
        //we can probably cache this information, but this should be a sparsely used function
        return _sheet.getPageSettings().getColumnBreaks();
    }


    /**
     * Sets a page break at the indicated column.
     * Breaks occur above the specified row and left of the specified column inclusive.
     *
     * For example, <code>sheet.setColumnBreak(2);</code> breaks the sheet into two parts
     * with columns A,B,C in the first and D,E,... in the second. Simuilar, <code>sheet.setRowBreak(2);</code>
     * breaks the sheet into two parts with first three rows (rownum=1...3) in the first part
     * and rows starting with rownum=4 in the second.
     *
     * @param column the column to break, inclusive
     */
    public void setColumnBreak(int column) {
        validateColumn((short)column);
        _sheet.getPageSettings().setColumnBreak((short)column, (short)0, (short) SpreadsheetVersion.EXCEL97.getLastRowIndex());
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * Determines if there is a page break at the indicated column
     * @param column FIXME: Document this!
     * @return FIXME: Document this!
     */
<<<<<<< HEAD
    public boolean isColumnBroken(short column) {
    	return sheet.isColumnBroken(column);
    }
    
=======
    public boolean isColumnBroken(int column) {
        return _sheet.getPageSettings().isColumnBroken(column);
    }

>>>>>>> 76aa07461566a5976980e6696204781271955163
    /**
     * Removes a page break at the indicated column
     * @param column
     */
<<<<<<< HEAD
    public void removeColumnBreak(short column) {
    	sheet.removeColumnBreak(column);
    }
    
=======
    public void removeColumnBreak(int column) {
        _sheet.getPageSettings().removeColumnBreak(column);
    }

>>>>>>> 76aa07461566a5976980e6696204781271955163
    /**
     * Runs a bounds check for row numbers
     * @param row
     */
    protected void validateRow(int row) {
<<<<<<< HEAD
    	if (row > 65535) throw new IllegalArgumentException("Maximum row number is 65535");
    	if (row < 0) throw new IllegalArgumentException("Minumum row number is 0");
    }
    
=======
        int maxrow = SpreadsheetVersion.EXCEL97.getLastRowIndex();
        if (row > maxrow) throw new IllegalArgumentException("Maximum row number is " + maxrow);
        if (row < 0) throw new IllegalArgumentException("Minumum row number is 0");
    }

>>>>>>> 76aa07461566a5976980e6696204781271955163
    /**
     * Runs a bounds check for column numbers
     * @param column
     */
<<<<<<< HEAD
    protected void validateColumn(short column) {
    	if (column > 255) throw new IllegalArgumentException("Maximum column number is 255");
    	if (column < 0)	throw new IllegalArgumentException("Minimum column number is 0");
=======
    protected void validateColumn(int column) {
        int maxcol = SpreadsheetVersion.EXCEL97.getLastColumnIndex();
        if (column > maxcol) throw new IllegalArgumentException("Maximum column number is " + maxcol);
        if (column < 0)    throw new IllegalArgumentException("Minimum column number is 0");
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * Aggregates the drawing records and dumps the escher record hierarchy
     * to the standard output.
     */
<<<<<<< HEAD
    public void dumpDrawingRecords(boolean fat)
    {
        sheet.aggregateDrawingRecords(book.getDrawingManager());

        EscherAggregate r = (EscherAggregate) getSheet().findFirstRecordBySid(EscherAggregate.sid);
        List escherRecords = r.getEscherRecords();
        PrintWriter w = new PrintWriter(System.out);
        for ( Iterator iterator = escherRecords.iterator(); iterator.hasNext(); )
        {
            EscherRecord escherRecord = (EscherRecord) iterator.next();
            if (fat)
                System.out.println(escherRecord.toString());
            else
                escherRecord.display(w, 0);
=======
    public void dumpDrawingRecords(boolean fat) {
        _sheet.aggregateDrawingRecords(_book.getDrawingManager(), false);

        EscherAggregate r = (EscherAggregate) getSheet().findFirstRecordBySid(EscherAggregate.sid);
        List<EscherRecord> escherRecords = r.getEscherRecords();
        PrintWriter w = new PrintWriter(System.out);
        for (Iterator<EscherRecord> iterator = escherRecords.iterator(); iterator.hasNext();) {
            EscherRecord escherRecord = iterator.next();
            if (fat) {
                System.out.println(escherRecord.toString());
            } else {
                escherRecord.display(w, 0);
            }
>>>>>>> 76aa07461566a5976980e6696204781271955163
        }
        w.flush();
    }

    /**
<<<<<<< HEAD
     * Creates the toplevel drawing patriarch.  This will have the effect of
     * removing any existing drawings on this sheet.
     *
     * @return  The new patriarch.
     */
    public HSSFPatriarch createDrawingPatriarch()
    {
        // Create the drawing group if it doesn't already exist.
        book.createDrawingGroup();

        sheet.aggregateDrawingRecords(book.getDrawingManager());
        EscherAggregate agg = (EscherAggregate) sheet.findFirstRecordBySid(EscherAggregate.sid);
        HSSFPatriarch patriarch = new HSSFPatriarch(this);
        agg.clear();     // Initially the behaviour will be to clear out any existing shapes in the sheet when
                         // creating a new patriarch.
        agg.setPatriarch(patriarch);
        return patriarch;
=======
     * Creates the top-level drawing patriarch.  This will have
     *  the effect of removing any existing drawings on this
     *  sheet.
     * This may then be used to add graphics or charts
     * @return  The new patriarch.
     */
    public HSSFPatriarch createDrawingPatriarch() {
        if(_patriarch == null){
            // Create the drawing group if it doesn't already exist.
            _workbook.initDrawings();

            if(_patriarch == null){
                _sheet.aggregateDrawingRecords(_book.getDrawingManager(), true);
                EscherAggregate agg = (EscherAggregate) _sheet.findFirstRecordBySid(EscherAggregate.sid);
                _patriarch = new HSSFPatriarch(this, agg);
                agg.setPatriarch(_patriarch);
            }
        }
        return _patriarch;
    }

    /**
     * Returns the agregate escher records for this sheet,
     *  it there is one.
     * WARNING - calling this will trigger a parsing of the
     *  associated escher records. Any that aren't supported
     *  (such as charts and complex drawing types) will almost
     *  certainly be lost or corrupted when written out.
     */
    public EscherAggregate getDrawingEscherAggregate() {
        _book.findDrawingGroup();

        // If there's now no drawing manager, then there's
        //  no drawing escher records on the workbook
        if(_book.getDrawingManager() == null) {
            return null;
        }

        int found = _sheet.aggregateDrawingRecords(
                _book.getDrawingManager(), false
        );
        if(found == -1) {
            // Workbook has drawing stuff, but this sheet doesn't
            return null;
        }

        // Grab our aggregate record, and wire it up
        EscherAggregate agg = (EscherAggregate) _sheet.findFirstRecordBySid(EscherAggregate.sid);
        return agg;
    }

    /**
     * Returns the top-level drawing patriach, if there is
     *  one.
     * This will hold any graphics or charts for the sheet.
     * WARNING - calling this will trigger a parsing of the
     *  associated escher records. Any that aren't supported
     *  (such as charts and complex drawing types) will almost
     *  certainly be lost or corrupted when written out. Only
     *  use this with simple drawings, otherwise call
     *  {@link HSSFSheet#createDrawingPatriarch()} and
     *  start from scratch!
     */
    public HSSFPatriarch getDrawingPatriarch() {
        if(_patriarch != null) return _patriarch;
        
        EscherAggregate agg = getDrawingEscherAggregate();
        if(agg == null) return null;

        _patriarch = new HSSFPatriarch(this, agg);
        agg.setPatriarch(_patriarch);

        // Have it process the records into high level objects
        //  as best it can do (this step may eat anything
        //  that isn't supported, you were warned...)
        agg.convertRecordsToUserModel();

        // Return what we could cope with
        return _patriarch;
    }

    /**
     * @deprecated (Sep 2008) use {@link #setColumnGroupCollapsed(int, boolean)}
     */
    public void setColumnGroupCollapsed(short columnNumber, boolean collapsed) {
        setColumnGroupCollapsed(columnNumber & 0xFFFF, collapsed);
    }
    /**
     * @deprecated (Sep 2008) use {@link #groupColumn(int, int)}
     */
    public void groupColumn(short fromColumn, short toColumn) {
        groupColumn(fromColumn & 0xFFFF, toColumn & 0xFFFF);
    }
    /**
     * @deprecated (Sep 2008) use {@link #ungroupColumn(int, int)}
     */
    public void ungroupColumn(short fromColumn, short toColumn) {
        ungroupColumn(fromColumn & 0xFFFF, toColumn & 0xFFFF);
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * Expands or collapses a column group.
     *
     * @param columnNumber      One of the columns in the group.
     * @param collapsed         true = collapse group, false = expand group.
     */
<<<<<<< HEAD
    public void setColumnGroupCollapsed( short columnNumber, boolean collapsed )
    {
        sheet.setColumnGroupCollapsed( columnNumber, collapsed );
=======
    public void setColumnGroupCollapsed(int columnNumber, boolean collapsed) {
        _sheet.setColumnGroupCollapsed(columnNumber, collapsed);
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * Create an outline for the provided column range.
     *
     * @param fromColumn        beginning of the column range.
     * @param toColumn          end of the column range.
     */
<<<<<<< HEAD
    public void groupColumn(short fromColumn, short toColumn)
    {
        sheet.groupColumnRange( fromColumn, toColumn, true );
    }

    public void ungroupColumn( short fromColumn, short toColumn )
    {
        sheet.groupColumnRange( fromColumn, toColumn, false );
    }

    public void groupRow(int fromRow, int toRow)
    {
        sheet.groupRowRange( fromRow, toRow, true );
    }

    public void ungroupRow(int fromRow, int toRow)
    {
        sheet.groupRowRange( fromRow, toRow, false );
    }

    public void setRowGroupCollapsed( int row, boolean collapse )
    {
        sheet.setRowGroupCollapsed( row, collapse );
=======
    public void groupColumn(int fromColumn, int toColumn) {
        _sheet.groupColumnRange(fromColumn, toColumn, true);
    }

    public void ungroupColumn(int fromColumn, int toColumn) {
        _sheet.groupColumnRange(fromColumn, toColumn, false);
    }

    /**
     * Tie a range of cell together so that they can be collapsed or expanded
     *
     * @param fromRow   start row (0-based)
     * @param toRow     end row (0-based)
     */
    public void groupRow(int fromRow, int toRow) {
        _sheet.groupRowRange(fromRow, toRow, true);
    }

    public void ungroupRow(int fromRow, int toRow) {
        _sheet.groupRowRange(fromRow, toRow, false);
    }

    public void setRowGroupCollapsed(int rowIndex, boolean collapse) {
        if (collapse) {
            _sheet.getRowsAggregate().collapseRow(rowIndex);
        } else {
            _sheet.getRowsAggregate().expandRow(rowIndex);
        }
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * Sets the default column style for a given column.  POI will only apply this style to new cells added to the sheet.
     *
     * @param column the column index
     * @param style the style to set
     */
<<<<<<< HEAD
    public void setDefaultColumnStyle(short column, HSSFCellStyle style) {
	sheet.setColumn(column, new Short(style.getIndex()), null, null, null, null);
=======
    public void setDefaultColumnStyle(int column, CellStyle style) {
        _sheet.setDefaultColumnStyle(column, ((HSSFCellStyle)style).getIndex());
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * Adjusts the column width to fit the contents.
     *
     * This process can be relatively slow on large sheets, so this should
     *  normally only be called once per column, at the end of your
     *  processing.
     *
     * @param column the column index
     */
<<<<<<< HEAD
    public void autoSizeColumn(short column) {
        AttributedString str;
        TextLayout layout;
        /**
         * Excel measures columns in units of 1/256th of a character width
         * but the docs say nothing about what particular character is used.
         * '0' looks to be a good choice.
         */
        char defaultChar = '0';
       
        /**
         * This is the multiple that the font height is scaled by when determining the
         * boundary of rotated text.
         */
        double fontHeightMultiple = 2.0;
       
        FontRenderContext frc = new FontRenderContext(null, true, true);

        HSSFWorkbook wb = new HSSFWorkbook(book);
        HSSFFont defaultFont = wb.getFontAt((short) 0);

        str = new AttributedString("" + defaultChar);
        copyAttributes(defaultFont, str, 0, 1);
        layout = new TextLayout(str.getIterator(), frc);
        int defaultCharWidth = (int)layout.getAdvance();

        double width = -1;
        for (Iterator it = rowIterator(); it.hasNext();) {
            HSSFRow row = (HSSFRow) it.next();
            HSSFCell cell = row.getCell(column);
            if (cell == null) continue;

            HSSFCellStyle style = cell.getCellStyle();
            HSSFFont font = wb.getFontAt(style.getFontIndex());

            if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                HSSFRichTextString rt = cell.getRichStringCellValue();
                String[] lines = rt.getString().split("\\n");
                for (int i = 0; i < lines.length; i++) {
                    String txt = lines[i] + defaultChar;
                    str = new AttributedString(txt);
                    copyAttributes(font, str, 0, txt.length());

                    if (rt.numFormattingRuns() > 0) {
                        for (int j = 0; j < lines[i].length(); j++) {
                            int idx = rt.getFontAtIndex(j);
                            if (idx != 0) {
                                HSSFFont fnt = wb.getFontAt((short) idx);
                                copyAttributes(fnt, str, j, j + 1);
                            }
                        }
                    }

                    layout = new TextLayout(str.getIterator(), frc);
                    if(style.getRotation() != 0){
                        /*
                         * Transform the text using a scale so that it's height is increased by a multiple of the leading,
                         * and then rotate the text before computing the bounds. The scale results in some whitespace around
                         * the unrotated top and bottom of the text that normally wouldn't be present if unscaled, but
                         * is added by the standard Excel autosize.
                         */
                        AffineTransform trans = new AffineTransform();
                        trans.concatenate(AffineTransform.getRotateInstance(style.getRotation()*2.0*Math.PI/360.0));
                        trans.concatenate(
                        AffineTransform.getScaleInstance(1, fontHeightMultiple)
                        );
                        width = Math.max(width, layout.getOutline(trans).getBounds().getWidth() / defaultCharWidth);
                    } else {
                        width = Math.max(width, layout.getBounds().getWidth() / defaultCharWidth);
                    }
                }
            } else {
                String sval = null;
                if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
                    HSSFDataFormat dataformat = wb.createDataFormat();
                    short idx = style.getDataFormat();
                    String format = dataformat.getFormat(idx).replaceAll("\"", "");
                    double value = cell.getNumericCellValue();
                    try {
                        NumberFormat fmt;
                        if ("General".equals(format))
                            sval = "" + value;
                        else
                        {
                            fmt = new DecimalFormat(format);
                            sval = fmt.format(value);
                        }
                    } catch (Exception e) {
                        sval = "" + value;
                    }
                } else if (cell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN) {
                    sval = String.valueOf(cell.getBooleanCellValue());
                }

                String txt = sval + defaultChar;
                str = new AttributedString(txt);
                copyAttributes(font, str, 0, txt.length());

                layout = new TextLayout(str.getIterator(), frc);
                if(style.getRotation() != 0){
                    /*
                     * Transform the text using a scale so that it's height is increased by a multiple of the leading,
                     * and then rotate the text before computing the bounds. The scale results in some whitespace around
                     * the unrotated top and bottom of the text that normally wouldn't be present if unscaled, but
                     * is added by the standard Excel autosize.
                     */
                    AffineTransform trans = new AffineTransform();
                    trans.concatenate(AffineTransform.getRotateInstance(style.getRotation()*2.0*Math.PI/360.0));
                    trans.concatenate(
                    AffineTransform.getScaleInstance(1, fontHeightMultiple)
                    );
                    width = Math.max(width, layout.getOutline(trans).getBounds().getWidth() / defaultCharWidth);
                } else {
                    width = Math.max(width, layout.getBounds().getWidth() / defaultCharWidth);
                }
            }

            if (width != -1) {
                if (width > Short.MAX_VALUE) { //width can be bigger that Short.MAX_VALUE!
                     width = Short.MAX_VALUE;
                }
                sheet.setColumnWidth(column, (short) (width * 256));
            }
        }
    }

    /**
     * Copy text attributes from the supplied HSSFFont to Java2D AttributedString
     */
    private void copyAttributes(HSSFFont font, AttributedString str, int startIdx, int endIdx){
        str.addAttribute(TextAttribute.FAMILY, font.getFontName(), startIdx, endIdx);
        str.addAttribute(TextAttribute.SIZE, new Float(font.getFontHeightInPoints()));
        if (font.getBoldweight() == HSSFFont.BOLDWEIGHT_BOLD) str.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD, startIdx, endIdx);
        if (font.getItalic() ) str.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE, startIdx, endIdx);
        if (font.getUnderline() == HSSFFont.U_SINGLE ) str.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, startIdx, endIdx);
=======
    public void autoSizeColumn(int column) {
        autoSizeColumn(column, false);
    }

    /**
     * Adjusts the column width to fit the contents.
     *
     * This process can be relatively slow on large sheets, so this should
     *  normally only be called once per column, at the end of your
     *  processing.
     *
     * You can specify whether the content of merged cells should be considered or ignored.
     *  Default is to ignore merged cells.
     *
     * @param column the column index
     * @param useMergedCells whether to use the contents of merged cells when calculating the width of the column
     */
    public void autoSizeColumn(int column, boolean useMergedCells) {
        double width = SheetUtil.getColumnWidth(this, column, useMergedCells);

        if (width != -1) {
            width *= 256;
            int maxColumnWidth = 255*256; // The maximum column width for an individual cell is 255 characters
            if (width > maxColumnWidth) {
                width = maxColumnWidth;
            }
            setColumnWidth(column, (int)(width));
        }

>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    /**
     * Returns cell comment for the specified row and column
     *
     * @return cell comment or <code>null</code> if not found
     */
<<<<<<< HEAD
     public HSSFComment getCellComment(int row, int column){
        return HSSFCell.findCellComment(sheet, row, column);
=======
     public HSSFComment getCellComment(int row, int column) {
        // Don't call findCellComment directly, otherwise
        //  two calls to this method will result in two
        //  new HSSFComment instances, which is bad
        HSSFRow r = getRow(row);
        if(r != null) {
            HSSFCell c = r.getCell(column);
            if(c != null) {
                return c.getCellComment();
            }
            // No cell, so you will get new
            //  objects every time, sorry...
            return HSSFCell.findCellComment(_sheet, row, column);
        }
        return null;
    }

    public HSSFSheetConditionalFormatting getSheetConditionalFormatting() {
        return new HSSFSheetConditionalFormatting(this);
    }

    /**
     * Returns the name of this sheet
     *
     * @return the name of this sheet
     */
    public String getSheetName() {
        HSSFWorkbook wb = getWorkbook();
        int idx = wb.getSheetIndex(this);
        return wb.getSheetName(idx);
    }

    /**
     * Also creates cells if they don't exist
     */
    private CellRange<HSSFCell> getCellRange(CellRangeAddress range) {
        int firstRow = range.getFirstRow();
        int firstColumn = range.getFirstColumn();
        int lastRow = range.getLastRow();
        int lastColumn = range.getLastColumn();
        int height = lastRow - firstRow + 1;
        int width = lastColumn - firstColumn + 1;
        List<HSSFCell> temp = new ArrayList<HSSFCell>(height*width);
        for (int rowIn = firstRow; rowIn <= lastRow; rowIn++) {
            for (int colIn = firstColumn; colIn <= lastColumn; colIn++) {
                HSSFRow row = getRow(rowIn);
                if (row == null) {
                    row = createRow(rowIn);
                }
                HSSFCell cell = row.getCell(colIn);
                if (cell == null) {
                    cell = row.createCell(colIn);
                }
                temp.add(cell);
            }
        }
        return SSCellRange.create(firstRow, firstColumn, height, width, temp, HSSFCell.class);
    }

    public CellRange<HSSFCell> setArrayFormula(String formula, CellRangeAddress range) {
        // make sure the formula parses OK first
        int sheetIndex = _workbook.getSheetIndex(this);
        Ptg[] ptgs = HSSFFormulaParser.parse(formula, _workbook, FormulaType.ARRAY, sheetIndex);
        CellRange<HSSFCell> cells = getCellRange(range);

        for (HSSFCell c : cells) {
            c.setCellArrayFormula(range);
        }
        HSSFCell mainArrayFormulaCell = cells.getTopLeftCell();
        FormulaRecordAggregate agg = (FormulaRecordAggregate)mainArrayFormulaCell.getCellValueRecord();
        agg.setArrayFormula(range, ptgs);
        return cells;
    }


    public CellRange<HSSFCell> removeArrayFormula(Cell cell) {
        if (cell.getSheet() != this) {
            throw new IllegalArgumentException("Specified cell does not belong to this sheet.");
        }
        CellValueRecordInterface rec = ((HSSFCell) cell).getCellValueRecord();
        if (!(rec instanceof FormulaRecordAggregate)) {
            String ref = new CellReference(cell).formatAsString();
            throw new IllegalArgumentException("Cell " + ref + " is not part of an array formula.");
        }
        FormulaRecordAggregate fra = (FormulaRecordAggregate) rec;
        CellRangeAddress range = fra.removeArrayFormula(cell.getRowIndex(), cell.getColumnIndex());

        CellRange<HSSFCell> result = getCellRange(range);
        // clear all cells in the range
        for (Cell c : result) {
            c.setCellType(Cell.CELL_TYPE_BLANK);
        }
        return result;
    }

	public DataValidationHelper getDataValidationHelper() {
		return new HSSFDataValidationHelper(this);
	}
    
    public HSSFAutoFilter setAutoFilter(CellRangeAddress range) {


        InternalWorkbook workbook = _workbook.getWorkbook();
        int sheetIndex = _workbook.getSheetIndex(this);

        NameRecord name = workbook.getSpecificBuiltinRecord(NameRecord.BUILTIN_FILTER_DB, sheetIndex+1);

        if (name == null) {
            name = workbook.createBuiltInName(NameRecord.BUILTIN_FILTER_DB, sheetIndex+1);
        }

        // The built-in name must consist of a single Area3d Ptg.
        Area3DPtg ptg = new Area3DPtg(range.getFirstRow(), range.getLastRow(),
                range.getFirstColumn(), range.getLastColumn(),
                false, false, false, false, sheetIndex);
        name.setNameDefinition(new Ptg[]{ptg});

        AutoFilterInfoRecord r = new AutoFilterInfoRecord();
        // the number of columns that have AutoFilter enabled.
        int numcols = 1 + range.getLastColumn() - range.getFirstColumn();
        r.setNumEntries((short)numcols);
        int idx = _sheet.findFirstRecordLocBySid(DimensionsRecord.sid);
        _sheet.getRecords().add(idx, r);

        //create a combobox control for each column
        HSSFPatriarch p = createDrawingPatriarch();
        for(int col = range.getFirstColumn(); col <= range.getLastColumn(); col++){
            p.createComboBox(new HSSFClientAnchor(0,0,0,0,
                    (short)col, range.getFirstRow(), (short)(col+1), range.getFirstRow()+1));
        }
        
        return new HSSFAutoFilter(this);
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

}

