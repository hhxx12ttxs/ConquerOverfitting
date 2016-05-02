/**
 * Copyright Š 2010 Inkrypt Technologies Corporation
 * @author Shaheen Georgee
 * @date August 13, 2010
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * Note: Portion of this class based on source code from Beginning BlackBerry Development by Anthony Rizk. Also consider the approach at http://www.thinkingblackberry.com/archives/133
 */
package com.inkrypt.bb.ui.manager;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Manager;

/**
 *
 */
public class GridFieldManager extends Manager {

  private int m_iNumberOfColumns;
  private int m_iColumnMargin;
  private int m_iRowMargin;

  public GridFieldManager(int numberOfColumns, int columnMargin, int rowMargin, long style) {
    super(style);
    m_iNumberOfColumns = numberOfColumns;
    m_iColumnMargin = columnMargin;
    m_iRowMargin = rowMargin;
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.rim.device.api.ui.Manager#sublayout(int, int)
   */
  protected void sublayout(int width, int height) {

    int[] columnWidths = new int[m_iNumberOfColumns];
    int[] rowHeights = new int[(int) Math.ceil(getFieldCount() / m_iNumberOfColumns)];
    int availableWidth = width;
    int availableHeight = height;

    // For each column
    for (int column = 0; column < m_iNumberOfColumns; column++) {
      // size each field in all its rows and get the maximum width of the column
      for (int fieldIndex = column, row = 0; fieldIndex < getFieldCount(); fieldIndex += m_iNumberOfColumns, row++) {
        // layout to get accurate idea of required width, height
        Field field = getField(fieldIndex);
        layoutChild(field, availableWidth, availableHeight);

        // determine the width of the column from the maximum width of all its fields
        if (field.getWidth() > columnWidths[column]) {
          // FUTURE_FEATURE what should happen if Field.USE_ALL_WIDTH style is used by a Field in any column but the last?
          columnWidths[column] = field.getWidth();
        }

        // determine the height of each row from the maximum height of all fields in that row
        if (field.getHeight() > rowHeights[row]) {
          rowHeights[row] = field.getHeight();
        }
      }

      // we add a margin to all column widths but the last column
      if (column < m_iNumberOfColumns - 1) {
        columnWidths[column] += m_iColumnMargin;
      }
      availableWidth -= columnWidths[column];
    }

    int currentRow = 0;
    int currentRowHeight = 0;
    int rowYOffset = 0;

    int fieldWidthOffset;
    int fieldHeightOffset;

    // Position each field in the proper column, keeping in mind its position style bits
    for (int fieldIndex = 0; fieldIndex < getFieldCount(); fieldIndex++) {
      fieldWidthOffset = 0;
      fieldHeightOffset = 0;

      Field field = getField(fieldIndex);
      long fieldStyle = field.getStyle();
      // determine horizontal offset
      if ((fieldStyle & Field.FIELD_RIGHT) == Field.FIELD_RIGHT) { // used constant as bit-mask
        fieldWidthOffset = columnWidths[fieldIndex % m_iNumberOfColumns] - field.getWidth();
      }
      else if ((fieldStyle & Field.FIELD_HCENTER) == Field.FIELD_HCENTER) {
        fieldWidthOffset = (int) ((columnWidths[fieldIndex % m_iNumberOfColumns] - field.getWidth()) / 2);
      }

      // determine vertical offset (right now, I've defaulted it to treat it as FIELD_VCENTER because that style is not
      // being recognized when set, for some reason, and it's the style I'm using. FUTURE_FEATURE Determine the cause)
      if ((fieldStyle & Field.FIELD_BOTTOM) == Field.FIELD_BOTTOM/*
                                                                  * ) { fieldHeightOffset = rowHeights[currentRow] - field.getHeight(); }
                                                                  * else if (
                                                                  */|| (fieldStyle & Field.FIELD_VCENTER) == Field.FIELD_VCENTER) {
        fieldHeightOffset = (int) ((rowHeights[currentRow] - field.getHeight()) / 2);
      }

      // field positioning
      if (fieldIndex % m_iNumberOfColumns == 0) {
        setPositionChild(field, 0 + fieldWidthOffset, rowYOffset + fieldHeightOffset);
      }
      else {
        setPositionChild(field, columnWidths[(fieldIndex % m_iNumberOfColumns) - 1] + fieldWidthOffset, rowYOffset + fieldHeightOffset);
      }

      if (fieldIndex % m_iNumberOfColumns == m_iNumberOfColumns - 1) { // last field in row
        // offset the next row in the y-direction by the maximum height of any field in this row
        rowYOffset = rowYOffset + rowHeights[currentRow] + m_iRowMargin;
        currentRow++;
      }
    }

    // set the extent of the Manager based on the total width of all columns and the total height of all rows
    int totalWidth = 0;
    for (int i = 0; i < m_iNumberOfColumns; i++) {
      totalWidth += columnWidths[i];
    }
    setExtent(totalWidth, rowYOffset + currentRowHeight);
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.rim.device.api.ui.Manager#navigationMovement(int, int, int, int) Default focus-movement behaviour is to use the order the
   * fields are added to the manager as the focus order and to move to later fields in the focus order when the trackball is moved right or
   * down and to earlier fields when the trackball is moved left or up. For a single column of focusable fields, there is no need to change
   * this Manager. FUTURE_FEATURE Otherwise, override this method by referring to the book's website.
   */
  protected boolean navigationMovement(int dx, int dy, int status, int time) {
    return super.navigationMovement(dx, dy, status, time);
  }
}

