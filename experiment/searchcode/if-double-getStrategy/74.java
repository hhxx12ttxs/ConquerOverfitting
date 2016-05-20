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
import dk.lindhardt.gwt.geie.shared.CellFormat;
import jxl.biff.DisplayFormat;
import jxl.biff.FontRecord;
import jxl.common.Logger;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.*;
import jxl.format.VerticalAlignment;
import jxl.write.*;
import jxl.write.biff.RowsExceededException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: AnAmuser
 * Date: 28-05-11
 * <p/>
 * Class for building sheets in a {@link jxl.write.WritableWorkbook} from a {@link TableLayout}
 */
public class TableLayout2Excel {

   /**
    * map&lt;color, colour index on workbook>
    */
   private Map<SerializableColor, Integer> colourPallette = new HashMap<SerializableColor, Integer>();
   /**
    * map(format key, format)
    */
   private Map<String, WritableCellFormat> cellFormats = new HashMap<String, WritableCellFormat>();
   private WritableWorkbook workbook;
   private TableLayout layout;
   private static Logger logger = Logger.getLogger(TableLayout2Excel.class);

   /**
    * Table layout can be both EXCEL and HTML structured. It will be converted before making the excel sheet
    * @param layout the layout
    */
   public TableLayout2Excel(TableLayout layout) {
      this.layout = layout;
   }

   /**
    * Builds the excel sheet in the given workbook
    * @param workbook      Workbook to write sheet in
    * @param sheetTitle    Title of the sheet
    * @param sheetIndex    Index of the sheet
    * @return The created sheet
    * @throws WriteException
    */
   public WritableSheet build(
         WritableWorkbook workbook,
         String sheetTitle,
         int sheetIndex) throws WriteException {
      this.workbook = workbook;
      createColorPallette(layout.getCellFormats());

      WritableSheet sheet = workbook.createSheet(sheetTitle, sheetIndex);
      setColumnWidths(sheet, layout.getColumnWidths());
      setRowHeights(sheet, layout.getRowHeights());

      insertCells(sheet, layout.getCells());
      insertImages(sheet, layout.getImages());

      return sheet;
   }

   private void setRowHeights(WritableSheet sheet, Map<Integer, Integer> rowHeights) throws RowsExceededException {
      for (Integer row : rowHeights.keySet()) {
         Integer height = rowHeights.get(row);
         double excelHeight = (double) height / Math.pow(15.0, -1.0);
         sheet.setRowView(row, (int) Math.round(excelHeight));
      }
   }

   private void setColumnWidths(WritableSheet sheet, Map<Integer, Integer> columnWidths) {
      for (Integer column : columnWidths.keySet()) {
         Integer width = columnWidths.get(column);
         double excelWidth = (double) width / 7;
         sheet.setColumnView(column, (int) Math.round(excelWidth));
      }
   }

   private void insertImages(WritableSheet sheet, List<Image> images) {
      for (Image image : images) {
         byte[] bytes = Base64Utils.fromBase64(image.getBase64Data());
         sheet.addImage(new WritableImage(image.getX(), image.getY(), image.getWidth(), image.getHeight(), bytes));
      }
   }

   private void insertCells(WritableSheet sheet, List<Cell> cells) throws WriteException {
      for (Cell cell : cells) {
         WritableCell writableCell = getCell(cell);
         if (writableCell != null) {
            sheet.addCell(writableCell);
            createHyperLink(cell, sheet);
            Position position = cell.getPosition();
            int column = position.getColumn();
            int row = position.getRow();
            if (position.getSpanColumn() > 1 || position.getSpanRow() > 1) {
               sheet.mergeCells(
                     column, row,
                     position.getSpanColumn() > 1 ? column + position.getSpanColumn() - 1 : column,
                     position.getSpanRow() > 1 ? row + position.getSpanRow() - 1 : row);
            }
         }
      }
   }

