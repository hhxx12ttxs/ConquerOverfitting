/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package RaceLibrary;

import java.net.URL;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 *
 * @author Brian
 */
public class DisplayTable implements ActionListener {

    private static final boolean DEBUG = false;

    public static final int ROW_HEIGHT             = 20;
    public static final int CELL_PADDING           = 6;
    public static final int ICON_PADDING           = 4;

    public static final int COLUMN_JUSTIFY_LEFT         = 0;
    public static final int COLUMN_JUSTIFY_RIGHT        = 1;
    public static final int COLUMN_JUSTIFY_CENTER       = 2;

    protected       JPanel          panelWorking;

    protected       JScrollBar      scrollBar;
    protected       int             scrollBarSize;
    protected       boolean         scrollBarEnabled;

    protected       int             nColumns;
    protected       int             cellPadding;
    protected       String          colLabel[];
    protected       int             colWidth[];
    protected       int             colJustify[];
    protected       boolean         colUsed[];
    protected       int             colXStart;
    protected       int             colYStart;
    protected       int             colTotalWidth;

    protected       JPanel          panelRow[];

    protected       int             maxRows;
    protected       int             lastRow;

    protected       JLabel          labelData[][];

    protected       String          cellData[][];
    protected       String          refData[];

    protected       Font smallFont  = new Font("Arial", Font.PLAIN, 10);
    protected       Font normalFont = new Font("Arial", Font.PLAIN, 12);
    protected       Font largeFont  = new Font("Arial", Font.PLAIN, 18);

    protected       Font toolsFont1 = new Font("Arial", Font.BOLD,18);

//    javax.swing.ImageIcon iconEdit;
//    javax.swing.ImageIcon iconDelete;

    public DisplayTable(JPanel panel) {
        panelWorking = panel;

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

        panelRow = new JPanel[maxRows];
        scrollBarEnabled    = true;
    }

    public void setStartY(int y) {
        colYStart = y;
    }

    public void setCellPadding(int padding) {
        cellPadding = padding;
    }

    public void setMaxRows(int rows) {
        maxRows = rows;

        panelRow = new JPanel[maxRows];
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

        colXStart = (panelWorking.getWidth() - colTotalWidth) / 2;

        setupPanels();

        setupRowFields();
        
        clearCellData();

    }

    protected void setupPanels() {
        Color line1 = new Color(170,170,255);
        Color line2 = new Color(210,210,255);
        Rectangle rect;

        for (int r=0;r<maxRows;r++) {
            panelRow[r] = new JPanel();
            panelRow[r].setLayout(null);
            rect = new Rectangle(colXStart,colYStart, colTotalWidth, ROW_HEIGHT);
            rect.y = colYStart + ROW_HEIGHT * (r+1);
            panelRow[r].setBounds(rect);
            if (r % 2 == 0) panelRow[r].setBackground(line1);
                else        panelRow[r].setBackground(line2);
            //panelRow[r].setSize(colTotalWidth, ROW_HEIGHT);
            panelWorking.add(panelRow[r]);

        }
    }

    protected void setupRowFields() {
        Rectangle rect;

        labelData = new JLabel[maxRows][nColumns];

        for (int r=0;r<maxRows;r++) {
            rect = new Rectangle(0,0, colTotalWidth, ROW_HEIGHT);
            for (int c=0;c<nColumns;c++) {
                //String txt = "C:" + String.valueOf(c);
                labelData[r][c] = new JLabel("");

                rect.x = getColX(c) - colXStart + cellPadding;
                rect.width = colWidth[c] - cellPadding*2;
                labelData[r][c].setBounds(rect);
                switch (colJustify[c]) {
                    default :
                    case COLUMN_JUSTIFY_LEFT :
                        labelData[r][c].setHorizontalAlignment(JLabel.LEFT);
                        break;
                    case COLUMN_JUSTIFY_RIGHT :
                        labelData[r][c].setHorizontalAlignment(JLabel.RIGHT);
                        break;
                    case COLUMN_JUSTIFY_CENTER :
                        labelData[r][c].setHorizontalAlignment(JLabel.CENTER);
                        break;
                }
                if (colUsed[c]) panelRow[r].add(labelData[r][c]);
            }
        }
    }

    public void setJustify(int c,int justify) {
        if (c < 0 || c >= nColumns) return;
        colJustify[c] = justify;
    }

    public void clearCellData() {
        refData = new String[maxRows];
        cellData = new String[maxRows][nColumns];
        for (int r=0;r<maxRows;r++) {
            refData[r] = "";
            clearExtraRow(r);
            for (int c=0;c<nColumns;c++) {
                cellData[r][c] = "";
                labelData[r][c].setText("");
                labelData[r][c].setVisible(false);
                clearExtraData(r,c);
            }
        }
        lastRow = 0;
    }

    protected void clearExtraRow(int row) {

    }

    protected void clearExtraData(int row,int col) {

    }

