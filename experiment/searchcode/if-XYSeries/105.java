/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package org.esa.beam.visat.toolviews.spectrum;

import com.bc.ceres.glevel.MultiLevelModel;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.DataNode;
import org.esa.beam.framework.datamodel.Placemark;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductManager;
import org.esa.beam.framework.datamodel.ProductNodeEvent;
import org.esa.beam.framework.datamodel.ProductNodeGroup;
import org.esa.beam.framework.datamodel.ProductNodeListenerAdapter;
import org.esa.beam.framework.help.HelpSys;
import org.esa.beam.framework.ui.GridBagUtils;
import org.esa.beam.framework.ui.ModalDialog;
import org.esa.beam.framework.ui.PixelPositionListener;
import org.esa.beam.framework.ui.UIUtils;
import org.esa.beam.framework.ui.application.support.AbstractToolView;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.esa.beam.framework.ui.product.spectrum.DisplayableSpectrum;
import org.esa.beam.framework.ui.product.spectrum.SpectrumChooser;
import org.esa.beam.framework.ui.product.spectrum.SpectrumConstants;
import org.esa.beam.framework.ui.tool.ToolButtonFactory;
import org.esa.beam.jai.ImageManager;
import org.esa.beam.util.Debug;
import org.esa.beam.util.ProductUtils;
import org.esa.beam.visat.VisatApp;
import org.esa.beam.visat.toolviews.nav.CursorSynchronizer;
import org.esa.beam.visat.toolviews.placemark.PlacemarkUtils;
import org.esa.beam.visat.toolviews.stat.XYPlotMarker;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.block.LineBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A window which displays product allSpectra.
 */
public class SpectrumToolView extends AbstractToolView {

    public static final String ID = SpectrumToolView.class.getName();
    public static final String CHART_TITLE = "Spectrum View";

    private static final String SUPPRESS_MESSAGE_KEY = "plugin.spectrum.tip";

    private final Map<Product, List<DisplayableSpectrum>> productToAllSpectraMap;

    private final ProductNodeListenerAdapter productNodeHandler;
    private final PinSelectionChangeListener pinSelectionChangeListener;
    private final PixelPositionListener pixelPositionListener;

    private AbstractButton filterButton;
    private AbstractButton showSpectrumForCursorButton;
    private AbstractButton showSpectraForSelectedPinsButton;
    private AbstractButton showSpectraForAllPinsButton;
    private AbstractButton showGridButton;
// todo - not yet implemented for 4.1 but planned for 4.2 (mp - 31.10.2007)
//    private AbstractButton showAveragePinSpectrumButton;
    //    private AbstractButton showGraphPointsButton;

    private String titleBase;
    private boolean tipShown;
    private ProductSceneView currentView;
    private Product currentProduct;
    private final ChartPanel chartPanel;
    private final ChartHandler chartHandler;
    private CursorSynchronizer cursorSynchronizer;

    public SpectrumToolView() {
        productNodeHandler = new ProductNodeHandler();
        pinSelectionChangeListener = new PinSelectionChangeListener();
        productToAllSpectraMap = new HashMap<Product, List<DisplayableSpectrum>>();
        pixelPositionListener = new CursorSpectrumPixelPositionListener(this);
        final JFreeChart chart = ChartFactory.createXYLineChart(CHART_TITLE, "Wavelength (nm)", "mW/(m^2*sr*nm)", null, PlotOrientation.VERTICAL, true, true, false);
        chartPanel = new ChartPanel(chart);
        chartHandler = new ChartHandler(chart);
    }

    private ProductSceneView getCurrentView() {
        return currentView;
    }

    private void setCurrentView(ProductSceneView view) {
        ProductSceneView oldView = currentView;
        currentView = view;
        if (oldView != currentView) {
            if (oldView != null) {
                oldView.removePropertyChangeListener(ProductSceneView.PROPERTY_NAME_SELECTED_PIN, pinSelectionChangeListener);
            }
            if (currentView != null) {
                currentView.addPropertyChangeListener(ProductSceneView.PROPERTY_NAME_SELECTED_PIN, pinSelectionChangeListener);
                setCurrentProduct(currentView.getProduct());
            }
            updateUIState();
        }
    }

