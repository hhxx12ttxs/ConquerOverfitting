package eu.isas.peptideshaker.gui.tabpanels;

import com.compomics.util.Util;
import com.compomics.util.examples.BareBonesBrowserLaunch;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.PeptideMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.gui.dialogs.ProgressDialogParent;
import com.compomics.util.gui.dialogs.ProgressDialogX;
import com.compomics.util.pdbfinder.FindPdbForUniprotAccessions;
import com.compomics.util.pdbfinder.pdb.PdbBlock;
import com.compomics.util.pdbfinder.pdb.PdbParameter;
import eu.isas.peptideshaker.export.OutputGenerator;
import eu.isas.peptideshaker.gui.ExportGraphicsDialog;
import eu.isas.peptideshaker.gui.HelpDialog;
import eu.isas.peptideshaker.gui.PeptideShakerGUI;
import eu.isas.peptideshaker.gui.ProteinInferenceDialog;
import eu.isas.peptideshaker.gui.ProteinInferencePeptideLevelDialog;
import eu.isas.peptideshaker.gui.tablemodels.ProteinTableModel;
import eu.isas.peptideshaker.myparameters.PSParameter;
import eu.isas.peptideshaker.preferences.SpectrumCountingPreferences.SpectralCountingMethod;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import no.uib.jsparklines.data.XYDataPoint;
import no.uib.jsparklines.extra.HtmlLinksRenderer;
import no.uib.jsparklines.extra.TrueFalseIconRenderer;
import no.uib.jsparklines.renderers.JSparklinesBarChartTableCellRenderer;
import no.uib.jsparklines.renderers.JSparklinesIntegerColorTableCellRenderer;
import no.uib.jsparklines.renderers.JSparklinesIntervalChartTableCellRenderer;
import no.uib.jsparklines.renderers.JSparklinesTwoValueBarChartTableCellRenderer;
import org.jfree.chart.plot.PlotOrientation;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolViewer;

/**
 * The Protein Structures tab.
 *
 * @author Harald Barsnes
 */
public class ProteinStructurePanel extends javax.swing.JPanel implements ProgressDialogParent {

    /**
     * Peptide keys that can be mapped to the current pdb file.
     */
    private ArrayList<String> peptidePdbArray;

    /**
     * Indexes for the three main data tables.
     */
    private enum TableIndex {

        PROTEIN_TABLE, PEPTIDE_TABLE, PDB_MATCHES, PDB_CHAINS
    };
    /**
     * If true, labels are shown for the modifications in the 3D structure.
     */
    private boolean showModificationLabels = true;
    /**
     * If true, the 3D model will be spinning.
     */
    private boolean spinModel = true;
    /**
     * If true the ribbon model is used.
     */
    private boolean ribbonModel = true;
    /**
     * If true the backbone model is used.
     */
    private boolean backboneModel = false;
    /**
     * The currently displayed PDB file.
     */
    private String currentlyDisplayedPdbFile;
    /**
     * A simple progress dialog.
     */
    private ProgressDialogX progressDialog;
    /**
     * If true the progress bar is disposed of.
     */
    private static boolean cancelProgress = false;
    /**
     * The UniProt to PDB finder.
     */
    private FindPdbForUniprotAccessions uniProtPdb;
    /**
     * The PeptideShaker main frame.
     */
    private PeptideShakerGUI peptideShakerGUI;
    /**
     * The Jmol panel.
     */
    private JmolPanel jmolPanel;
    /**
     * The protein table column header tooltips.
     */
    private ArrayList<String> proteinTableToolTips;
    /**
     * The peptide table column header tooltips.
     */
    private ArrayList<String> peptideTableToolTips;
    /**
     * The pdb files table column header tooltips.
     */
    private ArrayList<String> pdbTableToolTips;
    /**
     * The pdb chains table column header tooltips.
     */
    private ArrayList<String> pdbChainsTableToolTips;
    /**
     * A mapping of the peptide table entries.
     */
    private HashMap<Integer, String> peptideTableMap = new HashMap<Integer, String>();
    /**
     * If true Jmol is currently displaying a structure.
     */
    private boolean jmolStructureShown = false;
    /**
     * The current PDB chains.
     */
    private PdbBlock[] chains;
    /**
     * The amino acid sequence of the current chain.
     */
    private String chainSequence;
    /**
     * The current protein sequence.
     */
    private String proteinSequence;
    /**
     * The sequence factory
     */
    private SequenceFactory sequenceFactory = SequenceFactory.getInstance();
    /**
     * A list of proteins in the protein table.
     */
    private ArrayList<String> proteinKeys = new ArrayList<String>();

    /**
     * Creates a new ProteinPanel.
     *
     * @param peptideShakerGUI the PeptideShaker main frame
     */
    public ProteinStructurePanel(PeptideShakerGUI peptideShakerGUI) {
        initComponents();
        this.peptideShakerGUI = peptideShakerGUI;

        jmolPanel = new JmolPanel();
        pdbPanel.add(jmolPanel);

        setUpTableHeaderToolTips();
        setTableProperties();

        proteinScrollPane.getViewport().setOpaque(false);
        peptideScrollPane.getViewport().setOpaque(false);
        pdbJScrollPane.getViewport().setOpaque(false);
        pdbChainsJScrollPane.getViewport().setOpaque(false);
    }

