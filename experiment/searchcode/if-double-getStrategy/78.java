/*
 * Copyright 2011 Kim Lindhardt Madsen
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dk.lindhardt.gwt.geie.server;

import com.google.gwt.user.server.Base64Utils;
import dk.lindhardt.gwt.geie.server.convert.CellConverterStrategy;
import dk.lindhardt.gwt.geie.server.convert.CellConverterStrategyRegister;
import dk.lindhardt.gwt.geie.shared.*;
import dk.lindhardt.gwt.geie.shared.LabelCell;
import jxl.Cell;
import jxl.*;
import jxl.biff.FormatRecord;
import jxl.format.*;
import jxl.format.CellFormat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * User: AnAmuser
 * Date: 28-05-11
 * <p/>
 * Class for building sheets in a {@link TableLayout} from a {@link jxl.write.WritableWorkbook}
 */
@SuppressWarnings({"JavaDoc", "UnusedDeclaration"})
public class Excel2TableLayout {

   private Workbook workbook;
   private TableLayout layout;

   private Map<CellFormat, dk.lindhardt.gwt.geie.shared.CellFormat> cellFormatMap =
         new HashMap<CellFormat, dk.lindhardt.gwt.geie.shared.CellFormat>();
   private Sheet sheet;

   /**
    * New instance of a table builder.
    * @param workbook an excel workbook
    */
   public Excel2TableLayout(Workbook workbook) {
      this.workbook = workbook;
      layout = new TableLayout();
      layout.addCellFormat(
            "default",
            new dk.lindhardt.gwt.geie.shared.CellFormat());
   }

   /**
    * Builds the {@link TableLayout} from an excel sheet
    * @param sheetIndex index of the sheet in the workbook to convert
    * @return the {@link TableLayout}
    */
   public TableLayout build(int sheetIndex) {
      sheet = workbook.getSheet(sheetIndex);
      createColumnWidths(sheet);
      createRowHeights(sheet);
      createCells(sheet);
      createHyperlinks(sheet);
      createImages(sheet);
      setFormatsOnTableLayout();

      return layout;
   }

   private void createImages(Sheet sheet) {
      for (int i = 0; i < sheet.getNumberOfImages(); i++) {
         jxl.Image image = sheet.getDrawing(i);
         String base64Data = Base64Utils.toBase64(image.getImageData()).replace("_", "/").replace("$", "+");
         layout.addImage(
               new dk.lindhardt.gwt.geie.shared.Image(
                     base64Data, image.getColumn(), image.getRow(), image.getWidth(), image.getHeight()));
      }
   }

   private void createHyperlinks(Sheet sheet) {
      Hyperlink[] hyperlinks = sheet.getHyperlinks();
      for (Hyperlink hyperlink : hyperlinks) {
         Link link = null;
         if (hyperlink.isURL()) {
            String destination = hyperlink.getURL().toExternalForm();
            int index = destination.indexOf('\u0000');
            if (index > 0) {
               destination = destination.substring(0, index);
            }
            link = new Link(destination);
         }
         dk.lindhardt.gwt.geie.shared.Cell cell = layout.getCell(hyperlink.getRow(), hyperlink.getColumn());
         cell.setLink(link);
      }
   }

   private void createCells(Sheet sheet) {
      int columns = sheet.getColumns();
      int rows = sheet.getRows();
      for (int c = 0; c < columns; c++) {
         for (int r = 0; r < rows; r++) {
            Cell cell = sheet.getCell(c, r);
            prepareCellFormat(cell);
            Range range = getRange(sheet, cell);
            if (range != null) {
               Cell topLeft = range.getTopLeft();
               Cell bottomRight = range.getBottomRight();
               createTableCell(topLeft, bottomRight, cell);
            } else {
               createTableCell(cell);
            }
         }
      }
   }

   /**
    * Moves the created formats to the table layout
    */
   private void setFormatsOnTableLayout() {
      for (dk.lindhardt.gwt.geie.shared.CellFormat cellFormat : cellFormatMap.values()) {
         layout.addCellFormat(cellFormat.toString(), cellFormat);
      }
   }