    private Product getCurrentProduct() {
        return currentProduct;
    }

    private void setCurrentProduct(Product product) {
        Product oldProduct = currentProduct;
        currentProduct = product;
        if (currentProduct != oldProduct) {
            if (oldProduct != null) {
                oldProduct.removeProductNodeListener(productNodeHandler);
            }
            if (currentProduct != null) {
                currentProduct.addProductNodeListener(productNodeHandler);
                initSpectra();
                recreateChart();
            }
            if (currentProduct == null) {
                chartHandler.setEmptyPlot();
            }
            updateUIState();
            updateTitle();
        }
    }

    private void updateTitle() {
        if (currentProduct != null) {
            setTitle(titleBase + " - " + currentView.getProduct().getProductRefString());
        } else {
            setTitle(titleBase);
        }
    }

    private void updateUIState() {
        boolean hasView = getCurrentView() != null;
        boolean hasProduct = getCurrentProduct() != null;
        boolean hasSelectedPins = hasView && getCurrentView().getSelectedPins().length > 0;
        boolean hasPins = hasProduct && getCurrentProduct().getPinGroup().getNodeCount() > 0;
        boolean chartHandlerHasDiagram = chartHandler.hasDiagram();
        filterButton.setEnabled(hasProduct);
        showSpectrumForCursorButton.setEnabled(hasView);
        showSpectraForSelectedPinsButton.setEnabled(hasSelectedPins);
        showSpectraForAllPinsButton.setEnabled(hasPins);
        showGridButton.setEnabled(chartHandlerHasDiagram);
// todo - not yet implemented for 4.1 but planned for 4.2 (mp - 31.10.2007)
//        showAveragePinSpectrumButton.setEnabled(hasPins); // todo - hasSpectraGraphs
//        showGraphPointsButton.setEnabled(chartHandlerHasDiagram);
        chartPanel.setEnabled(hasProduct);    // todo - hasSpectraGraphs
        if (chartHandlerHasDiagram) {
            showGridButton.setSelected(true);
        }
    }

    protected void updateSpectra(int pixelX, int pixelY, int level) {
        maybeShowTip();
        chartHandler.setPosition(pixelX, pixelY);
        chartHandler.setLevel(level);
        chartHandler.updateChart();
        chartPanel.repaint();
    }

    private void maybeShowTip() {
        //todo remove when axes cannot be adjusted anymore
        if (!tipShown) {
            final String message = "Tip: If you press the SHIFT key while moving the mouse cursor over \n" +
                    "an image, " + VisatApp.getApp().getAppName() + " adjusts the diagram axes " +
                    "to the local values at the\n" +
                    "current pixel position, if you release the SHIFT key again, then the\n" +
                    "min/max are accumulated again.";
            VisatApp.getApp().showInfoDialog("Spectrum Tip", message, SUPPRESS_MESSAGE_KEY);
            tipShown = true;
        }
    }

    private Band[] getAvailableSpectralBands() {
        Debug.assertNotNull(getCurrentProduct());
        Band[] bands = getCurrentProduct().getBands();
        ArrayList<Band> spectralBands = new ArrayList<Band>(15);
        for (Band band : bands) {
            if (band.getSpectralWavelength() > 0.0) {
                if (!band.isFlagBand()) {
                    spectralBands.add(band);
                }
            }
        }
        return spectralBands.toArray(new Band[spectralBands.size()]);
    }