    /**
     * Set up the table header tooltips.
     */
    private void setUpTableHeaderToolTips() {
        proteinTableToolTips = new ArrayList<String>();
        proteinTableToolTips.add(null);
        proteinTableToolTips.add("Starred");
        proteinTableToolTips.add("Protein Inference Class");
        proteinTableToolTips.add("Protein Accession Number");
        proteinTableToolTips.add("Protein Description");
        proteinTableToolTips.add("Protein Seqeunce Coverage (%) (Observed / Possible)");
        proteinTableToolTips.add("Number of Peptides (Validated / Total)");
        proteinTableToolTips.add("Number of Spectra (Validated / Total)");
        proteinTableToolTips.add("MS2 Quantification");
        proteinTableToolTips.add("Protein Molecular Weight (kDa)");

        if (peptideShakerGUI.getDisplayPreferences().showScores()) {
            proteinTableToolTips.add("Protein Score");
        } else {
            proteinTableToolTips.add("Protein Confidence");
        }

        proteinTableToolTips.add("Validated");

        peptideTableToolTips = new ArrayList<String>();
        peptideTableToolTips.add(null);
        peptideTableToolTips.add("Starred");
        peptideTableToolTips.add("Protein Inference");
        peptideTableToolTips.add("Peptide Sequence");
        peptideTableToolTips.add("Peptide Start Index");
        peptideTableToolTips.add("In PDB Sequence");
        peptideTableToolTips.add("Validated");

        pdbTableToolTips = new ArrayList<String>();
        pdbTableToolTips.add(null);
        pdbTableToolTips.add("PDB Accession Number");
        pdbTableToolTips.add("PDB Title");
        pdbTableToolTips.add("Type of Structure");
        pdbTableToolTips.add("Number of Chains");

        pdbChainsTableToolTips = new ArrayList<String>();
        pdbChainsTableToolTips.add(null);
        pdbChainsTableToolTips.add("Chain Label");
        pdbChainsTableToolTips.add("Protein-PDB Alignment");
        pdbChainsTableToolTips.add("Protein Coverage for PDB Sequence");
    }

    /**
     * Set up the properties of the tables.
     */
    private void setTableProperties() {
        setProteinTableProperties();
        setPeptideTableProperties();
        setPdbTablesProperties();
    }

