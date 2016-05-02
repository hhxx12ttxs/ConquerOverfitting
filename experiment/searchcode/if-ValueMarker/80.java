package eu.isas.peptideshaker.gui.tabpanels;

import com.compomics.util.Util;
import com.compomics.util.gui.dialogs.ProgressDialogParent;
import com.compomics.util.gui.dialogs.ProgressDialogX;
import com.compomics.util.gui.renderers.AlignedListCellRenderer;
import eu.isas.peptideshaker.PeptideShaker;
import eu.isas.peptideshaker.gui.ExportGraphicsDialog;
import eu.isas.peptideshaker.scoring.PeptideSpecificMap;
import eu.isas.peptideshaker.scoring.targetdecoy.TargetDecoyMap;
import eu.isas.peptideshaker.scoring.targetdecoy.TargetDecoyResults;
import eu.isas.peptideshaker.scoring.targetdecoy.TargetDecoySeries;
import eu.isas.peptideshaker.gui.HelpDialog;
import eu.isas.peptideshaker.gui.PeptideShakerGUI;
import eu.isas.peptideshaker.gui.ProgressDialogWaitingHandler;
import eu.isas.peptideshaker.myparameters.PSMaps;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockFrame;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.ui.RectangleEdge;

/**
 * This panel displays statistical information about the dataset.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class StatsPanel extends javax.swing.JPanel implements ProgressDialogParent {

    /**
     * It true the tab has been initiated, i.e., the data displayed at leaat
     * once. False means that the tab has to be loaded from scratch.
     */
    private boolean tabInitiated = false;
    /**
     * The progress dialog.
     */
    private ProgressDialogX progressDialog;
    /**
     * The progress dialog waiting handler.
     */
    private ProgressDialogWaitingHandler progressDialogWaitingHandler;
    /**
     * If true the progress bar is disposed of.
     */
    private static boolean cancelProgress = false;
    /**
     * If true the data has been (re-)loaded with the current thresold setting.
     */
    private boolean dataValidated = true;
    /**
     * If true the data has been (re-loaded) with the current PEP window size.
     */
    private boolean pepWindowApplied = true;
    /**
     * The main peptide shaker gui.
     */
    private PeptideShakerGUI peptideShakerGUI;
    /**
     * The default line width for the line plots.
     */
    private final int LINE_WIDTH = 2;
    /**
     * The currently displayed Target Decoy map.
     */
    private TargetDecoyMap currentTargetDecoyMap;
    /**
     * The Target Decoy metrics series of the currently displayed map.
     */
    private TargetDecoySeries targetDecoySeries;
    /**
     * The psms map: # in the list -> map key.
     */
    private HashMap<Integer, Integer> psmMap = new HashMap<Integer, Integer>();
    /**
     * The peptide map: # in the list -> map key.
     */
    private HashMap<Integer, String> peptideMap = new HashMap<Integer, String>();
    /**
     * The confidence plot.
     */
    private XYPlot confidencePlot = new XYPlot();
    /**
     * The fdr fnr plot.
     */
    private XYPlot fdrFnrPlot = new XYPlot();
    /**
     * The PEP plot.
     */
    private XYPlot pepPlot = new XYPlot();
    /**
     * The FDR/FNR plot.
     */
    private XYPlot fdrPlot = new XYPlot();
    /**
     * The Benefit/cost plot.
     */
    private XYPlot benefitCostPlot = new XYPlot();
    /**
     * The last threshold input.
     */
    private double lastThreshold = 1;
    /**
     * The last threshold type 0 -> confidence 1 -> FDR 2 -> FNR
     */
    private int lastThresholdType = 1;
    /**
     * The confidence threshold marker.
     */
    private ValueMarker confidenceMarker = new ValueMarker(1);
    /**
     * Map keeping track of probabilities modifications.
     */
    private HashMap<Integer, Boolean> modifiedMaps = new HashMap<Integer, Boolean>();
    /**
     * The score log axis.
     */
    private LogAxis scoreAxis;
    /**
     * The classical FDR axis in the FDRs plot
     */
    private NumberAxis classicalAxis;
    /**
     * The probabilistic FDR axis in the FDRs plot
     */
    private NumberAxis probaAxis;
    /**
     * The highlighting to use for FNR.
     */
    private Color fnrHighlightColor = new Color(0, 255, 0, 15);
    /**
     * The highlighting to use for FDR.
     */
    private Color fdrHighlightColor = new Color(255, 0, 0, 15);

    /**
     * Create a new StatsPanel.
     *
     * @param parent the PeptideShaker parent frame.
     */
    public StatsPanel(PeptideShakerGUI parent) {

        this.peptideShakerGUI = parent;

        initComponents();

        // add the default values to the group selection
        ((DefaultTableModel) groupSelectionTable.getModel()).addRow(new Object[]{1, "Proteins"});
        ((DefaultTableModel) groupSelectionTable.getModel()).addRow(new Object[]{2, "Peptides"});
        ((DefaultTableModel) groupSelectionTable.getModel()).addRow(new Object[]{3, "PSMs"});

        groupSelectionScrollPaneScrollPane.getViewport().setOpaque(false);

        // the index column
        groupSelectionTable.getColumn(" ").setMaxWidth(30);
        groupSelectionTable.getColumn(" ").setMinWidth(30);

        // set table properties
        groupSelectionTable.getTableHeader().setReorderingAllowed(false);

        // for some reason background highlighting with alpha values does not work on the backup look and feel...
        if (UIManager.getLookAndFeel().getName().equalsIgnoreCase("Nimbus")) {
            fdrTxt.setBackground(fdrHighlightColor);
            fnrTxt.setBackground(fnrHighlightColor);
        } else {
            fdrTxt.setBackground(confidenceTxt.getBackground());
            fnrTxt.setBackground(confidenceTxt.getBackground());
        }

        // Initialize confidence plot
        scoreAxis = new LogAxis("Probabilistic Score");
        NumberAxis confidenceAxis = new NumberAxis("Confidence [%]");
        confidenceAxis.setAutoRangeIncludesZero(true);
        confidencePlot.setDomainAxis(scoreAxis);
        confidencePlot.setRangeAxis(0, confidenceAxis);
        confidencePlot.setRangeAxisLocation(0, AxisLocation.TOP_OR_LEFT);
        confidencePlot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
        confidenceMarker.setPaint(Color.red);
        confidenceMarker.setStroke(new BasicStroke(LINE_WIDTH));
        confidencePlot.addDomainMarker(confidenceMarker);

        // Initialize PEP plot
        NumberAxis pepAxis = new NumberAxis("PEP [%]");
        pepAxis.setAutoRangeIncludesZero(true);
        pepPlot.setDomainAxis(scoreAxis);
        pepPlot.setRangeAxis(0, pepAxis);
        pepPlot.setRangeAxisLocation(0, AxisLocation.TOP_OR_LEFT);
        pepPlot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
        confidenceMarker.setPaint(Color.red);
        confidenceMarker.setStroke(new BasicStroke(LINE_WIDTH));
        pepPlot.addDomainMarker(confidenceMarker);

        // Initialize FDRs plot
        classicalAxis = new NumberAxis("Classical FDR [%]");
        probaAxis = new NumberAxis("Probabilistic FDR [%]");
        classicalAxis.setAutoRangeIncludesZero(true);
        probaAxis.setAutoRangeIncludesZero(true);
        fdrPlot.setDomainAxis(classicalAxis);
        fdrPlot.setRangeAxis(0, probaAxis);
        fdrPlot.setRangeAxisLocation(0, AxisLocation.TOP_OR_LEFT);
        fdrPlot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);

        // Initialize FDR/FNR plot
        NumberAxis fdrAxis = new NumberAxis("FDR - FNR [%]");
        fdrAxis.setAutoRangeIncludesZero(true);
        fdrFnrPlot.setDomainAxis(scoreAxis);
        fdrFnrPlot.setRangeAxis(0, fdrAxis);
        fdrFnrPlot.setRangeAxisLocation(0, AxisLocation.TOP_OR_LEFT);
        fdrFnrPlot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);

        // Initialize benefit/cost plot
        NumberAxis benefitAxis = new NumberAxis("Benefit (1-FNR) [%]");
        NumberAxis costAxis = new NumberAxis("Cost (FDR) [%]");
        benefitAxis.setAutoRangeIncludesZero(true);
        costAxis.setAutoRangeIncludesZero(true);
        benefitCostPlot.setDomainAxis(costAxis);
        benefitCostPlot.setRangeAxis(0, benefitAxis);
        benefitCostPlot.setRangeAxisLocation(0, AxisLocation.TOP_OR_LEFT);
        benefitCostPlot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);

        fdrCombo1.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        thresholdTypeCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));

        // make the tabs in the spectrum tabbed pane go from right to left
        optimizationTabbedPane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        formComponentResized(null);
    }

    /**
     * Update the plot sizes.
     */
    public void updatePlotSizes() {
        formComponentResized(null);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        groupListJPanel = new javax.swing.JPanel();
        groupSelectionLayeredPane = new javax.swing.JLayeredPane();
        groupSelectionHelpJButton = new javax.swing.JButton();
        groupSelectionScrollPaneScrollPane = new javax.swing.JScrollPane();
        groupSelectionTable = new javax.swing.JTable();
        idSummaryJPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        nTotalTxt = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        nValidatedTxt = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        nTPlTxt = new javax.swing.JTextField();
        nFPTxt = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        fdrTxt = new javax.swing.JTextField();
        fnrTxt = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        nMaxTxt = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        confidenceTxt = new javax.swing.JTextField();
        totalTPHelpJButton = new javax.swing.JButton();
        validatedHitsHelpJButton = new javax.swing.JButton();
        falsePositivesHelpJButton = new javax.swing.JButton();
        truePositivesHelpJButton = new javax.swing.JButton();
        nMaxHelpJButton = new javax.swing.JButton();
        confidenceHelpJButton = new javax.swing.JButton();
        fdrHelpJButton = new javax.swing.JButton();
        fnrHelpJButton = new javax.swing.JButton();
        optimizationJPanel = new javax.swing.JPanel();
        optimizationTabbedPane = new javax.swing.JTabbedPane();
        estimatorOptimizationTab = new javax.swing.JPanel();
        estimatorsPlotSplitPane = new javax.swing.JSplitPane();
        pepPanel = new javax.swing.JPanel();
        jLabel30 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        sensitivitySlider1 = new javax.swing.JSlider();
        pepPlotLayeredPane = new javax.swing.JLayeredPane();
        pepChartPanel = new javax.swing.JPanel();
        pepPlotHelpJButton = new javax.swing.JButton();
        pepPlotExportJButton = new javax.swing.JButton();
        fdrsPanel = new javax.swing.JPanel();
        jLabel34 = new javax.swing.JLabel();
        sensitivitySlider2 = new javax.swing.JSlider();
        jLabel35 = new javax.swing.JLabel();
        fdrsPlotLayeredPane = new javax.swing.JLayeredPane();
        fdrsChartPanel = new javax.swing.JPanel();
        fdrsPlotHelpJButton = new javax.swing.JButton();
        fdrPlotExportJButton = new javax.swing.JButton();
        thresholdOptimizationTab = new javax.swing.JPanel();
        leftPlotSplitPane = new javax.swing.JSplitPane();
        confidencePanel = new javax.swing.JPanel();
        jLabel25 = new javax.swing.JLabel();
        confidenceSlider = new javax.swing.JSlider();
        jLabel26 = new javax.swing.JLabel();
        confidencePlotLayeredPane = new javax.swing.JLayeredPane();
        confidenceChartPanel = new javax.swing.JPanel();
        confidencePlotHelpJButton = new javax.swing.JButton();
        confidencePlotExportJButton = new javax.swing.JButton();
        rightPlotSplitPane = new javax.swing.JSplitPane();
        fdrFnrPanel = new javax.swing.JPanel();
        jLabel28 = new javax.swing.JLabel();
        fdrSlider1 = new javax.swing.JSlider();
        jLabel29 = new javax.swing.JLabel();
        fdrPlotLayeredPane = new javax.swing.JLayeredPane();
        fdrFnrChartPanel = new javax.swing.JPanel();
        fdrFnrPlotHelpJButton = new javax.swing.JButton();
        fdrFnrPlotExportJButton = new javax.swing.JButton();
        benefitCostPanel = new javax.swing.JPanel();
        jLabel32 = new javax.swing.JLabel();
        fdrSlider2 = new javax.swing.JSlider();
        jLabel33 = new javax.swing.JLabel();
        benefitPlotLayeredPane = new javax.swing.JLayeredPane();
        benefitCostChartPanel = new javax.swing.JPanel();
        benefitPlotHelpJButton = new javax.swing.JButton();
        benefitPlotExportJButton = new javax.swing.JButton();
        parametersJPanel = new javax.swing.JPanel();
        thresholdInput = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        validateButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        windowTxt = new javax.swing.JTextField();
        applyButton = new javax.swing.JButton();
        fdrCombo1 = new javax.swing.JComboBox();
        thresholdTypeCmb = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        thresholdHelpJButton = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        estimatorHelpJButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        groupListJPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Group Selection"));
        groupListJPanel.setOpaque(false);

        groupSelectionLayeredPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                groupSelectionLayeredPaneComponentResized(evt);
            }
        });

        groupSelectionHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        groupSelectionHelpJButton.setToolTipText("Help");
        groupSelectionHelpJButton.setBorder(null);
        groupSelectionHelpJButton.setBorderPainted(false);
        groupSelectionHelpJButton.setContentAreaFilled(false);
        groupSelectionHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        groupSelectionHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                groupSelectionHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                groupSelectionHelpJButtonMouseExited(evt);
            }
        });
        groupSelectionHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                groupSelectionHelpJButtonActionPerformed(evt);
            }
        });
        groupSelectionHelpJButton.setBounds(170, 130, 27, 25);
        groupSelectionLayeredPane.add(groupSelectionHelpJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        groupSelectionScrollPaneScrollPane.setOpaque(false);

        groupSelectionTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "Type"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        groupSelectionTable.setOpaque(false);
        groupSelectionTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                groupSelectionTableMouseReleased(evt);
            }
        });
        groupSelectionTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                groupSelectionTableKeyReleased(evt);
            }
        });
        groupSelectionScrollPaneScrollPane.setViewportView(groupSelectionTable);

        groupSelectionScrollPaneScrollPane.setBounds(0, 0, 200, 170);
        groupSelectionLayeredPane.add(groupSelectionScrollPaneScrollPane, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout groupListJPanelLayout = new javax.swing.GroupLayout(groupListJPanel);
        groupListJPanel.setLayout(groupListJPanelLayout);
        groupListJPanelLayout.setHorizontalGroup(
            groupListJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(groupListJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(groupSelectionLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
                .addContainerGap())
        );
        groupListJPanelLayout.setVerticalGroup(
            groupListJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, groupListJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(groupSelectionLayeredPane)
                .addContainerGap())
        );

        idSummaryJPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Identification Summary"));
        idSummaryJPanel.setOpaque(false);

        jLabel2.setText("Total TP:");
        jLabel2.setToolTipText("Total number of true positives");

        nTotalTxt.setBackground(new java.awt.Color(245, 245, 245));
        nTotalTxt.setEditable(false);
        nTotalTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        nTotalTxt.setToolTipText("Total number of true positives");

        jLabel1.setText("# Validated Hits:");
        jLabel1.setToolTipText("Number of validated hits");

        nValidatedTxt.setBackground(new java.awt.Color(245, 245, 245));
        nValidatedTxt.setEditable(false);
        nValidatedTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        nValidatedTxt.setToolTipText("Number of validated hits");

        jLabel3.setText("# FP:");
        jLabel3.setToolTipText("Number of false positives");

        jLabel10.setText("# TP:");
        jLabel10.setToolTipText("Number of true positives");

        nTPlTxt.setBackground(new java.awt.Color(245, 245, 245));
        nTPlTxt.setEditable(false);
        nTPlTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        nTPlTxt.setToolTipText("Number of true positives");

        nFPTxt.setBackground(new java.awt.Color(245, 245, 245));
        nFPTxt.setEditable(false);
        nFPTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        nFPTxt.setToolTipText("Number of false positives");

        jLabel13.setText("FDR:");
        jLabel13.setToolTipText("False Discovery Rate");

        jLabel36.setText("FNR:");
        jLabel36.setToolTipText("False Negative Rate");

        fdrTxt.setEditable(false);
        fdrTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        fdrTxt.setToolTipText("False Discovery Rate");

        fnrTxt.setEditable(false);
        fnrTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        fnrTxt.setToolTipText("False Negative Rate");

        jLabel6.setFont(jLabel6.getFont().deriveFont((jLabel6.getFont().getStyle() | java.awt.Font.ITALIC)));
        jLabel6.setText("Dataset Properties");

        jLabel20.setText("Resolution:");
        jLabel20.setToolTipText("Confidence estimation resolution");

        nMaxTxt.setBackground(new java.awt.Color(245, 245, 245));
        nMaxTxt.setEditable(false);
        nMaxTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        nMaxTxt.setToolTipText("Confidence estimation resolution");

        jLabel7.setFont(jLabel7.getFont().deriveFont((jLabel7.getFont().getStyle() | java.awt.Font.ITALIC)));
        jLabel7.setText("Validation Results");

        jLabel23.setText("Confidence:");
        jLabel23.setToolTipText("Minimum Confidence");

        confidenceTxt.setBackground(new java.awt.Color(245, 245, 245));
        confidenceTxt.setEditable(false);
        confidenceTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        confidenceTxt.setToolTipText("Minimum Confidence");

        totalTPHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        totalTPHelpJButton.setToolTipText("Help");
        totalTPHelpJButton.setBorder(null);
        totalTPHelpJButton.setBorderPainted(false);
        totalTPHelpJButton.setContentAreaFilled(false);
        totalTPHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        totalTPHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                totalTPHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                totalTPHelpJButtonMouseExited(evt);
            }
        });
        totalTPHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                totalTPHelpJButtonActionPerformed(evt);
            }
        });

        validatedHitsHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        validatedHitsHelpJButton.setToolTipText("Help");
        validatedHitsHelpJButton.setBorder(null);
        validatedHitsHelpJButton.setBorderPainted(false);
        validatedHitsHelpJButton.setContentAreaFilled(false);
        validatedHitsHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        validatedHitsHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                validatedHitsHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                validatedHitsHelpJButtonMouseExited(evt);
            }
        });
        validatedHitsHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validatedHitsHelpJButtonActionPerformed(evt);
            }
        });

        falsePositivesHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        falsePositivesHelpJButton.setToolTipText("Help");
        falsePositivesHelpJButton.setBorder(null);
        falsePositivesHelpJButton.setBorderPainted(false);
        falsePositivesHelpJButton.setContentAreaFilled(false);
        falsePositivesHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        falsePositivesHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                falsePositivesHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                falsePositivesHelpJButtonMouseExited(evt);
            }
        });
        falsePositivesHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                falsePositivesHelpJButtonActionPerformed(evt);
            }
        });

        truePositivesHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        truePositivesHelpJButton.setToolTipText("Help");
        truePositivesHelpJButton.setBorder(null);
        truePositivesHelpJButton.setBorderPainted(false);
        truePositivesHelpJButton.setContentAreaFilled(false);
        truePositivesHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        truePositivesHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                truePositivesHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                truePositivesHelpJButtonMouseExited(evt);
            }
        });
        truePositivesHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                truePositivesHelpJButtonActionPerformed(evt);
            }
        });

        nMaxHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        nMaxHelpJButton.setToolTipText("Help");
        nMaxHelpJButton.setBorder(null);
        nMaxHelpJButton.setBorderPainted(false);
        nMaxHelpJButton.setContentAreaFilled(false);
        nMaxHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        nMaxHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                nMaxHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                nMaxHelpJButtonMouseExited(evt);
            }
        });
        nMaxHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nMaxHelpJButtonActionPerformed(evt);
            }
        });

        confidenceHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        confidenceHelpJButton.setToolTipText("Help");
        confidenceHelpJButton.setBorder(null);
        confidenceHelpJButton.setBorderPainted(false);
        confidenceHelpJButton.setContentAreaFilled(false);
        confidenceHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        confidenceHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                confidenceHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                confidenceHelpJButtonMouseExited(evt);
            }
        });
        confidenceHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confidenceHelpJButtonActionPerformed(evt);
            }
        });

        fdrHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        fdrHelpJButton.setToolTipText("Help");
        fdrHelpJButton.setBorder(null);
        fdrHelpJButton.setBorderPainted(false);
        fdrHelpJButton.setContentAreaFilled(false);
        fdrHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        fdrHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                fdrHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                fdrHelpJButtonMouseExited(evt);
            }
        });
        fdrHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fdrHelpJButtonActionPerformed(evt);
            }
        });

        fnrHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        fnrHelpJButton.setToolTipText("Help");
        fnrHelpJButton.setBorder(null);
        fnrHelpJButton.setBorderPainted(false);
        fnrHelpJButton.setContentAreaFilled(false);
        fnrHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        fnrHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                fnrHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                fnrHelpJButtonMouseExited(evt);
            }
        });
        fnrHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fnrHelpJButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout idSummaryJPanelLayout = new javax.swing.GroupLayout(idSummaryJPanel);
        idSummaryJPanel.setLayout(idSummaryJPanelLayout);
        idSummaryJPanelLayout.setHorizontalGroup(
            idSummaryJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(idSummaryJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(idSummaryJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addGroup(idSummaryJPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(idSummaryJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3)
                            .addComponent(jLabel10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(idSummaryJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(idSummaryJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(nValidatedTxt, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(nTotalTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(nFPTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(nTPlTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(idSummaryJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(validatedHitsHelpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(totalTPHelpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(falsePositivesHelpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(truePositivesHelpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(27, 27, 27)
                        .addGroup(idSummaryJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel23)
                            .addComponent(jLabel20)
                            .addComponent(jLabel13)
                            .addComponent(jLabel36))
                        .addGap(9, 9, 9)
                        .addGroup(idSummaryJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(idSummaryJPanelLayout.createSequentialGroup()
                                .addComponent(fnrTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fnrHelpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(idSummaryJPanelLayout.createSequentialGroup()
                                .addComponent(fdrTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fdrHelpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(idSummaryJPanelLayout.createSequentialGroup()
                                .addGroup(idSummaryJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(nMaxTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(confidenceTxt, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(idSummaryJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(confidenceHelpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(nMaxHelpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        idSummaryJPanelLayout.setVerticalGroup(
            idSummaryJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(idSummaryJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(idSummaryJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel2)
                    .addComponent(nTotalTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(totalTPHelpJButton)
                    .addComponent(jLabel20)
                    .addComponent(nMaxTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nMaxHelpJButton))
                .addGap(18, 18, 18)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(idSummaryJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel1)
                    .addComponent(nValidatedTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(validatedHitsHelpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23)
                    .addComponent(confidenceTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(confidenceHelpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(idSummaryJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel3)
                    .addComponent(nFPTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(falsePositivesHelpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(fdrTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fdrHelpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(idSummaryJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel10)
                    .addComponent(nTPlTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(truePositivesHelpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel36)
                    .addComponent(fnrTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fnrHelpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        idSummaryJPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {confidenceHelpJButton, confidenceTxt, falsePositivesHelpJButton, fdrHelpJButton, fdrTxt, fnrHelpJButton, fnrTxt, nFPTxt, nTPlTxt, nValidatedTxt, truePositivesHelpJButton, validatedHitsHelpJButton});

        optimizationJPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Optimization"));
        optimizationJPanel.setOpaque(false);

        optimizationTabbedPane.setBackground(new java.awt.Color(255, 255, 255));
        optimizationTabbedPane.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);

        estimatorOptimizationTab.setBackground(new java.awt.Color(255, 255, 255));

        estimatorsPlotSplitPane.setBorder(null);
        estimatorsPlotSplitPane.setDividerLocation(estimatorsPlotSplitPane.getWidth() / 2);
        estimatorsPlotSplitPane.setDividerSize(0);
        estimatorsPlotSplitPane.setResizeWeight(0.5);
        estimatorsPlotSplitPane.setOpaque(false);

        pepPanel.setOpaque(false);

        jLabel30.setText("Sensitivity");

        jLabel27.setText("Robustness");

        sensitivitySlider1.setToolTipText("" + sensitivitySlider1.getValue());
        sensitivitySlider1.setEnabled(false);
        sensitivitySlider1.setOpaque(false);
        sensitivitySlider1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                sensitivitySlider1MouseReleased(evt);
            }
        });
        sensitivitySlider1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                sensitivitySlider1MouseDragged(evt);
            }
        });

        pepChartPanel.setOpaque(false);
        pepChartPanel.setLayout(new javax.swing.BoxLayout(pepChartPanel, javax.swing.BoxLayout.LINE_AXIS));
        pepChartPanel.setBounds(0, 90, 587, 168);
        pepPlotLayeredPane.add(pepChartPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        pepPlotHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        pepPlotHelpJButton.setToolTipText("Help");
        pepPlotHelpJButton.setBorder(null);
        pepPlotHelpJButton.setBorderPainted(false);
        pepPlotHelpJButton.setContentAreaFilled(false);
        pepPlotHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        pepPlotHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pepPlotHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pepPlotHelpJButtonMouseExited(evt);
            }
        });
        pepPlotHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pepPlotHelpJButtonActionPerformed(evt);
            }
        });
        pepPlotHelpJButton.setBounds(577, 10, 10, 25);
        pepPlotLayeredPane.add(pepPlotHelpJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        pepPlotExportJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        pepPlotExportJButton.setToolTipText("Export");
        pepPlotExportJButton.setBorder(null);
        pepPlotExportJButton.setBorderPainted(false);
        pepPlotExportJButton.setContentAreaFilled(false);
        pepPlotExportJButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        pepPlotExportJButton.setEnabled(false);
        pepPlotExportJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame.png"))); // NOI18N
        pepPlotExportJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pepPlotExportJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pepPlotExportJButtonMouseExited(evt);
            }
        });
        pepPlotExportJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pepPlotExportJButtonActionPerformed(evt);
            }
        });
        pepPlotExportJButton.setBounds(550, 10, 10, 25);
        pepPlotLayeredPane.add(pepPlotExportJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        javax.swing.GroupLayout pepPanelLayout = new javax.swing.GroupLayout(pepPanel);
        pepPanel.setLayout(pepPanelLayout);
        pepPanelLayout.setHorizontalGroup(
            pepPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pepPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pepPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pepPlotLayeredPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 503, Short.MAX_VALUE)
                    .addGroup(pepPanelLayout.createSequentialGroup()
                        .addComponent(jLabel30)
                        .addGap(18, 18, 18)
                        .addComponent(sensitivitySlider1, javax.swing.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel27)))
                .addContainerGap())
        );
        pepPanelLayout.setVerticalGroup(
            pepPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pepPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pepPlotLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pepPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel30)
                    .addComponent(sensitivitySlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel27))
                .addContainerGap())
        );

        estimatorsPlotSplitPane.setLeftComponent(pepPanel);

        fdrsPanel.setOpaque(false);

        jLabel34.setText("Sensitivity");

        sensitivitySlider2.setEnabled(false);
        sensitivitySlider2.setOpaque(false);
        sensitivitySlider2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                sensitivitySlider2MouseReleased(evt);
            }
        });
        sensitivitySlider2.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                sensitivitySlider2MouseDragged(evt);
            }
        });

        jLabel35.setText("Robustness");

        fdrsChartPanel.setOpaque(false);
        fdrsChartPanel.setLayout(new javax.swing.BoxLayout(fdrsChartPanel, javax.swing.BoxLayout.LINE_AXIS));
        fdrsChartPanel.setBounds(0, 80, 588, 173);
        fdrsPlotLayeredPane.add(fdrsChartPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        fdrsPlotHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        fdrsPlotHelpJButton.setToolTipText("Help");
        fdrsPlotHelpJButton.setBorder(null);
        fdrsPlotHelpJButton.setBorderPainted(false);
        fdrsPlotHelpJButton.setContentAreaFilled(false);
        fdrsPlotHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        fdrsPlotHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                fdrsPlotHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                fdrsPlotHelpJButtonMouseExited(evt);
            }
        });
        fdrsPlotHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fdrsPlotHelpJButtonActionPerformed(evt);
            }
        });
        fdrsPlotHelpJButton.setBounds(570, 10, 10, 25);
        fdrsPlotLayeredPane.add(fdrsPlotHelpJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        fdrPlotExportJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        fdrPlotExportJButton.setToolTipText("Export");
        fdrPlotExportJButton.setBorder(null);
        fdrPlotExportJButton.setBorderPainted(false);
        fdrPlotExportJButton.setContentAreaFilled(false);
        fdrPlotExportJButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        fdrPlotExportJButton.setEnabled(false);
        fdrPlotExportJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame.png"))); // NOI18N
        fdrPlotExportJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                fdrPlotExportJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                fdrPlotExportJButtonMouseExited(evt);
            }
        });
        fdrPlotExportJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fdrPlotExportJButtonActionPerformed(evt);
            }
        });
        fdrPlotExportJButton.setBounds(550, 10, 10, 25);
        fdrsPlotLayeredPane.add(fdrPlotExportJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        javax.swing.GroupLayout fdrsPanelLayout = new javax.swing.GroupLayout(fdrsPanel);
        fdrsPanel.setLayout(fdrsPanelLayout);
        fdrsPanelLayout.setHorizontalGroup(
            fdrsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, fdrsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(fdrsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(fdrsPlotLayeredPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 503, Short.MAX_VALUE)
                    .addGroup(fdrsPanelLayout.createSequentialGroup()
                        .addComponent(jLabel34)
                        .addGap(18, 18, 18)
                        .addComponent(sensitivitySlider2, javax.swing.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel35)))
                .addContainerGap())
        );
        fdrsPanelLayout.setVerticalGroup(
            fdrsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, fdrsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fdrsPlotLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(fdrsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(sensitivitySlider2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel35)
                    .addComponent(jLabel34))
                .addContainerGap())
        );

        estimatorsPlotSplitPane.setRightComponent(fdrsPanel);

        javax.swing.GroupLayout estimatorOptimizationTabLayout = new javax.swing.GroupLayout(estimatorOptimizationTab);
        estimatorOptimizationTab.setLayout(estimatorOptimizationTabLayout);
        estimatorOptimizationTabLayout.setHorizontalGroup(
            estimatorOptimizationTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(estimatorsPlotSplitPane)
        );
        estimatorOptimizationTabLayout.setVerticalGroup(
            estimatorOptimizationTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(estimatorsPlotSplitPane)
        );

        optimizationTabbedPane.addTab("Estimators", estimatorOptimizationTab);

        thresholdOptimizationTab.setBackground(new java.awt.Color(255, 255, 255));

        leftPlotSplitPane.setBorder(null);
        leftPlotSplitPane.setDividerLocation(leftPlotSplitPane.getWidth() / 3);
        leftPlotSplitPane.setDividerSize(0);
        leftPlotSplitPane.setResizeWeight(0.5);
        leftPlotSplitPane.setOpaque(false);

        confidencePanel.setOpaque(false);

        jLabel25.setText("Quantity");

        confidenceSlider.setToolTipText("Confidence Threshold");
        confidenceSlider.setEnabled(false);
        confidenceSlider.setOpaque(false);
        confidenceSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                confidenceSliderMouseReleased(evt);
            }
        });
        confidenceSlider.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                confidenceSliderMouseDragged(evt);
            }
        });

        jLabel26.setText("Quality");

        confidenceChartPanel.setOpaque(false);
        confidenceChartPanel.setLayout(new javax.swing.BoxLayout(confidenceChartPanel, javax.swing.BoxLayout.LINE_AXIS));
        confidenceChartPanel.setBounds(0, 0, 500, 460);
        confidencePlotLayeredPane.add(confidenceChartPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        confidencePlotHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        confidencePlotHelpJButton.setToolTipText("Help");
        confidencePlotHelpJButton.setBorder(null);
        confidencePlotHelpJButton.setBorderPainted(false);
        confidencePlotHelpJButton.setContentAreaFilled(false);
        confidencePlotHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        confidencePlotHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                confidencePlotHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                confidencePlotHelpJButtonMouseExited(evt);
            }
        });
        confidencePlotHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confidencePlotHelpJButtonActionPerformed(evt);
            }
        });
        confidencePlotHelpJButton.setBounds(480, 0, 10, 25);
        confidencePlotLayeredPane.add(confidencePlotHelpJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        confidencePlotExportJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        confidencePlotExportJButton.setToolTipText("Export");
        confidencePlotExportJButton.setBorder(null);
        confidencePlotExportJButton.setBorderPainted(false);
        confidencePlotExportJButton.setContentAreaFilled(false);
        confidencePlotExportJButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        confidencePlotExportJButton.setEnabled(false);
        confidencePlotExportJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame.png"))); // NOI18N
        confidencePlotExportJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                confidencePlotExportJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                confidencePlotExportJButtonMouseExited(evt);
            }
        });
        confidencePlotExportJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confidencePlotExportJButtonActionPerformed(evt);
            }
        });
        confidencePlotExportJButton.setBounds(460, 0, 10, 25);
        confidencePlotLayeredPane.add(confidencePlotExportJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        javax.swing.GroupLayout confidencePanelLayout = new javax.swing.GroupLayout(confidencePanel);
        confidencePanel.setLayout(confidencePanelLayout);
        confidencePanelLayout.setHorizontalGroup(
            confidencePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(confidencePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(confidencePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(confidencePlotLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)
                    .addGroup(confidencePanelLayout.createSequentialGroup()
                        .addComponent(jLabel25)
                        .addGap(18, 18, 18)
                        .addComponent(confidenceSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel26)))
                .addContainerGap())
        );
        confidencePanelLayout.setVerticalGroup(
            confidencePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, confidencePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(confidencePlotLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(confidencePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(confidenceSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(confidencePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel25)))
                .addContainerGap())
        );

        leftPlotSplitPane.setLeftComponent(confidencePanel);

        rightPlotSplitPane.setBorder(null);
        rightPlotSplitPane.setDividerLocation(rightPlotSplitPane.getWidth() / 2);
        rightPlotSplitPane.setDividerSize(0);
        rightPlotSplitPane.setResizeWeight(0.5);
        rightPlotSplitPane.setOpaque(false);

        fdrFnrPanel.setOpaque(false);

        jLabel28.setText("Quality");

        fdrSlider1.setToolTipText("FDR Threshold");
        fdrSlider1.setEnabled(false);
        fdrSlider1.setOpaque(false);
        fdrSlider1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fdrSlider1MouseReleased(evt);
            }
        });
        fdrSlider1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                fdrSlider1MouseDragged(evt);
            }
        });

        jLabel29.setText("Quantity");

        fdrFnrChartPanel.setOpaque(false);
        fdrFnrChartPanel.setLayout(new javax.swing.BoxLayout(fdrFnrChartPanel, javax.swing.BoxLayout.LINE_AXIS));
        fdrFnrChartPanel.setBounds(0, 3, 320, 450);
        fdrPlotLayeredPane.add(fdrFnrChartPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        fdrFnrPlotHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        fdrFnrPlotHelpJButton.setToolTipText("Help");
        fdrFnrPlotHelpJButton.setBorder(null);
        fdrFnrPlotHelpJButton.setBorderPainted(false);
        fdrFnrPlotHelpJButton.setContentAreaFilled(false);
        fdrFnrPlotHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        fdrFnrPlotHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                fdrFnrPlotHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                fdrFnrPlotHelpJButtonMouseExited(evt);
            }
        });
        fdrFnrPlotHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fdrFnrPlotHelpJButtonActionPerformed(evt);
            }
        });
        fdrFnrPlotHelpJButton.setBounds(300, 10, 10, 25);
        fdrPlotLayeredPane.add(fdrFnrPlotHelpJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        fdrFnrPlotExportJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        fdrFnrPlotExportJButton.setToolTipText("Export");
        fdrFnrPlotExportJButton.setBorder(null);
        fdrFnrPlotExportJButton.setBorderPainted(false);
        fdrFnrPlotExportJButton.setContentAreaFilled(false);
        fdrFnrPlotExportJButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        fdrFnrPlotExportJButton.setEnabled(false);
        fdrFnrPlotExportJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame.png"))); // NOI18N
        fdrFnrPlotExportJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                fdrFnrPlotExportJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                fdrFnrPlotExportJButtonMouseExited(evt);
            }
        });
        fdrFnrPlotExportJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fdrFnrPlotExportJButtonActionPerformed(evt);
            }
        });
        fdrFnrPlotExportJButton.setBounds(290, 10, 10, 25);
        fdrPlotLayeredPane.add(fdrFnrPlotExportJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        javax.swing.GroupLayout fdrFnrPanelLayout = new javax.swing.GroupLayout(fdrFnrPanel);
        fdrFnrPanel.setLayout(fdrFnrPanelLayout);
        fdrFnrPanelLayout.setHorizontalGroup(
            fdrFnrPanelLayout.createParallelGroup(javax.swing