   /**
    * Creates format if not already created
    */
   private void prepareCellFormat(Cell cell) {
      CellFormat cellFormat = cell.getCellFormat();
      if (cellFormat != null && cellFormatMap.get(cellFormat) == null) {
         dk.lindhardt.gwt.geie.shared.CellFormat tableCellFormat = new dk.lindhardt.gwt.geie.shared.CellFormat();
         setBorder(cellFormat, tableCellFormat);
         setBackgroundColor(cellFormat, tableCellFormat);
         setPattern(cellFormat, cell.getType(), tableCellFormat);
         Font font = cellFormat.getFont();
         tableCellFormat.setUnderlined(UnderlineStyle.SINGLE.equals(font.getUnderlineStyle()));
         tableCellFormat.setFontSize(font.getPointSize());
         setAlignment(cellFormat, cell.getType(), tableCellFormat);
         setVAlignment(cellFormat, tableCellFormat);
         if (font.getBoldWeight() == 0x2bc) {
            tableCellFormat.setBold(true);
         }
         setColor(cellFormat, tableCellFormat);
         tableCellFormat.setItalic(font.isItalic());
         setFont(cellFormat, tableCellFormat);
         cellFormatMap.put(cellFormat, tableCellFormat);
      }
   }

   private void setVAlignment(CellFormat cellFormat, dk.lindhardt.gwt.geie.shared.CellFormat tableCellFormat) {
      VerticalAlignment verticalAlignment = cellFormat.getVerticalAlignment();
      if (VerticalAlignment.BOTTOM.equals(verticalAlignment)) {
         tableCellFormat.setValignment(dk.lindhardt.gwt.geie.shared.CellFormat.VAlignment.BOTTOM);
      } else if (VerticalAlignment.CENTRE.equals(verticalAlignment)) {
         tableCellFormat.setValignment(dk.lindhardt.gwt.geie.shared.CellFormat.VAlignment.CENTER);
      } else if (VerticalAlignment.TOP.equals(verticalAlignment)) {
         tableCellFormat.setValignment(dk.lindhardt.gwt.geie.shared.CellFormat.VAlignment.TOP);
      }
   }

   private void setPattern(
         jxl.format.CellFormat cellFormat,
         CellType cellType,
         dk.lindhardt.gwt.geie.shared.CellFormat tableCellFormat) {
      if (cellFormat instanceof FormatRecord) {
         if (cellType.equals(CellType.DATE)) {
            DateFormat dateFormat = ((FormatRecord) cellFormat).getDateFormat();
            if (dateFormat instanceof SimpleDateFormat) {
               tableCellFormat.setPattern(((SimpleDateFormat) dateFormat).toPattern());
            }
         }
      }
      if (cellType.equals(CellType.NUMBER) || cellType.equals(CellType.NUMBER_FORMULA)) {
         String formatString = cellFormat.getFormat().getFormatString();
         formatString = escapeFormat(formatString);
         tableCellFormat.setPattern(formatString);
      }
   }

   /**
    * Escapes the excel format best possible
    */
   private String escapeFormat(String formatString) {
      formatString = formatString.replaceAll("\\\\\\)", ")").replaceAll("\\\\\\(", "(");
      formatString = formatString.replaceAll("_\\(", "").replaceAll("_\\)", "");
      return formatString;
   }

   /**
    * Sets the cell font
    */
   private void setFont(jxl.format.CellFormat cellFormat, dk.lindhardt.gwt.geie.shared.CellFormat tableCellFormat) {
      String fontName = cellFormat.getFont().getName();
      if (fontName.toLowerCase().equals("arial")) {
         tableCellFormat.setFont(dk.lindhardt.gwt.geie.shared.CellFormat.Font.ARIAL);
      } else if (fontName.toLowerCase().equals("tahoma")) {
         tableCellFormat.setFont(dk.lindhardt.gwt.geie.shared.CellFormat.Font.TAHOMA);
      } else if (fontName.toLowerCase().equals("times new roman")) {
         tableCellFormat.setFont(dk.lindhardt.gwt.geie.shared.CellFormat.Font.TIMES);
      }
   }