    @Override
    public JComponent createControl() {
        titleBase = getDescriptor().getTitle();
        filterButton = ToolButtonFactory.createButton(UIUtils.loadImageIcon("icons/Filter24.gif"), false);
        filterButton.setName("filterButton");
        filterButton.setEnabled(false);
        filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectSpectralBands();
                recreateChart();
            }
        });

        showSpectrumForCursorButton = ToolButtonFactory.createButton(
                UIUtils.loadImageIcon("icons/CursorSpectrum24.gif"), true);
        showSpectrumForCursorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                recreateChart();
            }
        });
        showSpectrumForCursorButton.setName("showSpectrumForCursorButton");
        showSpectrumForCursorButton.setSelected(true);
        showSpectrumForCursorButton.setToolTipText("Show spectrum at cursor position.");

        showSpectraForSelectedPinsButton = ToolButtonFactory.createButton(
                UIUtils.loadImageIcon("icons/SelectedPinSpectra24.gif"), true);
        showSpectraForSelectedPinsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isShowingSpectraForAllPins()) {
                    showSpectraForAllPinsButton.setSelected(false);
                }
                recreateChart();
            }
        });
        showSpectraForSelectedPinsButton.setName("showSpectraForSelectedPinsButton");
        showSpectraForSelectedPinsButton.setToolTipText("Show allSpectra for selected pins.");

        showSpectraForAllPinsButton = ToolButtonFactory.createButton(UIUtils.loadImageIcon("icons/PinSpectra24.gif"),
                                                                     true);
        showSpectraForAllPinsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isShowingSpectraForSelectedPins()) {
                    showSpectraForSelectedPinsButton.setSelected(false);
                }
                recreateChart();
            }
        });
        showSpectraForAllPinsButton.setName("showSpectraForAllPinsButton");
        showSpectraForAllPinsButton.setToolTipText("Show allSpectra for all pins.");

// todo - not yet implemented for 4.1 but planned for 4.2 (mp - 31.10.2007)
//        showAveragePinSpectrumButton = ToolButtonFactory.createButton(
//                UIUtils.loadImageIcon("icons/AverageSpectrum24.gif"), true);
//        showAveragePinSpectrumButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                // todo - implement
//                JOptionPane.showMessageDialog(null, "Not implemented");
//            }
//        });
//        showAveragePinSpectrumButton.setName("showAveragePinSpectrumButton");
//        showAveragePinSpectrumButton.setToolTipText("Show average spectrum of all pin allSpectra.");

        showGridButton = ToolButtonFactory.createButton(UIUtils.loadImageIcon("icons/SpectrumGrid24.gif"), true);
        showGridButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chartHandler.setGridVisible(showGridButton.isSelected());
            }
        });
        showGridButton.setName("showGridButton");
        showGridButton.setToolTipText("Show diagram grid.");

// todo - not yet implemented for 4.1 but planned for 4.2 (mp - 31.10.2007)
//        showGraphPointsButton = ToolButtonFactory.createButton(UIUtils.loadImageIcon("icons/GraphPoints24.gif"), true);
//        showGraphPointsButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                // todo - implement
//                JOptionPane.showMessageDialog(null, "Not implemented");
//            }
//        });
//        showGraphPointsButton.setName("showGraphPointsButton");
//        showGraphPointsButton.setToolTipText("Show graph points grid.");

        AbstractButton exportSpectraButton = ToolButtonFactory.createButton(UIUtils.loadImageIcon("icons/Export24.gif"),
                                                                            false);
        exportSpectraButton.addActionListener(new SpectraExportAction(this));
        exportSpectraButton.setToolTipText("Export allSpectra to text file.");
        exportSpectraButton.setName("exportSpectraButton");

        AbstractButton helpButton = ToolButtonFactory.createButton(UIUtils.loadImageIcon("icons/Help22.png"), false);
        helpButton.setName("helpButton");
        helpButton.setToolTipText("Help."); /*I18N*/

        final JPanel buttonPane = GridBagUtils.createPanel();
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets.top = 2;
        gbc.gridy = 0;
        buttonPane.add(filterButton, gbc);
        gbc.gridy++;
        buttonPane.add(showSpectrumForCursorButton, gbc);
        gbc.gridy++;
        buttonPane.add(showSpectraForSelectedPinsButton, gbc);
        gbc.gridy++;
        buttonPane.add(showSpectraForAllPinsButton, gbc);
        gbc.gridy++;
// todo - not yet implemented for 4.1 but planned for 4.2 (mp - 31.10.2007)
//        buttonPane.add(showAveragePinSpectrumButton, gbc);
//        gbc.gridy++;
        buttonPane.add(showGridButton, gbc);
        gbc.gridy++;
