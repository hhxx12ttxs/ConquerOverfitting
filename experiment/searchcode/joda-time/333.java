/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bff.bjj.utils;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.JTextComponent;

/**
 *
 * @author Bill
 */
public class Utils {

    public static void sizeAllColumnsToFitData(JTable table) {
        sizeAllColumnsToFitData(table, 0);
    }

    public static void sizeAllColumnsToFitData(JTable table, int padding) {
        for (int col = 0; col < table.getColumnCount(); col++) {
            TableColumn curColumn = table.getColumn(table.getColumnName(col));

            if (curColumn == null) {
                continue; // not a valid column skip
            }

            // Translate to the model
            int modelColumn = curColumn.getModelIndex();

            // Loop for all rows in this column looking for the widest piece of data
            DefaultTableColumnModel dcm = (DefaultTableColumnModel) table.getTableHeader().getColumnModel();
            TableColumn curHeader = dcm.getColumn(col);
            Object header = curHeader.getHeaderValue();
            TableCellRenderer curHeaderRenderer = curHeader.getCellRenderer();
            if (curHeaderRenderer == null) {
                curHeaderRenderer = new DefaultTableCellRenderer();
            }

            Component curHeaderRenderComponent = curHeaderRenderer.getTableCellRendererComponent(table, header, true, true, -1, modelColumn);
            Dimension headerDimension = curHeaderRenderComponent.getPreferredSize();
            int maxColumnWidth = headerDimension.width;

            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer curCellRenderer = table.getCellRenderer(row, modelColumn);
                Object value = table.getValueAt(row, modelColumn);
                Component curRenderComponent = curCellRenderer.getTableCellRendererComponent(table, value, true, true, row, modelColumn);
                Dimension cellDimension = curRenderComponent.getPreferredSize();

                if (cellDimension.width > maxColumnWidth) {
                    maxColumnWidth = cellDimension.width;
                }
            }

            // Set the column width to fit the maximum
            Dimension cellSpacing = table.getIntercellSpacing();
            curColumn.setPreferredWidth(maxColumnWidth + (cellSpacing != null ? cellSpacing.width : 1) + padding);
        }
    }

    public static JFrame getParentComponent(Component component) {
        if (component instanceof JPopupMenu) {
            component = ((JPopupMenu) component).getInvoker();
        }
        Component parent = component.getParent();
        if (parent instanceof JPopupMenu) {
            component = ((JPopupMenu) parent).getInvoker();
            parent = component.getParent();
        }
        while (!(parent instanceof JFrame) && (parent != null)) {
            parent = parent.getParent();
        }
        return (JFrame) parent;
    }

    public static String formatTime(long seconds) {
        final int MINUTES_PER_HOUR = 60;
        final int SECONDS_PER_MINUTE = 60;
        final int NUM10 = 10;

        String label = "0:00";
        if (seconds > 0) {
            int hours = (int) (seconds / (MINUTES_PER_HOUR * SECONDS_PER_MINUTE));
            int minutes = ((int) (seconds - (hours * (MINUTES_PER_HOUR * SECONDS_PER_MINUTE)))) / SECONDS_PER_MINUTE;
            int secs = (int) seconds - (hours * (MINUTES_PER_HOUR * SECONDS_PER_MINUTE) + (minutes * SECONDS_PER_MINUTE));
            label = (hours > 0 ? hours + ":" : "");

            if (hours > 0 && minutes < NUM10) {
                label += "0";
            }
            label += minutes
                    + ":"
                    + (secs < NUM10 ? "0"
                    + secs : Integer.toString(secs));
        }
        return (label);
    }

    public static String convertTime(long time) {
        final long secsInYear = 31536000;
        final long secsInDay = 86400;
        final long secsInHour = 3600;
        final long secsInMinute = 60;

        StringBuffer sb = new StringBuffer();

        long years = time / secsInYear;
        if (years > 0) {
            sb.append(Long.toString(years) + " years ");
        }

        long days = (time % secsInYear) / secsInDay;
        if (days > 0) {
            sb.append(Long.toString(days) + " days ");
        }
        long hours = (time % secsInDay) / secsInHour;
        if (hours > 0) {
            sb.append(Long.toString(hours) + " hours ");
        }

        //left=time%(secsInYear+secsInDay+secsInHour);
        long minutes = (time % secsInHour) / secsInMinute;
        if (minutes > 0) {
            sb.append(Long.toString(minutes) + " minutes ");
        }

        //long left=time-(secsInYear*years+secsInDay*days+secsInHour*hours+secsInMinute*minutes);
        long left = time % secsInMinute;
        if (left > 0) {
            sb.append(Long.toString(left) + " seconds");
        }
        return (sb.toString());
    }

    /* READ FILE */
    public static void readFile(JTextArea textArea, File file) {
        int numLines = 0;
        BufferedReader fileInput = null;
        FileReader inFile = null;

        try {
            inFile = new FileReader(file);
            fileInput = new BufferedReader(inFile);
        } catch (IOException ioException) {
        }

        try {
            String line = fileInput.readLine();
            while (line != null) {
                textArea.append(line + "\n");
                line = fileInput.readLine();
            }
        } catch (Exception e) {
        }

        if (fileInput != null) {
            try {
                fileInput.close();
            } catch (IOException ioException) {
            }
        }
    }

    public static void centerFrame(JFrame frame) {
        //set frame nicely in screen
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        int heightScreen = d.height / 2;
        int widthScreen = d.width / 2;

        frame.setLocation(widthScreen - (int) ((frame.getWidth() > 0 ? frame.getWidth() : frame.getPreferredSize().getWidth()) / 2),
                heightScreen - (int) ((frame.getHeight() > 0 ? frame.getHeight() : frame.getPreferredSize().getHeight()) / 2));
    }

    public static void centerInParent(Component component, Frame parent) {
        int x;
        int y;

        // Find out our parent
        //Container myParent = getParent();
        Point topLeft = parent.getLocationOnScreen();
        Dimension parentSize = parent.getSize();

        Dimension mySize = component.getSize();

        if (parentSize.width > mySize.width) {
            x = ((parentSize.width - mySize.width) / 2) + topLeft.x;
        } else {
            x = topLeft.x;
        }

        if (parentSize.height > mySize.height) {
            y = ((parentSize.height - mySize.height) / 2) + topLeft.y;
        } else {
            y = topLeft.y;
        }

        component.setLocation(x, y);
    }

    public static Cursor getHourglassCursor() {
        return (new Cursor(Cursor.WAIT_CURSOR));
    }

    public static Cursor getNormalCursor() {
        return (new Cursor(Cursor.DEFAULT_CURSOR));
    }
    private static final int DEFAULT_COLUMN_PADDING = 5;

    /*
     * @param JTable aTable, the JTable to autoresize the columns on
     * @param boolean includeColumnHeaderWidth, use the Column Header width as a minimum width
     * @returns The table width, just in case the caller wants it...
     */
    public static int autoResizeTable(JTable aTable, boolean includeColumnHeaderWidth) {
        return (autoResizeTable(aTable, includeColumnHeaderWidth, DEFAULT_COLUMN_PADDING));
    }

    /*
     * @param JTable aTable, the JTable to autoresize the columns on
     * @param boolean includeColumnHeaderWidth, use the Column Header width as a minimum width
     * @param int columnPadding, how many extra pixels do you want on the end of each column
     * @returns The table width, just in case the caller wants it...
     */
    public static int autoResizeTable(JTable aTable, boolean includeColumnHeaderWidth, int columnPadding) {
        int columnCount = aTable.getColumnCount();
        int currentTableWidth = aTable.getWidth();
        int tableWidth = 0;

        Dimension cellSpacing = aTable.getIntercellSpacing();

        if (columnCount > 0) // must have columns !
        {
            // STEP ONE : Work out the column widths

            int columnWidth[] = new int[columnCount];

            for (int i = 0; i < columnCount; i++) {
                columnWidth[i] = getMaxColumnWidth(aTable, i, true, columnPadding);

                tableWidth += columnWidth[i];
            }

            // account for cell spacing too
            tableWidth += ((columnCount - 1) * cellSpacing.width);

            // STEP TWO : Dynamically resize each column

            // try changing the size of the column names area
            JTableHeader tableHeader = aTable.getTableHeader();

            Dimension headerDim = tableHeader.getPreferredSize();

            // headerDim.height = tableHeader.getHeight();
            headerDim.width = tableWidth;
            tableHeader.setPreferredSize(headerDim);

            Dimension interCellSpacing = aTable.getIntercellSpacing();
            Dimension dim = new Dimension();
            int rowHeight = aTable.getRowHeight();

            if (rowHeight == 0) {
                rowHeight = 16;    // default rowheight
            }
            // System.out.println ("Row Height : " + rowHeight );

            dim.height = headerDim.height + ((rowHeight + interCellSpacing.height) * aTable.getRowCount());
            dim.width = tableWidth;

            // System.out.println ("AutofitTableColumns.autoResizeTable() - Setting Table size to ( " + dim.width + ", " + dim.height + " )" );
            // aTable.setPreferredSize ( dim );

            TableColumnModel tableColumnModel = aTable.getColumnModel();
            TableColumn tableColumn;

            for (int i = 0; i < columnCount; i++) {
                tableColumn = tableColumnModel.getColumn(i);

                tableColumn.setPreferredWidth(columnWidth[i]);
            }

            aTable.invalidate();
            aTable.doLayout();
            aTable.repaint();
        }

        return (tableWidth);
    }

    /*
     * @param JTable aTable, the JTable to autoresize the columns on
     * @param int columnNo, the column number, starting at zero, to calculate the maximum width on
     * @param boolean includeColumnHeaderWidth, use the Column Header width as a minimum width
     * @param int columnPadding, how many extra pixels do you want on the end of each column
     * @returns The table width, just in case the caller wants it...
     */
    private static int getMaxColumnWidth(JTable aTable, int columnNo,
            boolean includeColumnHeaderWidth,
            int columnPadding) {
        TableColumn column = aTable.getColumnModel().getColumn(columnNo);
        Component comp = null;
        int maxWidth = 0;

        if (includeColumnHeaderWidth) {
            TableCellRenderer headerRenderer = column.getHeaderRenderer();
            if (headerRenderer != null) {
                comp = headerRenderer.getTableCellRendererComponent(aTable, column.getHeaderValue(), false, false, 0, columnNo);

                if (comp instanceof JTextComponent) {
                    JTextComponent jtextComp = (JTextComponent) comp;

                    String text = jtextComp.getText();
                    Font font = jtextComp.getFont();
                    FontMetrics fontMetrics = jtextComp.getFontMetrics(font);

                    maxWidth = SwingUtilities.computeStringWidth(fontMetrics, text);
                } else {
                    maxWidth = comp.getPreferredSize().width;
                }
            } else {
                try {
                    String headerText = (String) column.getHeaderValue();
                    JLabel defaultLabel = new JLabel(headerText);

                    Font font = defaultLabel.getFont();
                    FontMetrics fontMetrics = defaultLabel.getFontMetrics(font);

                    maxWidth = SwingUtilities.computeStringWidth(fontMetrics, headerText);
                } catch (ClassCastException ce) {
                    // Can't work out the header column width..
                    maxWidth = 0;
                }
            }
        }

        TableCellRenderer tableCellRenderer;
        // Component comp;
        int cellWidth = 0;

        for (int i = 0; i < aTable.getRowCount(); i++) {
            tableCellRenderer = aTable.getCellRenderer(i, columnNo);

            comp = tableCellRenderer.getTableCellRendererComponent(aTable, aTable.getValueAt(i, columnNo), false, false, i, columnNo);

            if (comp instanceof JTextComponent) {
                JTextComponent jtextComp = (JTextComponent) comp;

                String text = jtextComp.getText();
                Font font = jtextComp.getFont();
                FontMetrics fontMetrics = jtextComp.getFontMetrics(font);

                int textWidth = SwingUtilities.computeStringWidth(fontMetrics, text);

                maxWidth = Math.max(maxWidth, textWidth);
            } else {
                cellWidth = comp.getPreferredSize().width;

                // maxWidth = Math.max ( headerWidth, cellWidth );
                maxWidth = Math.max(maxWidth, cellWidth);
            }
        }

        return (maxWidth + columnPadding);
    }

    /**
     * Loads an <code>ImageIcon</code> object from this archive.
     *
     * Note: This method is compatible under Java Web Start.
     *
     * @param name the name of the icon file
     * @return the icon if loaded, <code>null</code> otherwise
     */
    public static Image loadImage(InputStream inStream) {
        if (inStream != null) {
            try {
                return ImageIO.read(inStream);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        System.err.println("loadIcon Error: Could not find icon");

        return null;
    }

    public static void copyFile(File in, File out)
            throws IOException {
        FileChannel inChannel = new FileInputStream(in).getChannel();
        FileChannel outChannel = new FileOutputStream(out).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(),
                    outChannel);
        } catch (IOException e) {
            throw e;
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }

    public static ImageIcon scale(Image src, double scale, ImageObserver observer) {
        int w = (int) (scale * src.getWidth(observer));
        int h = (int) (scale * src.getHeight(observer));
        int type = BufferedImage.TYPE_INT_RGB;
        BufferedImage dst = new BufferedImage(w, h, type);
        Graphics2D g2 = dst.createGraphics();
        g2.drawImage(src, 0, 0, w, h, observer);
        g2.dispose();
        return new ImageIcon(dst);
    }

    public static ImageIcon scale(Image src, double scale, ImageObserver observer, Color background) {
        int w = (int) (scale * src.getWidth(observer));
        int h = (int) (scale * src.getHeight(observer));
        int type = BufferedImage.TYPE_INT_RGB;
        BufferedImage dst = new BufferedImage(w, h, type);
        Graphics2D g2 = dst.createGraphics();
        g2.setBackground(background);
        g2.setBackground(Color.yellow);
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(src, 0, 0, w, h, observer);
        g2.dispose();
        return new ImageIcon(dst);
    }

    private static BufferedImage resize(BufferedImage image, int width, int height) {
        int type = image.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : image.getType();
        BufferedImage resizedImage = new BufferedImage(width, height, type);
        Graphics2D g = resizedImage.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        return resizedImage;
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    public static ImageIcon createImageIcon(Class baseClass,
            String path,
            String description) {
        java.net.URL imgURL = baseClass.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    private static final ResourceBundle bundle =
            ResourceBundle.getBundle("messages");

    /**
     * For internationalization
     * @param message
     * @return
     */
    public static String getMessage(String message) {
        return bundle.getString(message);
    }
}

