package com.cyberaka.visualaccounts.common.export;

import com.cyberaka.visualaccounts.common.*;
import com.cyberaka.visualaccounts.common.datadriver.*;
import com.cyberaka.visualaccounts.client.*;
import com.cyberaka.visualaccounts.client.gui.GuiEnvironmentComposite;
import com.cyberaka.visualaccounts.client.gui.reports.outstanding.*;
import com.cyberaka.visualaccounts.client.gui.reports.outstanding.ledger.*;
import com.cyberaka.visualaccounts.client.gui.reports.outstanding.ledger.*;
import com.cyberaka.visualaccounts.client.gui.reports.table.*;
import javax.swing.table.*;
import java.io.*;
import java.text.*;
import java.awt.print.*;
import java.awt.*;
import java.awt.font.*;
import java.text.*;
import java.awt.geom.*;
import java.util.*;

/**
 * This class is used to export a given table model's data into a binary file
 * which can be further parsed by other reporting classes to the printer or
 * it can be written into a file.
 *
 * This class's constructor takes the composite and individual column's size
 * into account and proportionately formats the report to fit into the
 * size supported by the printer's paper margins (as dictated by the page
 * format object).
 *
 * Title:        Visual Accounts
 * Description:  Accounting Software
 * Copyright:    Copyright (c) 2003
 * Company:      Computer Care
 * @author Abhinav Anand
 * @version 1.0
 */
public class TableWrappedWithHeaderFooterExporter {

    /** Export type. */
    private int exportType;

    /** Report format model for row specific line formatting. */
    private ReportFormattingModel formattingModel;

    /** The table model containing tabular data. */
    protected AbstractTableModel tableModel;

    /** The horizontal column space between two rows. */
    protected final static int INTER_COLUMN_SPACE = 1;

    /** The progress box to indicate the progress. */
    protected ProgressBox progressBox;

    /** The composite to get access to client configuration and pageformat. */
    protected GuiEnvironmentComposite composite;

    /** The individual column size in the 80 column proportion. */
    protected int[] specifiedColumnSize;

    /**
     * The columns for which wrap indent will be supported in the following way:
     *  xxxxxxxxxxxxxxxxxx
     *    xxxxxxxxxxxxxxxx
     *    xxxxxxxxxxxxxxxx
     */
    protected boolean[] wrapIndentColumns;

    /** The total column size (by adding up the individual column size). */
    protected int totalSpecifiedColumnSize;

    /** Total width of the report. */
    protected int totalWidth = 0;

    /** Total number of rows in the report. */
    protected int totalRows = 0;

    /** Total lines availble for the report. */
    private int totalLinesPerPage = 0;

    /** Total lines for the header. */
    private int totalLinesHeader = 0;

    /** Total lines for the footer. */
    private int totalLinesFooter = 0;

    /** Total work to be done for the progress bar. */
    private int totalWork = 0;

    /** Total number of pages in the report. */
    private int totalPages = 0;

    /**
     * Page header to be printed on the first page and then on the subsequent
     * pages.
     */
    private String[] firstPageHeader, otherPageHeader;

    /**
     * Model for generation of the footer & header.
     */
    private ReportHeaderFooterModel headerFooterModel;

    /**
     * Total number of rows to be left out just below the report header for
     * any information to be inserted by the model.
     */
    private int bodyHeaderRowCount;

    /**
     * Total number of rows to be left out just below the report body for
     * footer.
     */
    private int bodyFooterRowCount;

    /** Details about the column name and its corresponding size. */
    protected Object[][] columnDetails;

    /** The data driver used for export implementation. */
    protected Data data;

    /** Exception to signify any sort or error in the data export thread. */
    protected Exception exception;

    String[] firstPageFullHeader, otherPageFullHeader;
    int headerCount = 0;
    int footerCount = 0;
    int count = 0;