   /**
    * Sets the cell alignment
    */
   private void setAlignment(
         jxl.format.CellFormat cellFormat,
         CellType cellType,
         dk.lindhardt.gwt.geie.shared.CellFormat tableCellFormat) {
      Alignment alignment = cellFormat.getAlignment();
      if (alignment.equals(Alignment.LEFT)) {
         tableCellFormat.setAlignment(dk.lindhardt.gwt.geie.shared.CellFormat.Alignment.LEFT);
      } else if (alignment.equals(Alignment.CENTRE)) {
         tableCellFormat.setAlignment(dk.lindhardt.gwt.geie.shared.CellFormat.Alignment.CENTER);
      } else if (alignment.equals(Alignment.RIGHT)) {
         tableCellFormat.setAlignment(dk.lindhardt.gwt.geie.shared.CellFormat.Alignment.RIGHT);
      } else if (alignment.equals(Alignment.GENERAL)) {
         if (cellType.equals(CellType.NUMBER) || cellType.equals(CellType.NUMBER_FORMULA)) {
            tableCellFormat.setAlignment(dk.lindhardt.gwt.geie.shared.CellFormat.Alignment.RIGHT);
         } else if (cellType.equals(CellType.BOOLEAN) || cellType.equals(CellType.BOOLEAN_FORMULA)) {
            tableCellFormat.setAlignment(dk.lindhardt.gwt.geie.shared.CellFormat.Alignment.CENTER);
         }
      }
   }

   /**
    * Sets background color
    */
   private void setBackgroundColor(
         jxl.format.CellFormat cellFormat, dk.lindhardt.gwt.geie.shared.CellFormat tableCellFormat) {
      RGB rgb = cellFormat.getBackgroundColour().getDefaultRGB();
      SerializableColor color = new SerializableColor(rgb.getRed(), rgb.getGreen(), rgb.getBlue());
      tableCellFormat.setBackgroundColor(color);
   }

   /**
    * Sets color
    */
   private void setColor(
         jxl.format.CellFormat cellFormat, dk.lindhardt.gwt.geie.shared.CellFormat tableCellFormat) {
      RGB rgb = cellFormat.getFont().getColour().getDefaultRGB();
      SerializableColor color = new SerializableColor(rgb.getRed(), rgb.getGreen(), rgb.getBlue());
      tableCellFormat.setColor(color);
   }

   /**
    * Sets borders on cell format (If there is a border it gets solid gets width 1)
    */
   private void setBorder(CellFormat cellFormat, dk.lindhardt.gwt.geie.shared.CellFormat tableCellFormat) {
      setBorder(
            tableCellFormat,
            cellFormat.getBorder(Border.BOTTOM),
            dk.lindhardt.gwt.geie.shared.CellFormat.Border.BOTTOM);
      setBorder(
            tableCellFormat, cellFormat.getBorder(Border.LEFT), dk.lindhardt.gwt.geie.shared.CellFormat.Border.LEFT);
      setBorder(
            tableCellFormat, cellFormat.getBorder(Border.RIGHT), dk.lindhardt.gwt.geie.shared.CellFormat.Border.RIGHT);
      setBorder(tableCellFormat, cellFormat.getBorder(Border.TOP), dk.lindhardt.gwt.geie.shared.CellFormat.Border.TOP);
   }

   private void setBorder(
         dk.lindhardt.gwt.geie.shared.CellFormat tableCellFormat,
         BorderLineStyle lineStyle,
         dk.lindhardt.gwt.geie.shared.CellFormat.Border border) {
      if (!lineStyle.equals(BorderLineStyle.NONE)) {
         tableCellFormat.addBorder(border);
         dk.lindhardt.gwt.geie.shared.CellFormat.BorderType borderStyle = getBorderStyle(lineStyle);
         if (borderStyle.equals(dk.lindhardt.gwt.geie.shared.CellFormat.BorderType.DOUBLE)) {
            tableCellFormat.setBorderWidth(border, 3);
         }
         tableCellFormat.setBorderType(border, borderStyle);
      }
   }

