/*
 * Created by JFormDesigner on Fri Jan 25 00:11:22 CET 2013
 */

package at.ac.tuwien.machinelearning;

import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.neural.NeuralConnection;
import weka.classifiers.functions.neural.NeuralNode;
import weka.classifiers.meta.AdaBoostM1;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.gui.visualize.Plot2D;
import weka.gui.visualize.PlotData2D;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author Jakob Korherr
 */
public class MainFrame extends JFrame {

    private Instances instances;
    private Instances importInstances;
    private java.util.List<String> attributes;
    private AdaBoostM1 adaBoost;
    private JFileChooser fileChooser;
    private int numIterations;
    private Object attr1Name;
    private Object attr2Name;
    private DefaultTableModel tableErrorRateModel;
    private java.util.List<Double> errorRates;
    private Map<Integer, Checkbox> classesCheckboxes;

    public MainFrame() {
        initComponents();
        fileChooser = new JFileChooser();
    }

    private void buttonImportActionPerformed(ActionEvent e) {
        importData();
    }

    private void importData() {
        // Read all the instances in the file (ARFF, CSV, XRFF, ...)
        try {
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(textFieldImport.getText());
            importInstances = source.getDataSet();
        } catch (Exception e) {
            showError(e);
            return;
        }

        // Make the last attribute be the class
        importInstances.setClassIndex(importInstances.numAttributes() - 1);

        // create index of attributes
        attributes = new LinkedList<String>();
        Enumeration attributeEnumeration = importInstances.enumerateAttributes();
        while (attributeEnumeration.hasMoreElements()) {
            Attribute attribute = (Attribute) attributeEnumeration.nextElement();
            attributes.add(attribute.name());
        }

        // configure + enable GUI elements for configuration
        comboBoxAttr1.setEnabled(true);
        comboBoxAttr1.setModel(new DefaultComboBoxModel(attributes.toArray()));
        comboBoxAttr1.setSelectedIndex(0);
        comboBoxAttr2.setEnabled(true);
        comboBoxAttr2.setModel(new DefaultComboBoxModel(attributes.toArray()));
        comboBoxAttr2.setSelectedIndex(1);
        textFieldIterationCount.setEnabled(true);
        buttonCalculate.setEnabled(true);
    }