   private void createHyperLink(Cell cell, WritableSheet sheet) throws WriteException {
      Link link = cell.getLink();
      Position position = cell.getPosition();
      if (link != null) {
         int row = position.getRow();
         int column = position.getColumn();
         try {
            sheet.addHyperlink(
                  new WritableHyperlink(
                        column,
                        row,
                        column + position.getSpanColumn(),
                        row + position.getSpanRow(),
                        new URL(link.getDestination())));
         } catch (MalformedURLException e) {
            logger.error("Could not create url on " + cell, e);
         }
      }
   }

   private WritableCell getCell(Cell cell) throws WriteException {
      WritableCellFormat cellFormat = prepareCellFormat(cell);
      CellConverterStrategy strategy = CellConverterStrategyRegister.getStrategy(cell.getClass());
      if (strategy != null) {
         return strategy.convert(cell, cellFormat);
      }
      throw new RuntimeException("No cell converter strategy was found for type: " + cell.getClass().getName());
   }

   /**
    * Creates all the different colors found in the tables cell formats to the workbook
    */
   private void createColorPallette(Map<CellFormatKey, CellFormat> cellFormats) {
      for (CellFormatKey key : cellFormats.keySet()) {
         CellFormat cellFormat = cellFormats.get(key);
         SerializableColor backgroundColor = cellFormat.getBackgroundColor();
         createColor(backgroundColor);
         SerializableColor color = cellFormat.getColor();
         createColor(color);
      }
   }

   /**
    * Adds the color to the workbook if it does not yet exist
    */
   private void createColor(SerializableColor color) {
      if (color != null && !colourPallette.containsKey(color)) {
         int colourIndex = 12 + colourPallette.size();
         colourPallette.put(color, colourIndex);
         workbook.setColourRGB(
               Colour.getInternalColour(colourIndex),
               color.getRed(),
               color.getGreen(),
               color.getBlue());
      }
   }

   private WritableCellFormat prepareCellFormat(Cell cell) throws WriteException {
      WritableCellFormat cellFormat = null;
      CellFormatKey formatKey = cell.getCellFormatKey();
      String excelFormatKey = formatKey.getKey() + cell.getClass().getName();

      if (cellFormats.containsKey(excelFormatKey)) {
         return cellFormats.get(excelFormatKey);
      }

      CellFormat tableCellFormat = layout.getCellFormat(cell.getCellFormatKey());
      if (cell instanceof DateCell) {
         CellFormat layoutCellFormat = layout.getCellFormat(cell.getCellFormatKey());
         if (layoutCellFormat.getPattern() != null && layoutCellFormat.getPattern().length() > 0) {
            DateFormat dateFormat = new DateFormat(layoutCellFormat.getPattern());
            cellFormat = new WritableCellFormat(dateFormat);
         }
      } else if (cell instanceof NumberCell) {
         CellFormat layoutCellFormat = layout.getCellFormat(cell.getCellFormatKey());
         if (layoutCellFormat.getPattern() != null && layoutCellFormat.getPattern().length() > 0) {
            DisplayFormat numberFormat = new NumberFormat(layoutCellFormat.getPattern());
            cellFormat = new WritableCellFormat(numberFormat);
         }
      }

      if (cellFormat == null) {
         cellFormat = new WritableCellFormat();
      }
      SerializableColor backgroundColor = tableCellFormat.getBackgroundColor();
      if (backgroundColor != null) {
         cellFormat.setBackground(Colour.getInternalColour(colourPallette.get(backgroundColor)));
      }
      cellFormat.setFont(getFont(tableCellFormat));
      cellFormat.setAlignment(getAlignment(tableCellFormat));
      cellFormat.setVerticalAlignment(getVerticalAlignment(tableCellFormat));
      setBorders(cellFormat, tableCellFormat);

      cellFormats.put(excelFormatKey, cellFormat);

      return cellFormat;
   }

