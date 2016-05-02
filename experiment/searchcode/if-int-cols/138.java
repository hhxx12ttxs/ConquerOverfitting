/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package example;

import java.util.Vector;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author Caio
 */
public class ScrollableTable{
    public int currentCol = 0;
    public int currentRow = 0;
    static final String VOID_STRING = "";
    static final char SPACE_CHAR = ' ';
    int rows = 0;
    int cols = 0;
    int[] colWidths = null;
    int[] rowHeights = null;
    int[] colLeft = null;
    int[] rowTop = null;
    String[][] data = null;
    String[][][] dataRows = null;
    public static final int SCROLL_STEP = 25;
    int viewportWidth = 0;
    int viewportHeight = 0;
    int viewportX = 0;
    int viewportY = 0;
    int width = 0;
    int height = 0;
    Font font = Font.getDefaultFont();
    int bgColor = 0xffffff;
    int bgFocusedColor = 0x0000ff;
    int foreColor = 0x000000;
    int borderWidth = 1;
    int borderColor = 0x000000;
    int interline = 5;
    int padding = 2;

    public ScrollableTable(String[][] data, int width, int height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
        setData(data);
    }
    public void setData(String[][] data) {
        this.data = data;
        this.rows = this.data.length;
        this.cols = this.data[0].length;
        initialize();
    }

    public void initialize() {
        this.rowTop = new int[rows];
        this.colLeft = new int[cols];
        this.colWidths = new int[cols];
        this.dataRows = new String[rows][][];
        int mediumWidth = (viewportWidth - cols * (2 * padding + borderWidth) - borderWidth) / cols;
        int extraWidth = 0;
        int overflowCols = 0;
        for (int i = 0; i < cols; i++) {
            int colWidth = 0;
            for (int j = 0; j < rows; j++) {
                colWidth = Math.max(
                        colWidth,
                        font.stringWidth(data[j][i]));
            }
            if (colWidth < mediumWidth) {
                colWidths[i] = colWidth;
                extraWidth += (mediumWidth - colWidth);
            } else {
                colWidths[i] = mediumWidth;
                overflowCols++;
            }
        }
        for (int i = 0; i < cols; i++) {
            if (overflowCols > 0 && extraWidth > 0 && colWidths[i] == mediumWidth) {
                colWidths[i] += (extraWidth / overflowCols);
            }
            colLeft[i] = width;
            colWidths[i] += +2 * padding;
            width += colWidths[i] + borderWidth;
        }
        width += borderWidth;
        rowHeights = new int[rows];
        for (int i = 0; i < rows; i++) {
            this.dataRows[i] = new String[cols][];
            for (int j = 0; j < cols; j++) {
                this.dataRows[i][j] = getTextRows(this.data[i][j], font, colWidths[j]);
                rowHeights[i] = Math.max(
                        rowHeights[i],
                        dataRows[i][j].length * (font.getHeight() + interline) - (dataRows[i][j].length > 0 ? interline : 0));
            }
            rowHeights[i] += 2 * padding;
            rowTop[i] = height;
            height += rowHeights[i] + borderWidth;
        }
        height += borderWidth;
    }
    public void paint(Graphics g) {
        g.setClip(0, 0, viewportWidth, viewportHeight);
        g.translate(-viewportX, -viewportY);
        g.setColor(bgColor);
        g.fillRect(0, 0, width, height);
        int currentX = 0;
        int currentY = 0;
        for (int i = 0; i < rows; i++) {
            currentX = 0;
            g.setColor(borderColor);
            g.fillRect(0, currentY, width, borderWidth);
            currentY += borderWidth;
            for (int j = 0; j < cols; j++) {
                if (i == 0) {
                    g.setColor(borderColor);
                    g.fillRect(currentX, 0, borderWidth, height);
                }
                if (j == currentCol && i == currentRow) {
                    g.setColor(bgFocusedColor);
                    g.fillRect(borderWidth + currentX, currentY, colWidths[j], rowHeights[i]);
                    g.setColor(foreColor);
                }
                currentX += borderWidth;
                g.setColor(foreColor);
                for (int k = 0; k < dataRows[i][j].length; k++) {
                    g.drawString(dataRows[i][j][k], padding + currentX, padding + currentY + k * (font.getHeight() + interline), Graphics.TOP | Graphics.LEFT);
                }
                currentX += colWidths[j];
            }
            if (i == 0) {
                g.setColor(borderColor);
                g.fillRect(currentX, 0, borderWidth, height);
            }
            currentY += rowHeights[i];
        }
        g.setColor(borderColor);
        g.fillRect(0, currentY, width, borderWidth);
        g.translate(viewportX, viewportY);
    }