    public void addHeader() {

        Rectangle rect = new Rectangle(colXStart,colYStart, colWidth[0], ROW_HEIGHT);
        Border border = BorderFactory.createLineBorder(Color.black,2);

        for (int c=0;c<nColumns;c++) {
            rect.x = getColX(c);
            rect.width = colWidth[c];
            addLabelBorder(colLabel[c],border,rect);
        }
    }

    public void addRowData(int r,int c,String data) {
        if (r < 0 || r >= maxRows) return;
        if (c < 0 || c >= nColumns) return;
        cellData[r][c] = data;
        labelData[r][c].setText(data);
        labelData[r][c].setVisible(true);
        checkLastRow(r);
    }

    public void addRowRef(int r,String data) {
        if (r < 0 || r >= maxRows) return;
        refData[r] = data;
        checkLastRow(r);
    }

    public String getRowRef(int r) {
        if (r < 0 || r >= maxRows) return null;
        return refData[r];
    }

    protected void checkLastRow(int r) {
        if (r > lastRow) lastRow = r;
    }

    protected void addLabelBorder(String text,javax.swing.border.Border border,Rectangle rect) {
        JLabel label = new JLabel(text);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setBorder(border);
        label.setBounds(rect.x,rect.y,rect.width,rect.height);
        panelWorking.add(label);
    }

    public void addButtons() {

    }

    public void createRowLines() {

        scrollBarSize  = 1;
        scrollBar = new JScrollBar();

        if (!scrollBarEnabled) return;

        scrollBar.setBounds(colXStart+colTotalWidth,colYStart+ROW_HEIGHT, 20, ROW_HEIGHT*maxRows);
        scrollBar.addAdjustmentListener(listenerScroll);
        panelWorking.add(scrollBar);

        panelWorking.repaint();

    }

    public void setScrollBarSize(int size) {
        scrollBarSize = size;
    }

    public void setScrollBarEnabled(boolean value) {
        scrollBarEnabled = value;
    }

    AdjustmentListener listenerScroll = new AdjustmentListener() {
        public void adjustmentValueChanged(AdjustmentEvent e) {
            if (DEBUG) System.out.println("Vertical: " + e.toString());

            int value = scrollBarSize * e.getValue() / scrollBar.getMaximum();
            scrollBarNewValue(value);
        }
    };

    protected void scrollBarNewValue(int value) {

    }

    public void paint(Graphics g) {

        Color line1 = new Color(170,170,255);
        Color line2 = new Color(210,210,255);

        Font saveFont = g.getFont();
        g.setFont(normalFont);



        Rectangle rect;
        //Border border = BorderFactory.createLineBorder(Color.gray,1);
        for (int r=0;r<maxRows;r++) {
            rect = new Rectangle(colXStart,colYStart, colTotalWidth, ROW_HEIGHT);
            rect.y = colYStart + ROW_HEIGHT * (r+1);
            int even = r % 2;

            if (even==0) g.setColor(line1);
            else g.setColor(line2);

            g.fillRect(rect.x,rect.y,rect.width,rect.height);

            g.setColor(Color.BLACK);

//            if (r <= lastRow && lastRow > 0) {
//                g.drawImage(iconEdit.getImage()     , rect.x+ICON_PADDING, rect.y, null);
//                g.drawImage(iconDelete.getImage()   , rect.x+iconEdit.getIconWidth()+ICON_PADDING*2, rect.y, null);
//            }
            for (int c=0;c<nColumns;c++) {
                //String txt = "Cell " + String.valueOf(c);

                rect.x = getColX(c);
                rect.width = colWidth[c];

                int x = rect.x;

                switch (colJustify[c]) {
                    default :
                    case COLUMN_JUSTIFY_LEFT :
                        x += cellPadding;
                        break;
                    case COLUMN_JUSTIFY_RIGHT :
                        x += rect.width - g.getFontMetrics().stringWidth(cellData[r][c]) - cellPadding;
                        break;
                    case COLUMN_JUSTIFY_CENTER :
                        x += (rect.width - g.getFontMetrics().stringWidth(cellData[r][c])) / 2;
                        break;
                }

                int y = rect.y + ROW_HEIGHT / 2 + g.getFontMetrics().getAscent() / 2;

                g.drawString(cellData[r][c],x,y);
            }
        }
        g.setFont(saveFont);
    }

    public int buttonEditClick(MouseEvent e) {
        return -1;
//        return buttonClick(e,rectEdit);
    }

    public int buttonDeleteClick(MouseEvent e) {
        return -1;
        //        return buttonClick(e,rectDelete);
    }

    public int buttonClick(MouseEvent e,Rectangle rectArea) {
        int row = -1;

        if (e.getX() >= rectArea.x && e.getX() < rectArea.x + rectArea.width &&
            e.getY() >= rectArea.y && e.getY() < rectArea.y + rectArea.height) {
            row = (e.getY() - rectArea.y) / ROW_HEIGHT;
            if (row < 0 || row > lastRow) row = -1;
        }

        return row;
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

    public void actionPerformed(ActionEvent e) {

    }
}