    /**
     * Constructor to create an instance of TableWrappedExporter.
     *
     * Assigns the variables and finds out the total number of columns that
     * can be successfully printed.
     *
     * @param model     The table model whose data is supposed to be exported.
     * @param composite The composite to gain access to the system page format
     *                  and the client's configuration setting.
     * @param cSize     The int[] array to hold details about the individual
     *                  columns size in the 80 column proportion.
     * @param wColumns  The columns for which wrap indent will be supported.
     * @param fpHeader  The header to be printed on the first page.
     * @param opHeader  The header to be printed on every other page.
     * @param bfc      Total space to be left out for the report footer.
     * @param bhc       Total space to be left out for the body header.
     * @param hfm       The header footer model.
     */
    public TableWrappedWithHeaderFooterExporter(AbstractTableModel model, GuiEnvironmentComposite comp, int[] cSize, boolean[] wColumns,
            String[] fpHeader, String[] opHeader, int bfc, int bhc, ReportHeaderFooterModel hfm, int etype)
            throws ResourceKeyNotDefinedException {
        tableModel = model;
        composite = comp;
        specifiedColumnSize = cSize;
        wrapIndentColumns = wColumns;
        firstPageHeader = fpHeader;
        otherPageHeader = opHeader;
        bodyFooterRowCount = bfc;
        bodyHeaderRowCount = bhc;
        headerFooterModel = hfm;
        exportType = etype;

        // Calculate the total column size specified. usually it is 80
        // but calculation is necessary just to be sure.
        totalSpecifiedColumnSize = 0;
        for (int i = 0; i < specifiedColumnSize.length; i++) {
            totalSpecifiedColumnSize += specifiedColumnSize[i];
        }
        calculateAdjustedColumnSize();
        determineReportHeight();
    }

    /**
     * Set the report formatting model for this report.
     */
    public void setReportFormattingModel(ReportFormattingModel model) {
        formattingModel = model;
    }

    /**
     * Calculates the column size of the columns of the report on the basis
     * of the specified column size of each column and the printer width of the
     * printer.
     *
     * First of all the ratio between printer width and the specified report
     * width is calculated. If the ratio is positive i.e. the printer width is
     * more than the report, then no action is supposed to be taken. However
     * if the ratio is negative i.e. the report width is more than the printer
     * width then the excess report width is calculated.
     *
     * Next we need to find out which columns need excess
     * If the column size is well below or equal to the specified column size
     * then no action is supposed to be taken.
     */
    protected void calculateAdjustedColumnSize() {
    }

    /**
     * Measures each and every row in the table model and finds out the width
     * that best fits them all. However if the total report size exceeds the
     * total print width supported by the printer then the report size will
     * be discarded and the total printer size will be returned.
     *
     * If a columns size is less than the size specified for it then the
     * specified size is choosen as the default size for the column.
     *
     * @param int the report size.
     */
    public int getReportWidth() {
        int width = 80;
        try {
            if (composite.getClientConfiguration().getPrintMode() == DesignConstants.TEXT_MODE_PRINTING) {
                width = composite.getClientConfiguration().getPrinterWidth();
            }
        } catch (ResourceKeyNotDefinedException e) {
        }
        return width;
    }

    /**
     * Determine report height.
     */
    private void determineReportHeight() throws ResourceKeyNotDefinedException {
        ClientConfiguration conf = composite.getClientConfiguration();
        if (conf.getPrintMode() == DesignConstants.GRAPHICS_MODE_PRINTING) {
            PageFormat pageFormat = composite.getPageFormat();
            // Find out the width of the frame
            double frameWidth = pageFormat.getImageableWidth() - 5;
            double frameHeight = pageFormat.getImageableHeight();
            String fontName = composite.getClientConfiguration().getPrintFontName();
            int fontSize = composite.getClientConfiguration().getPrintFontSize();
            int fontStyle = composite.getClientConfiguration().getPrintFontStyle();

            // Prepare the text layout.
            AffineTransform affineTransform = new AffineTransform();
            Font font = new Font(fontName, fontStyle, fontSize);
            FontRenderContext renderContext = new FontRenderContext(affineTransform, false, true);
            TextLayout layout = new TextLayout(getReportWidthString(), font, renderContext);

            // Find out the total number of rows that can be accomodated on one screen.
            totalLinesPerPage = (int) (frameHeight / (layout.getAscent() + layout.getDescent() + layout.getLeading()));
            totalLinesPerPage -= 1;
        } else {
            totalLinesPerPage = conf.getPrintoutLinesPerPage();
        }
        //System.out.println("Total Lines Per Page = " + totalLinesPerPage);
    }

