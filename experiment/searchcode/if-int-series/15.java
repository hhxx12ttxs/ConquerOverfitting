/*
 * SeriesList.java
 *
 * Created on March 10, 2006, 2:17 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package kuhnlab.trixy.data;

import kuhnlab.trixy.data.io.TabbedFileHandler;
import java.awt.Component;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.DomainInfo;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.AbstractIntervalXYDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.IntervalXYDelegate;

/**
 *
 * @author drjrkuhn
 */
public class SeriesList extends AbstractIntervalXYDataset
        implements IntervalXYDataset, DomainInfo, Transferable, Serializable {

    protected List<Series> data;
    transient protected IntervalXYDelegate intervalDelegate;
    transient protected EventListenerList dataListeners;

    protected SeriesList(List<Series> fromdata) {
        data = fromdata;
        intervalDelegate = new IntervalXYDelegate(this, false);
        dataListeners = new EventListenerList();
        addChangeListener(intervalDelegate);
    }

    public SeriesList(int initialCapacity) {
        this(new ArrayList<Series>(initialCapacity));
    }

    public SeriesList() {
        this(new ArrayList<Series>(10));
    }

    protected void update() {
        fireDatasetChanged();
        if (dataListeners.getListenerCount() < 0) {
            return;
        }
        ListDataEvent event = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, data.size());
        for (ListDataListener ldl : dataListeners.getListeners(ListDataListener.class)) {
            ldl.contentsChanged(event);
        }
    }

    public void addSeries(Series series) {
        data.add(series);
        series.addChangeListener(this);
        update();
    }

    public void addSeries(int beforeIndex, Series series) {
        data.add(beforeIndex, series);
        series.addChangeListener(this);
        update();
    }

    public void addAllSeries(Collection<Series> collection) {
        for (Series ser : collection) {
            data.add(ser);
            ser.addChangeListener(this);
        }
        update();
    }

    public void removeSeries(int series) {
        if (series >= 0 && series < data.size()) {
            removeSeries(data.get(series));
        }
    }

    public void removeSeries(Series series) {
        if (data.contains(series)) {
            series.removeChangeListener(this);
            data.remove(series);
            update();
        }
    }

    public void removeSeries(Collection<Series> allSeries) {
        for (Series ser : allSeries) {
            ser.removeChangeListener(this);
        }
        data.removeAll(allSeries);
        update();
    }

    public void removeAllSeries() {
        for (Series ser : data) {
            ser.removeChangeListener(this);
        }
        data.clear();
        update();
    }

    public int getSeriesCount() {
        return data.size();
    }

    public List<Series> getSeries() {
        return Collections.unmodifiableList(data);
    }

    public Series getSeries(int series) {
        return data.get(series);
    }

    public Comparable getSeriesKey(int series) {
        return data.get(series).getName();
    }

    public int getItemCount(int series) {
        return data.get(series).getSize();
    }

    public Number getX(int series, int item) {
        return getXValue(series, item);
    }

    public double getXValue(int series, int item) {
        return data.get(series).points.get(item).x;
    }

    public Number getStartX(int series, int item) {
        return intervalDelegate.getStartX(series, item);
    }

    public Number getEndX(int series, int item) {
        return intervalDelegate.getEndX(series, item);
    }

    public Number getY(int series, int index) {
        return getYValue(series, index);
    }

    public double getYValue(int series, int item) {
        return data.get(series).points.get(item).y;
    }

    public Number getStartY(int series, int item) {
        return getY(series, item);
    }

    public Number getEndY(int series, int item) {
        return getY(series, item);
    }

    public int hashCode() {
        return data.hashCode();
    }

    public double getDomainLowerBound(boolean includeInterval) {
        return intervalDelegate.getDomainLowerBound(includeInterval);
    }

    public double getDomainUpperBound(boolean includeInterval) {
        return intervalDelegate.getDomainUpperBound(includeInterval);
    }

    public Range getDomainBounds(boolean includeInterval) {
        if (includeInterval) {
            return intervalDelegate.getDomainBounds(includeInterval);
        } else {
            return DatasetUtilities.iterateDomainBounds(this, includeInterval);
        }

    }

    public double getIntervalWidth() {
        return intervalDelegate.getIntervalWidth();
    }

    public void setIntervalWidth(double width) {
        intervalDelegate.setFixedIntervalWidth(width);
        update();
    }

    public double getIntervalPositionFactor() {
        return intervalDelegate.getIntervalPositionFactor();
    }

    public void setIntervalPositionFactor(double factor) {
        intervalDelegate.setIntervalPositionFactor(factor);
        update();
    }

    public boolean isAutoWidth() {
        return intervalDelegate.isAutoWidth();
    }

    public void setAutoWidth(boolean b) {
        intervalDelegate.setAutoWidth(b);
        update();
    }

    public double[] getXRange() {
        double[] minmax = {Double.MAX_VALUE, Double.MIN_VALUE};
        for (Series ser : data) {
            double[] range = ser.getXRange();
            if (range[0] < minmax[0]) {
                minmax[0] = range[0];
            }
            if (range[1] > minmax[1]) {
                minmax[1] = range[1];
            }
        }
        return minmax;
    }

    public double[] getYRange() {
        double[] minmax = {Double.MAX_VALUE, Double.MIN_VALUE};
        for (Series ser : data) {
            double[] range = ser.getYRange();
            if (range[0] < minmax[0]) {
                minmax[0] = range[0];
            }
            if (range[1] > minmax[1]) {
                minmax[1] = range[1];
            }
        }
        return minmax;
    }

    public void keepYRange(double dMinY, double dMaxY, boolean setNaN) {
        for (Series ser : data) {
            ser.keepYRange(dMinY, dMaxY, setNaN);
        }
        update();
    }

    public void removeNaN() {
        for (Series ser : data) {
            ser.removeNaN();
        }
        update();
    }

    public SeriesList shallowCopy() {
        SeriesList copy = new SeriesList(data.size());
        copy.data.addAll(data);
        return copy;
    }

    public Object clone() {
        List<Series> dest = new ArrayList<Series>(data.size());
        for (Series ser : data) {
            dest.add((Series) ser.clone());
        }
        return new SeriesList(dest);
    }

    public double getAverageDeltaX() {
        double delx = 0;
        for (Series ser : data) {
            delx += ser.getAverageDeltaX();
        }
        return delx / data.size();
    }

    public void sortByName() {
        Collections.sort(data, new Comparator<Series>() {
            @Override
            public int compare(Series o1, Series o2) {
                return o1.name.compareTo(o2.name);
            }
        });
    }
    
    public void sortByNamesValue() {
        Collections.sort(data, new Comparator<Series>() {
            @Override
            public int compare(Series o1, Series o2) {
                return Double.valueOf(o1.name).compareTo(Double.valueOf(o2.name));
            }
        });
    }

    //=======================================================================
    // ListModel implementation
    //=======================================================================
    public class SeriesListModel implements ListModel {

        public int getSize() {
            return data.size();
        }

        public Object getElementAt(int index) {
            return data.get(index).getName();
        }

        public void addListDataListener(ListDataListener l) {
            dataListeners.add(ListDataListener.class, l);
        }

        public void removeListDataListener(ListDataListener l) {
            dataListeners.remove(ListDataListener.class, l);
        }
    }
    transient protected SeriesListModel listModel;

    public SeriesListModel getListModel() {
        if (listModel == null) {
            listModel = new SeriesListModel();
        }
        return listModel;
    }

    //=======================================================================
    // ListCellRenderer implementation
    //=======================================================================
    public class SeriesListCellRenderer extends DefaultListCellRenderer {

        public Font boldFont = null;
        public Font italicFont = null;
        public Border separatorBorder = null;
        XYItemRenderer chartRenderer = null;

        SeriesListCellRenderer(XYItemRenderer chartRenderer) {
            this.chartRenderer = chartRenderer;
            separatorBorder = new CompoundBorder(new BevelBorder(BevelBorder.RAISED),
                    new LineBorder(SystemColor.control, 2));
        }

        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            Series ser = data.get(index);
            Component renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (boldFont == null) {
                boldFont = renderer.getFont().deriveFont(Font.BOLD);
            }
            if (italicFont == null) {
                italicFont = renderer.getFont().deriveFont(Font.ITALIC + Font.BOLD);
            }
            if (ser.getName().equals("")) {
                setBorder(separatorBorder);
            }
            boolean visible = true;
            if (chartRenderer != null) {
                visible = chartRenderer.isSeriesVisible(index);
            }
            renderer.setForeground(visible ? ser.getColor() : SystemColor.textInactiveText);
            renderer.setFont(visible ? boldFont : italicFont);
            return renderer;
        }
    }

    public SeriesListCellRenderer makeListRenderer(XYItemRenderer chartRenderer) {
        return new SeriesListCellRenderer(chartRenderer);
    }

    /**
     * Get visibility from mainChartRenderer and set a temporary flag for each
     * series.
     */
    public void saveVisibility(XYItemRenderer chartRenderer) {
        if (chartRenderer == null) {
            return;
        }
        int nSeries = data.size();
        for (int i = 0; i < nSeries; i++) {
            Boolean bv = chartRenderer.isSeriesVisible(i);
            data.get(i).visible = (bv == null) ? true : bv.booleanValue();
        }
    }

    /**
     * Set visibility in mainChartRenderer from temporary flag stored in each
     * series.
     */
    public void restoreVisibility(XYItemRenderer chartRenderer, boolean notify) {
        if (chartRenderer == null) {
            return;
        }
        int nSeries = data.size();
        for (int i = 0; i < nSeries; i++) {
            Boolean bv = data.get(i).visible ? Boolean.TRUE : Boolean.FALSE;
            chartRenderer.setSeriesVisible(i, bv, notify);
        }

    }
    //=======================================================================
    // Transferable implementation
    //=======================================================================
    public static final DataFlavor objectFlavor = new DataFlavor(SeriesList.class, "SeriesList object");
    public static final DataFlavor[] supportedFlavors = {objectFlavor, DataFlavor.stringFlavor};

    public DataFlavor[] getTransferDataFlavors() {
        return supportedFlavors;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        for (int i = 0; i < supportedFlavors.length; i++) {
            if (supportedFlavors[i].equals(flavor)) {
                return true;
            }
        }
        return false;
    }

    public Object getTransferData(DataFlavor flavor) throws
            UnsupportedFlavorException, IOException {
        if (flavor.equals(DataFlavor.stringFlavor)) {
            StringWriter sw = new StringWriter();
            TabbedFileHandler handler = new TabbedFileHandler();
            if (!handler.seriesListToWriter(this, sw)) {
                return null;
            } else {
                return sw.toString();
            }
        } else if (flavor.equals(objectFlavor)) {
            return this;
        }
        throw new UnsupportedFlavorException(flavor);
    }

    //=======================================================================
    // Clipboard handling extras
    //=======================================================================
    /**
     * Creates a new SeriesList as a sublist of this SeriesList. Series to
     * include are listed in indices.
     */
    public SeriesList subList(int[] indices) {
        if (indices == null) {
            return null;
        }
        SeriesList list = new SeriesList(indices.length);
        for (int i = 0; i < indices.length; i++) {
            int index = indices[i];
            list.addSeries(getSeries(index));
        }
        return list;
    }

    /**
     * Check if data transfer can handle any of these data flavors.
     */
    public boolean canImport(DataFlavor[] flavors) {
        for (int i = 0; i < flavors.length; i++) {
            if (isDataFlavorSupported(flavors[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * used to paste transfer data to the list. NOTE: Not part of Transferable
     * interface
     */
    public boolean insertTransferData(int beforeIndex, Transferable transfer) throws
            UnsupportedFlavorException, IOException {
        SeriesList source;
        if (transfer.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            String data = (String) transfer.getTransferData(DataFlavor.stringFlavor);
            StringReader sr = new StringReader(data);
            TabbedFileHandler handler = new TabbedFileHandler();
            source = handler.seriesListFromReader(sr);
        } else if (transfer.isDataFlavorSupported(objectFlavor)) {
            source = (SeriesList) transfer.getTransferData(objectFlavor);
        } else {
            return false;
        }
        if (source == null) {
            return false;
        }
        data.addAll(beforeIndex, source.data);
        for (Series ser : source.data) {
            ser.addChangeListener(this);
        }
        update();
        return true;
    }

    //=======================================================================
    // Serializable overrides
    //=======================================================================
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // contruction tasks. see method:
        //      protected SeriesList(List<Series> fromdata);
        intervalDelegate = new IntervalXYDelegate(this, false);
        dataListeners = new EventListenerList();
        addChangeListener(intervalDelegate);
    }
}

