/*************************************************************
 *  (C) Copyright 2011, 2012 James Talbut.
 *  jim-emitters@spudsoft.co.uk
 * 
 *  This file is part of The SpudSoft BIRT Excel Emitters.
 *  The SpudSoft BIRT Excel Emitters are free software: you can 
 *  redistribute them and/or modify them under the terms of the 
 *  GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  The SpudSoft BIRT Excel Emitters are distributed in the hope 
 *  that they will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or 
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with the SpudSoft BIRT Excel Emitters.
 *  If not, see <http://www.gnu.org/licenses/>.
 * 
 *************************************************************/
package uk.co.spudsoft.birt.emitters.excel;

import java.util.Iterator;

import org.apache.poi.hssf.util.PaneInformation;
import org.apache.poi.ss.usermodel.AutoFilter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellRange;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

public class FilteredSheet implements Sheet {
	
	private Sheet sheet;
	private int minRow;
	private int maxRow;
	
	public FilteredSheet(Sheet sheet, int minRow, int maxRow) {
		this.sheet = sheet;
		this.minRow = minRow;
		this.maxRow = maxRow;
	}
	
	private class FilteredIterator implements Iterator<Row> {

		private Iterator<Row> iter;
		private int minRow;
		private int maxRow;
		private Row cur;
		
		public FilteredIterator(Iterator<Row> iter, int minRow, int maxRow) {
			this.iter = iter;
			this.minRow = minRow;
			this.maxRow = maxRow;
		}

		public boolean hasNext() {
			if( iter.hasNext() ) {
				cur = iter.next();
				while( cur.getRowNum() < minRow ) {
					if( ! iter.hasNext() ) {
						return false;
					}
					cur = iter.next();
				}
				if( cur.getRowNum() > maxRow ) {
					return false;
				}
				return true;
			}
			return false;
		}

		public Row next() {
			return cur;
		}

		public void remove() {
			iter.remove();
		}
		
	}

	public Iterator<Row> iterator() {
		return rowIterator();
	}

	public Row createRow(int rownum) {
		return sheet.createRow(rownum);
	}

	public void removeRow(Row row) {
		sheet.removeRow(row);
	}


	public Row getRow(int rownum) {
		return sheet.getRow(rownum);
	}


	public int getPhysicalNumberOfRows() {
		return sheet.getPhysicalNumberOfRows();
	}


	public int getFirstRowNum() {
		return Math.max(minRow, sheet.getFirstRowNum());
	}


	public int getLastRowNum() {
		return Math.min(maxRow, sheet.getLastRowNum());
	}


	public void setColumnHidden(int columnIndex, boolean hidden) {
		sheet.setColumnHidden(columnIndex, hidden);
	}


	public boolean isColumnHidden(int columnIndex) {
		return sheet.isColumnHidden(columnIndex);
	}


	public void setRightToLeft(boolean value) {
		sheet.setRightToLeft(value);
	}


	public boolean isRightToLeft() {
		return sheet.isRightToLeft();
	}


	public void setColumnWidth(int columnIndex, int width) {
		sheet.setColumnWidth(columnIndex, width);
	}


	public int getColumnWidth(int columnIndex) {
		return sheet.getColumnWidth(columnIndex);
	}


	public void setDefaultColumnWidth(int width) {
		sheet.setDefaultColumnWidth(width);
	}


	public int getDefaultColumnWidth() {
		return sheet.getDefaultColumnWidth();
	}


	public short getDefaultRowHeight() {
		return sheet.getDefaultRowHeight();
	}


	public float getDefaultRowHeightInPoints() {
		return sheet.getDefaultRowHeightInPoints();
	}


	public void setDefaultRowHeight(short height) {
		sheet.setDefaultRowHeight(height);
	}


	public void setDefaultRowHeightInPoints(float height) {
		sheet.setDefaultRowHeightInPoints(height);
	}


	public CellStyle getColumnStyle(int column) {
		return sheet.getColumnStyle(column);
	}


	public int addMergedRegion(CellRangeAddress region) {
		return sheet.addMergedRegion(region);
	}