    public String getSelectedData() {
        return data[currentRow][currentCol];
    }

    public void keyPressed(int keyCode) {
        switch (keyCode) {
            case Canvas.UP:
                move(0, -1);
                break;
            case Canvas.DOWN:
                move(0, 1);
                break;
            case Canvas.RIGHT:
                move(1, 0);
                break;
            case Canvas.LEFT:
                move(-1, 0);
                break;
        }
    }
    boolean scroll(int colDelta, int rowDelta) {
        boolean scrolled = false;
        if (colDelta > 0 && colLeft[currentCol] + colWidths[currentCol] > viewportX + viewportWidth) {
            viewportX += SCROLL_STEP;
            scrolled = true;
        } else if (colDelta < 0 && colLeft[currentCol] < viewportX) {
            viewportX -= SCROLL_STEP;
            scrolled = true;
        }
        if (rowDelta > 0 && rowTop[currentRow] + rowHeights[currentRow] > viewportY + viewportHeight) {
            viewportY += SCROLL_STEP;
            scrolled = true;
        } else if (rowDelta < 0 && rowTop[currentRow] < viewportY) {
            viewportY -= SCROLL_STEP;
            scrolled = true;
        }
        if (scrolled) {
            viewportX = Math.max(0, Math.min(viewportX, width - viewportWidth));
            viewportY = Math.max(0, Math.min(viewportY, height - viewportHeight));
        }
        return scrolled;
    }

    public void move(int colDelta, int rowDelta) {
        if (!scroll(colDelta, rowDelta)) {
            int newCol = currentCol + colDelta;
            int newRow = currentRow + rowDelta;

            if (newCol >= 0 && newRow >= 0 && newRow < rows && newCol < cols) {
                currentCol = newCol;
                currentRow = newRow;
                scroll(colDelta, rowDelta);
            }
        }
    }
    public static String[] getTextRows(String text, Font font, int width) {
        int prevIndex = 0;
        int currIndex = text.indexOf(SPACE_CHAR);
        Vector rowsVector = new Vector();
        StringBuffer stringBuffer = new StringBuffer();
        String currentToken;
        String currentRowText = VOID_STRING;
        while (prevIndex != -1) {
            int startCharIndex = prevIndex == 0 ? prevIndex : prevIndex + 1;
            if (currIndex != -1) {
                currentToken = text.substring(startCharIndex, currIndex);
            } else {
                currentToken = text.substring(startCharIndex);
            }
            prevIndex = currIndex;
            currIndex = text.indexOf(SPACE_CHAR, prevIndex + 1);
            if (currentToken.length() == 0) {
                continue;
            }
            if (stringBuffer.length() > 0) {
                stringBuffer.append(SPACE_CHAR);
            }
            stringBuffer.append(currentToken);
            if (font.stringWidth(stringBuffer.toString()) > width) {
                if (currentRowText.length() > 0) {
                    rowsVector.addElement(currentRowText);
                }
                stringBuffer.setLength(0);
                currentRowText = VOID_STRING;
                stringBuffer.append(currentToken);
                currentRowText = stringBuffer.toString();
            } else {
                currentRowText = stringBuffer.toString();
            }
        }
        if (currentRowText.length() > 0) {
            rowsVector.addElement(currentRowText);
        }
        String[] rowsArray = new String[rowsVector.size()];
        rowsVector.copyInto(rowsArray);
        return rowsArray;
    }
}