    private void showError(Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void runCalculation() {
        instances = new Instances(importInstances);  // use a copy of original import data

        MultilayerPerceptron classifier = createSimplePerceptronClassifier();

        reduceAttributes();  // only keep selected attributes

        // Ada boost
        adaBoost = new AdaBoostM1();
        adaBoost.setClassifier(classifier);
        adaBoost.setNumIterations(numIterations);
        adaBoost.setUseResampling(true);

        // build classifier, but no iterations yet (impl changed!)
        try {
            adaBoost.buildClassifier(instances);
        } catch (Exception e) {
            showError(e);
        }

        // pre iteration setup
        buildTableErrorRateModel();
        while (tabbedPaneIterations.getTabCount() > 0) {
            tabbedPaneIterations.removeTabAt(0);
        }
        errorRates = new ArrayList<Double>(numIterations);

        // iterations
        for (int i = 0; i < numIterations; i++) {
            Instances iterationInstances = calculateIteration();
            visualizeIteration(i, iterationInstances);
        }

        // post iteration stuff
        buildGraphErrorRate();

        classesCheckboxes = new HashMap<Integer, Checkbox>();
        panelClassValues.removeAll();
        Enumeration enumClassValues = instances.classAttribute().enumerateValues();
        int classValueNr = 0;
        while (enumClassValues.hasMoreElements()) {
            String classValue = enumClassValues.nextElement().toString();
            Checkbox checkbox = new Checkbox(classValue, true);
            checkbox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent itemEvent) {
                    repaintIterationGraphs();
                }
            });
            classesCheckboxes.put(classValueNr++, checkbox);
            panelClassValues.add(checkbox);
        }
        panelClassValues.updateUI();
    }

    private void repaintIterationGraphs() {
        for (int i = 0; i < tabbedPaneIterations.getTabCount(); i++) {
            JPanel tabPanel = (JPanel) tabbedPaneIterations.getComponentAt(i);
            Plot2D plot2D = (Plot2D) tabPanel.getComponent(0);
            plot2D.repaint();
        }
    }

    private void buildGraphErrorRate() {
        final Plot2D plot2D = new Plot2D(){
            public void paintComponent(Graphics gx) {
                super.paintComponent(gx);
                gx.setColor(Color.RED);
                Double previousErrorRate = errorRates.get(0);
                for (int i = 1, errorRatesSize = errorRates.size(); i < errorRatesSize; i++) {
                    Double errorRate = errorRates.get(i);
                    gx.drawLine(
                            (int) Math.round(this.convertToPanelX(i)),
                            (int) Math.round(this.convertToPanelY(previousErrorRate)),
                            (int) Math.round(this.convertToPanelX(i + 1)),
                            (int) Math.round(this.convertToPanelY(errorRate)));
                    previousErrorRate = errorRate;
                }
            }
        };
        panelErrorRateGraph.removeAll();
        plot2D.setBackground(Color.WHITE);
        panelErrorRateGraph.setBackground(Color.WHITE);
        panelErrorRateGraph.add(plot2D, BorderLayout.CENTER);

        ArrayList<Attribute> attInfo = new ArrayList<Attribute>();
        attInfo.add(new Attribute("X"));
        attInfo.add(new Attribute("Y"));
        attInfo.add(new Attribute("C"));
        Instances dummyInstances = new Instances("dummy", attInfo, 1);
        int maxY = Collections.max(errorRates).intValue() + 1;
        int minY = Collections.min(errorRates).intValue() - 1;
        dummyInstances.add(new DenseInstance(1, new double[] {numIterations, maxY, 1}));
        dummyInstances.add(new DenseInstance(1, new double[] {1, minY, 1}));
        try {
            PlotData2D pd = new PlotData2D(dummyInstances);
            pd.setPlotName("Plot of error rate");
            // set color so that points are not actually visible
            pd.setCustomColour(plot2D.getBackground());
            plot2D.setMasterPlot(pd);
            plot2D.setXindex(0);
            plot2D.setYindex(1);
            plot2D.setCindex(2);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        panelErrorRateGraph.repaint();
    }

    private void buildTableErrorRateModel() {
        tableErrorRateModel = new DefaultTableModel();
        tableErrorRateModel.addColumn("Iteration");
        tableErrorRateModel.addColumn("Error Rate");
        tableErrorRate.setModel(tableErrorRateModel);
    }

    private void visualizeIteration(int iterationNr, Instances iterationInstances) {
        // error rate
        double errorRate = calculateErrorRate(iterationInstances);
        tableErrorRateModel.addRow(new Object[] {Integer.toString(iterationNr + 1), Double.toString(errorRate) + " %"});
        errorRates.add(errorRate);

        // find NeuralNodes (NOTE: apparently, if nothing changes, AdaBoost does not use any more of its classifiers
        final java.util.List<java.util.List<double[]>> nodeWeightsList = new LinkedList<java.util.List<double[]>>();
        for (int i = 0; i <= iterationNr; i++) {
            LinkedList<double[]> weightsList = new LinkedList<double[]>();
            nodeWeightsList.add(weightsList);

            // find last classifier with valid neuralNodes
            NeuralConnection[] neuralNodes;
            int lookupIterationNr = i;
            do {
                MultilayerPerceptron iterationPerceptron = (MultilayerPerceptron) adaBoost.getClassifiers()[lookupIterationNr];
                neuralNodes = iterationPerceptron.getNeuralNodes();
                lookupIterationNr--;
            } while (neuralNodes.length < 1 && lookupIterationNr >= 0);

            // copy weights of classifier
            for (NeuralConnection neuralNode : neuralNodes) {
                double[] weightsSource = ((NeuralNode) neuralNode).getWeights();
                double[] weights = new double[weightsSource.length];
                System.arraycopy(weightsSource, 0, weights, 0, weightsSource.length);  // use a copy of current weights
                weightsList.add(weights);
            }
        }

        JPanel panelPerceptron = new JPanel(new BorderLayout());
        tabbedPaneIterations.addTab("Iteration " + Integer.toString(iterationNr + 1), panelPerceptron);
        final Plot2D plot2D = new Plot2D(){
            public void paintComponent(Graphics gx) {
                super.paintComponent(gx);
                int nodeNr = 0;
                for (java.util.List<double[]> weightsList : nodeWeightsList) {
                    boolean resetLineThickness = false;
                    if (nodeWeightsList.size() - 1 == nodeNr++) {   // last line(s) should be thicker
                        Graphics2D g2d = (Graphics2D) gx;
                        g2d.setStroke(new BasicStroke(2.5f));
                        resetLineThickness = true;
                    }

                    int classValueNr = 0;
                    for (double[] weights : weightsList) {
                        if (shouldDrawPerceptron(classValueNr)) {
                            // [0]...threshold (d), [1]...x, [2]...y
                            int x1 = (int) Math.round(this.convertToPanelX(this.getMinX()));
                            int y1 = (int) Math.round(this.convertToPanelY(-(weights[0] - (-this.getMinX()) * weights[1]) / weights[2]));
                            int x2 = (int) Math.round(this.convertToPanelX(this.getMaxX()));
                            int y2 = (int) Math.round(this.convertToPanelY(-(weights[0] - (-this.getMaxX()) * weights[1]) / weights[2]));

                            gx.setColor(this.getDefaultColors()[(classValueNr) % this.getDefaultColors().length]);
                            gx.drawLine(x1, y1, x2, y2);
                        }
                        classValueNr++;
                    }

                    if (resetLineThickness) {    // reset line thickness
                        Graphics2D g2d = (Graphics2D) gx;
                        g2d.setStroke(new BasicStroke(1f));
                    }
                }
            }
        };
        panelPerceptron.add(plot2D, BorderLayout.CENTER);
        plot2D.setBackground(Color.WHITE);
        panelPerceptron.setBackground(Color.WHITE);
        try {
            PlotData2D pd = new PlotData2D(iterationInstances);
            pd.setPlotName("Plot of iteration " + Integer.toString(iterationNr + 1));
            plot2D.setMasterPlot(pd);
            plot2D.setXindex(0);
            plot2D.setYindex(1);
            plot2D.setCindex(iterationInstances.classIndex());
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean shouldDrawPerceptron(int classValueNr) {
        return classesCheckboxes.get(classValueNr).getState();
    }

    private double calculateErrorRate(Instances iterationInstances) {
        int misclassifiedInstanceCount = 0;
        for (int i = 0; i < instances.size(); i++) {
            Instance realInstance = instances.get(i);
            Instance classifiedInstance = iterationInstances.get(i);
            if (realInstance.classValue() != classifiedInstance.classValue()) {
                misclassifiedInstanceCount++;
            }
        }
        return (int)(((double) misclassifiedInstanceCount) / instances.numInstances() * 10000) / 100.0;
    }

    private MultilayerPerceptron createSimplePerceptronClassifier() {
        MultilayerPerceptron perceptron = new MultilayerPerceptron();
        perceptron.setHiddenLayers("0");   // this option lets MultilayerPerceptron behave like a std Perceptron
        return perceptron;
    }

    private void reduceAttributes() {
        for (int i = 0; i < instances.numAttributes(); i++) {
            if (instances.classIndex() != i) {
                String attributeName = instances.attribute(i).name();
                if (!attr1Name.equals(attributeName) && !attr2Name.equals(attributeName)) {
                    instances.deleteAttributeAt(i);
                    i--;  // as we deleted the attribute, we must stay on the same index
                }
            }
        }
    }

    private Instances calculateIteration() {
        try {
            adaBoost.doIteration();
        } catch (Exception e) {
            showError(e);
            throw new RuntimeException(e);
        }

        Instances instancesToVisualize = new Instances(instances);
        for (Instance instance : instancesToVisualize) {
            try {
                instance.setClassValue(adaBoost.classifyInstance(instance));
            } catch (Exception e) {
                showError(e);
                throw new RuntimeException(e);
            }

        }
        return instancesToVisualize;
    }

    private void buttonChooseActionPerformed(ActionEvent e) {
        fileChooser.showOpenDialog(this);
        File file = fileChooser.getSelectedFile();
        if (file != null) {
            textFieldImport.setText(file.getAbsolutePath());
        }
    }

    private void buttonStartActionPerformed(ActionEvent event) {
        if (parseCalculationConfiguration()) {
            return;  // some error occurred
        }
        runCalculation();
    }

    private boolean parseCalculationConfiguration() {
        attr1Name = comboBoxAttr1.getModel().getSelectedItem();
        attr2Name = comboBoxAttr2.getModel().getSelectedItem();
        if (attr1Name.equals(attr2Name)) {
            showError("Please choose two different attributes.");
            return true;
        }

        // num iterations
        try {
            numIterations = Integer.parseInt(textFieldIterationCount.getText());
        } catch (NumberFormatException e) {
            showError(e);
            return true;
        }
        return false;
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Jakob Korherr
        labelHeader = new JLabel();
        label1 = new JLabel();
        textFieldImport = new JTextField();
        buttonImport = new JButton();
        buttonChoose = new JButton();
        label4 = new JLabel();
        textFieldIterationCount = new JTextField();
        separator2 = new JSeparator();
        label3 = new JLabel();
        label5 = new JLabel();
        comboBoxAttr1 = new JComboBox();
        comboBoxAttr2 = new JComboBox();
        buttonCalculate = new JButton();
        splitPane1 = new JSplitPane();
        tabbedPaneIterations = new JTabbedPane();
        tabbedPaneErrorRate = new JTabbedPane();
        panelErrorRateGraph = new JPanel();
        panelErrorRateTable = new JPanel();
        scrollPane1 = new JScrollPane();
        tableErrorRate = new JTable();
        separator1 = new JSeparator();
        panelClassValuesWrapper = new JPanel();
        scrollPaneClassValues = new JScrollPane();
        panelClassValues = new JPanel();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container contentPane = getContentPane();

        //---- labelHeader ----
        labelHeader.setText("Ada boost visualisation - machine learning 2012W");
        labelHeader.setHorizontalAlignment(SwingConstants.CENTER);

        //---- label1 ----
        label1.setText("File:");

        //---- buttonImport ----
        buttonImport.setText("Import");
        buttonImport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonImportActionPerformed(e);
            }
        });

        //---- buttonChoose ----
        buttonChoose.setText("Choose");
        buttonChoose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonChooseActionPerformed(e);
            }
        });

        //---- label4 ----
        label4.setText("Iterations:");

        //---- textFieldIterationCount ----
        textFieldIterationCount.setText("10");
        textFieldIterationCount.setEnabled(false);

        //---- label3 ----
        label3.setText("Attribute 1:");

        //---- label5 ----
        label5.setText("Attribute 2:");

        //---- comboBoxAttr1 ----
        comboBoxAttr1.setEnabled(false);

        //---- comboBoxAttr2 ----
        comboBoxAttr2.setEnabled(false);

        //---- buttonCalculate ----
        buttonCalculate.setText("Calculate");
        buttonCalculate.setEnabled(false);
        buttonCalculate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonStartActionPerformed(e);
            }
        });

        //======== splitPane1 ========
        {
            splitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
            splitPane1.setDividerLocation(250);

            //======== tabbedPaneIterations ========
            {
                tabbedPaneIterations.setMinimumSize(new Dimension(243, 50));
            }
            splitPane1.setTopComponent(tabbedPaneIterations);

            //======== tabbedPaneErrorRate ========
            {

                //======== panelErrorRateGraph ========
                {

                    // JFormDesigner evaluation mark
                    panelErrorRateGraph.setBorder(new javax.swing.border.CompoundBorder(
                        new javax.swing.border.TitledBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0),
                            "JFormDesigner Evaluation", javax.swing.border.TitledBorder.CENTER,
                            javax.swing.border.TitledBorder.BOTTOM, new java.awt.Font("Dialog", java.awt.Font.BOLD, 12),
                            java.awt.Color.red), panelErrorRateGraph.getBorder())); panelErrorRateGraph.addPropertyChangeListener(new java.beans.PropertyChangeListener(){public void propertyChange(java.beans.PropertyChangeEvent e){if("border".equals(e.getPropertyName()))throw new RuntimeException();}});

                    panelErrorRateGraph.setLayout(new BorderLayout());
                }
                tabbedPaneErrorRate.addTab("Graph", panelErrorRateGraph);


                //======== panelErrorRateTable ========
                {
                    panelErrorRateTable.setLayout(new BorderLayout());

                    //======== scrollPane1 ========
                    {
                        scrollPane1.setViewportView(tableErrorRate);
                    }
                    panelErrorRateTable.add(scrollPane1, BorderLayout.CENTER);
                }
                tabbedPaneErrorRate.addTab("Table", panelErrorRateTable);

            }
            splitPane1.setBottomComponent(tabbedPaneErrorRate);
        }

        //======== panelClassValuesWrapper ========
        {
            panelClassValuesWrapper.setMaximumSize(new Dimension(50, 50));
            panelClassValuesWrapper.setLayout(new BorderLayout());

            //======== scrollPaneClassValues ========
            {

                //======== panelClassValues ========
                {
                    panelClassValues.setLayout(new FlowLayout(FlowLayout.LEFT));
                }
                scrollPaneClassValues.setViewportView(panelClassValues);
            }
            panelClassValuesWrapper.add(scrollPaneClassValues, BorderLayout.CENTER);
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addComponent(labelHeader, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(313, 313, 313))
                        .addComponent(separator2)
                        .addComponent(separator1, GroupLayout.DEFAULT_SIZE, 1268, Short.MAX_VALUE)
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addGroup(contentPaneLayout.createParallelGroup()
                                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                                .addGap(317, 317, 317)
                                                                .addComponent(buttonChoose))
                                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                                .addComponent(label1)
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(textFieldImport, GroupLayout.PREFERRED_SIZE, 276, GroupLayout.PREFERRED_SIZE)))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(buttonImport))
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addComponent(label3)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(comboBoxAttr1, GroupLayout.PREFERRED_SIZE, 211, GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(label5)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(comboBoxAttr2, GroupLayout.PREFERRED_SIZE, 266, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(contentPaneLayout.createSequentialGroup()
                                                .addComponent(label4)
                                                .addGap(18, 18, 18)
                                                .addComponent(textFieldIterationCount, GroupLayout.PREFERRED_SIZE, 48, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(buttonCalculate)))
                                .addGap(0, 619, Short.MAX_VALUE))
                        .addComponent(panelClassValuesWrapper, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 1268, Short.MAX_VALUE)
                        .addComponent(splitPane1))
                    .addContainerGap())
        );
        contentPaneLayout.setVerticalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(labelHeader)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(label1)
                        .addComponent(buttonChoose)
                        .addComponent(buttonImport)
                        .addComponent(textFieldImport, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGap(8, 8, 8)
                    .addComponent(separator2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(label3)
                        .addComponent(comboBoxAttr1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(label5)
                        .addComponent(comboBoxAttr2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(label4)
                        .addComponent(textFieldIterationCount, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(buttonCalculate))
                    .addGap(3, 3, 3)
                    .addComponent(separator1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(panelClassValuesWrapper, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(splitPane1, GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE)
                    .addContainerGap())
        );
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Jakob Korherr
    private JLabel labelHeader;
    private JLabel label1;
    private JTextField textFieldImport;
    private JButton buttonImport;
    private JButton buttonChoose;
    private JLabel label4;
    private JTextField textFieldIterationCount;
    private JSeparator separator2;
    private JLabel label3;
    private JLabel label5;
    private JComboBox comboBoxAttr1;
    private JComboBox comboBoxAttr2;
    private JButton buttonCalculate;
    private JSplitPane splitPane1;
    private JTabbedPane tabbedPaneIterations;
    private JTabbedPane tabbedPaneErrorRate;
    private JPanel panelErrorRateGraph;
    private JPanel panelErrorRateTable;
    private JScrollPane scrollPane1;
    private JTable tableErrorRate;
    private JSeparator separator1;
    private JPanel panelClassValuesWrapper;
    private JScrollPane scrollPaneClassValues;
    private JPanel panelClassValues;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

}