// todo - not yet implemented for 4.1 but planned for 4.2 (mp - 31.10.2007)
//        buttonPane.add(showGraphPointsButton, gbc);
//        gbc.gridy++;
        buttonPane.add(exportSpectraButton, gbc);

        gbc.gridy++;
        gbc.insets.bottom = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.weighty = 1.0;
        gbc.gridwidth = 2;
        buttonPane.add(new JLabel(" "), gbc); // filler
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0.0;
        gbc.gridy = 10;
        gbc.anchor = GridBagConstraints.EAST;
        buttonPane.add(helpButton, gbc);

        chartPanel.setPreferredSize(new Dimension(300, 200));
        chartPanel.setBackground(Color.white);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createBevelBorder(BevelBorder.LOWERED),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        chartPanel.addChartMouseListener(new XYPlotMarker(chartPanel, new XYPlotMarker.Listener() {
            @Override
            public void pointSelected(XYDataset xyDataset, int seriesIndex, Point2D dataPoint) {
                if (hasDiagram()) {
                    if (cursorSynchronizer == null) {
                        cursorSynchronizer = new CursorSynchronizer(VisatApp.getApp());
                    }
                    if (!cursorSynchronizer.isEnabled()) {
                        cursorSynchronizer.setEnabled(true);
                    }
                }
            }

            @Override
            public void pointDeselected() {
                cursorSynchronizer.setEnabled(false);
            }
        }));

        JPanel mainPane = new JPanel(new BorderLayout(4, 4));
        mainPane.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        mainPane.add(BorderLayout.CENTER, chartPanel);
        mainPane.add(BorderLayout.EAST, buttonPane);
        mainPane.setPreferredSize(new Dimension(320, 200));

        if (getDescriptor().getHelpId() != null) {
            HelpSys.enableHelpOnButton(helpButton, getDescriptor().getHelpId());
            HelpSys.enableHelpKey(mainPane, getDescriptor().getHelpId());
        }

        // Add an internal frame listener to VISAT so that we can update our
        // spectrum dialog with the information of the currently activated
        // product scene view.
        //
        VisatApp.getApp().addInternalFrameListener(new SpectrumIFL());

        VisatApp.getApp().getProductManager().addListener(new ProductManager.Listener() {
            @Override
            public void productAdded(ProductManager.Event event) {
                // ignored
            }

            @Override
            public void productRemoved(ProductManager.Event event) {
                final Product product = event.getProduct();
                if (getCurrentProduct() == product) {
                    chartPanel.getChart().getXYPlot().setDataset(null);
                    setCurrentView(null);
                    setCurrentProduct(null);
                }
            }
        });


        final ProductSceneView view = VisatApp.getApp().getSelectedProductSceneView();
        if (view != null) {
            handleViewActivated(view);
        } else {
            setCurrentView(view);
        }
        updateUIState();
        return mainPane;
    }

    private void selectSpectralBands() {
        final List<DisplayableSpectrum> allSpectra = productToAllSpectraMap.get(getCurrentProduct());
        final SpectrumChooser spectrumChooser = new SpectrumChooser(getPaneWindow(), allSpectra,
                                                                    getDescriptor().getHelpId());
        if (spectrumChooser.show() == ModalDialog.ID_OK) {
            final List<DisplayableSpectrum> spectra = spectrumChooser.getSpectra();
            productToAllSpectraMap.put(getCurrentProduct(), spectra);
        }
    }

    boolean isShowingCursorSpectrum() {
        return showSpectrumForCursorButton.isSelected();
    }

    private boolean isShowingPinSpectra() {
        return isShowingSpectraForSelectedPins() || isShowingSpectraForAllPins();
    }

    private boolean isShowingSpectraForAllPins() {
        return showSpectraForAllPinsButton.isSelected();
    }

    private void recreateChart() {
        chartHandler.updateChart();
        chartPanel.repaint();
        updateUIState();
    }

    Placemark[] getDisplayedPins() {
        if (isShowingSpectraForSelectedPins()) {
            return getCurrentView().getSelectedPins();
        } else if (isShowingSpectraForAllPins()) {
            ProductNodeGroup<Placemark> pinGroup = getCurrentProduct().getPinGroup();
            return pinGroup.toArray(new Placemark[pinGroup.getNodeCount()]);
        } else {
            return new Placemark[0];
        }
    }

    private void initSpectra() {
        if (!areSpectralBandsAvailable()) {
            final ArrayList<DisplayableSpectrum> emptySpectraList = new ArrayList<DisplayableSpectrum>();
            productToAllSpectraMap.put(getCurrentProduct(), emptySpectraList);
        }
        final Product.AutoGrouping autoGrouping = this.getCurrentProduct().getAutoGrouping();
        if (autoGrouping != null) {
            List<DisplayableSpectrum> spectra = new ArrayList<DisplayableSpectrum>();
            final Band[] availableSpectralBands = getAvailableSpectralBands();
            final Iterator<String[]> iterator = autoGrouping.iterator();
            int groupIndex = 0;
            while (iterator.hasNext()) {
                final String spectrumName = iterator.next()[0];
                DisplayableSpectrum spectrum = new DisplayableSpectrum(spectrumName);
                for (Band availableSpectralBand : availableSpectralBands) {
                    if (autoGrouping.indexOf(availableSpectralBand.getName()) == groupIndex) {
                        spectrum.addBand(availableSpectralBand);
                    }
                }
                if (groupIndex != 0) {
                    spectrum.setSelected(false);
                }
                groupIndex++;
                if (spectrum.hasBands()) {
                    spectra.add(spectrum);
                }
            }
            productToAllSpectraMap.put(getCurrentProduct(), spectra);
        } else {
            final ArrayList<DisplayableSpectrum> spectra = new ArrayList<DisplayableSpectrum>();
            spectra.add(new DisplayableSpectrum("Available spectral bands", getAvailableSpectralBands()));
            productToAllSpectraMap.put(getCurrentProduct(), spectra);
        }
    }

    private List<DisplayableSpectrum> getAllSpectra() {
        return productToAllSpectraMap.get(getCurrentProduct());
    }

    private boolean areSpectralBandsAvailable() {
        return getAvailableSpectralBands().length > 0;
    }

    private boolean isShowingSpectraForSelectedPins() {
        return showSpectraForSelectedPinsButton.isSelected();
    }

    List<DisplayableSpectrum> getSelectedSpectra() {
        List<DisplayableSpectrum> selectedSpectra = new ArrayList<DisplayableSpectrum>();
        List<DisplayableSpectrum> allSpectra = productToAllSpectraMap.get(getCurrentProduct());
        if (allSpectra != null) {
            for (DisplayableSpectrum displayableSpectrum : allSpectra) {
                if (displayableSpectrum.isSelected()) {
                    selectedSpectra.add(displayableSpectrum);
                }
            }
        }
        return selectedSpectra;
    }

    void removeCursorSpectra() {
        chartHandler.removeCursorSpectra();
    }

    boolean hasDiagram() {
        return chartHandler.hasDiagram();
    }

    private void handleViewActivated(final ProductSceneView view) {
        view.addPixelPositionListener(pixelPositionListener);
        setCurrentView(view);
    }

    private void handleViewDeactivated(final ProductSceneView view) {
        view.removePixelPositionListener(pixelPositionListener);
        setCurrentView(null);
    }

    public boolean hasValidCursorPosition() {
        return chartHandler.hasValidCursorPosition();
    }

    private class ChartHandler {

        private static final String MESSAGE_NO_SPECTRAL_BANDS = "No spectral bands available";   /*I18N*/
        private static final String MESSAGE_NO_PRODUCT_SELECTED = "No product selected";
        private static final String MESSAGE_NO_SPECTRA_SELECTED = "No spectra selected";
        private static final String MESSAGE_COLLECTING_SPECTRAL_INFORMATION = "Collecting spectral information...";

        private final JFreeChart chart;
        private final ChartUpdater chartUpdater;

        private ChartHandler(JFreeChart chart) {
            chartUpdater = new ChartUpdater();
            this.chart = chart;
            setLegend(chart);
            final XYPlot plot = chart.getXYPlot();
            final XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
            renderer.setBaseShapesVisible(true);
            renderer.setBaseShapesFilled(false);
            setPlotMessage(MESSAGE_NO_PRODUCT_SELECTED);
        }

        private void setLegend(JFreeChart chart) {
            chart.removeLegend();
            final LegendTitle legend = new LegendTitle(new SpectrumLegendItemSource());
            legend.setPosition(RectangleEdge.BOTTOM);
            LineBorder border = new LineBorder(Color.BLACK, new BasicStroke(), new RectangleInsets(2, 2, 2, 2));
            legend.setFrame(border);
            chart.addLegend(legend);
        }

        private void setPosition(int pixelX, int pixelY) {
            chartUpdater.setPositionAndLevel(pixelX, pixelY);
        }

        private void setLevel(int level) {
            chartUpdater.setLevel(level);
        }

        private void updateChart() {
            List<DisplayableSpectrum> spectra = getSelectedSpectra();
            if (spectra.isEmpty()) {
                setEmptyPlot();
                return;
            }
            setPlotMessage(MESSAGE_COLLECTING_SPECTRAL_INFORMATION);
            chartUpdater.updateChart(chart, spectra);
            chart.getXYPlot().clearAnnotations();
        }

        private void setEmptyPlot() {
            chart.getXYPlot().setDataset(null);
            if (getCurrentProduct() == null) {
                setPlotMessage(MESSAGE_NO_PRODUCT_SELECTED);
            } else if (!getAllSpectra().isEmpty()) {
                setPlotMessage(MESSAGE_NO_SPECTRA_SELECTED);
            } else {
                setPlotMessage(MESSAGE_NO_SPECTRAL_BANDS);
            }
        }

        private void setGridVisible(boolean visible) {
            chart.getXYPlot().setDomainGridlinesVisible(visible);
            chart.getXYPlot().setRangeGridlinesVisible(visible);
        }

        private boolean hasDiagram() {
            return chart.getXYPlot().getDataset() != null;
        }

        private void removePinInformation(Placemark pin) {
            chartUpdater.removePinInformation(pin);
        }

        private void setPlotMessage(String messageText) {
            chart.getXYPlot().clearAnnotations();
            TextTitle tt = new TextTitle(messageText);
            tt.setTextAlignment(HorizontalAlignment.RIGHT);
            tt.setFont(chart.getLegend().getItemFont());
            tt.setBackgroundPaint(new Color(200, 200, 255, 50));
            tt.setFrame(new BlockBorder(Color.white));
            tt.setPosition(RectangleEdge.BOTTOM);
            XYTitleAnnotation message = new XYTitleAnnotation(0.5, 0.5, tt, RectangleAnchor.CENTER);
            chart.getXYPlot().addAnnotation(message);
        }

        public boolean hasValidCursorPosition() {
            return chartUpdater.hasValidCursorPosition();
        }

        public void removeCursorSpectra() {
            chartUpdater.removeCursorSpectra();
            updateChart();
        }
    }

    private class ChartUpdater {

        private final Map<Placemark, Map<Band, Double>> pinToEnergies;
        private int pixelX;
        private int pixelY;
        private int level;

        private ChartUpdater() {
            pinToEnergies = new HashMap<Placemark, Map<Band, Double>>();
        }

        private void setLevel(int level) {
            this.level = level;
        }

        private void setPositionAndLevel(int pixelX, int pixelY) {
            this.pixelX = pixelX;
            this.pixelY = pixelY;
        }

        private void updateChart(JFreeChart chart, List<DisplayableSpectrum> spectra) {
            final XYSeriesCollection dataset = new XYSeriesCollection();
            if (level >= 0) {
                fillDatasetWithPinSeries(spectra, dataset, chart);
                if (hasValidCursorPosition()) {
                    fillDatasetWithCursorSeries(spectra, dataset, chart);
                }
            }
            chart.getXYPlot().setDataset(dataset);
        }

        private void fillDatasetWithCursorSeries(List<DisplayableSpectrum> spectra, XYSeriesCollection dataset, JFreeChart chart) {
            if (isShowingCursorSpectrum() && getCurrentView().isCurrentPixelPosValid()) {
                for (DisplayableSpectrum spectrum : spectra) {
                    XYSeries series = new XYSeries(spectrum.getName());
                    final Band[] spectralBands = spectrum.getSelectedBands();
                    for (Band spectralBand : spectralBands) {
                        final float wavelength = spectralBand.getSpectralWavelength();
                        final double energy = ProductUtils.getGeophysicalSampleDouble(spectralBand, pixelX, pixelY, level);
                        if (energy != spectralBand.getGeophysicalNoDataValue()) {
                            series.add(wavelength, energy);
                        }
                    }
                    updateRenderer(dataset.getSeriesCount(), Color.BLACK, spectrum, chart);
                    dataset.addSeries(series);
                }
            }
        }

        private void fillDatasetWithPinSeries(List<DisplayableSpectrum> spectra, XYSeriesCollection dataset, JFreeChart chart) {
            Placemark[] pins = getDisplayedPins();
            for (Placemark pin : pins) {
                List<XYSeries> pinSeries = createXYSeriesFromPin(pin, dataset.getSeriesCount(), spectra, chart);
                for (XYSeries series : pinSeries) {
                    dataset.addSeries(series);
                }
            }
        }

        private List<XYSeries> createXYSeriesFromPin(Placemark pin, int seriesIndex, List<DisplayableSpectrum> spectra, JFreeChart chart) {
            List<XYSeries> pinSeries = new ArrayList<XYSeries>();
            Color pinColor = PlacemarkUtils.getPlacemarkColor(pin, getCurrentView());
            for (DisplayableSpectrum spectrum : spectra) {
                XYSeries series = new XYSeries(spectrum.getName() + "_" + pin.getLabel());
                final Band[] spectralBands = spectrum.getSelectedBands();
                Map<Band, Double> bandToEnergy;
                if (pinToEnergies.containsKey(pin)) {
                    bandToEnergy = pinToEnergies.get(pin);
                } else {
                    bandToEnergy = new HashMap<Band, Double>();
                    pinToEnergies.put(pin, bandToEnergy);
                }
                for (Band spectralBand : spectralBands) {
                    double energy;
                    if (bandToEnergy.containsKey(spectralBand)) {
                        energy = bandToEnergy.get(spectralBand);
                    } else {
                        energy = readEnergy(pin, spectralBand);
                        bandToEnergy.put(spectralBand, energy);
                    }
                    final float wavelength = spectralBand.getSpectralWavelength();
                    if (energy != spectralBand.getGeophysicalNoDataValue()) {
                        series.add(wavelength, energy);
                    }
                }
                updateRenderer(seriesIndex++, pinColor, spectrum, chart);
                pinSeries.add(series);
            }
            return pinSeries;
        }

        private void updateRenderer(int seriesIndex, Color seriesColor, DisplayableSpectrum spectrum, JFreeChart chart) {
            final XYItemRenderer renderer = chart.getXYPlot().getRenderer();
            renderer.setSeriesPaint(seriesIndex, seriesColor);
            final Stroke lineStyle = spectrum.getLineStyle();
            if (lineStyle != null) {
                renderer.setSeriesStroke(seriesIndex, lineStyle);
            } else {
                renderer.setSeriesStroke(seriesIndex, SpectrumConstants.strokes[0]);
            }
            final Shape symbol = spectrum.getSymbol();
            if (symbol != null) {
                renderer.setSeriesShape(seriesIndex, symbol);
            } else {
                renderer.setSeriesShape(seriesIndex, SpectrumConstants.shapes[0]);
            }
        }

        private double readEnergy(Placemark pin, Band spectralBand) {
            final MultiLevelModel multiLevelModel = ImageManager.getMultiLevelModel(spectralBand);
            final AffineTransform i2mTransform = multiLevelModel.getImageToModelTransform(0);
            final AffineTransform m2iTransform = multiLevelModel.getModelToImageTransform(level);
            final Point2D modelPixel = i2mTransform.transform(pin.getPixelPos(), null);
            final Point2D imagePixel = m2iTransform.transform(modelPixel, null);
            int pinPixelX = (int) Math.floor(imagePixel.getX());
            int pinPixelY = (int) Math.floor(imagePixel.getY());
            return ProductUtils.getGeophysicalSampleDouble(spectralBand, pinPixelX, pinPixelY, level);
        }

        private void removePinInformation(Placemark pin) {
            pinToEnergies.remove(pin);
        }

        public boolean hasValidCursorPosition() {
            return pixelX > Integer.MIN_VALUE && pixelY > Integer.MIN_VALUE;
        }

        public void removeCursorSpectra() {
            pixelX = Integer.MIN_VALUE;
            pixelY = Integer.MIN_VALUE;
        }
    }

    private class SpectrumLegendItemSource implements LegendItemSource {

        @Override
        public LegendItemCollection getLegendItems() {
            LegendItemCollection itemCollection = new LegendItemCollection();
            final Placemark[] displayedPins = getDisplayedPins();
            final List<DisplayableSpectrum> spectra = getSelectedSpectra();
            for (Placemark pin : displayedPins) {
                Paint pinPaint = PlacemarkUtils.getPlacemarkColor(pin, getCurrentView());
                for (DisplayableSpectrum spectrum : spectra) {
                    String legendLabel = pin.getLabel() + "_" + spectrum.getName();
                    LegendItem item = createLegendItem(spectrum, pinPaint, legendLabel);
                    itemCollection.add(item);
                }
            }
            if (isShowingCursorSpectrum() && hasValidCursorPosition()) {
                for (DisplayableSpectrum spectrum : spectra) {
                    Paint defaultPaint = Color.BLACK;
                    LegendItem item = createLegendItem(spectrum, defaultPaint, spectrum.getName());
                    itemCollection.add(item);
                }
            }
            return itemCollection;
        }

        private LegendItem createLegendItem(DisplayableSpectrum spectrum, Paint paint, String legendLabel) {
            Stroke outlineStroke = new BasicStroke();
            Line2D lineShape = new Line2D.Double(0, 5, 40, 5);
            Stroke lineStyle = spectrum.getLineStyle();
            if (lineStyle == null) {
                lineStyle = SpectrumConstants.strokes[0];
            }
            Shape symbol = spectrum.getSymbol();
            if (symbol == null) {
                symbol = SpectrumConstants.shapes[0];
            }
            return new LegendItem(legendLabel, legendLabel, legendLabel, legendLabel,
                                  true, symbol, false,
                                  paint, true, paint, outlineStroke,
                                  true, lineShape, lineStyle, paint);
        }

    }

