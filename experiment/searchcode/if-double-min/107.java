<<<<<<< HEAD
/*
 * Copyright 2012 AndroidPlot.com
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.androidplot.util;

import android.graphics.PointF;
import android.graphics.RectF;

/**
 * Utility methods for converting pixel coordinates into real values and vice versa.
 */
public class ValPixConverter {
    private static final int ZERO = 0;


    public static float valToPix(double val, double min, double max, float lengthPix, boolean flip) {
        if(lengthPix <= ZERO) {
            throw new IllegalArgumentException("Length in pixels must be greater than 0.");
        }
        double range = range(min, max);
        double scale = lengthPix / range;
        double raw = val - min;
        float pix = (float)(raw * scale);

        if(flip) {
            pix = (lengthPix - pix);
        }
        return pix;
    }

    public static double range(double min, double max) {
        return (max-min);
    }

    
    public static double valPerPix(double min, double max, float lengthPix) {
        double valRange = range(min, max);
        return valRange/lengthPix;
    }

    /**
     * Convert a value in pixels to the type passed into min/max
     * @param pix
     * @param min
     * @param max
     * @param lengthPix
     * @param flip True if the axis should be reversed before calculated. This is the case
     * with the y axis for screen coords.
     * @return
     */
    public static double pixToVal(float pix, double min, double max, float lengthPix, boolean flip) {
        if(pix < ZERO) {
            throw new IllegalArgumentException("pixel values cannot be negative.");
        }

        if(lengthPix <= ZERO) {
            throw new IllegalArgumentException("Length in pixels must be greater than 0.");
        }
        float pMult = pix;
        if(flip) {
            pMult = lengthPix - pix;
        }
        double range = range(min, max);
        return ((range / lengthPix) * pMult) + min;
    }

    /**
     * Converts a real value into a pixel value.
     * @param x Real d (domain) component of the point to convert.
     * @param y Real y (range) component of the point to convert.
     * @param plotArea
     * @param minX Minimum visible real value on the d (domain) axis.
     * @param maxX Maximum visible real value on the y (domain) axis.
     * @param minY Minimum visible real value on the y (range) axis.
     * @param maxY Maximum visible real value on the y (range axis.
     * @return
     */
    public static PointF valToPix(Number x, Number y, RectF plotArea, Number minX, Number maxX, Number minY, Number maxY) {
        float pixX = ValPixConverter.valToPix(x.doubleValue(), minX.doubleValue(), maxX.doubleValue(), plotArea.width(), false) + (plotArea.left);
        float pixY = ValPixConverter.valToPix(y.doubleValue(), minY.doubleValue(), maxY.doubleValue(), plotArea.height(), true) + plotArea.top;
        return new PointF(pixX, pixY);
    }
}
=======
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kuhnlab.trixy;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import javax.swing.filechooser.FileFilter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import kuhnlab.coordinates.KPoint2D;
import kuhnlab.gui.GenericInputDialog;
import kuhnlab.gui.GenericOptionsDialog;
import kuhnlab.gui.GenericOptionsPanel;
import kuhnlab.gui.TableOutputDialog;
import kuhnlab.math.StatTools;
import kuhnlab.trixy.data.io.CommaFileHandler;
import kuhnlab.trixy.data.io.PtiFileHandler;
import kuhnlab.trixy.data.io.SerializedFileHandler;
import kuhnlab.trixy.data.Series;
import kuhnlab.trixy.data.io.SeriesFileFilter;
import kuhnlab.trixy.data.io.SeriesFileHandler;
import kuhnlab.trixy.data.SeriesList;
import kuhnlab.trixy.data.io.KinsimFileHandler;
import kuhnlab.trixy.data.io.SoftmaxFileHandler;
import kuhnlab.trixy.data.io.TabbedFileHandler;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationAction;
import org.jdesktop.application.ApplicationActionMap;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.utils.AppHelper;
import org.jdesktop.application.utils.PlatformType;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.ui.RectangleEdge;

/**
 *
 * @author jrkuhn
 */
