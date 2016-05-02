package ecf3.ui;

import ecf3.ECFUtil;
import ecf3.model.Layer;
import ecf3.model.Model;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;

public class ECFPrinterJob implements Printable, Pageable {

    public boolean fPrintHeadings = false;
    Model model = Model.getInstance();
    int clayer = model.getNoOfLayers(); // number of layers
    MainFrame mainframe = MainFrame.getInstance();
    // Different PRINT_SCALE requires recompiling of LayerPanel and ECFNode!
    protected double PRINT_SCALE = Model.scaleDisplay;
    // Width and height used by LayerPanel and ECFNode.
    private int panelwidth = ECFUtil.PANEL_WIDTH;
    private int panelheight = ECFUtil.PANEL_HEIGHT;
    private double xOffset = 0.0;
    private double yOffset = 0.0;

    ECFPrinterJob(boolean fPrintHeadings) {
        this.fPrintHeadings = fPrintHeadings;
//		clayer = ECF.getNoOfLayers();
        PrinterJob printerjob = PrinterJob.getPrinterJob();
        printerjob.setPrintable(this);
        printerjob.setPageable(this);
        if (printerjob.printDialog()) {
            try {
                printerjob.print();
            } catch (Exception printexception) {
                System.out.println("ECFPrinterJob: " + printexception);
            } // try/catch
        } // if
    } // ECFPrinterJob

    /** Get the number of pages to be printed.
     * @return The number of pages. */
    @Override
    public int getNumberOfPages() {
        return ECFUtil.NUMBER_OF_PAGES_WIDE * ECFUtil.NUMBER_OF_PAGES_HIGH;
    } // getNumberOfPages

    @Override
    public PageFormat getPageFormat(int ipage) {
//		System.out.println("ECFPrinterJob.getPageFormat: ipage=" + ipage);
        // Define imagable area on paper
        double mm2pixel = 72 / 25.4;
        int vrcxOffset[] = {0, 1, 0, 1};
        int vrcyOffset[] = {0, 0, 1, 1};
        Paper paper = new Paper();
        double x = 20 * mm2pixel;
        double y = 10 * mm2pixel;
        xOffset = vrcxOffset[ipage] * 160 * mm2pixel;
        yOffset = vrcyOffset[ipage] * 240 * mm2pixel;
        double width = (210 - 20) * 72 / 25.4;
        double height = (297 - 10) * 72 / 25.4;
        paper.setImageableArea(x - xOffset, y - yOffset,
                ECFUtil.NUMBER_OF_PAGES_WIDE * width, ECFUtil.NUMBER_OF_PAGES_HIGH * height);
        // Page format
        PageFormat pageformat = new PageFormat();
        pageformat.setPaper(paper);
        return pageformat;
    } // getPageFormat

    @Override
    public Printable getPrintable(int ipage) {
        return this;
    }

    @Override
    public int print(java.awt.Graphics g, java.awt.print.PageFormat pageformat, int ipage) {

        // Layer index
        GraphsPanel panelGraphs = mainframe.getGraphsPanel();
        LayerFrame frame = (LayerFrame) panelGraphs.getSelectedFrame();
        LayerPanel layerpanel = frame.getLayerPanel();
        Layer layer = null;
        if (frame != null) {
            layer = frame.getModelLayer();
        }
        // Print heading line 1 or top of diagram
        int y = 10;
        int x = 0;

        if (ipage < getNumberOfPages()) {

//			System.out.println("ECFPrinterJob.print: ipage=" + ipage);

            Graphics2D g2 = (Graphics2D) g;
            g2.translate(pageformat.getImageableX(), pageformat.getImageableY());

            if (fPrintHeadings) {

                // File Name
                g2.drawString(mainframe.getFileName(), (int) (x + xOffset), (int) (y + yOffset));
                // Layer name
//		GraphsPanel panelGraphs = ECF.frameMain.getGraphsPanel();
//            LayerFrame frame = (LayerFrame)panelGraphs.getSelectedFrame();
//            int ilayer = 0;
//            if (frame!=null) ilayer = frame.getIndex();
//			System.out.println("ECFPrinterJob.print: ilayer=" + ilayer);
                String szLayer = layer.getName();
                x = (int) (0.35 * ECFUtil.PAGE_WIDTH);
                g2.drawString(szLayer, (int) (x + xOffset), (int) (y + yOffset));
                // Time
                String szTime = "Time: " + model.getTimeAxis().getSelectedTime();
                x = (int) (0.55 * ECFUtil.PAGE_WIDTH);
                g2.drawString(szTime, (int) (x + xOffset), (int) (y + yOffset));

                // Print heading line 2
                y = 50;
                x = 0; // (x,y) = upper left corner of enclosing triangle
                String szDescTime = model.getTimeAxis().getSelectedIncrementDescription();
                GraphLabel graphlbl = new GraphLabel(szDescTime, x, y, Color.white);
                y += graphlbl.getHeight();  // (x,y) = lower left corner of enclosing triangle
                // Time increment description
                graphlbl.paint(g2);

                // Print time for printing
                int xTime = (int) (0.71 * ECFUtil.PAGE_WIDTH) - 60;
                int yTime = (int) (0.71 * ECFUtil.PAGE_HEIGHT + 120);
                xTime = (int) (400 + xOffset);
                yTime = (int) (730 + yOffset);
//			System.out.println("ECFPrinterJob.print: Time printed at x=" + xTime + " y= " + yTime +
//				" time: " + ECFUtil.getCurrentTime());
                g2.drawString(ECFUtil.getCurrentTime(), xTime, yTime);

            } // if fPrintHeadings

            // Print model
            g2.translate(0, y);
            double sc = PRINT_SCALE;
            g2.scale(sc, sc);
            model.paintRootCurrent(g,layer);

            return Printable.PAGE_EXISTS;
        }
        return Printable.NO_SUCH_PAGE;
    } // print
} // class ECFPrinterJob