   private void setBorders(WritableCellFormat cellFormat, CellFormat tableCellFormat) throws WriteException {
      for (CellFormat.Border border : tableCellFormat.getBorders()) {
         switch (border) {
            case ALL:
               cellFormat.setBorder(
                     jxl.format.Border.ALL,
                     getBorderLineStyle(tableCellFormat.getBorderType(CellFormat.Border.ALL)));
               break;
            case BOTTOM:
               cellFormat.setBorder(
                     jxl.format.Border.BOTTOM,
                     getBorderLineStyle(tableCellFormat.getBorderType(CellFormat.Border.BOTTOM)));
               break;
            case LEFT:
               cellFormat.setBorder(
                     jxl.format.Border.LEFT,
                     getBorderLineStyle(tableCellFormat.getBorderType(CellFormat.Border.LEFT)));
               break;
            case RIGHT:
               cellFormat.setBorder(
                     jxl.format.Border.RIGHT,
                     getBorderLineStyle(tableCellFormat.getBorderType(CellFormat.Border.RIGHT)));
               break;
            case TOP:
               cellFormat.setBorder(
                     jxl.format.Border.TOP,
                     getBorderLineStyle(tableCellFormat.getBorderType(CellFormat.Border.TOP)));
               break;
         }
      }
      if (tableCellFormat.getBorders().contains(CellFormat.Border.NONE)) {
         cellFormat.setBorder(jxl.format.Border.NONE, BorderLineStyle.NONE);
      }
   }

   private BorderLineStyle getBorderLineStyle(CellFormat.BorderType borderType) {
      if (borderType != null) {
         switch (borderType) {
            case SOLID:
               return BorderLineStyle.THIN;
            case DOUBLE:
               return BorderLineStyle.DOUBLE;
            case DOTTED:
               return BorderLineStyle.DOTTED;
            case NONE:
               return BorderLineStyle.NONE;
         }
      }
      return BorderLineStyle.THIN;
   }

   private FontRecord getFont(CellFormat cellFormat) throws WriteException {
      CellFormat.Font font = cellFormat.getFont();
      WritableFont writableFont = null;
      switch (font) {
         case ARIAL:
            writableFont = new WritableFont(WritableFont.ARIAL);
            break;
         case TAHOMA:
            writableFont = new WritableFont(WritableFont.TAHOMA);
            break;
         case TIMES:
            writableFont = new WritableFont(WritableFont.TIMES);
            break;
      }
      writableFont.setBoldStyle(cellFormat.isBold() ? WritableFont.BOLD : WritableFont.NO_BOLD);
      writableFont.setItalic(cellFormat.isItalic());
      writableFont.setPointSize(cellFormat.getFontSize());
      SerializableColor color = cellFormat.getColor();
      if (color != null) {
         writableFont.setColour(Colour.getInternalColour(colourPallette.get(color)));
      }
      if (cellFormat.isUnderlined()) {
         writableFont.setUnderlineStyle(UnderlineStyle.SINGLE);
      }
      return writableFont;
   }

   private VerticalAlignment getVerticalAlignment(dk.lindhardt.gwt.geie.shared.CellFormat tableCellFormat) {
      CellFormat.VAlignment vAlignment = tableCellFormat.getValignment();
      if (vAlignment != null) {
         switch (vAlignment) {
            case BOTTOM:
               return VerticalAlignment.BOTTOM;
            case CENTER:
               return VerticalAlignment.CENTRE;
            case TOP:
               return VerticalAlignment.TOP;
         }
      }
      return null;
   }

   private jxl.format.Alignment getAlignment(CellFormat cellFormat) {
      CellFormat.Alignment alignment = cellFormat.getAlignment();
      if (alignment != null) {
         switch (alignment) {
            case LEFT:
               return jxl.format.Alignment.LEFT;
            case RIGHT:
               return jxl.format.Alignment.RIGHT;
            case CENTER:
               return jxl.format.Alignment.CENTRE;
         }
      }
      return jxl.format.Alignment.GENERAL;
   }
}