   /*
    * Gets BorderType from excel BorderLineStyle
    */
   private dk.lindhardt.gwt.geie.shared.CellFormat.BorderType getBorderStyle(BorderLineStyle lineStyle) {
      if (lineStyle.equals(BorderLineStyle.THIN)) {
         return dk.lindhardt.gwt.geie.shared.CellFormat.BorderType.SOLID;
      } else if (lineStyle.equals(BorderLineStyle.DOTTED)) {
         return dk.lindhardt.gwt.geie.shared.CellFormat.BorderType.DOTTED;
      } else if (lineStyle.equals(BorderLineStyle.DOUBLE)) {
         return dk.lindhardt.gwt.geie.shared.CellFormat.BorderType.DOUBLE;
      } else if (lineStyle.equals(BorderLineStyle.NONE)) {
         return dk.lindhardt.gwt.geie.shared.CellFormat.BorderType.NONE;
      }
      // Defaults to solid style
      return dk.lindhardt.gwt.geie.shared.CellFormat.BorderType.SOLID;
   }

   /**
    * Creates a cell which spans columns, rows or both
    */
   private void createTableCell(Cell topLeft, Cell bottomRight, Cell cell) {
      dk.lindhardt.gwt.geie.shared.Cell tableCell = getTableCell(
            cell,
            topLeft.getRow(),
            topLeft.getColumn(),
            bottomRight.getRow() - topLeft.getRow() + 1,
            bottomRight.getColumn() - topLeft.getColumn() + 1);
      if (tableCell != null) {
         layout.addCell(tableCell);
      }
   }

   /**
    * Creates a normal cell which is not merged
    */
   private void createTableCell(Cell cell) {
      dk.lindhardt.gwt.geie.shared.Cell tableCell = getTableCell(cell, cell.getRow(), cell.getColumn(), 0, 0);
      if (tableCell != null) {
         layout.addCell(tableCell);
      }
   }

   /**
    * Gets correct type of cell from the cell converter registry. If no converter is found it creates a labelcell.
    */
   private dk.lindhardt.gwt.geie.shared.Cell getTableCell(Cell cell, int row, int col, int rowSpan, int colSpan) {
      if (isPositionInsideARange(row, col)) {
         return null;
      }

      CellType type = cell.getType();
      CellFormat cellFormat = cell.getCellFormat();
      CellFormatKey cellFormatKey;
      if (cellFormat != null) {
         cellFormatKey = new CellFormatKey(cellFormatMap.get(cellFormat).toString());
      } else {
         cellFormatKey = new CellFormatKey("default");
      }
      CellConverterStrategy strategy = CellConverterStrategyRegister.getStrategy(type);
      if (strategy != null) {
         return strategy.convert(cell, new Position(row, col, rowSpan, colSpan), cellFormatKey);
      } else {
         return new LabelCell(cell.getContents(), row, col, rowSpan, colSpan, cellFormatKey);
      }
   }

   /**
    * Gets if the given position is inside a span or not
    */
   private boolean isPositionInsideARange(int row, int col) {
      Range[] mergedCells = sheet.getMergedCells();
      for (Range range : mergedCells) {
         Cell topLeft = range.getTopLeft();
         Cell bottomRight = range.getBottomRight();
         int topLeftRow = topLeft.getRow();
         int topLeftColumn = topLeft.getColumn();
         int bottomRightRow = bottomRight.getRow();
         int bottomRightColumn = bottomRight.getColumn();
         if (row == topLeftRow && col == topLeftColumn) {
            return false;
         }
         if (row >= topLeftRow && row <= bottomRightRow) {
            if (col >= topLeftColumn && col <= bottomRightColumn) {
               return true;
            }
         }
      }
      return false;
   }

   /**
    * Return range if the cell is merged and else returns null
    */
   private Range getRange(Sheet sheet, Cell cell) {
      Range[] mergedCells = sheet.getMergedCells();
      for (Range range : mergedCells) {
         if (range.getTopLeft().equals(cell)) {
            return range;
         }
      }
      return null;
   }

   private void createRowHeights(Sheet sheet) {
      int rows = sheet.getRows();
      for (int r = 0; r < rows; r++) {
         int rowHeight = sheet.getRowView(r).getSize() / 15;
         layout.setRowHeights(r, rowHeight);
      }
   }

   private void createColumnWidths(Sheet sheet) {
      int columns = sheet.getColumns();
      for (int c = 0; c < columns; c++) {
         int columnWidth = sheet.getColumnView(c).getSize() / 36;
         layout.setColumnWidth(c, columnWidth);
      }
   }
}

