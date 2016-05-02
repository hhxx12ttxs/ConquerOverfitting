/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package RaceLibrary;

import java.awt.*;
import javax.swing.*;

/**
 *
 * @author Brian
 */
public class LayoutTable {

    public static final int ROW_HEIGHT             = 20;
    public static final int CELL_PADDING           = 6;
    public static final int ICON_PADDING           = 4;

    public static final int COLUMN_JUSTIFY_LEFT         = 0;
    public static final int COLUMN_JUSTIFY_RIGHT        = 1;
    public static final int COLUMN_JUSTIFY_CENTER       = 2;

    protected       int             nColumns;
    protected       int             cellPadding;
    protected       String          colLabel[];
    protected       int             colWidth[];
    protected       int             colJustify[];
    protected       boolean         colUsed[];
    protected       int             colXStart;
    protected       int             colYStart;
    protected       int             colTotalWidth;

    protected       int             maxRows;
    protected       int             rowHeight;

    public LayoutTable() {

        cellPadding = CELL_PADDING;
        nColumns = 0;
        colWidth = new int[1];
        colLabel = new String[1];
        colUsed  = new boolean[1];
        colWidth[0] = 20;
        colUsed[0]  = true;
        colXStart = 0;
        colYStart = 50;
        colTotalWidth = 20;

        maxRows = 10;
        rowHeight = ROW_HEIGHT;

    }

    public void setStartX(int x) {
        colXStart = x;
    }
    public void setStartY(int y) {
        colYStart = y;
    }

    public void setCellPadding(int padding) {

    }

    public void setMaxRows(int rows) {
        maxRows = rows;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public void setupColumns(int ncol,int width[],String label[],int justify[],boolean used[]) {

        if (ncol < 1) return;

        nColumns = ncol;
        colWidth = new int[nColumns + 2];
        colLabel = new String[nColumns + 2];
        colJustify  = new int[nColumns + 2];
        colUsed     = new boolean[nColumns + 2];
        colTotalWidth = 0;
        for (int n=0;n<nColumns;n++) {
            colWidth[n]     = width[n];
            colLabel[n]     = label[n];
            colJustify[n]   = justify[n];
            colUsed[n]      = used[n];
            colTotalWidth   += colWidth[n];
        }

    }

    public void setJustify(int c,int justify) {
        if (c < 0 || c >= nColumns) return;
        colJustify[c] = justify;
    }

    public int getJustify(int c) {
        if (c < 0 || c >= nColumns) return 0;
        return colJustify[c];
    }

    public void setRowHeight(int ht) {
        rowHeight = ht;
    }

    public int getRowHeight() {
        return rowHeight;
    }

    public int getWidth() {
        return colTotalWidth;
    }

    public int getNColumns() {
        return nColumns;
    }

    public String getLabel(int ncol) {
        return colLabel[ncol];
    }

    public int getColX(int ncol) {
        int x = colXStart;
        if (ncol < 0 || ncol >= nColumns) return x;

        int n = 0;
        while (n < ncol) {
            x += colWidth[n];
            n++;
        }
        return x;
    }

    public Rectangle getRect() {
        Rectangle rect = new Rectangle(colXStart,colYStart,colTotalWidth,rowHeight*(maxRows+1));
        return rect;
    }

    public Rectangle getRowRect(int r) {
        Rectangle rect = new Rectangle(0,0, colTotalWidth, rowHeight);
        rect.x = colXStart;
        rect.y = colYStart + r * rowHeight;
        return rect;
    }

    public Rectangle getFieldRect(int r,int c) {
        Rectangle rect = new Rectangle(0,0, colTotalWidth, rowHeight);
        rect.x = getColX(c) + cellPadding;
        rect.y = colYStart + r * rowHeight;
        rect.width = colWidth[c] - cellPadding*2;
        return rect;
    }

    public JLabel getTitleLabel(int c) {
        JLabel label = new JLabel(colLabel[c]);
        label.setBounds(getFieldRect(0,c));
        label.setHorizontalAlignment(JLabel.CENTER);

        return label;
    }

}