/////////////////////////////////////////////////////////////////////////
// View change handling

    private class SpectrumIFL extends InternalFrameAdapter {

        @Override
        public void internalFrameActivated(InternalFrameEvent e) {
            final Container contentPane = e.getInternalFrame().getContentPane();
            if (contentPane instanceof ProductSceneView) {
                handleViewActivated((ProductSceneView) contentPane);
            }
        }

        @Override
        public void internalFrameDeactivated(InternalFrameEvent e) {
            final Container contentPane = e.getInternalFrame().getContentPane();
            if (contentPane instanceof ProductSceneView) {
                handleViewDeactivated((ProductSceneView) contentPane);
            }
        }
    }

/////////////////////////////////////////////////////////////////////////
// Product change handling

    private class ProductNodeHandler extends ProductNodeListenerAdapter {

        @Override
        public void nodeChanged(final ProductNodeEvent event) {
            if (!isActive()) {
                return;
            }
            if (event.getSourceNode() instanceof Band) {
                final String propertyName = event.getPropertyName();
                if (propertyName.equals(DataNode.PROPERTY_NAME_UNIT)
                        || propertyName.equals(Band.PROPERTY_NAME_SPECTRAL_WAVELENGTH)) {
                    recreateChart();
                }
            } else if (event.getSourceNode() instanceof Placemark) {
                if (event.getPropertyName().equals("geoPos") || event.getPropertyName().equals("pixelPos")) {
                    chartHandler.removePinInformation((Placemark) event.getSourceNode());
                }
                if (isShowingPinSpectra()) {
                    recreateChart();
                }
            }
            updateUIState();
        }

        @Override
        public void nodeAdded(final ProductNodeEvent event) {
            if (!isActive()) {
                return;
            }
            if (event.getSourceNode() instanceof Band) {
                recreateChart();
            } else if (event.getSourceNode() instanceof Placemark) {
                if (isShowingPinSpectra()) {
                    recreateChart();
                }
            }
            updateUIState();
        }

        @Override
        public void nodeRemoved(final ProductNodeEvent event) {
            if (!isActive()) {
                return;
            }
            if (event.getSourceNode() instanceof Band) {
                recreateChart();
            } else if (event.getSourceNode() instanceof Placemark) {
                if (isShowingPinSpectra()) {
                    recreateChart();
                }
            }
            updateUIState();
        }

        private boolean isActive() {
            return isVisible() && getCurrentProduct() != null;
        }
    }

    private class PinSelectionChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            recreateChart();
        }

    }

}