    /**
     * Returns a report string that is of the width
     * of the whole report.
     */
    private String getReportWidthString() {
        String str = "";
        for (int i = 0; i < 80; i++) {
            str += "*";
        }
        return str;
    }

    private void incrementProgress() {
        progressBox.setValue(count++);
        Thread.yield();
    }

    private Data initializeData(String file) throws DatabaseException, IOException {
        Iterator iterator = null;
        String uniqueFileName = file;
        FieldInfo[] info = new FieldInfo[] { new FieldInfo("1", 15),
                new FieldInfo(SystemConstants.REPORT_FIELD_NAME, SystemConstants.REPORT_FIELD_SIZE), new FieldInfo("control_set", 10),
                new FieldInfo("control_reset", 10) };
        return new Data(uniqueFileName, info);
    }

    private void determineHeader() {
        // The column array with their corresponding maximum length.
        columnDetails = new Object[tableModel.getColumnCount()][4];
        totalRows = tableModel.getRowCount();
        totalWidth = 0;

        // Total work calculation.
        totalWork = columnDetails.length; // * totalRows; // width analysis
        totalWork += firstPageHeader.length; // heading writing.
        totalWork += columnDetails.length; // column details writing.
        totalWork += totalRows * columnDetails.length; // body writing.
        progressBox.setMaximum(totalWork);
        progressBox.start();
        int count = 0;

        // determine the size of each column. The maximum.
        progressBox.setProgressStatus("Calculating width");
        for (int i = 0; i < columnDetails.length; i++) {
            columnDetails[i][0] = tableModel.getColumnName(i);
            columnDetails[i][1] = new Integer(specifiedColumnSize[i] + INTER_COLUMN_SPACE);
            columnDetails[i][2] = "";
            columnDetails[i][3] = "";
            totalWidth += specifiedColumnSize[i] + INTER_COLUMN_SPACE;
        }

        // Make sure that the totalWidth of the report doesn't goes
        // the total width of the whole report set in the system constants..
        if (totalWidth > (SystemConstants.REPORT_FIELD_SIZE - 2)) {
            totalWidth = SystemConstants.REPORT_FIELD_SIZE - 2;
        } else {
            totalWidth -= INTER_COLUMN_SPACE;
        }

        // Write the company header.
        progressBox.setProgressStatus("Writing heading");
        // Analyze the report header. Construct the column names
        // array and assign values to it.
        String[] columnHeader = new String[columnDetails.length];
        for (int i = 0; i < columnDetails.length; i++) {
            String ch = columnDetails[i][0].toString();

            if (tableModel.getColumnClass(i) == Float.class || tableModel.getColumnClass(i) == Balance.class
                    || tableModel.getColumnClass(i) == Double.class || tableModel.getColumnClass(i) == PendingAmount.class
                    || tableModel.getColumnClass(i) == OpeningAmount.class
                    || tableModel.getColumnClass(i) == LedgerOutstandingPendingAmount.class
                    || tableModel.getColumnClass(i) == LedgerOutstandingOpeningAmount.class
                    || tableModel.getColumnClass(i) == DaybookAmount.class
                    || tableModel.getColumnClass(i) == LedgerVoucherSummaryAmount.class) {
                ch = SystemUtilities.align(columnDetails[i][0].toString(), SystemUtilities.RIGHT,
                        ((Integer) columnDetails[i][1]).intValue() - INTER_COLUMN_SPACE);
            }
            columnHeader[i] = ch;
            incrementProgress();
        }

        // Generate the report header.
        ArrayList headerList = new ArrayList();
        headerList.add(SystemUtilities.getStringInSize("=", totalWidth, "="));
        Iterator iterator = SystemUtilities.getColumnBreakupIterator(columnHeader, specifiedColumnSize, INTER_COLUMN_SPACE,
                wrapIndentColumns);
        count = 3;
        while (iterator.hasNext()) {
            headerList.add("*" + iterator.next() + "*");
        }
        headerList.add(SystemUtilities.getStringInSize("=", totalWidth, "="));

        // Now create the heading for the first page and the second page.
        firstPageFullHeader = new String[headerList.size() + firstPageHeader.length];
        otherPageFullHeader = new String[headerList.size() + otherPageHeader.length];

        // First page heading.
        for (int i = 0; i < firstPageHeader.length; i++) {
            String str = "*" + SystemUtilities.getStringInSize(firstPageHeader[i], totalWidth, " ") + "*";
            firstPageFullHeader[i] = str;
            incrementProgress();
        }
        // Second page heading.
        for (int i = 0; i < otherPageHeader.length; i++) {
            String str = "*" + SystemUtilities.getStringInSize(otherPageHeader[i], totalWidth, " ") + "*";
            otherPageFullHeader[i] = str;
            incrementProgress();
        }
        // Add the report header.
        iterator = headerList.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            String str = (String) iterator.next();
            firstPageFullHeader[firstPageHeader.length + i] = str;
            otherPageFullHeader[otherPageHeader.length + i] = str;
            i++;
        }
    }

    /**
     *
     */
    private void writeHeader(int pageNumber, int startRow, int endRow) throws DatabaseException, IOException {
        // Determine the page header and write it into the database.
        String[] header = this.otherPageFullHeader;
        if (pageNumber == 1) {
            header = this.firstPageFullHeader;
        }
        for (int i = 0; i < header.length; i++) {
            //String str = SystemUtilities.getStringInSize(header[i], totalWidth, " ");
            //System.out.println(str);
            ReportFormat format = formattingModel.getReportFormat(ReportFormattingModel.PAGE_HEADER, i);
            data.add(new String[] { "heading" + (headerCount++),
                    header[i] == null ? SystemUtilities.replicate(" ", totalWidth) : header[i], format.getControlSet(),
                    format.getControlReset() });
            incrementProgress();
        }
        // Write the report body header.
        String[] bodyHeader = headerFooterModel.getReportHeader(startRow, endRow);
        if (bodyHeader == null || bodyHeader.length != bodyHeaderRowCount) {
            bodyHeader = new String[bodyHeaderRowCount];
        }
        //System.out.println("Body header length = " + bodyHeader.length);
        //
        for (int i = 0; i < bodyHeader.length; i++) {
            String str = "*" + SystemUtilities.getStringInSize(bodyHeader[i], totalWidth, " ") + "*";
            ReportFormat format = formattingModel.getReportFormat(ReportFormattingModel.REPORT_HEADER, i);
            data.add(new String[] { "heading" + (headerCount++), str, format.getControlSet(), format.getControlReset() });
            incrementProgress();
        }
    }

    private void writeFooter(int pageNumber, int startRow, int endRow) throws DatabaseException, IOException {
        // Write the report body footer.
        String[] reportFooter = headerFooterModel.getReportFooter(startRow, endRow);
        if (reportFooter == null || reportFooter.length != bodyFooterRowCount) {
            reportFooter = new String[bodyFooterRowCount];
        }
        for (int i = 0; i < reportFooter.length; i++) {
            String str = "*" + SystemUtilities.getStringInSize(reportFooter[i], totalWidth, " ") + "*";
            ReportFormat format = formattingModel.getReportFormat(ReportFormattingModel.REPORT_FOOTER, i);
            data.add(new String[] { "footer" + (footerCount++), str, format.getControlSet(), format.getControlReset() });
            incrementProgress();
        }
        // Write the page footer.
        String footer = null;

        if (pageNumber == 1) {
            footer = DateUtilities.getCurrentDateTime();
            footer += SystemUtilities.align("Page " + pageNumber, SystemUtilities.RIGHT, totalWidth - footer.length());
        } else {
            footer = "*" + SystemUtilities.align("Page " + pageNumber, SystemUtilities.RIGHT, totalWidth) + "*";
        }
        ReportFormat format = formattingModel.getReportFormat(ReportFormattingModel.PAGE_FOOTER, 0);
        data.add(new String[] { "FOOTER" + (footerCount++), footer, format.getControlSet(), format.getControlReset() });
    }

    private void writeReport() throws DatabaseException, IOException, ArrayIndexOutOfBoundsException {
        // Write the body.
        progressBox.setProgressStatus("Exporting");

        totalRows = tableModel.getRowCount();
        totalLinesFooter = this.bodyFooterRowCount + 1; // one extra for page footer.
        int firstPageTotalHeaderRows = firstPageFullHeader.length + bodyHeaderRowCount;
        int otherPageTotalHeaderRows = otherPageFullHeader.length + bodyHeaderRowCount;
        int totalBodyLines = 0;

        int printRowCount = 0;
        int pages = 0;
        int startRow = 0;
        int endRow = 0;
        ArrayList pendingLines = new ArrayList();

        for (int i = 0; i < totalRows; i++) {
            // reinitialize the toWrite array to null.
            String[] toWrite = new String[columnDetails.length];
            // iterate through each and every columns.
            for (int j = 0; j < columnDetails.length; j++) {
                Object obj = tableModel.getValueAt(i, j);
                if (obj == null) {
                    obj = "";
                }
                String str = obj.toString();
                int columnLength = ((Integer) columnDetails[j][1]).intValue();
                if (obj instanceof java.util.Date) {
                    str = DateUtilities.toString((java.util.Date) obj);
                } else if (obj instanceof Float) {
                    str = getFormattedBalanceText((Float) obj);
                    str = SystemUtilities.align(str, SystemUtilities.RIGHT, columnLength - INTER_COLUMN_SPACE);
                } else if (obj instanceof Balance) {
                    str = getFormattedBalanceText((Balance) obj);
                    str = SystemUtilities.align(str, SystemUtilities.RIGHT, columnLength - INTER_COLUMN_SPACE);
                } else if (obj instanceof Double) {
                    str = getFormattedBalanceText((Double) obj);
                    str = SystemUtilities.align(str, SystemUtilities.RIGHT, columnLength - INTER_COLUMN_SPACE);
                }

                toWrite[j] = str; //SystemUtilities.getStringInSize(str, columnLength, " ");
                // Progress bar incrementation
                progressBox.setProgressStatus("Exporting (" + i + "/" + totalRows + ")");
                incrementProgress();
            }
            // Generate a proper breakup of the columns and write
            // them down into the data file.
            Iterator iterator = SystemUtilities.getColumnBreakupIterator(toWrite, specifiedColumnSize, INTER_COLUMN_SPACE,
                    wrapIndentColumns);
            int wrapCount = 1;
            while (iterator.hasNext()) {
                // Check header.
                if (printRowCount == 0) {
                    pages++;
                    if (pages <= 1) {
                        totalLinesHeader = firstPageTotalHeaderRows;
                    } else {
                        totalLinesHeader = otherPageTotalHeaderRows;
                    }
                    totalBodyLines = totalLinesPerPage - totalLinesHeader - totalLinesFooter;
                    startRow = i;
                    endRow = i;

                    // Write the header.
                    writeHeader(pages, startRow, endRow);
                }
                ReportFormat format = formattingModel.getReportFormat(ReportFormattingModel.REPORT_BODY, i);
                String controlSet = format.getControlSet();
                String controlReset = format.getControlReset();
                data.add(new String[] { "body" + (i + 1) + "-" + (wrapCount++), "*" + iterator.next() + "*", controlSet, controlReset });
                printRowCount++;
                // Check footer.
                if (exportType == Exportable.PRINT_EXPORT) { // Write any report footer only in case of print export
                    if (printRowCount >= totalBodyLines) {
                        writeFooter(pages, startRow, endRow);
                        printRowCount = 0;
                    }
                }
            }
            endRow++;
        }
        if (exportType == Exportable.SAVE_EXPORT) {
            writeFooter(1, 0, endRow);
        } else if (printRowCount < totalBodyLines) {
            writeFooter(pages, startRow, endRow);
        }
    }

    /**
     * Exports the table model into the given file.
     */
    public Data export(final String file) throws DatabaseException, ResourceKeyNotDefinedException, IOException {
        progressBox = new ProgressBox(MessageBox.getParentFrame(), "Please Wait..");
        exception = null;
        Thread workThread = new Thread() {
            public void run() {
                try {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                    }

                    progressBox.setProgressStatus("Initializing");

                    if (formattingModel == null) {
                        formattingModel = new ReportFormattingModel() {
                            public ReportFormat getReportFormat(int rowIdentifier, int row) {
                                return new ReportFormat("", "");
                            }
                        };
                    }

                    data = initializeData(file); // Create a unique database file.

                    determineHeader(); // Define the attributes relating to the header.

                    writeReport(); // Write the complete report.
                } catch (DatabaseException ex) {
                    ex.printStackTrace();
                    exception = ex;
                } catch (IOException ex) {
                    ex.printStackTrace();
                    exception = ex;
                } catch (ArrayIndexOutOfBoundsException ex) { // This strange bug was encountered at run time.
                    ex.printStackTrace();
                    exception = new DatabaseException(ex.getMessage());
                } finally {
                    // Stop the progressbox
                    progressBox.stop();
                    progressBox.setVisible(false);
                    progressBox.dispose();
                }
            }
        };
        progressBox.setMinimum(0);
        progressBox.start();
        workThread.start();
        progressBox.setVisible(true);

        // If any exception is thrown in the export process.
        if (exception != null) {
            try {
                data.close();
            } catch (Exception ex) {
            }
            if (exception instanceof DatabaseException) {
                throw new DatabaseException(exception.getMessage());
            } else if (exception instanceof IOException) {
                throw new IOException(exception.getMessage());
            }
        }
        return data;
    }

    /**
     * This method returns the textual representation of the amount denoted
     * by the balance object.
     */
    private String getFormattedBalanceText(Balance balance) {
        String formattedText = "";
        double amount = balance.getBalanceAmount();
        formattedText = getFormattedBalanceText(new Double(amount));
        if (amount != 0) {
            formattedText += balance.getBalanceType() == DesignConstants.DEBIT_BALANCE ? " Dr " : " Cr ";
        }
        return formattedText;
    }

    /**
     * This method returns the textual representation of the amount denoted
     * by the balance object.
     */
    private String getFormattedBalanceText(Float balance) {
        if (balance.doubleValue() == 0) {
            return "";
        }
        return getFormattedBalanceText(new Double(balance.doubleValue()));
    }

    /**
     * This method returns the textual representation of the amount denoted
     * by the balance object.
     */
    private String getFormattedBalanceText(Double balance) {
        if (balance.doubleValue() == 0) {
            return "";
        }
        DecimalFormat formatter = new DecimalFormat("###,##0.00");
        return formatter.format(balance.doubleValue());
    }
}