	public void setVerticallyCenter(boolean value) {
		sheet.setVerticallyCenter(value);
	}


	public void setHorizontallyCenter(boolean value) {
		sheet.setHorizontallyCenter(value);
	}


	public boolean getHorizontallyCenter() {
		return sheet.getHorizontallyCenter();
	}


	public boolean getVerticallyCenter() {
		return sheet.getVerticallyCenter();
	}


	public void removeMergedRegion(int index) {
		sheet.removeMergedRegion(index);
	}


	public int getNumMergedRegions() {
		return sheet.getNumMergedRegions();
	}


	public CellRangeAddress getMergedRegion(int index) {
		return sheet.getMergedRegion(index);
	}


	public Iterator<Row> rowIterator() {
		return new FilteredIterator( sheet.rowIterator(), minRow, maxRow ); 
	}


	public void setForceFormulaRecalculation(boolean value) {
		sheet.setForceFormulaRecalculation(value);
	}


	public boolean getForceFormulaRecalculation() {
		return sheet.getForceFormulaRecalculation();
	}


	public void setAutobreaks(boolean value) {
		sheet.setAutobreaks(value);
	}


	public void setDisplayGuts(boolean value) {
		sheet.setDisplayGuts(value);
	}


	public void setDisplayZeros(boolean value) {
		sheet.setDisplayZeros(value);
	}


	public boolean isDisplayZeros() {
		return sheet.isDisplayZeros();
	}


	public void setFitToPage(boolean value) {
		sheet.setFitToPage(value);
	}


	public void setRowSumsBelow(boolean value) {
		sheet.setRowSumsBelow(value);
	}


	public void setRowSumsRight(boolean value) {
		sheet.setRowSumsRight(value);
	}


	public boolean getAutobreaks() {
		return sheet.getAutobreaks();
	}


	public boolean getDisplayGuts() {
		return sheet.getDisplayGuts();
	}


	public boolean getFitToPage() {
		return sheet.getFitToPage();
	}


	public boolean getRowSumsBelow() {
		return sheet.getRowSumsBelow();
	}


	public boolean getRowSumsRight() {
		return sheet.getRowSumsRight();
	}


	public boolean isPrintGridlines() {
		return sheet.isPrintGridlines();
	}


	public void setPrintGridlines(boolean show) {
		sheet.setPrintGridlines(show);
	}


	public PrintSetup getPrintSetup() {
		return sheet.getPrintSetup();
	}


	public Header getHeader() {
		return sheet.getHeader();
	}


	public Footer getFooter() {
		return sheet.getFooter();
	}


	public void setSelected(boolean value) {
		sheet.setSelected(value);
	}


	public double getMargin(short margin) {
		return sheet.getMargin(margin);
	}


	public void setMargin(short margin, double size) {
		sheet.setMargin(margin, size);
	}


	public boolean getProtect() {
		return sheet.getProtect();
	}


	public void protectSheet(String password) {
		sheet.protectSheet(password);
	}


	public boolean getScenarioProtect() {
		return sheet.getScenarioProtect();
	}


	public void setZoom(int numerator, int denominator) {
		sheet.setZoom(numerator, denominator);
	}


	public short getTopRow() {
		return sheet.getTopRow();
	}


	public short getLeftCol() {
		return sheet.getLeftCol();
	}


	public void showInPane(short toprow, short leftcol) {
		sheet.showInPane(toprow, leftcol);
	}


	public void shiftRows(int startRow, int endRow, int n) {
		sheet.shiftRows(startRow, endRow, n);
	}


	public void shiftRows(int startRow, int endRow, int n,
			boolean copyRowHeight, boolean resetOriginalRowHeight) {
		sheet.shiftRows(startRow, endRow, n, copyRowHeight, resetOriginalRowHeight);
	}


	public void createFreezePane(int colSplit, int rowSplit,
			int leftmostColumn, int topRow) {
		sheet.createFreezePane(colSplit, rowSplit, leftmostColumn, topRow);
	}


	public void createFreezePane(int colSplit, int rowSplit) {
		sheet.createFreezePane(colSplit, rowSplit);
	}


