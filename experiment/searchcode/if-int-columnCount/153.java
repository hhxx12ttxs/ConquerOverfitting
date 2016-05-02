package org.esa.beam.smos.gui;


import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.PropertyDescriptor;
import com.bc.ceres.binding.ValidationException;
import com.bc.ceres.binding.ValueSet;
import com.bc.ceres.swing.TableLayout;
import com.bc.ceres.swing.binding.*;
import com.bc.ceres.swing.binding.internal.AbstractButtonAdapter;
import com.bc.ceres.swing.binding.internal.SingleSelectionEditor;
import com.bc.ceres.swing.binding.internal.TextComponentAdapter;
import com.bc.ceres.swing.binding.internal.TextFieldEditor;
import com.bc.ceres.swing.selection.Selection;
import com.bc.ceres.swing.selection.SelectionContext;
import com.bc.ceres.swing.selection.SelectionManager;
import com.vividsolutions.jts.geom.Geometry;
import org.esa.beam.framework.datamodel.PlainFeatureFactory;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductNodeGroup;
import org.esa.beam.framework.datamodel.VectorDataNode;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.framework.ui.application.ApplicationPage;
import org.esa.beam.framework.ui.product.SimpleFeatureShapeFigure;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GuiHelper {

    public static final String LAST_SOURCE_DIR_KEY = "org.esa.beam.smos.export.sourceDir";
    public static final String LAST_TARGET_FILE_KEY = "org.esa.beam.smos.export.targetFile";
    public static final String LAST_TARGET_DIR_KEY = "org.esa.beam.smos.export.targetDir";

    public static JPanel createPanelWithBoxLayout() {
        final JPanel mainPanel = new JPanel();
        final BoxLayout layout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);

        mainPanel.setLayout(layout);
        return mainPanel;
    }

    public static TableLayout createTableLayout(int columnCount) {
        final TableLayout layout = new TableLayout(columnCount);
        layout.setTableAnchor(TableLayout.Anchor.WEST);
        layout.setTableFill(TableLayout.Fill.HORIZONTAL);
        layout.setTablePadding(3, 3);
        return layout;
    }

    public static TableLayout createWeightedTablelayout(int columnCount) {
        final TableLayout layout = createTableLayout(columnCount);
        layout.setTableWeightX(1.0);
        return layout;
    }

    public static void addSourceProductsButtons(JPanel sourceProductPanel, boolean canProductSelectionBeEnabled, BindingContext bindingContext) {
        final JRadioButton useSelectedProductButton = new JRadioButton(BindingConstants.USE_SELECTED_PRODUCT_BUTTON_NAME);
        final JRadioButton useAllProductsInDirectoryButton = new JRadioButton("Use all SMOS products in directory:");

        final ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(useSelectedProductButton);
        buttonGroup.add(useAllProductsInDirectoryButton);

        final Map<AbstractButton, Object> buttonGroupValueSet = new HashMap<>();
        buttonGroupValueSet.put(useSelectedProductButton, true);
        buttonGroupValueSet.put(useAllProductsInDirectoryButton, false);

        bindingContext.bind(BindingConstants.SELECTED_PRODUCT, buttonGroup, buttonGroupValueSet);
        bindingContext.bindEnabledState(BindingConstants.SOURCE_DIRECTORY, true, BindingConstants.SELECTED_PRODUCT, false);
        bindingContext.bindEnabledState(BindingConstants.OPEN_FILE_DIALOG, true, BindingConstants.SELECTED_PRODUCT, false);

        useSelectedProductButton.setEnabled(canProductSelectionBeEnabled);

        sourceProductPanel.add(useSelectedProductButton);
        sourceProductPanel.add(useAllProductsInDirectoryButton);
    }

    public static JComponent createFileEditorComponent(PropertyDescriptor descriptor, final ChooserFactory cf, BindingContext bindingContext) {
        return createFileEditorComponent(descriptor, cf, bindingContext, true);
    }

    public static JComponent createFileEditorComponent(PropertyDescriptor descriptor, final ChooserFactory cf, BindingContext bindingContext, boolean bindEtcButton) {
        final JTextField textField = new JTextField();
        textField.setColumns(30);
        final ComponentAdapter adapter = new TextComponentAdapter(textField);
        final Binding binding = bindingContext.bind(descriptor.getName(), adapter);

        final JButton etcButton = new JButton("...");
        final Dimension size = new Dimension(26, 16);
        etcButton.setPreferredSize(size);
        etcButton.setMinimumSize(size);
        if (bindEtcButton) {
            bindingContext.bind(BindingConstants.OPEN_FILE_DIALOG, new AbstractButtonAdapter(etcButton));
        }

        final JPanel panel = new JPanel(new BorderLayout(2, 2));
        panel.add(textField, BorderLayout.CENTER);
        panel.add(etcButton, BorderLayout.EAST);

        etcButton.addActionListener(new ActionListener() {
                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            final JFileChooser fileChooser = cf.createChooser((File) binding.getPropertyValue());
                                            final int state = fileChooser.showDialog(panel, "Select");
                                            if (state == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile() != null) {
                                                binding.setPropertyValue(fileChooser.getSelectedFile());
                                            }
                                        }
                                    }
        );

        return panel;
    }

    public static File getDefaultSourceDirectory(AppContext appContext) {
        return getFileFromProperties(appContext, LAST_SOURCE_DIR_KEY);
    }

    public static void setDefaultSourceDirectory(File sourceDirectory, AppContext appContext) {
        appContext.getPreferences().setPropertyString(LAST_SOURCE_DIR_KEY, sourceDirectory.getPath());
    }

    public static File getDefaultTargetDirectory(AppContext appContext) {
        return getFileFromProperties(appContext, LAST_TARGET_DIR_KEY);
    }

    public static void setDefaultTargetDirectory(File targetDirectory, AppContext appContext) {
        appContext.getPreferences().setPropertyString(LAST_TARGET_DIR_KEY, targetDirectory.getPath());
    }

    private static File getFileFromProperties(AppContext appContext, String lastSourceDirKey) {
        final String def = System.getProperty("user.home", ".");
        return new File(appContext.getPreferences().getPropertyString(lastSourceDirKey, def));
    }

    // @todo 3 tb/tb write test 2013-04-08
    public static Geometry getSelectedGeometry(AppContext appContext) {
        final ApplicationPage applicationPage = appContext.getApplicationPage();
        if (applicationPage != null) {
            final SelectionManager selectionManager = applicationPage.getSelectionManager();
            if (selectionManager != null) {
                final SelectionContext selectionContext = selectionManager.getSelectionContext();
                if (selectionContext != null) {
                    final Selection selection = selectionContext.getSelection();
                    final Object selectedValue = selection.getSelectedValue();
                    if (selectedValue instanceof SimpleFeatureShapeFigure) {
                        final SimpleFeatureShapeFigure shapeFigure = (SimpleFeatureShapeFigure) selectedValue;
                        return shapeFigure.getGeometry();
                    }
                }
            }
        }

        return null;
    }

    public static java.util.List<VectorDataNode> getGeometries(Product selectedProduct) {
        final java.util.List<VectorDataNode> geometryNodeList = new ArrayList<>();
        final ProductNodeGroup<VectorDataNode> vectorDataGroup = selectedProduct.getVectorDataGroup();
        for (VectorDataNode node : vectorDataGroup.toArray(new VectorDataNode[vectorDataGroup.getNodeCount()])) {
            if (node.getFeatureType().getTypeName().equals(PlainFeatureFactory.DEFAULT_TYPE_NAME)) {
                if (!node.getFeatureCollection().isEmpty()) {
                    geometryNodeList.add(node);
                }
            }
        }
        return geometryNodeList;
    }

    public static java.util.List<Geometry> getPolygonGeometries(Product selectedProduct) {
        final java.util.List<Geometry> geometryNodeList = new ArrayList<>();
        final ProductNodeGroup<VectorDataNode> vectorDataGroup = selectedProduct.getVectorDataGroup();
        for (VectorDataNode node : vectorDataGroup.toArray(new VectorDataNode[vectorDataGroup.getNodeCount()])) {
            if (node.getFeatureType().getTypeName().equals(PlainFeatureFactory.DEFAULT_TYPE_NAME)) {
                final FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = node.getFeatureCollection();
                if (!featureCollection.isEmpty()) {
                    final FeatureIterator<SimpleFeature> features = featureCollection.features();
                    while (features.hasNext()) {
                        final SimpleFeature next = features.next();
                        final Object defaultGeometry = next.getDefaultGeometry();
                        if (defaultGeometry instanceof Geometry) {
                            final Geometry geometry = (Geometry) defaultGeometry;
                            if (geometry instanceof com.vividsolutions.jts.geom.Polygon) {
                                geometryNodeList.add(geometry);
                            }
                        }
                    }
                }
            }
        }
        return geometryNodeList;
    }

    public static void bindGeometryVectorDataNodes(java.util.List<VectorDataNode> geometryNodeList, PropertyContainer propertyContainer) throws ValidationException {
        final PropertyDescriptor descriptor = propertyContainer.getDescriptor(BindingConstants.REGION);
        descriptor.setNotNull(false);
        descriptor.setNotEmpty(false);
        descriptor.setValueSet(new ValueSet(geometryNodeList.toArray()));

        propertyContainer.setValue(BindingConstants.ROI_TYPE, BindingConstants.ROI_TYPE_GEOMETRY);
        propertyContainer.getProperty(BindingConstants.REGION).setValue(geometryNodeList.get(0));
    }

    public static void bindGeometries(java.util.List<Geometry> geometryNodeList, PropertyContainer propertyContainer) throws ValidationException {
        final PropertyDescriptor descriptor = propertyContainer.getDescriptor(BindingConstants.REGION);
        descriptor.setNotNull(false);
        descriptor.setNotEmpty(false);
        descriptor.setValueSet(new ValueSet(geometryNodeList.toArray()));

        propertyContainer.setValue(BindingConstants.ROI_TYPE, BindingConstants.ROI_TYPE_GEOMETRY);
        propertyContainer.getProperty(BindingConstants.REGION).setValue(geometryNodeList.get(0));
    }

    public static JComboBox createGeometryComboBox(PropertyDescriptor geometryDescriptor, BindingContext bindingContext) {
        final PropertyEditor selectionEditor = PropertyEditorRegistry.getInstance().getPropertyEditor(SingleSelectionEditor.class.getName());
        final JComboBox geometryComboBox = (JComboBox) selectionEditor.createEditorComponent(geometryDescriptor, bindingContext);

        final DefaultListCellRenderer listCellRenderer = new ProductNodeRenderer();
        geometryComboBox.setRenderer(listCellRenderer);
        return geometryComboBox;
    }

    public static Component createLatLonCoordinatePanel(String name, String displayName, int numColumns, PropertyContainer propertyContainer, BindingContext bindingContext) {
        final PropertyEditor editor = PropertyEditorRegistry.getInstance().getPropertyEditor(TextFieldEditor.class.getName());
        final JTextField textField = (JTextField) editor.createEditorComponent(propertyContainer.getDescriptor(name), bindingContext);

        final JLabel nameLabel = new JLabel(displayName);
        final JLabel unitLabel = new JLabel("\u00b0");
        nameLabel.setEnabled(textField.isEnabled());
        unitLabel.setEnabled(textField.isEnabled());

        textField.setColumns(numColumns);
        textField.addPropertyChangeListener("enabled", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                final Boolean enabled = (Boolean) evt.getNewValue();
                nameLabel.setEnabled(enabled);
                unitLabel.setEnabled(enabled);
            }
        });

        final JPanel panel = new JPanel(new FlowLayout());
        panel.add(nameLabel);
        panel.add(textField);
        panel.add(unitLabel);

        return panel;
    }

    public static Component createLatLonPanel(PropertyContainer propertyContainer, BindingContext bindingContext) {
        final TableLayout layout = createTableLayout(3);

        final JPanel areaPanel = new JPanel(layout);
        final JLabel emptyLabel = new JLabel(" ");
        areaPanel.add(emptyLabel);
        final Component northPanel = createLatLonCoordinatePanel(BindingConstants.NORTH, "North:", 4, propertyContainer, bindingContext);
        areaPanel.add(northPanel);
        areaPanel.add(emptyLabel);

        final Component westPanel = createLatLonCoordinatePanel(BindingConstants.WEST, "West:", 5, propertyContainer, bindingContext);
        areaPanel.add(westPanel);
        areaPanel.add(emptyLabel);
        final Component eastPanel = createLatLonCoordinatePanel(BindingConstants.EAST, "East:", 5, propertyContainer, bindingContext);
        areaPanel.add(eastPanel);

        areaPanel.add(emptyLabel);
        final Component southPanel = createLatLonCoordinatePanel(BindingConstants.SOUTH, "South:", 4, propertyContainer, bindingContext);
        areaPanel.add(southPanel);
        areaPanel.add(emptyLabel);

        return areaPanel;
    }

    public static void bindLonLatPanelToRoiType(int roiTypeId, BindingContext bindingContext) {
        bindingContext.bindEnabledState(BindingConstants.NORTH, true, BindingConstants.ROI_TYPE, roiTypeId);
        bindingContext.bindEnabledState(BindingConstants.SOUTH, true, BindingConstants.ROI_TYPE, roiTypeId);
        bindingContext.bindEnabledState(BindingConstants.EAST, true, BindingConstants.ROI_TYPE, roiTypeId);
        bindingContext.bindEnabledState(BindingConstants.WEST, true, BindingConstants.ROI_TYPE, roiTypeId);
    }
}