    /**
     * Set up the properties of the protein table.
     */
    private void setProteinTableProperties() {

        proteinTable.getColumn(" ").setMaxWidth(50);
        proteinTable.getColumn(" ").setMinWidth(50);

        // the validated column
        proteinTable.getColumn("").setMaxWidth(30);
        proteinTable.getColumn("").setMinWidth(30);

        // the selected columns
        proteinTable.getColumn("  ").setMaxWidth(30);
        proteinTable.getColumn("  ").setMinWidth(30);

        // the protein inference column
        proteinTable.getColumn("PI").setMaxWidth(37);
        proteinTable.getColumn("PI").setMinWidth(37);

        try {
            proteinTable.getColumn("Confidence").setMaxWidth(90);
            proteinTable.getColumn("Confidence").setMinWidth(90);
        } catch (IllegalArgumentException w) {
            proteinTable.getColumn("Score").setMaxWidth(90);
            proteinTable.getColumn("Score").setMinWidth(90);
        }

        proteinTable.getTableHeader().setReorderingAllowed(false);

        proteinTable.setAutoCreateRowSorter(true);

        // set up the protein inference color map
        HashMap<Integer, Color> proteinInferenceColorMap = new HashMap<Integer, Color>();
        proteinInferenceColorMap.put(PSParameter.NOT_GROUP, peptideShakerGUI.getSparklineColor()); // NOT_GROUP
        proteinInferenceColorMap.put(PSParameter.ISOFORMS, Color.YELLOW); // ISOFORMS
        proteinInferenceColorMap.put(PSParameter.ISOFORMS_UNRELATED, Color.ORANGE); // ISOFORMS_UNRELATED
        proteinInferenceColorMap.put(PSParameter.UNRELATED, Color.RED); // UNRELATED

        // set up the protein inference tooltip map
        HashMap<Integer, String> proteinInferenceTooltipMap = new HashMap<Integer, String>();
        proteinInferenceTooltipMap.put(PSParameter.NOT_GROUP, "Single Protein");
        proteinInferenceTooltipMap.put(PSParameter.ISOFORMS, "Isoforms");
        proteinInferenceTooltipMap.put(PSParameter.ISOFORMS_UNRELATED, "Unrelated Isoforms");
        proteinInferenceTooltipMap.put(PSParameter.UNRELATED, "Unrelated Proteins");

        proteinTable.getColumn("Accession").setCellRenderer(new HtmlLinksRenderer(peptideShakerGUI.getSelectedRowHtmlTagFontColor(), peptideShakerGUI.getNotSelectedRowHtmlTagFontColor()));
        proteinTable.getColumn("PI").setCellRenderer(new JSparklinesIntegerColorTableCellRenderer(peptideShakerGUI.getSparklineColor(), proteinInferenceColorMap, proteinInferenceTooltipMap));
        proteinTable.getColumn("#Peptides").setCellRenderer(new JSparklinesTwoValueBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100.0,
                peptideShakerGUI.getSparklineColor(), peptideShakerGUI.getSparklineColorNonValidated(), false));
        ((JSparklinesTwoValueBarChartTableCellRenderer) proteinTable.getColumn("#Peptides").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth(), new DecimalFormat("0"));
        proteinTable.getColumn("#Spectra").setCellRenderer(new JSparklinesTwoValueBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100.0,
                peptideShakerGUI.getSparklineColor(), peptideShakerGUI.getSparklineColorNonValidated(), false));
        ((JSparklinesTwoValueBarChartTableCellRenderer) proteinTable.getColumn("#Spectra").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth(), new DecimalFormat("0"));
        proteinTable.getColumn("MS2 Quant.").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 10.0, peptideShakerGUI.getSparklineColor()));
        ((JSparklinesBarChartTableCellRenderer) proteinTable.getColumn("MS2 Quant.").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth());
        proteinTable.getColumn("MW").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 10.0, peptideShakerGUI.getSparklineColor()));
        ((JSparklinesBarChartTableCellRenderer) proteinTable.getColumn("MW").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth());

        try {
            proteinTable.getColumn("Confidence").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100.0, peptideShakerGUI.getSparklineColor()));
            ((JSparklinesBarChartTableCellRenderer) proteinTable.getColumn("Confidence").getCellRenderer()).showNumberAndChart(
                    true, peptideShakerGUI.getLabelWidth() - 20, peptideShakerGUI.getScoreAndConfidenceDecimalFormat());
        } catch (IllegalArgumentException e) {
            proteinTable.getColumn("Score").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100.0, peptideShakerGUI.getSparklineColor()));
            ((JSparklinesBarChartTableCellRenderer) proteinTable.getColumn("Score").getCellRenderer()).showNumberAndChart(
                    true, peptideShakerGUI.getLabelWidth() - 20, peptideShakerGUI.getScoreAndConfidenceDecimalFormat());
        }

        proteinTable.getColumn("Coverage").setCellRenderer(new JSparklinesTwoValueBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100.0,
                peptideShakerGUI.getSparklineColor(), peptideShakerGUI.getUserPreferences().getSparklineColorNotFound(), true));
        ((JSparklinesTwoValueBarChartTableCellRenderer) proteinTable.getColumn("Coverage").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth(), new DecimalFormat("0.00"));
        proteinTable.getColumn("").setCellRenderer(new TrueFalseIconRenderer(
                new ImageIcon(this.getClass().getResource("/icons/accept.png")),
                new ImageIcon(this.getClass().getResource("/icons/Error_3.png")),
                "Validated", "Not Validated"));
        proteinTable.getColumn("  ").setCellRenderer(new TrueFalseIconRenderer(
                new ImageIcon(this.getClass().getResource("/icons/star_yellow.png")),
                new ImageIcon(this.getClass().getResource("/icons/star_grey.png")),
                new ImageIcon(this.getClass().getResource("/icons/star_grey.png")),
                "Starred", null, null));

        // invoke later to give time for components to update
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                // set the preferred size of the accession column
                int width = peptideShakerGUI.getPreferredColumnWidth(proteinTable, proteinTable.getColumn("Accession").getModelIndex(), 6);
                proteinTable.getColumn("Accession").setMinWidth(width);
                proteinTable.getColumn("Accession").setMaxWidth(width);
            }
        });
    }

    /**
     * Set up the properties of the peptide table.
     */
    private void setPeptideTableProperties() {

        peptideTable.getColumn(" ").setMaxWidth(50);
        peptideTable.getColumn(" ").setMinWidth(50);

        peptideTable.getColumn("PDB").setMinWidth(50);
        peptideTable.getColumn("PDB").setMaxWidth(50);
        peptideTable.getColumn("Start").setMinWidth(50);

        // the validated column
        peptideTable.getColumn("").setMaxWidth(30);
        peptideTable.getColumn("").setMinWidth(30);

        // the selected columns
        peptideTable.getColumn("  ").setMaxWidth(30);
        peptideTable.getColumn("  ").setMinWidth(30);

        // the protein inference column
        peptideTable.getColumn("PI").setMaxWidth(37);
        peptideTable.getColumn("PI").setMinWidth(37);

        peptideTable.getTableHeader().setReorderingAllowed(false);

        peptideTable.setAutoCreateRowSorter(true);

        // set up the peptide inference color map
        HashMap<Integer, Color> peptideInferenceColorMap = new HashMap<Integer, Color>();
        peptideInferenceColorMap.put(PSParameter.NOT_GROUP, peptideShakerGUI.getSparklineColor());
        peptideInferenceColorMap.put(PSParameter.ISOFORMS, Color.YELLOW);
        peptideInferenceColorMap.put(PSParameter.ISOFORMS_UNRELATED, Color.ORANGE);
        peptideInferenceColorMap.put(PSParameter.UNRELATED, Color.RED);

        // set up the peptide inference tooltip map
        HashMap<Integer, String> peptideInferenceTooltipMap = new HashMap<Integer, String>();
        peptideInferenceTooltipMap.put(0, "Unique to Protein/Protein Group");
        peptideInferenceTooltipMap.put(1, "Maps to 2 Proteins/Protein Groups");
        peptideInferenceTooltipMap.put(2, "Maps to 3-5 Proteins/Protein Groups");
        peptideInferenceTooltipMap.put(3, "Maps to >5 Proteins/Protein Groups");

        peptideTable.getColumn("PI").setCellRenderer(new JSparklinesIntegerColorTableCellRenderer(peptideShakerGUI.getSparklineColor(), peptideInferenceColorMap, peptideInferenceTooltipMap));
        peptideTable.getColumn("Start").setCellRenderer(new JSparklinesIntervalChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100d, 100d, peptideShakerGUI.getSparklineColor()));
        peptideTable.getColumn("PDB").setCellRenderer(new TrueFalseIconRenderer(
                new ImageIcon(this.getClass().getResource("/icons/pdb.png")),
                null,
                "Mapped to PDB Structure", null));
        peptideTable.getColumn("").setCellRenderer(new TrueFalseIconRenderer(
                new ImageIcon(this.getClass().getResource("/icons/accept.png")),
                new ImageIcon(this.getClass().getResource("/icons/Error_3.png")),
                "Validated", "Not Validated"));
        peptideTable.getColumn("  ").setCellRenderer(new TrueFalseIconRenderer(
                new ImageIcon(this.getClass().getResource("/icons/star_yellow.png")),
                new ImageIcon(this.getClass().getResource("/icons/star_grey.png")),
                new ImageIcon(this.getClass().getResource("/icons/star_grey.png")),
                "Starred", null, null));
    }

    /**
     * Set up the properties of the pdb and pdb chains tables.
     */
    private void setPdbTablesProperties() {

        pdbMatchesJTable.getColumn(" ").setMaxWidth(50);
        pdbChainsJTable.getColumn(" ").setMaxWidth(50);
        pdbMatchesJTable.getColumn("PDB").setMaxWidth(50);
        pdbChainsJTable.getColumn("Chain").setMaxWidth(50);
        pdbMatchesJTable.getColumn(" ").setMinWidth(50);
        pdbChainsJTable.getColumn(" ").setMinWidth(50);
        pdbMatchesJTable.getColumn("PDB").setMinWidth(50);
        pdbChainsJTable.getColumn("Chain").setMinWidth(50);

        pdbMatchesJTable.getColumn("Chains").setMinWidth(100);
        pdbMatchesJTable.getColumn("Chains").setMaxWidth(100);

        pdbMatchesJTable.getTableHeader().setReorderingAllowed(false);
        pdbChainsJTable.getTableHeader().setReorderingAllowed(false);

        pdbChainsJTable.setAutoCreateRowSorter(true);
        pdbMatchesJTable.setAutoCreateRowSorter(true);

        pdbMatchesJTable.getColumn("PDB").setCellRenderer(new HtmlLinksRenderer(peptideShakerGUI.getSelectedRowHtmlTagFontColor(), peptideShakerGUI.getNotSelectedRowHtmlTagFontColor()));
        pdbMatchesJTable.getColumn("Chains").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 10.0, peptideShakerGUI.getSparklineColor()));
        ((JSparklinesBarChartTableCellRenderer) pdbMatchesJTable.getColumn("Chains").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth());

        pdbChainsJTable.getColumn("Coverage").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100.0, peptideShakerGUI.getSparklineColor()));
        ((JSparklinesBarChartTableCellRenderer) pdbChainsJTable.getColumn("Coverage").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth());

        pdbChainsJTable.getColumn("PDB-Protein").setCellRenderer(new JSparklinesIntervalChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100.0, 10.0, peptideShakerGUI.getSparklineColor()));
        ((JSparklinesIntervalChartTableCellRenderer) pdbChainsJTable.getColumn("PDB-Protein").getCellRenderer()).showReferenceLine(true, 0.02, Color.BLACK);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pdbStructureJPanel = new javax.swing.JPanel();
        pdbStructureLayeredPane = new javax.swing.JLayeredPane();
        pdbOuterPanel = new javax.swing.JPanel();
        pdbLayeredPane = new javax.swing.JLayeredPane();
        pdbPanel = new javax.swing.JPanel();
        labelsJButton = new javax.swing.JButton();
        ribbonJButton = new javax.swing.JButton();
        backboneJButton = new javax.swing.JButton();
        playJButton = new javax.swing.JButton();
        pdbStructureHelpJButton = new javax.swing.JButton();
        exportPdbStructureJButton = new javax.swing.JButton();
        contextMenuPdbStructureBackgroundPanel = new javax.swing.JPanel();
        proteinsJPanel = new javax.swing.JPanel();
        proteinsLayeredPane = new javax.swing.JLayeredPane();
        proteinsPanel = new javax.swing.JPanel();
        proteinScrollPane = new javax.swing.JScrollPane();
        proteinTable = new JTable() {
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        String tip = (String) proteinTableToolTips.get(realIndex);
                        return tip;
                    }
                };
            }
        };
        proteinsHelpJButton = new javax.swing.JButton();
        exportProteinsJButton = new javax.swing.JButton();
        contextMenuProteinsBackgroundPanel = new javax.swing.JPanel();
        peptidesJPanel = new javax.swing.JPanel();
        peptidesLayeredPane = new javax.swing.JLayeredPane();
        peptidesPanel = new javax.swing.JPanel();
        peptideScrollPane = new javax.swing.JScrollPane();
        peptideTable = new JTable() {
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        String tip = (String) peptideTableToolTips.get(realIndex);
                        return tip;
                    }
                };
            }
        };
        peptidesHelpJButton = new javax.swing.JButton();
        exportPeptidesJButton = new javax.swing.JButton();
        contextMenuPeptidesBackgroundPanel = new javax.swing.JPanel();
        pdbMatchesJPanel = new javax.swing.JPanel();
        pdbMatchesLayeredPane = new javax.swing.JLayeredPane();
        pdbMatchesPanel = new javax.swing.JPanel();
        pdbJScrollPane = new javax.swing.JScrollPane();
        pdbMatchesJTable = new JTable() {
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        String tip = (String) pdbTableToolTips.get(realIndex);
                        return tip;
                    }
                };
            }
        };
        pdbMatchesHelpJButton = new javax.swing.JButton();
        exportPdbMatchesJButton = new javax.swing.JButton();
        contextMenuPdbMatchesBackgroundPanel = new javax.swing.JPanel();
        pdbChainsJPanel = new javax.swing.JPanel();
        pdbChainsLayeredPane = new javax.swing.JLayeredPane();
        pdbChainsPanel = new javax.swing.JPanel();
        pdbChainsJScrollPane = new javax.swing.JScrollPane();
        pdbChainsJTable = new JTable() {
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        String tip = (String) pdbChainsTableToolTips.get(realIndex);
                        return tip;
                    }
                };
            }
        };
        pdbChainHelpJButton = new javax.swing.JButton();
        exportPdbChainsJButton = new javax.swing.JButton();
        contextMenuPdbChainsBackgroundPanel = new javax.swing.JPanel();

        setBackground(new java.awt.Color(255, 255, 255));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        pdbStructureJPanel.setOpaque(false);

        pdbOuterPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("PDB Structure"));
        pdbOuterPanel.setOpaque(false);

        pdbLayeredPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                pdbLayeredPaneComponentResized(evt);
            }
        });

        pdbPanel.setLayout(new javax.swing.BoxLayout(pdbPanel, javax.swing.BoxLayout.LINE_AXIS));
        pdbPanel.setBounds(0, 0, 435, 440);
        pdbLayeredPane.add(pdbPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        labelsJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/labels_selected.png"))); // NOI18N
        labelsJButton.setToolTipText("Hide Modification Labels");
        labelsJButton.setBorder(null);
        labelsJButton.setBorderPainted(false);
        labelsJButton.setContentAreaFilled(false);
        labelsJButton.setFocusable(false);
        labelsJButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        labelsJButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        labelsJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                labelsJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                labelsJButtonMouseExited(evt);
            }
        });
        labelsJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                labelsJButtonActionPerformed(evt);
            }
        });
        labelsJButton.setBounds(0, 0, 25, 25);
        pdbLayeredPane.add(labelsJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        ribbonJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/ribbon_selected.png"))); // NOI18N
        ribbonJButton.setToolTipText("Ribbon Model");
        ribbonJButton.setBorder(null);
        ribbonJButton.setBorderPainted(false);
        ribbonJButton.setContentAreaFilled(false);
        ribbonJButton.setFocusable(false);
        ribbonJButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ribbonJButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        ribbonJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                ribbonJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                ribbonJButtonMouseExited(evt);
            }
        });
        ribbonJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ribbonJButtonActionPerformed(evt);
            }
        });
        ribbonJButton.setBounds(0, 0, 25, 25);
        pdbLayeredPane.add(ribbonJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        backboneJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/backbone.png"))); // NOI18N
        backboneJButton.setToolTipText("Backbone Model");
        backboneJButton.setBorder(null);
        backboneJButton.setBorderPainted(false);
        backboneJButton.setContentAreaFilled(false);
        backboneJButton.setFocusable(false);
        backboneJButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        backboneJButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        backboneJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                backboneJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                backboneJButtonMouseExited(evt);
            }
        });
        backboneJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backboneJButtonActionPerformed(evt);
            }
        });
        backboneJButton.setBounds(0, 0, 25, 25);
        pdbLayeredPane.add(backboneJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        playJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/pause.png"))); // NOI18N
        playJButton.setToolTipText("Stop Rotation");
        playJButton.setBorder(null);
        playJButton.setBorderPainted(false);
        playJButton.setContentAreaFilled(false);
        playJButton.setFocusable(false);
        playJButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        playJButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        playJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                playJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                playJButtonMouseExited(evt);
            }
        });
        playJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playJButtonActionPerformed(evt);
            }
        });
        playJButton.setBounds(0, 0, 21, 21);
        pdbLayeredPane.add(playJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        javax.swing.GroupLayout pdbOuterPanelLayout = new javax.swing.GroupLayout(pdbOuterPanel);
        pdbOuterPanel.setLayout(pdbOuterPanelLayout);
        pdbOuterPanelLayout.setHorizontalGroup(
            pdbOuterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 448, Short.MAX_VALUE)
            .addGroup(pdbOuterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pdbOuterPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(pdbLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        pdbOuterPanelLayout.setVerticalGroup(
            pdbOuterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 463, Short.MAX_VALUE)
            .addGroup(pdbOuterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pdbOuterPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(pdbLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 441, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        pdbOuterPanel.setBounds(0, 0, 460, 490);
        pdbStructureLayeredPane.add(pdbOuterPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        pdbStructureHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        pdbStructureHelpJButton.setToolTipText("Help");
        pdbStructureHelpJButton.setBorder(null);
        pdbStructureHelpJButton.setBorderPainted(false);
        pdbStructureHelpJButton.setContentAreaFilled(false);
        pdbStructureHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        pdbStructureHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pdbStructureHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pdbStructureHelpJButtonMouseExited(evt);
            }
        });
        pdbStructureHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pdbStructureHelpJButtonActionPerformed(evt);
            }
        });
        pdbStructureHelpJButton.setBounds(440, 0, 10, 25);
        pdbStructureLayeredPane.add(pdbStructureHelpJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        exportPdbStructureJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        exportPdbStructureJButton.setToolTipText("Export");
        exportPdbStructureJButton.setBorder(null);
        exportPdbStructureJButton.setBorderPainted(false);
        exportPdbStructureJButton.setContentAreaFilled(false);
        exportPdbStructureJButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        exportPdbStructureJButton.setEnabled(false);
        exportPdbStructureJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame.png"))); // NOI18N
        exportPdbStructureJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                exportPdbStructureJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                exportPdbStructureJButtonMouseExited(evt);
            }
        });
        exportPdbStructureJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportPdbStructureJButtonActionPerformed(evt);
            }
        });
        exportPdbStructureJButton.setBounds(430, 0, 10, 25);
        pdbStructureLayeredPane.add(exportPdbStructureJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        contextMenuPdbStructureBackgroundPanel.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout contextMenuPdbStructureBackgroundPanelLayout = new javax.swing.GroupLayout(contextMenuPdbStructureBackgroundPanel);
        contextMenuPdbStructureBackgroundPanel.setLayout(contextMenuPdbStructureBackgroundPanelLayout);
        contextMenuPdbStructureBackgroundPanelLayout.setHorizontalGroup(
            contextMenuPdbStructureBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );
        contextMenuPdbStructureBackgroundPanelLayout.setVerticalGroup(
            contextMenuPdbStructureBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        contextMenuPdbStructureBackgroundPanel.setBounds(420, 0, 30, 20);
        pdbStructureLayeredPane.add(contextMenuPdbStructureBackgroundPanel, javax.swing.JLayeredPane.POPUP_LAYER);

        javax.swing.GroupLayout pdbStructureJPanelLayout = new javax.swing.GroupLayout(pdbStructureJPanel);
        pdbStructureJPanel.setLayout(pdbStructureJPanelLayout);
        pdbStructureJPanelLayout.setHorizontalGroup(
            pdbStructureJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pdbStructureLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
        );
        pdbStructureJPanelLayout.setVerticalGroup(
            pdbStructureJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pdbStructureLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
        );

        proteinsJPanel.setOpaque(false);

        proteinsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Proteins"));
        proteinsPanel.setOpaque(false);

        proteinScrollPane.setOpaque(false);

        proteinTable.setModel(new ProteinTableModel());
        proteinTable.setOpaque(false);
        proteinTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        proteinTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                proteinTableMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                proteinTableMouseReleased(evt);
            }
        });
        proteinTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                proteinTableMouseMoved(evt);
            }
        });
        proteinTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                proteinTableKeyReleased(evt);
            }
        });
        proteinScrollPane.setViewportView(proteinTable);

        javax.swing.GroupLayout proteinsPanelLayout = new javax.swing.GroupLayout(proteinsPanel);
        proteinsPanel.setLayout(proteinsPanelLayout);
        proteinsPanelLayout.setHorizontalGroup(
            proteinsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 938, Short.MAX_VALUE)
            .addGroup(proteinsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(proteinsPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(proteinScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 918, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        proteinsPanelLayout.setVerticalGroup(
            proteinsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 243, Short.MAX_VALUE)
            .addGroup(proteinsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(proteinsPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(proteinScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        proteinsPanel.setBounds(0, 0, 950, 270);
        proteinsLayeredPane.add(proteinsPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        proteinsHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        proteinsHelpJButton.setToolTipText("Help");
        proteinsHelpJButton.setBorder(null);
        proteinsHelpJButton.setBorderPainted(false);
        proteinsHelpJButton.setContentAreaFilled(false);
        proteinsHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        proteinsHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                proteinsHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                proteinsHelpJButtonMouseExited(evt);
            }
        });
        proteinsHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                proteinsHelpJButtonActionPerformed(evt);
            }
        });
        proteinsHelpJButton.setBounds(930, 0, 10, 25);
        proteinsLayeredPane.add(proteinsHelpJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        exportProteinsJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        exportProteinsJButton.setToolTipText("Copy to File");
        exportProteinsJButton.setBorder(null);
        exportProteinsJButton.setBorderPainted(false);
        exportProteinsJButton.setContentAreaFilled(false);
        exportProteinsJButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        exportProteinsJButton.setEnabled(false);
        exportProteinsJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame.png"))); // NOI18N
        exportProteinsJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                exportProteinsJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                exportProteinsJButtonMouseExited(evt);
            }
        });
        exportProteinsJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportProteinsJButtonActionPerformed(evt);
            }
        });
        exportProteinsJButton.setBounds(920, 0, 10, 25);
        proteinsLayeredPane.add(exportProteinsJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        contextMenuProteinsBackgroundPanel.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout contextMenuProteinsBackgroundPanelLayout = new javax.swing.GroupLayout(contextMenuProteinsBackgroundPanel);
        contextMenuProteinsBackgroundPanel.setLayout(contextMenuProteinsBackgroundPanelLayout);
        contextMenuProteinsBackgroundPanelLayout.setHorizontalGroup(
            contextMenuProteinsBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );
        contextMenuProteinsBackgroundPanelLayout.setVerticalGroup(
            contextMenuProteinsBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        contextMenuProteinsBackgroundPanel.setBounds(920, 0, 30, 20);
        proteinsLayeredPane.add(contextMenuProteinsBackgroundPanel, javax.swing.JLayeredPane.POPUP_LAYER);

        javax.swing.GroupLayout proteinsJPanelLayout = new javax.swing.GroupLayout(proteinsJPanel);
        proteinsJPanel.setLayout(proteinsJPanelLayout);
        proteinsJPanelLayout.setHorizontalGroup(
            proteinsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(proteinsLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 957, Short.MAX_VALUE)
        );
        proteinsJPanelLayout.setVerticalGroup(
            proteinsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(proteinsLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
        );

        peptidesJPanel.setOpaque(false);

        peptidesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Peptides"));
        peptidesPanel.setOpaque(false);

        peptideScrollPane.setOpaque(false);

        peptideTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "  ", "PI", "Sequence", "Start", "PDB", ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Boolean.class, java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Boolean.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        peptideTable.setOpaque(false);
        peptideTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        peptideTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                peptideTableMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                peptideTableMouseReleased(evt);
            }
        });
        peptideTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                peptideTableMouseMoved(evt);
            }
        });
        peptideTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                peptideTableKeyReleased(evt);
            }
        });
        peptideScrollPane.setViewportView(peptideTable);

        javax.swing.GroupLayout peptidesPanelLayout = new javax.swing.GroupLayout(peptidesPanel);
        peptidesPanel.setLayout(peptidesPanelLayout);
        peptidesPanelLayout.setHorizontalGroup(
            peptidesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 468, Short.MAX_VALUE)
            .addGroup(peptidesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(peptidesPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(peptideScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        peptidesPanelLayout.setVerticalGroup(
            peptidesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 133, Short.MAX_VALUE)
            .addGroup(peptidesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(peptidesPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(peptideScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        peptidesPanel.setBounds(0, 0, 480, 160);
        peptidesLayeredPane.add(peptidesPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        peptidesHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        peptidesHelpJButton.setToolTipText("Help");
        peptidesHelpJButton.setBorder(null);
        peptidesHelpJButton.setBorderPainted(false);
        peptidesHelpJButton.setContentAreaFilled(false);
        peptidesHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        peptidesHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                peptidesHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                peptidesHelpJButtonMouseExited(evt);
            }
        });
        peptidesHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                peptidesHelpJButtonActionPerformed(evt);
            }
        });
        peptidesHelpJButton.setBounds(460, 0, 10, 25);
        peptidesLayeredPane.add(peptidesHelpJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        exportPeptidesJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        exportPeptidesJButton.setToolTipText("Copy to File");
        exportPeptidesJButton.setBorder(null);
        exportPeptidesJButton.setBorderPainted(false);
        exportPeptidesJButton.setContentAreaFilled(false);
        exportPeptidesJButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        exportPeptidesJButton.setEnabled(false);
        exportPeptidesJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame.png"))); // NOI18N
        exportPeptidesJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                exportPeptidesJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                exportPeptidesJButtonMouseExited(evt);
            }
        });
        exportPeptidesJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportPeptidesJButtonActionPerformed(evt);
            }
        });
        exportPeptidesJButton.setBounds(450, 0, 10, 25);
        peptidesLayeredPane.add(exportPeptidesJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        contextMenuPeptidesBackgroundPanel.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout contextMenuPeptidesBackgroundPanelLayout = new javax.swing.GroupLayout(contextMenuPeptidesBackgroundPanel);
        contextMenuPeptidesBackgroundPanel.setLayout(contextMenuPeptidesBackgroundPanelLayout);
        contextMenuPeptidesBackgroundPanelLayout.setHorizontalGroup(
            contextMenuPeptidesBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );
        contextMenuPeptidesBackgroundPanelLayout.setVerticalGroup(
            contextMenuPeptidesBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        contextMenuPeptidesBackgroundPanel.setBounds(440, 0, 30, 20);
        peptidesLayeredPane.add(contextMenuPeptidesBackgroundPanel, javax.swing.JLayeredPane.POPUP_LAYER);

        javax.swing.GroupLayout peptidesJPanelLayout = new javax.swing.GroupLayout(peptidesJPanel);
        peptidesJPanel.setLayout(peptidesJPanelLayout);
        peptidesJPanelLayout.setHorizontalGroup(
            peptidesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(peptidesLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE)
        );
        peptidesJPanelLayout.setVerticalGroup(
            peptidesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(peptidesLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
        );

        pdbMatchesJPanel.setOpaque(false);

        pdbMatchesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("PDB Matches"));
        pdbMatchesPanel.setOpaque(false);

        pdbJScrollPane.setOpaque(false);

        pdbMatchesJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "PDB", "Title", "Type", "Chains"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        pdbMatchesJTable.setOpaque(false);
        pdbMatchesJTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        pdbMatchesJTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pdbMatchesJTableMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                pdbMatchesJTableMouseReleased(evt);
            }
        });
        pdbMatchesJTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                pdbMatchesJTableMouseMoved(evt);
            }
        });
        pdbMatchesJTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                pdbMatchesJTableKeyReleased(evt);
            }
        });
        pdbJScrollPane.setViewportView(pdbMatchesJTable);

        javax.swing.GroupLayout pdbMatchesPanelLayout = new javax.swing.GroupLayout(pdbMatchesPanel);
        pdbMatchesPanel.setLayout(pdbMatchesPanelLayout);
        pdbMatchesPanelLayout.setHorizontalGroup(
            pdbMatchesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 468, Short.MAX_VALUE)
            .addGroup(pdbMatchesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pdbMatchesPanelLayout.createSequentialGroup()
                    .addGap(8, 8, 8)
                    .addComponent(pdbJScrollPane)
                    .addGap(8, 8, 8)))
        );
        pdbMatchesPanelLayout.setVerticalGroup(
            pdbMatchesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 123, Short.MAX_VALUE)
            .addGroup(pdbMatchesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pdbMatchesPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(pdbJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        pdbMatchesPanel.setBounds(0, 0, 480, 150);
        pdbMatchesLayeredPane.add(pdbMatchesPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        pdbMatchesHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        pdbMatchesHelpJButton.setToolTipText("Help");
        pdbMatchesHelpJButton.setBorder(null);
        pdbMatchesHelpJButton.setBorderPainted(false);
        pdbMatchesHelpJButton.setContentAreaFilled(false);
        pdbMatchesHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        pdbMatchesHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pdbMatchesHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pdbMatchesHelpJButtonMouseExited(evt);
            }
        });
        pdbMatchesHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pdbMatchesHelpJButtonActionPerformed(evt);
            }
        });
        pdbMatchesHelpJButton.setBounds(460, 0, 10, 25);
        pdbMatchesLayeredPane.add(pdbMatchesHelpJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        exportPdbMatchesJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        exportPdbMatchesJButton.setToolTipText("Copy to File");
        exportPdbMatchesJButton.setBorder(null);
        exportPdbMatchesJButton.setBorderPainted(false);
        exportPdbMatchesJButton.setContentAreaFilled(false);
        exportPdbMatchesJButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        exportPdbMatchesJButton.setEnabled(false);
        exportPdbMatchesJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame.png"))); // NOI18N
        exportPdbMatchesJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                exportPdbMatchesJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                exportPdbMatchesJButtonMouseExited(evt);
            }
        });
        exportPdbMatchesJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportPdbMatchesJButtonActionPerformed(evt);
            }
        });
        exportPdbMatchesJButton.setBounds(450, 0, 10, 25);
        pdbMatchesLayeredPane.add(exportPdbMatchesJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        contextMenuPdbMatchesBackgroundPanel.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout contextMenuPdbMatchesBackgroundPanelLayout = new javax.swing.GroupLayout(contextMenuPdbMatchesBackgroundPanel);
        contextMenuPdbMatchesBackgroundPanel.setLayout(contextMenuPdbMatchesBackgroundPanelLayout);
        contextMenuPdbMatchesBackgroundPanelLayout.setHorizontalGroup(
            contextMenuPdbMatchesBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );
        contextMenuPdbMatchesBackgroundPanelLayout.setVerticalGroup(
            contextMenuPdbMatchesBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        contextMenuPdbMatchesBackgroundPanel.setBounds(440, 0, 30, 20);
        pdbMatchesLayeredPane.add(contextMenuPdbMatchesBackgroundPanel, javax.swing.JLayeredPane.POPUP_LAYER);

        javax.swing.GroupLayout pdbMatchesJPanelLayout = new javax.swing.GroupLayout(pdbMatchesJPanel);
        pdbMatchesJPanel.setLayout(pdbMatchesJPanelLayout);
        pdbMatchesJPanelLayout.setHorizontalGroup(
            pdbMatchesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pdbMatchesLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE)
        );
        pdbMatchesJPanelLayout.setVerticalGroup(
            pdbMatchesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pdbMatchesLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
        );

        pdbChainsJPanel.setOpaque(false);

        pdbChainsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("PDB Chains"));
        pdbChainsPanel.setOpaque(false);

        pdbChainsJScrollPane.setOpaque(false);

        pdbChainsJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "Chain", "PDB-Protein", "Coverage"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Object.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        pdbChainsJTable.setOpaque(false);
        pdbChainsJTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        pdbChainsJTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                pdbChainsJTableMouseReleased(evt);
            }
        });
        pdbChainsJTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                pdbChainsJTableKeyReleased(evt);
            }
        });
        pdbChainsJScrollPane.setViewportView(pdbChainsJTable);

        javax.swing.GroupLayout pdbChainsPanelLayout = new javax.swing.GroupLayout(pdbChainsPanel);
        pdbChainsPanel.setLayout(pdbChainsPanelLayout);
        pdbChainsPanelLayout.setHorizontalGroup(
            pdbChainsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 468, Short.MAX_VALUE)
            .addGroup(pdbChainsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pdbChainsPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(pdbChainsJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        pdbChainsPanelLayout.setVerticalGroup(
            pdbChainsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 133, Short.MAX_VALUE)
            .addGroup(pdbChainsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pdbChainsPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(pdbChainsJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        pdbChainsPanel.setBounds(0, 0, 480, 160);
        pdbChainsLayeredPane.add(pdbChainsPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        pdbChainHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        pdbChainHelpJButton.setToolTipText("Help");
        pdbChainHelpJButton.setBorder(null);
        pdbChainHelpJButton.setBorderPainted(false);
        pdbChainHelpJButton.setContentAreaFilled(false);
        pdbChainHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        pdbChainHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pdbChainHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pdbChainHelpJButtonMouseExited(evt);
            }
        });
        pdbChainHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.even