	public void createSplitPane(int xSplitPos, int ySplitPos,
			int leftmostColumn, int topRow, int activePane) {
		sheet.createSplitPane(xSplitPos, ySplitPos, leftmostColumn, topRow, activePane);
	}


	public PaneInformation getPaneInformation() {
		return sheet.getPaneInformation();
	}


	public void setDisplayGridlines(boolean show) {
		sheet.setDisplayGridlines(show);
	}


	public boolean isDisplayGridlines() {
		// TODO Auto-generated method stub
		return sheet.isDisplayGridlines();
	}


	public void setDisplayFormulas(boolean show) {
		sheet.setDisplayFormulas(show);
	}


	public boolean isDisplayFormulas() {
		return sheet.isDisplayFormulas();
	}


	public void setDisplayRowColHeadings(boolean show) {
		sheet.setDisplayRowColHeadings(show);
	}


	public boolean isDisplayRowColHeadings() {
		return sheet.isDisplayRowColHeadings();
	}


	public void setRowBreak(int row) {
		sheet.setRowBreak(row);
	}


	public boolean isRowBroken(int row) {
		return sheet.isRowBroken(row);
	}


	public void removeRowBreak(int row) {
		sheet.removeRowBreak(row);
	}


	public int[] getRowBreaks() {
		return sheet.getRowBreaks();
	}


	public int[] getColumnBreaks() {
		return sheet.getColumnBreaks();
	}


	public void setColumnBreak(int column) {
		sheet.setColumnBreak(column);
	}


	public boolean isColumnBroken(int column) {
		// TODO Auto-generated method stub
		return sheet.isColumnBroken(column);
	}


	public void removeColumnBreak(int column) {
		sheet.removeColumnBreak(column);
	}


	public void setColumnGroupCollapsed(int columnNumber, boolean collapsed) {
		sheet.setColumnGroupCollapsed(columnNumber, collapsed);
	}


	public void groupColumn(int fromColumn, int toColumn) {
		sheet.groupColumn(fromColumn, toColumn);
	}


	public void ungroupColumn(int fromColumn, int toColumn) {
		sheet.ungroupColumn(fromColumn, toColumn);
	}


	public void groupRow(int fromRow, int toRow) {
		sheet.groupRow(fromRow, toRow);
	}


	public void ungroupRow(int fromRow, int toRow) {
		sheet.ungroupRow(fromRow, toRow);
	}


	public void setRowGroupCollapsed(int row, boolean collapse) {
		sheet.setRowGroupCollapsed(row, collapse);
	}


	public void setDefaultColumnStyle(int column, CellStyle style) {
		sheet.setDefaultColumnStyle(column, style);
	}


	public void autoSizeColumn(int column) {
		sheet.autoSizeColumn(column);
	}


	public void autoSizeColumn(int column, boolean useMergedCells) {
		sheet.autoSizeColumn(column, useMergedCells);
	}


	public Comment getCellComment(int row, int column) {
		return sheet.getCellComment(row, column);
	}


	public Drawing createDrawingPatriarch() {
		return sheet.createDrawingPatriarch();
	}


	public Workbook getWorkbook() {
		return sheet.getWorkbook();
	}


	public String getSheetName() {
		return sheet.getSheetName();
	}


	public boolean isSelected() {
		return sheet.isSelected();
	}


	public CellRange<? extends Cell> setArrayFormula(String formula,
			CellRangeAddress range) {
		return sheet.setArrayFormula(formula, range);
	}


	public CellRange<? extends Cell> removeArrayFormula(Cell cell) {
		return sheet.removeArrayFormula(cell);
	}


	public DataValidationHelper getDataValidationHelper() {
		return sheet.getDataValidationHelper();
	}


	public void addValidationData(DataValidation dataValidation) {
		sheet.addValidationData(dataValidation);
	}


	public AutoFilter setAutoFilter(CellRangeAddress range) {
		return sheet.setAutoFilter(range);
	}


	public SheetConditionalFormatting getSheetConditionalFormatting() {
		return sheet.getSheetConditionalFormatting();
	}

}