public class TrixyApp extends SingleFrameApplication implements ClipboardOwner,
        ListDataListener {

    protected TrixyAboutBox aboutBox;
    public SeriesList data;
    public File currentFile;
    public FileFilter currentFileType;
    public File currentPath;
    public boolean modified = false;
    protected JList seriesJList;
    protected JPanel blankPanel;
    protected JScrollPane seriesScrollPane;
    protected JSplitPane splitPane;
    protected ChartPanel mainChartPanel;
    protected JFreeChart mainChart;
    protected XYItemRenderer mainChartRenderer;
    protected String appName;
    protected FrameView view;
    public SeriesList undoBuffer;
    public SeriesJList dataListComponent;
    protected boolean replace = false;
    protected boolean rename = true;
    protected ChartPanelMouseAdapter markListener;
    protected boolean fileOpen = false;
    protected boolean zoomMode = false;
    protected boolean legendVisible = false;
    protected boolean pasteAvailable = false;
    protected boolean undoAvailable = false;
    protected SeriesFileHandler[] seriesReaders = {
        new CommaFileHandler(), new TabbedFileHandler(), new SoftmaxFileHandler(),
        new PtiFileHandler(), new SerializedFileHandler()
    };
    protected SeriesFileHandler[] seriesWriters = {
        new CommaFileHandler(), new TabbedFileHandler(),
        new PtiFileHandler(), new KinsimFileHandler(), new SerializedFileHandler()
    };

    public boolean isFileOpen() {
        return fileOpen;
    }

    public void setFileOpen(boolean fileOpen) {
        boolean old = this.fileOpen;
        this.fileOpen = fileOpen;
        firePropertyChange("fileOpen", old, this.fileOpen);
    }

    public boolean isZoomMode() {
        return zoomMode;
    }

    public void setZoomMode(boolean zoomMode) {
        boolean old = this.zoomMode;
        this.zoomMode = zoomMode;
        firePropertyChange("zoomMode", old, this.zoomMode);
    }

    public boolean isLegendVisible() {
        return legendVisible;
    }

    public void setLegendVisible(boolean legendVisible) {
        boolean old = this.legendVisible;
        this.legendVisible = legendVisible;
        firePropertyChange("legendVisible", old, this.legendVisible);
    }

    public boolean isPasteAvailable() {
        return pasteAvailable;
    }

    public void setPasteAvailable(boolean pasteAvailable) {
        boolean old = this.pasteAvailable;
        this.pasteAvailable = pasteAvailable;
        firePropertyChange("pasteAvailable", old, this.pasteAvailable);
    }

    public boolean isUndoAvailable() {
        return undoAvailable;
    }

    public void setUndoAvailable(boolean undoAvailable) {
        boolean old = this.undoAvailable;
        this.undoAvailable = undoAvailable;
        firePropertyChange("undoAvailable", old, this.undoAvailable);
    }

    @Override
    protected void startup() {
        currentFile = null;
        currentFileType = null;
        data = null;
        undoBuffer = null;
        mainChart = null;
        mainChartRenderer = null;
        mainChartPanel = null;
        modified = false;

        final PlatformType platform = AppHelper.getPlatform();
        if (PlatformType.OS_X.equals(platform)) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.brushMetalLook", "true");
            //System.setProperty("apple.awt.fileDialogForDirectories", "true");
        }

        appName = getResourceString("Application.title");
        currentPath = new File(System.getProperty("user.home"));
        view = new FrameView(this);
        initView(view);
        this.addExitListener(new Application.ExitListener() {
            public boolean canExit(EventObject event) {
                return canExitApplication();
            }

            public void willExit(EventObject event) {
            }
        });

        show(view);
    }

    /**
     * A convenient static getter for the application instance.
     *
     * @return the instance of TrixyApp
     */
    public static TrixyApp getApplication() {
        return Application.getInstance(TrixyApp.class);
    }

    public String getResourceString(String name) {
        return getContext().getResourceMap().getString(name);
    }

    /**
     * Create a menu from a list of strings. Allows for sub-sub menus.
     *
     * @param items
     * @return
     */
    protected JMenu createMenu(String menuName) {
        JMenu menu = new JMenu();
        menu.setName(menuName);
        String menuItemsString = getResourceString(menuName + "_items");
        String[] menuItems = menuItemsString.split("\\s");
        for (String item : menuItems) {
            if (item.startsWith(">")) {
                menu.add(createMenu(item.substring(1)));
            } else if (item.startsWith("---")) {
                menu.add(new JSeparator());
            } else {
                boolean showIcon;
                if (item.startsWith("@")) {
                    showIcon = true;
                    item = item.substring(1);
                } else {
                    showIcon = false;
                }
                ApplicationAction action = getAction(item);
                boolean isSelectable = action.getValue(javax.swing.Action.SELECTED_KEY) != null;
                JMenuItem menuItem = isSelectable ? new JCheckBoxMenuItem(action) : new JMenuItem(action);
                if (!showIcon) {
                    menuItem.setIcon(null);
                }
                menu.add(menuItem);
            }
        }
        return menu;
    }

    protected JMenuBar createMenuBar() {
        String menusString = getResourceString("mainMenu");
        String[] menus = menusString.split("\\s");
        JMenuBar menuBar = new JMenuBar();
        for (String menu : menus) {
            menuBar.add(createMenu(menu));
        }
        return menuBar;
    }

    protected JToolBar createToolBar() {
        String itemsString = getResourceString("mainToolBar");
        String[] items = itemsString.split("\\s");
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        for (String item : items) {
            if (item.startsWith("---")) {
                toolBar.addSeparator();
            } else {
                ApplicationAction action = getAction(item);
                boolean isSelectable = action.getValue(javax.swing.Action.SELECTED_KEY) != null;
                AbstractButton button = isSelectable ? new JToggleButton(action) : new JButton(action);
                button.setText(null);
                button.setFocusable(false);
                toolBar.add(button);
            }
        }
        return toolBar;
    }

    protected ApplicationAction getAction(String actionName) {
        ApplicationActionMap map = getContext().getActionMap();
        return (ApplicationAction) map.get(actionName);
    }

    protected void initView(FrameView view) {
        splitPane = new javax.swing.JSplitPane();
        seriesScrollPane = new javax.swing.JScrollPane();
        seriesJList = new javax.swing.JList();
        blankPanel = new javax.swing.JPanel();

        splitPane.setDividerLocation(120);
        seriesJList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        seriesScrollPane.setViewportView(seriesJList);
        dataListComponent = new SeriesJList();
        seriesScrollPane.setViewportView(dataListComponent);

        splitPane.setLeftComponent(seriesScrollPane);

        blankPanel.setLayout(new java.awt.BorderLayout());

        blankPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("Button.disabledForeground"));
        blankPanel.setMinimumSize(new java.awt.Dimension(100, 100));
        blankPanel.setPreferredSize(new java.awt.Dimension(400, 400));
        splitPane.setRightComponent(blankPanel);

        view.setComponent(splitPane);

        JMenuBar menuBar = createMenuBar();
        view.setMenuBar(menuBar);

        JToolBar toolBar = createToolBar();
        view.setToolBar(toolBar);
    }

    //=======================================================================
    // Helper Methods
    //=======================================================================
    public SeriesList readSeriesFile(File file, FileFilter filter) {
        SeriesFileHandler mainHandler = null;
        List<SeriesFileHandler> possibleHandlers = new ArrayList<SeriesFileHandler>();
        // search for a single handler or a list of possible handlers
        for (SeriesFileHandler handler : seriesReaders) {
            if (filter == handler.getFilter()) {
                // the user specifically requested this file type
                mainHandler = handler;
                break;
            } else if (handler.isFileExtension(file)) {
                possibleHandlers.add(handler);
            }
        }

        if (mainHandler == null) {
            if (possibleHandlers.size() == 1) {
                // only one possible handler
                mainHandler = possibleHandlers.get(0);
            } else if (possibleHandlers.size() > 0) {
                // could not find a single handler for this file extension
                // guess handler based on file signature
                for (SeriesFileHandler handler : possibleHandlers) {
                    if (handler.isFileSignature(file)) {
                        mainHandler = handler;
                        break;
                    }
                }
            }
        }

        if (mainHandler == null) {
            // could not find any handlers
            return null;
        }

        // Allow the handler to prompt for options
        GenericOptionsPanel op = mainHandler.getOptionsPanel(false);
        if (op != null) {
            GenericOptionsDialog gd = new GenericOptionsDialog(view.getFrame(), "Read file options");
            gd.addOptionsTab(op.getName(), op);
            if (!gd.showDialog()) {
                return null;
            }
        }

        return mainHandler.readFile(file);
    }

    public void openData(File file, FileFilter filter) {
        currentPath = new File(file.getParent());
        SeriesList newdata = readSeriesFile(file, filter);
        if (newdata == null) {
            JOptionPane.showMessageDialog(view.getFrame(), "Unable to open file " + file.getName(), appName, JOptionPane.ERROR_MESSAGE);
            return;
        }

        closeData();
        currentFile = file;
        currentFileType = filter;
        data = newdata;
        ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
        mainChart = ChartFactory.createXYLineChart(null, "X", "Y", data, PlotOrientation.VERTICAL, false, true, false);
        mainChartRenderer = mainChart.getXYPlot().getRenderer();

        mainChartPanel = new ChartPanel(mainChart);
        if (markListener == null) {
            markListener = new ChartPanelMouseAdapter();
        }
        mainChartPanel.addMouseListener(markListener);
        mainChartPanel.addMouseMotionListener(markListener);

        splitPane.setRightComponent(mainChartPanel);

        dataListComponent.setSeriesList(data, mainChartRenderer);
        dataListComponent.addListDataListener(this);
        splitPane.setDividerLocation(120);

        modified = false;
        updateTitle();
        setFileOpen(true);

        setZoomMode(false);
        updateZoomMode();
        setLegendVisible(false);
        updateLegendVisible();

        // add X and Y axes
        XYPlot plot = mainChart.getXYPlot();
        java.awt.Stroke stroke = new java.awt.BasicStroke(0.5f);
        //plot.addAnnotation(new XYAxisAnnotation(0, XYAxisAnnotation.DOMAIN_AXIS, stroke, Color.GRAY));
        //plot.addAnnotation(new XYAxisAnnotation(0, XYAxisAnnotation.RANGE_AXIS, stroke, Color.GRAY));
        mainChart.fireChartChanged();
        updateColors();
        updateTitle();
        dataListComponent.clearSelection();
        dataListComponent.repaint();

        splitPane.validate();
    }

    public void closeData() {
        currentFile = null;
        currentFileType = null;
        data = null;
        undoBuffer = null;
        mainChart = null;
        mainChartRenderer = null;
        if (markListener != null && mainChartPanel != null) {
            mainChartPanel.removeMouseListener(markListener);
            mainChartPanel.removeMouseMotionListener(markListener);
        }
        mainChartPanel = null;

        splitPane.setRightComponent(blankPanel);

        dataListComponent.removeListDataListener(this);
        dataListComponent.clearSeriesList();

        modified = false;
        updateTitle();
        setFileOpen(false);
        view.getFrame().validate();
    }

    public boolean writeSeriesFile(SeriesList data, File file, FileFilter filter) {
        // search for a single handler or a list of possible handlers
        SeriesFileHandler mainHandler = null;
        List<SeriesFileHandler> possibleHandlers = new ArrayList<SeriesFileHandler>();
        for (SeriesFileHandler handler : seriesWriters) {
            if (filter == handler.getFilter()) {
                // the user specifically requested this file type
                mainHandler = handler;
                break;
            } else if (handler.isFileExtension(file)) {
                possibleHandlers.add(handler);
            }
        }

        if (mainHandler == null && possibleHandlers.size() > 0) {
            // could not find a single handler. Just use the first file
            // type that matches this file extension
            mainHandler = possibleHandlers.get(0);
        }

        if (mainHandler == null) {
            // could not find any handlers
            return false;
        }

        GenericOptionsPanel op = mainHandler.getOptionsPanel(true);
        if (op != null) {
            GenericOptionsDialog gd = new GenericOptionsDialog(view.getFrame(), "Save file options");
            gd.addOptionsTab(op.getName(), op);
            if (!gd.showDialog()) {
                return false;
            }
        }
        return mainHandler.writeFile(data, file);
    }

    public boolean saveData(File newfile, FileFilter filter) {
        boolean ret = writeSeriesFile(data, newfile, filter);
        if (!ret) {
            JOptionPane.showMessageDialog(view.getFrame(), "Unable to save file\n" + newfile.getName(), appName, JOptionPane.ERROR_MESSAGE);
            return false;
        }
        currentFile = newfile;
        currentFileType = filter;
        currentPath = new File(newfile.getParent());
        modified = false;
        updateTitle();
        view.getFrame().validate();
        return true;
    }

    public boolean canExitApplication() {
        if (modified) {
            JOptionPane op = new JOptionPane();
            int choice = JOptionPane.showConfirmDialog(null, "The file was not saved. Exit anyway?", appName, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) {
                return false;
            }
        }
        return true;
    }

    public SeriesList getSelectedSeries() {
        return data.subList(dataListComponent.getSelectedIndices());
    }

    public int getLastSelectedIndex() {
        int[] indices = dataListComponent.getSelectedIndices();
        if (indices != null && indices.length > 0) {
            return indices[indices.length - 1];
        } else {
            return -1;
        }
    }

    public void setSelectedSeries(SeriesList sublist) {
        if (sublist == null) {
            dataListComponent.clearSelection();
        }
        int nSeries = data.getSeriesCount();
        List<Integer> indexList = new ArrayList<Integer>();
        for (int index = 0; index < nSeries; index++) {
            if (sublist.getSeries().contains(data.getSeries(index))) {
                indexList.add(new Integer(index));
            }
        }
        if (indexList.size() > 0) {
            // convert List to array
            int nSelected = indexList.size();
            int[] indices = new int[nSelected];
            for (int i = 0; i < nSelected; i++) {
                indices[i] = indexList.get(i);
            }
            dataListComponent.setSelectedIndices(indices);
        } else {
            dataListComponent.clearSelection();
        }
    }

    public void updateColors() {
        // Attempt to obtain colors of each of the plot lines
        AbstractRenderer renderer;
        if (mainChartRenderer instanceof AbstractRenderer) {
            renderer = (AbstractRenderer) mainChartRenderer;
        } else {
            return;
        }
        for (int i = 0; i < data.getSeriesCount(); i++) {
            Paint paint = renderer.lookupSeriesPaint(i);
            if (paint instanceof Color) {
                data.getSeries(i).setColor((Color) paint);
            }
        }
    }

    public void updateTitle() {
        if (currentFile != null) {
            getMainFrame().setTitle(appName + " - " + currentFile.getName() + (modified ? " *" : ""));
        } else {
            getMainFrame().setTitle(appName);
        }
    }

    public void startModify() {
        data.saveVisibility(mainChartRenderer);
        undoBuffer = (SeriesList) data.clone();
    }

    public void endModify() {
        modified = true;
        data.restoreVisibility(mainChartRenderer, false);
        mainChart.fireChartChanged();
        setUndoAvailable(undoBuffer != null);
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        boolean canPaste = data.canImport(clip.getAvailableDataFlavors());
        setPasteAvailable(canPaste);
        updateColors();
        updateTitle();
        dataListComponent.repaint();
    }

    void updateZoomMode() {
        if (mainChartPanel == null) {
            return;
        }
        mainChartPanel.setDomainZoomable(zoomMode);
        mainChartPanel.setRangeZoomable(zoomMode);
    }

    void updateLegendVisible() {
        if (mainChart != null) {
            if (legendVisible) {
                LegendTitle legend = new LegendTitle(mainChart.getXYPlot());
                legend.setMargin(1.0, 1.0, 1.0, 1.0);
                legend.setFrame(new BlockBorder());
                legend.setBackgroundPaint(Color.white);
                legend.setPosition(RectangleEdge.RIGHT);
                mainChart.addLegend(legend);
            } else {
                mainChart.removeLegend();
            }
        }
    }

    void markXRange(double xmin, double xmax) {
        if (mainChart == null) {
            return;
        }
        IntervalMarker mark = new IntervalMarker(xmin, xmax);
        mark.setPaint(new Color(240, 240, 255));
        XYPlot plot = mainChart.getXYPlot();
        plot.clearDomainMarkers();
        plot.addDomainMarker(mark, org.jfree.ui.Layer.BACKGROUND);
    }

    void clearMarkedXRange() {
        if (mainChart == null) {
            return;
        }
        XYPlot plot = mainChart.getXYPlot();
        plot.clearDomainMarkers();
    }

    double[] getMarkedXRange() {
        if (mainChart == null) {
            return null;
        }
        XYPlot plot = mainChart.getXYPlot();
        Collection<Marker> markers = plot.getDomainMarkers(org.jfree.ui.Layer.BACKGROUND);
        if (markers == null) {
            return null;
        }
        for (Marker mark : markers) {
            if (mark instanceof IntervalMarker) {
                IntervalMarker imark = (IntervalMarker) mark;
                double[] range = {imark.getStartValue(), imark.getEndValue()};
                return range;
            }
        }
        return null;
    }

    //=======================================================================
    // ListDataListener implementation
    //=======================================================================
    public void intervalAdded(ListDataEvent e) {
        modified = true;
        mainChart.fireChartChanged();
        updateColors();
        dataListComponent.repaint();
    }

    public void intervalRemoved(ListDataEvent e) {
        modified = true;
        mainChart.fireChartChanged();
        updateColors();
        dataListComponent.repaint();
    }

    public void contentsChanged(ListDataEvent e) {
        modified = true;
        mainChart.fireChartChanged();
        updateColors();
        dataListComponent.repaint();
    }

    //=======================================================================
    // ClipboardOwner implementation
    //=======================================================================
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        boolean canImport = data.canImport(clipboard.getAvailableDataFlavors());
        setPasteAvailable(canImport);
    }

    //=======================================================================
    // MouseListener utility class to handle chart selection
    //=======================================================================
    public class ChartPanelMouseAdapter implements MouseListener, MouseMotionListener {

        Rectangle2D markArea = null;
        Point markStart = null;

        protected Point constrainPoint(int x, int y, Rectangle2D area) {
            int xmin = (int) Math.floor(area.getMinX()), xmax = (int) Math.ceil(area.getMaxX());
            int ymin = (int) Math.floor(area.getMinY()), ymax = (int) Math.ceil(area.getMaxY());
            x = (int) Math.max(xmin, Math.min(x, xmax));
            y = (int) Math.max(ymin, Math.min(y, ymax));
            return new Point(x, y);
        }

        public void mouseClicked(MouseEvent e) {
            clearMarkedXRange();
        }

        public void mouseMoved(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
            Object src = e.getSource();
            if (!(src instanceof ChartPanel) || zoomMode) {
                return;
            }
            ChartPanel chart = (ChartPanel) src;

            if (markArea == null) {
                Rectangle2D screenDataArea = chart.getScreenDataArea(e.getX(), e.getY());
                if (screenDataArea != null) {
                    markStart = constrainPoint(e.getX(), e.getY(), screenDataArea);
                } else {
                    markStart = null;
                }
            }
        }

        public void mouseDragged(MouseEvent e) {
            Object src = e.getSource();
            if (!(src instanceof ChartPanel) || zoomMode || markStart == null) {
                return;
            }
            ChartPanel chart = (ChartPanel) src;

            Graphics2D g2 = (Graphics2D) chart.getGraphics();
            g2.setXORMode(java.awt.Color.YELLOW);

            if (this.markArea != null) {
                g2.draw(markArea);
            }

            Rectangle2D plotArea = chart.getScreenDataArea(
                    (int) markStart.getX(), (int) markStart.getY());

            double xright = Math.min(e.getX(), plotArea.getMaxX());
            markArea = new Rectangle2D.Double(
                    markStart.getX(), plotArea.getMinY(),
                    xright - markStart.getX(), plotArea.getHeight());

            if (markArea != null) {
                g2.draw(markArea);
            }
            g2.dispose();

        }

        public void mouseReleased(MouseEvent e) {
            Object src = e.getSource();
            if (zoomMode) {
                setZoomMode(false);
                updateZoomMode();
                return;
            }
            if (!(src instanceof ChartPanel) || markArea == null) {
                return;
            }
            ChartPanel chart = (ChartPanel) src;

            Graphics2D g2 = (Graphics2D) chart.getGraphics();
            g2.setXORMode(java.awt.Color.YELLOW);

            if (markArea != null) {
                g2.draw(markArea);
            }

            Rectangle2D plotArea = chart.getScreenDataArea(
                    (int) markStart.getX(), (int) markStart.getY());

            double xright = Math.min(e.getX(), plotArea.getMaxX());
            markArea = new Rectangle2D.Double(
                    markStart.getX(), plotArea.getMinY(),
                    xright - markStart.getX(), plotArea.getHeight());

            XYPlot plot = chart.getChart().getXYPlot();
            ValueAxis xaxis = plot.getDomainAxis();
            double xminfrac = (markArea.getMinX() - plotArea.getMinX()) / plotArea.getWidth();
            double xmaxfrac = (markArea.getMaxX() - plotArea.getMinX()) / plotArea.getWidth();
            double xmin = xminfrac * (xaxis.getUpperBound() - xaxis.getLowerBound()) + xaxis.getLowerBound();
            double xmax = xmaxfrac * (xaxis.getUpperBound() - xaxis.getLowerBound()) + xaxis.getLowerBound();

            markXRange(xmin, xmax);
            markStart = null;
            markArea = null;
        }
    }

    //=======================================================================
    // FILE MENU Action Handlers
    //=======================================================================
    @Action
    public void newFile() {
        setFileOpen(true);
    }

    @Action
    public void openFile() {
        if (modified) {
            JOptionPane op = new JOptionPane();
            int choice = JOptionPane.showConfirmDialog(null, "The file was not saved. Replace anyway?", appName, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }
        JFileChooser fc = new JFileChooser(currentPath);
        fc.setAcceptAllFileFilterUsed(true);
        for (SeriesFileHandler handler : seriesReaders) {
            fc.addChoosableFileFilter(handler.getFilter());
        }
        fc.setFileFilter(currentFileType != null ? currentFileType : fc.getAcceptAllFileFilter());

        //fc.setFileHidingEnabled(false);
        fc.setFileHidingEnabled(true);
        if (fc.showOpenDialog(view.getFrame()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        openData(fc.getSelectedFile(), fc.getFileFilter());
    }

    @Action(enabledProperty = "fileOpen")
    public void saveFile() {
        if (data == null) {
            return;
        }
        if (currentFile == null) {
            return;
        }
        saveData(currentFile, currentFileType);
    }

    @Action(enabledProperty = "fileOpen")
    public void saveFileAs() {
        if (data == null) {
            return;
        }
        JFileChooser fc;
        fc = new JFileChooser();
        if (currentPath != null) {
            fc.setCurrentDirectory(currentPath);
        }
        fc.setAcceptAllFileFilterUsed(false);
        for (SeriesFileHandler handler : seriesWriters) {
            fc.addChoosableFileFilter(handler.getFilter());
        }
        fc.setFileFilter(currentFileType);
        fc.setFileHidingEnabled(false);
        fc.setSelectedFile(SeriesFileFilter.removeExtension(currentFile));
        if (fc.showSaveDialog(view.getFrame()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = fc.getSelectedFile();
        FileFilter filter = fc.getFileFilter();
        if (filter instanceof SeriesFileFilter) {
            file = ((SeriesFileFilter) filter).forceExtension(file);
        }
        saveData(file, filter);
    }

    @Action(enabledProperty = "fileOpen")
    public void closeFile() {
        if (modified) {
            JOptionPane op = new JOptionPane();
            int choice = JOptionPane.showConfirmDialog(null, "The file was not saved. Close anyway?", appName, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }
        closeData();
    }

    @Action
    public void exitFile() {
        exit();
    }

    //=======================================================================
    // EDIT MENU Action Handlers
    //=======================================================================
    @Action(enabledProperty = "undoAvailable")
    public void undo() {
        data.removeAllSeries();
        for (Series ser : undoBuffer.getSeries()) {
            data.addSeries(ser);
        }
        undoBuffer = null;
        mainChart.fireChartChanged();
        setUndoAvailable(false);
        updateTitle();
        dataListComponent.repaint();
    }

    @Action
    public void redo() {
    }

    @Action(enabledProperty = "fileOpen")
    public void cut() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        startModify();
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        clip.setContents(selection, this);
        setPasteAvailable(true);
        data.removeSeries(selection.getSeries());
        dataListComponent.clearSelection();
        endModify();
    }

    @Action(enabledProperty = "fileOpen")
    public void copy() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        clip.setContents(selection, this);
        setPasteAvailable(true);
    }

    @Action(enabledProperty = "pasteAvailable")
    public void paste() {
        if (data == null) {
            return;
        }
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transfer = clip.getContents(this);
        int insertIndex = getLastSelectedIndex();
        if (insertIndex == -1) {
            insertIndex = data.getSeriesCount() - 1;
        }
        startModify();
        try {
            int oldCount = data.getSeriesCount();
            dataListComponent.clearSelection();
            data.insertTransferData(insertIndex + 1, transfer);
            int newCount = data.getSeriesCount();
            dataListComponent.setSelectionInterval(insertIndex + 1, insertIndex + newCount - oldCount);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (UnsupportedFlavorException ex) {
            ex.printStackTrace();
        }
        endModify();
    }

    @Action(enabledProperty = "fileOpen")
    public void delete() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        startModify();
        data.removeSeries(selection.getSeries());
        dataListComponent.clearSelection();
        endModify();
    }

    @Action(enabledProperty = "fileOpen")
    public void insertFile() {
        // TODO: Update insertFile with common fileOpen method
        if (data == null) {
            return;
        }

        JFileChooser fc = new JFileChooser(currentPath);
        for (SeriesFileHandler handler : seriesReaders) {
            fc.addChoosableFileFilter(handler.getFilter());
        }
        fc.setAcceptAllFileFilterUsed(true);
        fc.setFileHidingEnabled(false);
        if (fc.showOpenDialog(view.getFrame()) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File newfile = fc.getSelectedFile();
        SeriesList newdata = readSeriesFile(newfile, fc.getFileFilter());

        if (newdata == null) {
            JOptionPane.showMessageDialog(view.getFrame(), "Unable to open file\n" + newfile.getName(), appName, JOptionPane.ERROR_MESSAGE);
            return;
        }
        int insertIndex = getLastSelectedIndex();
        if (insertIndex == -1) {
            insertIndex = data.getSeriesCount() - 1;
        }
        startModify();
        int oldCount = data.getSeriesCount();
        dataListComponent.clearSelection();
        insertIndex++;
        for (int i = 0; i < newdata.getSeriesCount(); i++) {
            data.addSeries(insertIndex++, newdata.getSeries(i));
        }
        int newCount = data.getSeriesCount();
        dataListComponent.setSelectionInterval(insertIndex + 1, insertIndex + newCount - oldCount);
        endModify();
        currentPath = new File(newfile.getParent());
    }

    @Action(enabledProperty = "fileOpen")
    public void addSeparator() {
        if (data == null) {
            return;
        }
        int insertIndex = getLastSelectedIndex();
        if (insertIndex == -1) {
            insertIndex = data.getSeriesCount() - 1;
        }
        startModify();
        data.addSeries(insertIndex + 1, new Series("", 0));
        endModify();
    }
    String lastFind = "";
    boolean regularExpression = false;
    boolean searchSelection = false;

    @Action(enabledProperty = "fileOpen")
    public void findNames() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        boolean hasSelection = selection.getSeriesCount() > 0;
        String[] findS = {lastFind};
        Boolean[] reB = {new Boolean(regularExpression)};
        Boolean[] inselB = {new Boolean(searchSelection)};

        GenericInputDialog gd = new GenericInputDialog(this, null, "Find");
        gd.addString("Find what:", findS, 30);
        gd.addBoolean("Regular expressions:", reB, false);
        if (hasSelection) {
            gd.addBoolean("Search selection:", inselB, false);
        }
        if (gd.showDialog()) {
            lastFind = findS[0];
            regularExpression = reB[0];
            searchSelection = inselB[0];
            SeriesList toSearch = (hasSelection && inselB[0]) ? selection : data;
            SeriesList found = new SeriesList();
            Pattern p = reB[0] ? Pattern.compile(findS[0]) : null;
            for (Series ser : toSearch.getSeries()) {
                if (reB[0]) {
                    if (p.matcher(ser.getName()).find()) {
                        found.addSeries(ser);
                    }
                } else {
                    if (ser.getName().contains(findS[0])) {
                        found.addSeries(ser);
                    }
                }
            }
            setSelectedSeries(found);
        }
    }
    String lastReplace = "";

    @Action(enabledProperty = "fileOpen")
    public void replaceNames() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        boolean hasSelection = selection.getSeriesCount() > 0;
        String[] findS = {lastFind};
        String[] replS = {lastReplace};
        Boolean[] reB = {new Boolean(regularExpression)};
        Boolean[] inselB = {new Boolean(searchSelection)};

        GenericInputDialog gd = new GenericInputDialog(this, null, "Replace");
        gd.addString("Find what:", findS, 30);
        gd.addString("Replace with:", replS, 30);
        gd.addBoolean("Regular expressions:", reB, false);
        if (hasSelection) {
            gd.addBoolean("Search selection:", inselB, false);
        }
        if (gd.showDialog()) {
            startModify();
            lastFind = findS[0];
            lastReplace = replS[0];
            regularExpression = reB[0];
            searchSelection = inselB[0];
            SeriesList toSearch = (hasSelection && inselB[0]) ? selection : data;
            SeriesList found = new SeriesList();
            Pattern p = reB[0] ? Pattern.compile(findS[0]) : null;
            String oldName, newName;
            for (Series ser : toSearch.getSeries()) {
                oldName = ser.getName();
                if (reB[0]) {
                    newName = oldName.replaceAll(findS[0], replS[0]);
                } else {
                    newName = oldName.replace(findS[0], replS[0]);
                }
                if (!newName.equals(oldName)) {
                    ser.setName(newName);
                    found.addSeries(ser);
                }
            }
            endModify();
            setSelectedSeries(found);
        }
    }

    @Action(enabledProperty = "fileOpen")
    public void renameSeries() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() != 1) {
            JOptionPane.showMessageDialog(view.getFrame(), "Please select one series to rename", appName, JOptionPane.ERROR_MESSAGE);
            return;
        }
        Series ser = selection.getSeries(0);
        String[] aname = {ser.getName()};
        GenericInputDialog gd = new GenericInputDialog(this, null, "Rename series");
        gd.addString("New name:", aname, 15);
        if (gd.showDialog()) {
            startModify();
            ser.setName(aname[0]);
            endModify();
        }
    }

    @Action(enabledProperty = "fileOpen")
    public void prefixNames() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        String[] prefix = {"Prefix-"};
        GenericInputDialog gd = new GenericInputDialog(this, "Prepend to series names", appName);
        gd.addString("Prefix:", prefix, 10);
        if (gd.showDialog()) {
            startModify();
            for (Series ser : selection.getSeries()) {
                ser.setName(prefix[0] + ser.getName());
            }
            endModify();
        }
    }

    @Action(enabledProperty = "fileOpen")
    public void suffixNames() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        String[] suffix = {"-Suffix"};
        GenericInputDialog gd = new GenericInputDialog(this, "Append to series names", appName);
        gd.addString("Suffix:", suffix, 10);
        if (gd.showDialog()) {
            startModify();
            for (Series ser : selection.getSeries()) {
                ser.setName(ser.getName() + suffix[0]);
            }
            endModify();
        }
    }

    @Action(enabledProperty = "fileOpen")
    public void selectAllSeries() {
        dataListComponent.setSelectionInterval(0, data.getSeriesCount() - 1);
    }

    @Action(enabledProperty = "fileOpen")
    public void sortSeries() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        startModify();
        data.removeSeries(selection.getSeries());
        selection.sortByName();
        data.addAllSeries(selection.getSeries());
        dataListComponent.clearSelection();
        endModify();
    }

    @Action(enabledProperty = "fileOpen")
    public void sortSeriesValue() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        startModify();
        data.removeSeries(selection.getSeries());
        selection.sortByNamesValue();
        data.addAllSeries(selection.getSeries());
        dataListComponent.clearSelection();
        endModify();
    }

    @Action
    public void preferences() {
    }

    //=======================================================================
    // VIEW MENU Action Handlers
    //=======================================================================
    @Action(enabledProperty = "fileOpen")
    public void toggleSeries() {
        int[] aiSelected = dataListComponent.getSelectedIndices();
        for (int index : aiSelected) {
            boolean visible = mainChartRenderer.isSeriesVisible(index);
            Boolean bv = new Boolean(!visible);
            mainChartRenderer.setSeriesVisible(index, bv);
        }
        dataListComponent.repaint();
    }

    @Action(enabledProperty = "fileOpen")
    public void setXAxis() {
        if (data == null || mainChart == null) {
            return;
        }
        ValueAxis axis = mainChart.getXYPlot().getDomainAxis();
        Double[] minD = {new Double(Math.rint(axis.getLowerBound() * 10) / 10)};
        Double[] maxD = {new Double(Math.rint(axis.getUpperBound() * 10) / 10)};
        GenericInputDialog gd = new GenericInputDialog(this, "X range to display", appName);
        gd.addDouble("Min x:", minD, 8);
        gd.addDouble("Max x:", maxD, 8);
        if (gd.showDialog()) {
            axis.setRange(minD[0], maxD[0]);
        }
    }

    @Action(enabledProperty = "fileOpen")
    public void setYAxis() {
        if (data == null || mainChart == null) {
            return;
        }
        ValueAxis axis = mainChart.getXYPlot().getRangeAxis();
        Double[] minD = {new Double(Math.rint(axis.getLowerBound() * 10) / 10)};
        Double[] maxD = {new Double(Math.rint(axis.getUpperBound() * 10) / 10)};
        GenericInputDialog gd = new GenericInputDialog(this, "Y range to display", appName);
        gd.addDouble("Min y:", minD, 8);
        gd.addDouble("Max y:", maxD, 8);
        if (gd.showDialog()) {
            axis.setRange(minD[0], maxD[0]);
        }
    }

    @Action(enabledProperty = "fileOpen", selectedProperty = "zoomMode")
    public void zoom() {
        updateZoomMode();
    }

    @Action(enabledProperty = "fileOpen")
    public void zoomIn() {
        if (mainChartPanel == null) {
            return;
        }
        Rectangle2D plotArea = mainChartPanel.getScreenDataArea();
        mainChartPanel.setZoomInFactor(0.8);
        mainChartPanel.zoomInBoth(plotArea.getCenterX(), plotArea.getCenterY());
    }

    @Action(enabledProperty = "fileOpen")
    public void zoomOut() {
        if (mainChartPanel == null) {
            return;
        }
        Rectangle2D plotArea = mainChartPanel.getScreenDataArea();
        mainChartPanel.setZoomOutFactor(1 / 0.8);
        mainChartPanel.zoomOutBoth(plotArea.getCenterX(), plotArea.getCenterY());
    }

    @Action(enabledProperty = "fileOpen")
    public void zoomExtents() {
        if (mainChartPanel == null) {
            return;
        }
        mainChartPanel.restoreAutoBounds();
    }

    @Action(enabledProperty = "fileOpen")
    public void selectRange() {
        if (data == null) {
            return;
        }
        double[] range = getMarkedXRange();
        if (range == null) {
            range = data.getXRange();
        }
        if (range[1] <= range[0]) {
            clearMarkedXRange();
        }
        Double[] xminD = {new Double(Math.rint(range[0] * 10) / 10)};
        Double[] xmaxD = {new Double(Math.rint(range[1] * 10) / 10)};
        GenericInputDialog gd = new GenericInputDialog(this, "Select range", appName);
        gd.addDouble("Min x:", xminD, 8);
        gd.addDouble("Max x:", xmaxD, 8);
        if (gd.showDialog()) {
            markXRange(xminD[0], xmaxD[0]);
        }
    }

    @Action(enabledProperty = "fileOpen", selectedProperty = "legendVisible")
    public void optShowLegend() {
        updateLegendVisible();
    }

    @Action(enabledProperty = "fileOpen")
    public void info() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        double[] xrange = selection.getXRange();
        double[] yrange = selection.getYRange();
        double delx = selection.getAverageDeltaX();
        int nPoints = 0;
        for (Series ser : selection.getSeries()) {
            nPoints += ser.getSize();
        }
        Object[][] table = {
            {"Description", "Minimum value", "Maximum value"},
            {"Series Count", new Integer(selection.getSeriesCount()), null},
            {"X range", new Double(xrange[0]), new Double(xrange[1])},
            {"Y range", new Double(yrange[0]), new Double(yrange[1])},
            {"Avg delta-x", new Double(delx), null},
            {"Total points", new Integer(nPoints), null}};
        new TableOutputDialog(view.getFrame(), "Series information", true, table).setVisible(true);
    }

    @Action(enabledProperty = "fileOpen")
    public void statistics() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        int nSelected = selection.getSeriesCount();
        if (nSelected < 1) {
            JOptionPane.showMessageDialog(view.getFrame(), "Please select at least one series", appName, JOptionPane.ERROR_MESSAGE);
            return;
        }

        // ADD a series containing ALL of the selected points
        Series allData = new Series("ALL");
        for (Series ser : selection.getSeries()) {
            double[] range = getMarkedXRange();
            if (range == null) {
                range = ser.getXRange();
            }
            allData.addAll(ser.subSeries(range[0], range[1]).getPoints());
        }
        selection.addSeries(allData);

        List<Object> header = new ArrayList<Object>(nSelected + 2);
        List<StatTools.Stats> selectedStats = new ArrayList<StatTools.Stats>(nSelected + 1);
        header.add("Statistics");
        Map<String, List<Object>> dataRows = new HashMap<String, List<Object>>();
        for (String desc : StatTools.Stats.DESCS) {
            List<Object> row = new ArrayList<Object>(nSelected + 1);
            row.add(desc);
            dataRows.put(desc, row);
        }
        for (Series ser : selection.getSeries()) {
            header.add(ser.getName());
            Series subSeries;
            if (ser == allData) {
                subSeries = ser;
            } else {
                double[] range = getMarkedXRange();
                if (range == null) {
                    range = ser.getXRange();
                }
                subSeries = ser.subSeries(range[0], range[1]);
            }
            subSeries.removeNaN();

            if (subSeries.getSize() < 3) {
                // not enough points for statistics. fill rows with blanks
                for (String desc : StatTools.Stats.DESCS) {
                    dataRows.get(desc).add("-");
                }
                selectedStats.add(null);
            } else {
                StatTools.Stats stats = StatTools.calcStats(subSeries.getYArray());
                //System.out.println(stats.toString());
                dataRows.get(stats.DESC_DOF).add(new Integer(stats.DOF));
                dataRows.get(stats.DESC_sum).add(new Double(stats.sum));
                dataRows.get(stats.DESC_sumSq).add(new Double(stats.sumSq));
                dataRows.get(stats.DESC_min).add(new Double(stats.min));
                dataRows.get(stats.DESC_max).add(new Double(stats.max));
                dataRows.get(stats.DESC_avg).add(new Double(stats.avg));
                dataRows.get(stats.DESC_avgDev).add(new Double(stats.avgDev));
                dataRows.get(stats.DESC_stdDev).add(new Double(stats.stdDev));
                dataRows.get(stats.DESC_var).add(new Double(stats.var));
                dataRows.get(stats.DESC_skew).add(new Double(stats.skew));
                dataRows.get(stats.DESC_kurt).add(new Double(stats.kurt));
                selectedStats.add(stats);
            }
        }

        List<List<Object>> data = new ArrayList<List<Object>>();
        data.add(header);
        for (String key : StatTools.Stats.DESCS) {
            List<Object> statRow = dataRows.get(key);
            data.add(statRow);
        }

        List<XYLineAnnotation> annotations = new ArrayList<XYLineAnnotation>();
        XYPlot plot = null;
        if (mainChart != null) {
            BasicStroke thinStroke = new BasicStroke(0.5f);
            BasicStroke dashStroke = new BasicStroke(1.5f, BasicStroke.CAP_SQUARE,
                    BasicStroke.JOIN_MITER, 10.0f, new float[]{20f, 10f}, 0.0f);
            plot = mainChart.getXYPlot();
            ValueAxis xaxis = plot.getDomainAxis();
            for (int i = 0; i < nSelected + 1; i++) {
                StatTools.Stats stats = selectedStats.get(i);
                if (stats != null) {
                    java.awt.Stroke stroke;
                    java.awt.Color color;
                    Series ser = selection.getSeries(i);
                    if (ser == allData) {
                        color = java.awt.Color.BLACK;
                        stroke = dashStroke;
                    } else {
                        color = ser.getColor();
                        stroke = thinStroke;
                    }
                    XYLineAnnotation line = new XYLineAnnotation(
                            xaxis.getLowerBound(), stats.avg,
                            xaxis.getUpperBound(), stats.avg,
                            stroke, color);
                    plot.addAnnotation(line);
                    annotations.add(line);
                }
            }
        }
        new TableOutputDialog(view.getFrame(), "Series Statistics", true, data).setVisible(true);
        if (mainChart != null) {
            for (XYLineAnnotation line : annotations) {
                plot.removeAnnotation(line);
            }
        }
    }

    @Action(enabledProperty = "fileOpen")
    public void linearFit() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        int nSelected = selection.getSeriesCount();
        if (nSelected < 1) {
            JOptionPane.showMessageDialog(view.getFrame(), "Please select at least one series", appName, JOptionPane.ERROR_MESSAGE);
            return;
        }

        // ADD a series containing ALL of the selected points
        Series allData = new Series("ALL");
        for (Series ser : selection.getSeries()) {
            double[] range = getMarkedXRange();
            if (range == null) {
                range = ser.getXRange();
            }
            allData.addAll(ser.subSeries(range[0], range[1]).getPoints());
        }
        selection.addSeries(allData);

        List<Object> header = new ArrayList<Object>(nSelected + 2);
        List<StatTools.LinFit> selectedFits = new ArrayList<StatTools.LinFit>(nSelected + 1);
        header.add("Linear Fit");
        Map<String, List<Object>> dataRows = new HashMap<String, List<Object>>();
        for (String desc : StatTools.LinFit.DESCS) {
            List<Object> row = new ArrayList<Object>(nSelected + 2);
            row.add(desc);
            dataRows.put(desc, row);
        }
        for (Series ser : selection.getSeries()) {
            header.add(ser.getName());
            Series subSeries;
            if (ser == allData) {
                subSeries = ser;
            } else {
                double[] range = getMarkedXRange();
                if (range == null) {
                    range = ser.getXRange();
                }
                subSeries = ser.subSeries(range[0], range[1]);
            }
            subSeries.removeNaN();

            if (subSeries.getSize() < 2) {
                // not enough points for statistics. fill rows with blanks
                for (String desc : StatTools.LinFit.DESCS) {
                    dataRows.get(desc).add("-");
                }
                selectedFits.add(null);
            } else {
                double[] xvals = subSeries.getXArray();
                double[] yvals = subSeries.getYArray();
                StatTools.LinFit fit = StatTools.calcLinFit(xvals, yvals, null, false);
                //System.out.println(fit.toString());
                dataRows.get(fit.DESC_DOF).add(new Integer(fit.DOF));
                dataRows.get(fit.DESC_inter).add(new Double(fit.inter));
                dataRows.get(fit.DESC_interStdDev).add(new Double(fit.interStdDev));
                dataRows.get(fit.DESC_interTStat).add(new Double(fit.interTStat));
                dataRows.get(fit.DESC_slope).add(new Double(fit.slope));
                dataRows.get(fit.DESC_slopeStdDev).add(new Double(fit.slopeStdDev));
                dataRows.get(fit.DESC_slopeTStat).add(new Double(fit.slopeTStat));
                dataRows.get(fit.DESC_RSq).add(new Double(fit.RSq));
                dataRows.get(fit.DESC_ChiSq).add(new Double(fit.ChiSq));
                dataRows.get(fit.DESC_stdErrEst).add(new Double(fit.stdErrEst));
                dataRows.get(fit.DESC_Q).add(new Double(fit.Q));
                dataRows.get(fit.DESC_nvar).add(new Double(fit.nvar));
                dataRows.get(fit.DESC_sumX).add(new Double(fit.sumX));
                dataRows.get(fit.DESC_sumXSq).add(new Double(fit.sumXSq));
                selectedFits.add(fit);
            }
        }

        List<List<Object>> data = new ArrayList<List<Object>>();
        data.add(header);
        for (String key : StatTools.LinFit.DESCS) {
            List<Object> statRow = dataRows.get(key);
            data.add(statRow);
        }

        List<XYLineAnnotation> annotations = new ArrayList<XYLineAnnotation>();
        XYPlot plot = null;
        if (mainChart != null) {
            BasicStroke thinStroke = new BasicStroke(0.5f);
            BasicStroke dashStroke = new BasicStroke(1.5f, BasicStroke.CAP_SQUARE,
                    BasicStroke.JOIN_MITER, 10.0f, new float[]{20f, 10f}, 0.0f);
            plot = mainChart.getXYPlot();
            ValueAxis xaxis = plot.getDomainAxis();
            for (int i = 0; i < nSelected + 1; i++) {
                StatTools.LinFit fit = selectedFits.get(i);
                if (fit != null) {
                    java.awt.Stroke stroke;
                    java.awt.Color color;
                    Series ser = selection.getSeries(i);
                    if (ser == allData) {
                        color = java.awt.Color.BLACK;
                        stroke = dashStroke;
                    } else {
                        color = ser.getColor();
                        stroke = thinStroke;
                    }
                    double xmin = xaxis.getLowerBound();
                    double xmax = xaxis.getUpperBound();

                    XYLineAnnotation line = new XYLineAnnotation(
                            xmin, xmin * fit.slope + fit.inter,
                            xmax, xmax * fit.slope + fit.inter,
                            stroke, color);

                    plot.addAnnotation(line);
                    annotations.add(line);
                }
            }
        }
        new TableOutputDialog(view.getFrame(), "Series Linear Fit", true, data).setVisible(true);
        if (mainChart != null) {
            for (XYLineAnnotation line : annotations) {
                plot.removeAnnotation(line);
            }
        }
    }
    //=======================================================================
    // TRANSFORM MENU Action Handlers
    //=======================================================================
    double keepRangeMin = 1000;
    double keepRangeMax = 300000;

    @Action(enabledProperty = "fileOpen")
    public void keepYRange() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        Double[] yminD = {new Double(keepRangeMin)};
        Double[] ymaxD = {new Double(keepRangeMax)};
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Y range to keep", appName);
        gd.addDouble("Min y:", yminD, 10);
        gd.addDouble("Max y:", ymaxD, 10);
        gd.addSeparator("Options");
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        if (gd.showDialog()) {
            startModify();
            keepRangeMin = yminD[0];
            keepRangeMax = ymaxD[0];
            replace = replaceB[0];
            rename = renameB[0];
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            for (Series ser : slist.getSeries()) {
                ser.keepYRange(keepRangeMin, keepRangeMax, true);
                if (rename) {
                    ser.setName("yrange(" + ser.getName() + ")");
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }
    int smoothPoints = 81;
    int smoothOrder = 2;
    boolean leftBias = false;

    @Action(enabledProperty = "fileOpen")
    public void smooth() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }

        Integer[] npointsI = {new Integer(smoothPoints)};
        Integer[] orderI = {new Integer(smoothOrder)};
        Boolean[] leftBiasB = {new Boolean(leftBias)};
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Savitzky-Golay Smoothing", appName);
        gd.addInteger("number of points:", npointsI, 4);
        gd.addInteger("polynomial order:", orderI, 2);
        gd.addBoolean("Bias to left", leftBiasB, true);
        gd.addSeparator("Options");
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        if (gd.showDialog()) {
            startModify();
            replace = replaceB[0];
            rename = renameB[0];
            smoothPoints = npointsI[0];
            leftBias = leftBiasB[0];
            int smoothL, smoothR;
            if (leftBias) {
                smoothL = 0;
                smoothR = 2 * (smoothPoints / 2);
            } else {
                smoothL = smoothPoints / 2;
                smoothR = smoothPoints / 2;
            }
            smoothPoints = smoothL + 1 + smoothR;
            smoothOrder = orderI[0];
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            for (Series ser : slist.getSeries()) {
                ser.smoothSavitzkyGolay(smoothL, smoothR, smoothOrder, 0);
                if (rename) {
                    ser.setName("smooth(" + smoothPoints + ";" + smoothOrder + ";" + ser.getName() + ")");
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }
    int derivOrder = 4;

    @Action(enabledProperty = "fileOpen")
    public void derivative() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }

        Integer[] npointsI = {new Integer(smoothPoints)};
        Integer[] orderI = {new Integer(derivOrder)};
        Boolean[] leftBiasB = {new Boolean(leftBias)};
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Savitzky-Golay Derivative", appName);
        gd.addInteger("number of points:", npointsI, 4);
        gd.addInteger("polynomial order:", orderI, 2);
        gd.addBoolean("Bias to left", leftBiasB, true);
        gd.addSeparator("Options");
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        if (gd.showDialog()) {
            startModify();
            replace = replaceB[0];
            rename = renameB[0];
            smoothPoints = npointsI[0];
            leftBias = leftBiasB[0];
            int smoothL, smoothR;
            if (leftBias) {
                smoothL = 0;
                smoothR = 2 * (smoothPoints / 2);
            } else {
                smoothL = smoothPoints / 2;
                smoothR = smoothPoints / 2;
            }
            smoothPoints = smoothL + 1 + smoothR;
            derivOrder = orderI[0];
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            for (Series ser : slist.getSeries()) {
                ser.smoothSavitzkyGolay(smoothL, smoothR, derivOrder, 1);
                if (rename) {
                    ser.setName("d/dx(" + smoothPoints + ";" + derivOrder + ";" + ser.getName() + ")");
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }
    double scaleYFactor = 2;

    @Action(enabledProperty = "fileOpen")
    public void scaleY() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        Double[] scaleD = {new Double(scaleYFactor)};
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Scale Y values", appName);
        gd.addDouble("Scale factor:", scaleD, 10);
        gd.addSeparator("Options");
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        if (gd.showDialog()) {
            startModify();
            replace = replaceB[0];
            rename = renameB[0];
            scaleYFactor = scaleD[0];
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            for (Series ser : slist.getSeries()) {
                ser.scaleY(scaleYFactor);
                if (rename) {
                    ser.setName("" + scaleYFactor + "*(" + ser.getName() + ")");
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }
    double offsetY = 1000;

    @Action(enabledProperty = "fileOpen")
    public void offsetY() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        Double[] offsetD = {new Double(offsetY)};
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Offset Y values", appName);
        gd.addDouble("Offset:", offsetD, 10);
        gd.addSeparator("Options");
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        if (gd.showDialog()) {
            startModify();
            replace = replaceB[0];
            rename = renameB[0];
            offsetY = offsetD[0];
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            String soff = offsetY < 0 ? "" + offsetY : "+" + offsetY;
            for (Series ser : slist.getSeries()) {
                ser.addY(offsetY);
                if (rename) {
                    ser.setName(ser.getName() + soff);
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }
    double newYIntercept = 0;

    @Action(enabledProperty = "fileOpen")
    public void adjustYIntercept() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        Double[] newYInterceptD = {new Double(newYIntercept)};
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Adjust Y intercept", appName);
        gd.addDouble("New Intercept:", newYInterceptD, 10);
        gd.addSeparator("Options");
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        if (gd.showDialog()) {
            startModify();
            replace = replaceB[0];
            rename = renameB[0];
            newYIntercept = newYInterceptD[0];
            double[] range = getMarkedXRange();
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            for (Series ser : slist.getSeries()) {
                Series subSer = ser.subSeries(range[0], range[1]);
                subSer.removeNaN();
                if (subSer.getSize() < 2) {
                    // can't fit if too few points
                    continue;
                }
                StatTools.LinFit fit = StatTools.calcLinFit(subSer.getXArray(), subSer.getYArray(), null, false);
                double offset = newYIntercept - fit.inter;
                ser.addY(offset);
                String soff = offset < 0 ? "" + offset : "+" + offset;
                if (rename) {
                    ser.setName(ser.getName() + soff);
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }

    @Action(enabledProperty = "fileOpen")
    public void invertY() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Invert Y values", appName);
        gd.addSeparator("Options");
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        if (gd.showDialog()) {
            startModify();
            replace = replaceB[0];
            rename = renameB[0];
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            for (Series ser : slist.getSeries()) {
                ser.invertY();
                if (rename) {
                    ser.setName("1/(" + ser.getName() + ")");
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }
    double scaleXFactor = 2;
    double logYBase = 10;

    @Action(enabledProperty = "fileOpen")
    public void logY() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        Double[] baseD = {new Double(logYBase)};
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Take logN(Y)", appName);
        gd.addDouble("Base (N):", baseD, 10);
        gd.addSeparator("Options");
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        if (gd.showDialog()) {
            startModify();
            replace = replaceB[0];
            rename = renameB[0];
            logYBase = baseD[0];
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            for (Series ser : slist.getSeries()) {
                ser.logY(logYBase);
                if (rename) {
                    ser.setName("log" + logYBase + "(" + ser.getName() + ")");
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }
    double antilogYBase = 10;

    @Action(enabledProperty = "fileOpen")
    public void antilogY() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        Double[] baseD = {new Double(antilogYBase)};
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Take Base^Y", appName);
        gd.addDouble("Base (N):", baseD, 10);
        gd.addSeparator("Options");
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        if (gd.showDialog()) {
            startModify();
            replace = replaceB[0];
            rename = renameB[0];
            antilogYBase = baseD[0];
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            for (Series ser : slist.getSeries()) {
                ser.antilogY(antilogYBase);
                if (rename) {
                    ser.setName("" + antilogYBase + "^(" + ser.getName() + ")");
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }
    double powYFactor = 10;

    @Action(enabledProperty = "fileOpen")
    public void powY() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        Double[] factD = {new Double(powYFactor)};
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Take Y^factor", appName);
        gd.addDouble("Factor:", factD, 10);
        gd.addSeparator("Options");
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        if (gd.showDialog()) {
            startModify();
            replace = replaceB[0];
            rename = renameB[0];
            powYFactor = factD[0];
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            for (Series ser : slist.getSeries()) {
                ser.powY(powYFactor);
                if (rename) {
                    ser.setName("(" + ser.getName() + ")^" + powYFactor);
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }

    @Action(enabledProperty = "fileOpen")
    public void atanY() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Arctan (Y)", appName);
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        if (gd.showDialog()) {
            startModify();
            replace = replaceB[0];
            rename = renameB[0];
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            for (Series ser : slist.getSeries()) {
                ser.atanY();
                if (rename) {
                    ser.setName("atan(" + ser.getName() + ")");
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }

    @Action(enabledProperty = "fileOpen")
    public void scaleX() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        Double[] scaleD = {new Double(scaleXFactor)};
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Scale X values", appName);
        gd.addDouble("Scale factor:", scaleD, 10);
        gd.addSeparator("Options");
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        if (gd.showDialog()) {
            startModify();
            replace = replaceB[0];
            rename = renameB[0];
            scaleXFactor = scaleD[0];
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            for (Series ser : slist.getSeries()) {
                ser.scaleX(scaleXFactor);
                if (rename) {
                    ser.setName("(x*" + scaleXFactor + ";" + ser.getName() + ")");
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }
    double offsetX = 100;

    @Action(enabledProperty = "fileOpen")
    public void offsetX() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        Double[] offsetD = {new Double(offsetX)};
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Offset X values", appName);
        gd.addDouble("Offset:", offsetD, 10);
        gd.addSeparator("Options");
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        if (gd.showDialog()) {
            startModify();
            replace = replaceB[0];
            rename = renameB[0];
            offsetX = offsetD[0];
            String soff = offsetX < 0 ? "" + offsetX : "+" + offsetX;
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            for (Series ser : slist.getSeries()) {
                ser.addX(offsetX);
                if (rename) {
                    ser.setName("(x" + soff + ";" + ser.getName() + ")");
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }
    boolean keepXVals = false;

    @Action(enabledProperty = "fileOpen")
    public void resampleX() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        double[] xrange = selection.getXRange();
        double xmin = xrange[0], xmax = xrange[1];
        double delx = selection.getAverageDeltaX();
        double olddelx = delx;
        Double[] xminD = {new Double(Math.floor(xmin))};
        Double[] xmaxD = {new Double(Math.ceil(xmax))};
        Double[] delxD = {new Double(Math.rint(delx * 10) / 10)};
        Boolean[] keepXB = {new Boolean(keepXVals)};
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Resample", appName);
        gd.addDouble("Min x:", xminD, 10);
        gd.addDouble("Max x:", xmaxD, 10);
        gd.addDouble("Delta-x:", delxD, 8);
        gd.addSeparator("Options");
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        gd.addSeparator("Special Option");
        gd.addBoolean("Keep existing X's", keepXB, false);
        if (gd.showDialog()) {
            startModify();
            replace = replaceB[0];
            rename = renameB[0];
            xmin = xminD[0];
            xmax = xmaxD[0];
            delx = delxD[0];
            keepXVals = keepXB[0];
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            for (Series ser : slist.getSeries()) {
                if (keepXVals) {
                    Series newser = (Series) ser.clone();
                    newser.resample(xmin, xmax, delx);
                    double eps = olddelx / 10;
                    for (KPoint2D ptser : ser.getPoints()) {
                        double y = Double.NaN;
                        for (KPoint2D ptnew : newser.getPoints()) {
                            if (Math.abs(ptnew.x - ptser.x) < eps) {
                                y = ptnew.y;
                            }
                        }
                        ptser.y = y;
                    }
                } else {
                    ser.resample(xmin, xmax, delx);
                }
                if (rename) {
                    ser.setName("resampled(" + ser.getName() + ")");
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }

    @Action(enabledProperty = "fileOpen")
    public void removeXRange() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        double[] xrange = getMarkedXRange();
        if (xrange == null) {
            xrange = selection.getXRange();
        }
        double xmin = xrange[0], xmax = xrange[1];
        Double[] xminD = {new Double(Math.floor(xmin))};
        Double[] xmaxD = {new Double(Math.ceil(xmax))};
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Remove points in range", appName);
        gd.addDouble("Min x:", xminD, 10);
        gd.addDouble("Max x:", xmaxD, 10);
        gd.addSeparator("Options");
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        if (gd.showDialog()) {
            startModify();
            replace = replaceB[0];
            rename = renameB[0];
            xmin = xminD[0];
            xmax = xmaxD[0];
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            for (Series ser : slist.getSeries()) {
                Series left = ser.subSeries(Double.MIN_VALUE, xmin);
                Series right = ser.subSeries(xmax, Double.MAX_VALUE);
                ser.clearPoints();
                if (left != null) {
                    ser.addAll(left.getPoints());
                }
                if (right != null) {
                    ser.addAll(right.getPoints());
                }
                if (rename) {
                    ser.setName("xrange(" + ser.getName() + ")");
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }

    @Action(enabledProperty = "fileOpen")
    public void addSeries() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        int insertIndex = getLastSelectedIndex();
        if (insertIndex == -1) {
            insertIndex = data.getSeriesCount() - 1;
        }
        int nSeries = selection.getSeriesCount();
        if (nSeries == 0) {
            return;
        }
        String name = "(";
        for (int i = 0; i < nSeries; i++) {
            name += selection.getSeries(i).getName();
            if (i < nSeries - 1) {
                name += "+";
            }
        }
        name += ")";
        double[] xrange = selection.getXRange();
        double xmin = xrange[0], xmax = xrange[1];
        double delx = selection.getAverageDeltaX();
        double N = (xmax - xmin) / delx + 1;
        int nPoints = (int) N;
        if (Double.isNaN(N) || N < 0 || nPoints == 0) {
            return;
        }
        List<KPoint2D> points = new ArrayList<KPoint2D>(nPoints);
        for (double x = xmin; x <= xmax; x += delx) {
            double y = 0;
            for (int i = 0; i < nSeries; i++) {
                y += selection.getSeries(i).interpolateY(x);
            }
            points.add(new KPoint2D(x, y));
        }
        Series dest = new Series(name, points);
        dest.removeNaN();
        startModify();
        data.addSeries(insertIndex + 1, dest);
        dataListComponent.setSelectedIndex(insertIndex + 1);
        endModify();
    }

    @Action(enabledProperty = "fileOpen")
    public void subtractSeries() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        int insertIndex = getLastSelectedIndex();
        if (insertIndex == -1) {
            insertIndex = data.getSeriesCount() - 1;
        }
        int nSeries = selection.getSeriesCount();
        if (nSeries != 2) {
            JOptionPane.showMessageDialog(view.getFrame(), "Please select exactly two series to subtract.", appName, JOptionPane.ERROR_MESSAGE);
            return;
        }
        Series sA = selection.getSeries(0);
        Series sB = selection.getSeries(1);
        String name = "(" + sA.getName() + "-" + sB.getName() + ")";
        double[] xrange = selection.getXRange();
        double xmin = xrange[0], xmax = xrange[1];
        double delx = selection.getAverageDeltaX();
        double N = (xmax - xmin) / delx + 1;
        int nPoints = (int) N;
        if (Double.isNaN(N) || N < 0 || nPoints == 0) {
            return;
        }
        List<KPoint2D> points = new ArrayList<KPoint2D>(nPoints);
        for (double x = xmin; x <= xmax; x += delx) {
            double y = sA.interpolateY(x) - sB.interpolateY(x);
            points.add(new KPoint2D(x, y));
        }
        Series dest = new Series(name, points);
        dest.removeNaN();
        startModify();
        data.addSeries(insertIndex + 1, dest);
        dataListComponent.setSelectedIndex(insertIndex + 1);
        endModify();
    }

    @Action(enabledProperty = "fileOpen")
    public void multiplySeries() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        int insertIndex = getLastSelectedIndex();
        if (insertIndex == -1) {
            insertIndex = data.getSeriesCount() - 1;
        }
        int nSeries = selection.getSeriesCount();
        if (nSeries == 0) {
            return;
        }
        String name = "(";
        for (int i = 0; i < nSeries; i++) {
            name += selection.getSeries(i).getName();
            if (i < nSeries - 1) {
                name += "*";
            }
        }
        name += ")";
        double[] xrange = selection.getXRange();
        double xmin = xrange[0], xmax = xrange[1];
        double delx = selection.getAverageDeltaX();
        double N = (xmax - xmin) / delx + 1;
        int nPoints = (int) N;
        if (Double.isNaN(N) || N < 0 || nPoints == 0) {
            return;
        }
        List<KPoint2D> points = new ArrayList<KPoint2D>(nPoints);
        for (double x = xmin; x <= xmax; x += delx) {
            double y = 1;
            for (int i = 0; i < nSeries; i++) {
                y *= selection.getSeries(i).interpolateY(x);
            }
            points.add(new KPoint2D(x, y));
        }
        Series dest = new Series(name, points);
        dest.removeNaN();
        startModify();
        data.addSeries(insertIndex + 1, dest);
        this.dataListComponent.setSelectedIndex(insertIndex + 1);
        endModify();
    }

    @Action(enabledProperty = "fileOpen")
    public void divideSeries() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        int insertIndex = getLastSelectedIndex();
        if (insertIndex == -1) {
            insertIndex = data.getSeriesCount() - 1;
        }
        int nSeries = selection.getSeriesCount();
        if (nSeries != 2) {
            JOptionPane.showMessageDialog(view.getFrame(), "Please select exactly two series to divide.", appName, JOptionPane.ERROR_MESSAGE);
            return;
        }
        Series sA = selection.getSeries(0);
        Series sB = selection.getSeries(1);
        String name = "(" + sA.getName() + "/" + sB.getName() + ")";
        double[] xrange = selection.getXRange();
        double xmin = xrange[0], xmax = xrange[1];
        double delx = selection.getAverageDeltaX();
        double N = (xmax - xmin) / delx + 1;
        int nPoints = (int) N;
        if (Double.isNaN(N) || N < 0 || nPoints == 0) {
            return;
        }
        List<KPoint2D> points = new ArrayList<KPoint2D>(nPoints);
        for (double x = xmin; x <= xmax; x += delx) {
            double y = sA.interpolateY(x) / sB.interpolateY(x);
            points.add(new KPoint2D(x, y));
        }
        Series dest = new Series(name, points);
        dest.removeNaN();
        startModify();
        data.addSeries(insertIndex + 1, dest);
        this.dataListComponent.setSelectedIndex(insertIndex + 1);
        endModify();
    }

    @Action(enabledProperty = "fileOpen")
    public void averageSereies() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        int insertIndex = getLastSelectedIndex();
        if (insertIndex == -1) {
            insertIndex = data.getSeriesCount() - 1;
        }
        int nSeries = selection.getSeriesCount();
        if (nSeries == 0) {
            return;
        }
        String name = "avg(";
        for (int i = 0; i < nSeries; i++) {
            name += selection.getSeries(i).getName();
            if (i < nSeries - 1) {
                name += ";";
            }
        }
        name += ")";
        double[] xrange = selection.getXRange();
        double xmin = xrange[0], xmax = xrange[1];
        double delx = selection.getAverageDeltaX();
        double M = (xmax - xmin) / delx + 1;
        int nPoints = (int) M;
        if (Double.isNaN(M) || M < 0 || nPoints == 0) {
            return;
        }
        List<KPoint2D> points = new ArrayList<KPoint2D>(nPoints);
        for (double x = xmin; x <= xmax; x += delx) {
            double sum = 0;
            for (int i = 0; i < nSeries; i++) {
                sum += selection.getSeries(i).interpolateY(x);
            }
            double y = sum / nSeries;
            points.add(new KPoint2D(x, y));
        }
        Series dest = new Series(name, points);
        dest.removeNaN();
        startModify();
        data.addSeries(insertIndex + 1, dest);
        this.dataListComponent.setSelectedIndex(insertIndex + 1);
        endModify();
    }

    @Action(enabledProperty = "fileOpen")
    public void avgSDSeries() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        int insertIndex = getLastSelectedIndex();
        if (insertIndex == -1) {
            insertIndex = data.getSeriesCount() - 1;
        }
        int nSeries = selection.getSeriesCount();
        if (nSeries < 3) {
            JOptionPane.showMessageDialog(view.getFrame(), "Please select at least 3 series for standard deviation.", appName, JOptionPane.ERROR_MESSAGE);
            return;
        }
        String name = "(";
        for (int i = 0; i < nSeries; i++) {
            name += selection.getSeries(i).getName();
            if (i < nSeries - 1) {
                name += ";";
            }
        }
        name += ")";
        double[] xrange = selection.getXRange();
        double xmin = xrange[0], xmax = xrange[1];
        double delx = selection.getAverageDeltaX();
        double M = (xmax - xmin) / delx + 1;
        int nPoints = (int) M;
        if (Double.isNaN(M) || M < 0 || nPoints == 0) {
            return;
        }
        List<KPoint2D> avgpoints = new ArrayList<KPoint2D>(nPoints);
        List<KPoint2D> sdpoints = new ArrayList<KPoint2D>(nPoints);
        double[] yvals = new double[nSeries];
        StatTools.Stats stats;
        for (double x = xmin; x <= xmax; x += delx) {
            for (int i = 0; i < nSeries; i++) {
                yvals[i] = selection.getSeries(i).interpolateY(x);
            }
            stats = StatTools.calcStats(yvals);
            avgpoints.add(new KPoint2D(x, stats.avg));
            sdpoints.add(new KPoint2D(x, stats.stdDev));
        }
        Series avgdest = new Series("avg" + name, avgpoints);
        Series sddest = new Series("sd" + name, sdpoints);
        avgdest.removeNaN();
        sddest.removeNaN();
        startModify();
        data.addSeries(insertIndex + 1, avgdest);
        data.addSeries(insertIndex + 2, sddest);
        int[] selind = {insertIndex + 1, insertIndex + 2};
        this.dataListComponent.setSelectedIndices(selind);
        endModify();
    }

    @Action(enabledProperty = "fileOpen")
    public void avgPlusSDSeries() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        int insertIndex = getLastSelectedIndex();
        if (insertIndex == -1) {
            insertIndex = data.getSeriesCount() - 1;
        }
        int nSeries = selection.getSeriesCount();
        if (nSeries < 3) {
            JOptionPane.showMessageDialog(view.getFrame(), "Please select at least 3 series for standard deviation.", appName, JOptionPane.ERROR_MESSAGE);
            return;
        }
        String name = "(";
        for (int i = 0; i < nSeries; i++) {
            name += selection.getSeries(i).getName();
            if (i < nSeries - 1) {
                name += ";";
            }
        }
        name += ")";
        double[] xrange = selection.getXRange();
        double xmin = xrange[0], xmax = xrange[1];
        double delx = selection.getAverageDeltaX();
        double M = (xmax - xmin) / delx + 1;
        int nPoints = (int) M;
        if (Double.isNaN(M) || M < 0 || nPoints == 0) {
            return;
        }
        List<KPoint2D> avgminussdpoints = new ArrayList<KPoint2D>(nPoints);
        List<KPoint2D> avgpoints = new ArrayList<KPoint2D>(nPoints);
        List<KPoint2D> avgplussdpoints = new ArrayList<KPoint2D>(nPoints);
        double[] yvals = new double[nSeries];
        StatTools.Stats stats;
        for (double x = xmin; x <= xmax; x += delx) {
            for (int i = 0; i < nSeries; i++) {
                yvals[i] = selection.getSeries(i).interpolateY(x);
            }
            stats = StatTools.calcStats(yvals);
            avgminussdpoints.add(new KPoint2D(x, stats.avg - stats.stdDev));
            avgpoints.add(new KPoint2D(x, stats.avg));
            avgplussdpoints.add(new KPoint2D(x, stats.avg + stats.stdDev));
        }
        Series avgminussddest = new Series("avg-sd" + name, avgminussdpoints);
        Series avgdest = new Series("avg" + name, avgpoints);
        Series avgplussddest = new Series("avg+sd" + name, avgplussdpoints);
        avgminussddest.removeNaN();
        avgdest.removeNaN();
        avgplussddest.removeNaN();
        startModify();
        data.addSeries(insertIndex + 1, avgplussddest);
        data.addSeries(insertIndex + 2, avgdest);
        data.addSeries(insertIndex + 3, avgminussddest);
        int[] selind = {insertIndex + 1, insertIndex + 2, insertIndex + 3};
        this.dataListComponent.setSelectedIndices(selind);
        endModify();
    }

    //      adjustYIntercept invertY --- scaleX offsetX resampleX removeXRange \
    //      seriesMathMenu.items = addSeries subtractSeries multiplySeries divideSeries \
    //      averageSereies avgSDSeries avgPlusSDSeries
    //=======================================================================
    // HELP MENU Action Handlers
    //=======================================================================
    @Action
    public void about() {
        if (aboutBox == null) {
            JFrame mainFrame = view.getFrame();
            aboutBox = new TrixyAboutBox(mainFrame, false);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        show(aboutBox);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        SplashScreen splash = SplashScreen.getSplashScreen();
//        if (splash != null) {
//            if (false) {
//                Graphics2D g = (Graphics2D) splash.createGraphics();
//                Dimension dim = splash.getSize();
//                // Simulate loading
//                final int STEPS = 3;
//                final int SLEEP = 300;
//                final int YPOS = 250;
//                final int HEIGHT = 5;
//                for (int i = 0; i <= STEPS; i++) {
//                    g.setColor(Color.LIGHT_GRAY);
//                    g.fillRect(0, YPOS, dim.width, HEIGHT);
//                    g.setColor(Color.BLACK);
//                    g.fillRect(0, YPOS, i*dim.width/STEPS, HEIGHT);
//                    g.drawRect(0, YPOS, dim.width, HEIGHT);
//                    splash.update();
//                    try {
//                        Thread.sleep(SLEEP);
//                    } catch (InterruptedException e) {
//                    }
//                }
//            }
//        }
        launch(TrixyApp.class, args);
    }
}
>>>>>>> 76aa07461566a5976980e6696204781271955163

