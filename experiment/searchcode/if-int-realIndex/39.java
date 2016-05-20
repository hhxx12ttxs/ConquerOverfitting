package eu.isas.peptideshaker.gui.tabpanels;

import eu.isas.peptideshaker.gui.tablemodels.ProteinTableModel;
import com.compomics.util.Util;
import com.compomics.util.examples.BareBonesBrowserLaunch;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.identification.PeptideAssumption;
import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.experiment.identification.SpectrumAnnotator;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.PeptideMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.massspectrometry.Precursor;
import com.compomics.util.gui.dialogs.ProgressDialogX;
import com.compomics.util.gui.dialogs.ProgressDialogParent;
import com.compomics.util.gui.spectrum.FragmentIonTable;
import com.compomics.util.gui.spectrum.IntensityHistogram;
import com.compomics.util.gui.spectrum.MassErrorBubblePlot;
import com.compomics.util.gui.spectrum.MassErrorPlot;
import com.compomics.util.gui.spectrum.SequenceFragmentationPanel;
import com.compomics.util.gui.spectrum.SpectrumPanel;
import eu.isas.peptideshaker.export.OutputGenerator;
import eu.isas.peptideshaker.gui.HelpDialog;
import eu.isas.peptideshaker.gui.PeptideShakerGUI;
import eu.isas.peptideshaker.gui.ProteinInferenceDialog;
import eu.isas.peptideshaker.gui.ProteinInferencePeptideLevelDialog;
import eu.isas.peptideshaker.gui.protein_sequence.ProteinSequencePanel;
import eu.isas.peptideshaker.gui.protein_sequence.ProteinSequencePanelParent;
import eu.isas.peptideshaker.gui.protein_sequence.ResidueAnnotation;
import eu.isas.peptideshaker.gui.tablemodels.PeptideTableModel;
import eu.isas.peptideshaker.gui.tablemodels.PsmTableModel;
import eu.isas.peptideshaker.myparameters.PSMaps;
import eu.isas.peptideshaker.myparameters.PSParameter;
import eu.isas.peptideshaker.myparameters.PSPtmScores;
import eu.isas.peptideshaker.preferences.AnnotationPreferences;
import eu.isas.peptideshaker.preferences.SpectrumCountingPreferences.SpectralCountingMethod;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import no.uib.jsparklines.data.JSparklinesDataSeries;
import no.uib.jsparklines.data.JSparklinesDataset;
import no.uib.jsparklines.extra.HtmlLinksRenderer;
import no.uib.jsparklines.extra.TrueFalseIconRenderer;
import no.uib.jsparklines.renderers.*;
import org.jfree.chart.*;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeListener;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.Range;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;

/**
 * The overview panel displaying the proteins, the peptides and the spectra.
 *
 * @author Harald Barsnes
 * @author Marc Vaudel
 */
public class OverviewPanel extends javax.swing.JPanel implements ProteinSequencePanelParent, ProgressDialogParent {

    /**
     * Indexes for the three main data tables.
     */
    private enum TableIndex {

        PROTEIN_TABLE, PEPTIDE_TABLE, PSM_TABLE
    };
    /**
     * A list of the panels that where visible when the spectum was maximixed.
     * The order is: protein, peptides and psms, coverage.
     */
    private ArrayList<Boolean> panelsShownUponMaximze;
    /**
     * The progress dialog.
     */
    private ProgressDialogX progressDialog;
    /**
     * The current spectrum key.
     */
    private String currentSpectrumKey = "";
    /**
     * The current sequence coverage.
     */
    private int[] coverage;
    /**
     * The current protein sequence.
     */
    private String currentProteinSequence = "";
    /**
     * The maximum sequence length for display in the sequence coverage panel.
     */
    private final int MAX_SEQUENCE_LENGTH = 6000;
    /**
     * The current spectrum panel.
     */
    private SpectrumPanel spectrumPanel;
    /**
     * boolean indicating whether the spectrum shall be displayed.
     */
    private boolean displaySpectrum = true;
    /**
     * Boolean indicating whether the sequence coverage shall be displayed.
     */
    private boolean displayCoverage = true;
    /**
     * Boolean indicating whether the protein table shall be displayed.
     */
    private boolean displayProteins = true;
    /**
     * Boolean indicating whether the PSMs shall be displayed.
     */
    private boolean displayPeptidesAndPSMs = true;
    /**
     * A list of proteins in the protein table.
     */
    private ArrayList<String> proteinKeys = new ArrayList<String>();
    /**
     * A list of the peptides in the peptide table.
     */
    private ArrayList<String> peptideKeys = new ArrayList<String>();
    /**
     * A list of PSMs in the psm table.
     */
    private ArrayList<String> psmKeys = new ArrayList<String>();
    /**
     * The main GUI.
     */
    private PeptideShakerGUI peptideShakerGUI;
    /**
     * The protein table column header tooltips.
     */
    private ArrayList<String> proteinTableToolTips;
    /**
     * The peptide table column header tooltips.
     */
    private ArrayList<String> peptideTableToolTips;
    /**
     * The PMS table column header tooltips.
     */
    private ArrayList<String> psmTableToolTips;
    /**
     * The sequence factory.
     */
    private SequenceFactory sequenceFactory = SequenceFactory.getInstance();
    /**
     * The sequence coverage chart.
     */
    private ChartPanel coverageChart;
    /**
     * The sequence ptm chart.
     */
    private ChartPanel ptmChart;
    /**
     * If true the progress bar is disposed of.
     */
    private static boolean cancelProgress = false;

    /**
     * Creates a new OverviewPanel.
     *
     * @param parent the PeptideShaker parent frame.
     */
    public OverviewPanel(PeptideShakerGUI parent) {

        this.peptideShakerGUI = parent;

        initComponents();

        // set main table properties
        proteinTable.getTableHeader().setReorderingAllowed(false);
        peptideTable.getTableHeader().setReorderingAllowed(false);
        psmTable.getTableHeader().setReorderingAllowed(false);

        proteinTable.setAutoCreateRowSorter(true);
        peptideTable.setAutoCreateRowSorter(true);
        psmTable.setAutoCreateRowSorter(true);

        // make sure that the scroll panes are see-through
        proteinScrollPane.getViewport().setOpaque(false);
        peptideScrollPane.getViewport().setOpaque(false);
        spectraScrollPane.getViewport().setOpaque(false);
        fragmentIonsJScrollPane.getViewport().setOpaque(false);

        // make the tabs in the spectrum tabbed pane go from right to left
        spectrumJTabbedPane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        // set up the table header tooltips
        setUpTableHeaderToolTips();

        updateSeparators();
        formComponentResized(null);
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
        peptideTableToolTips.add("Protein Inference Class");
        peptideTableToolTips.add("Peptide Sequence");
        peptideTableToolTips.add("Peptide Start Index");
        peptideTableToolTips.add("Number of Spectra (Validated / Non-Validated)");
        peptideTableToolTips.add("Peptide Confidence");
        peptideTableToolTips.add("Validated");

        psmTableToolTips = new ArrayList<String>();
        psmTableToolTips.add(null);
        psmTableToolTips.add("Starred");
        psmTableToolTips.add("Search Engine Agreement");
        psmTableToolTips.add("Peptide Sequence");
        psmTableToolTips.add("Precursor Charge");
        psmTableToolTips.add("Mass Error");
        psmTableToolTips.add("Peptide-Spectrum Match Confidence");
        psmTableToolTips.add("Validated");
    }

    /**
     * Set up the properties of the tables.
     */
    private void setTableProperties() {
        setProteinTableProperties();
        setPeptideTableProperties();
        setPsmTableProperties();
    }

    /**
     * Set up the properties of the protein table.
     */
    private void setProteinTableProperties() {

        // the index column
        proteinTable.getColumn(" ").setMaxWidth(50);
        proteinTable.getColumn(" ").setMinWidth(50);

        try {
            proteinTable.getColumn("Confidence").setMaxWidth(90);
            proteinTable.getColumn("Confidence").setMinWidth(90);
        } catch (IllegalArgumentException w) {
            proteinTable.getColumn("Score").setMaxWidth(90);
            proteinTable.getColumn("Score").setMinWidth(90);
        }

        // the validated column
        proteinTable.getColumn("").setMaxWidth(30);
        proteinTable.getColumn("").setMinWidth(30);

        // the selected columns
        proteinTable.getColumn("  ").setMaxWidth(30);
        proteinTable.getColumn("  ").setMinWidth(30);

        // the protein inference column
        proteinTable.getColumn("PI").setMaxWidth(37);
        proteinTable.getColumn("PI").setMinWidth(37);

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

        // the index column
        peptideTable.getColumn(" ").setMaxWidth(50);
        peptideTable.getColumn(" ").setMinWidth(50);
        peptideTable.getColumn("Start").setMinWidth(50);

        try {
            peptideTable.getColumn("Confidence").setMaxWidth(90);
            peptideTable.getColumn("Confidence").setMinWidth(90);
        } catch (IllegalArgumentException w) {
            peptideTable.getColumn("Score").setMaxWidth(90);
            peptideTable.getColumn("Score").setMinWidth(90);
        }

        // the validated column
        peptideTable.getColumn("").setMaxWidth(30);

        // the selected columns
        peptideTable.getColumn("  ").setMaxWidth(30);
        peptideTable.getColumn("  ").setMinWidth(30);

        // the protein inference column
        peptideTable.getColumn("PI").setMaxWidth(37);
        peptideTable.getColumn("PI").setMinWidth(37);

        // set up the peptide inference color map
        HashMap<Integer, Color> peptideInferenceColorMap = new HashMap<Integer, Color>();
        peptideInferenceColorMap.put(PSParameter.NOT_GROUP, peptideShakerGUI.getSparklineColor());
        peptideInferenceColorMap.put(PSParameter.ISOFORMS, Color.YELLOW);
        peptideInferenceColorMap.put(PSParameter.ISOFORMS_UNRELATED, Color.ORANGE);
        peptideInferenceColorMap.put(PSParameter.UNRELATED, Color.RED);

        // set up the peptide inference tooltip map
        HashMap<Integer, String> peptideInferenceTooltipMap = new HashMap<Integer, String>();
        peptideInferenceTooltipMap.put(PSParameter.NOT_GROUP, "Unique to a single protein");
        peptideInferenceTooltipMap.put(PSParameter.ISOFORMS, "Belongs to a group of isoforms");
        peptideInferenceTooltipMap.put(PSParameter.ISOFORMS_UNRELATED, "Belongs to a group of isoforms and unrelated proteins");
        peptideInferenceTooltipMap.put(PSParameter.UNRELATED, "Belongs to unrelated proteins");

        peptideTable.getColumn("PI").setCellRenderer(new JSparklinesIntegerColorTableCellRenderer(peptideShakerGUI.getSparklineColor(), peptideInferenceColorMap, peptideInferenceTooltipMap));
        peptideTable.getColumn("Start").setCellRenderer(new JSparklinesMultiIntervalChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100d, 100d, peptideShakerGUI.getSparklineColor()));
        peptideTable.getColumn("#Spectra").setCellRenderer(new JSparklinesTwoValueBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 10.0,
                peptideShakerGUI.getSparklineColor(), peptideShakerGUI.getSparklineColorNonValidated(), false));
        ((JSparklinesTwoValueBarChartTableCellRenderer) peptideTable.getColumn("#Spectra").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth(), new DecimalFormat("0"));
        peptideTable.getColumn("").setCellRenderer(new TrueFalseIconRenderer(
                new ImageIcon(this.getClass().getResource("/icons/accept.png")),
                new ImageIcon(this.getClass().getResource("/icons/Error_3.png")),
                "Validated", "Not Validated"));
        peptideTable.getColumn("  ").setCellRenderer(new TrueFalseIconRenderer(
                new ImageIcon(this.getClass().getResource("/icons/star_yellow.png")),
                new ImageIcon(this.getClass().getResource("/icons/star_grey.png")),
                new ImageIcon(this.getClass().getResource("/icons/star_grey.png")),
                "Starred", null, null));

        try {
            peptideTable.getColumn("Confidence").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100.0, peptideShakerGUI.getSparklineColor()));
            ((JSparklinesBarChartTableCellRenderer) peptideTable.getColumn("Confidence").getCellRenderer()).showNumberAndChart(
                    true, peptideShakerGUI.getLabelWidth() - 20, peptideShakerGUI.getScoreAndConfidenceDecimalFormat());
        } catch (IllegalArgumentException e) {
            peptideTable.getColumn("Score").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100.0, peptideShakerGUI.getSparklineColor()));
            ((JSparklinesBarChartTableCellRenderer) peptideTable.getColumn("Score").getCellRenderer()).showNumberAndChart(
                    true, peptideShakerGUI.getLabelWidth() - 20, peptideShakerGUI.getScoreAndConfidenceDecimalFormat());
        }
    }

    /**
     * Set up the properties of the PSM table.
     */
    private void setPsmTableProperties() {

        // the index column
        psmTable.getColumn(" ").setMaxWidth(50);
        psmTable.getColumn(" ").setMinWidth(50);

        try {
            psmTable.getColumn("Confidence").setMaxWidth(90);
            psmTable.getColumn("Confidence").setMinWidth(90);
        } catch (IllegalArgumentException w) {
            psmTable.getColumn("Score").setMaxWidth(90);
            psmTable.getColumn("Score").setMinWidth(90);
        }

        // the validated column
        psmTable.getColumn("").setMaxWidth(30);
        psmTable.getColumn("").setMinWidth(30);

        // the selected columns
        psmTable.getColumn("  ").setMaxWidth(30);
        psmTable.getColumn("  ").setMinWidth(30);

        // the protein inference column
        psmTable.getColumn("SE").setMaxWidth(37);
        psmTable.getColumn("SE").setMinWidth(37);

        // set up the psm color map
        HashMap<Integer, Color> psmColorMap = new HashMap<Integer, Color>();
        psmColorMap.put(SpectrumIdentificationPanel.AGREEMENT, peptideShakerGUI.getSparklineColor()); // search engines agree
        psmColorMap.put(SpectrumIdentificationPanel.CONFLICT, Color.YELLOW); // search engines don't agree
        psmColorMap.put(SpectrumIdentificationPanel.PARTIALLY_MISSING, java.awt.Color.ORANGE); // some search engines id'ed some didn't

        // set up the psm tooltip map
        HashMap<Integer, String> psmTooltipMap = new HashMap<Integer, String>();
        psmTooltipMap.put(SpectrumIdentificationPanel.AGREEMENT, "Search Engines Agree");
        psmTooltipMap.put(SpectrumIdentificationPanel.CONFLICT, "Search Engines Disagree");
        psmTooltipMap.put(SpectrumIdentificationPanel.PARTIALLY_MISSING, "First Hit(s) Missing");

        psmTable.getColumn("SE").setCellRenderer(new JSparklinesIntegerColorTableCellRenderer(Color.lightGray, psmColorMap, psmTooltipMap));
        psmTable.getColumn("Mass Error").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL,
                peptideShakerGUI.getSearchParameters().getPrecursorAccuracy(), peptideShakerGUI.getSparklineColor()));
        ((JSparklinesBarChartTableCellRenderer) psmTable.getColumn("Mass Error").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth());
        psmTable.getColumn("Charge").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL,
                10.0, peptideShakerGUI.getSparklineColor()));
        ((JSparklinesBarChartTableCellRenderer) psmTable.getColumn("Charge").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth() - 30);
        psmTable.getColumn("").setCellRenderer(new TrueFalseIconRenderer(
                new ImageIcon(this.getClass().getResource("/icons/accept.png")),
                new ImageIcon(this.getClass().getResource("/icons/Error_3.png")),
                "Validated", "Not Validated"));
        psmTable.getColumn("  ").setCellRenderer(new TrueFalseIconRenderer(
                new ImageIcon(this.getClass().getResource("/icons/star_yellow.png")),
                new ImageIcon(this.getClass().getResource("/icons/star_grey.png")),
                new ImageIcon(this.getClass().getResource("/icons/star_grey.png")),
                "Starred", null, null));

        ((JSparklinesBarChartTableCellRenderer) psmTable.getColumn("Mass Error").getCellRenderer()).setMaxValue(
                peptideShakerGUI.getSearchParameters().getPrecursorAccuracy());
        ((JSparklinesBarChartTableCellRenderer) psmTable.getColumn("Charge").getCellRenderer()).setMaxValue(
                (double) ((PSMaps) peptideShakerGUI.getIdentification().getUrParam(new PSMaps())).getPsmSpecificMap().getMaxCharge());

        try {
            psmTable.getColumn("Confidence").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100.0, peptideShakerGUI.getSparklineColor()));
            ((JSparklinesBarChartTableCellRenderer) psmTable.getColumn("Confidence").getCellRenderer()).showNumberAndChart(
                    true, peptideShakerGUI.getLabelWidth() - 20, peptideShakerGUI.getScoreAndConfidenceDecimalFormat());
        } catch (IllegalArgumentException e) {
            psmTable.getColumn("Score").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100.0, peptideShakerGUI.getSparklineColor()));
            ((JSparklinesBarChartTableCellRenderer) psmTable.getColumn("Score").getCellRenderer()).showNumberAndChart(
                    true, peptideShakerGUI.getLabelWidth() - 20, peptideShakerGUI.getScoreAndConfidenceDecimalFormat());
        }

        if (peptideShakerGUI.getSearchParameters().isPrecursorAccuracyTypePpm()) {
            psmTableToolTips.set(3, "Mass Error (ppm)");
        } else {
            psmTableToolTips.set(3, "Mass Error (Da)");
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        selectJPopupMenu = new javax.swing.JPopupMenu();
        selectAllMenuItem = new javax.swing.JMenuItem();
        deselectAllMenuItem = new javax.swing.JMenuItem();
        backgroundLayeredPane = new javax.swing.JLayeredPane();
        overviewJPanel = new javax.swing.JPanel();
        overviewJSplitPane = new javax.swing.JSplitPane();
        proteinsJPanel = new javax.swing.JPanel();
        proteinsLayeredPane = new javax.swing.JLayeredPane();
        proteinsLayeredPanel = new javax.swing.JPanel();
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
        hideProteinsJButton = new javax.swing.JButton();
        contextMenuProteinsBackgroundPanel = new javax.swing.JPanel();
        coverageJSplitPane = new javax.swing.JSplitPane();
        sequenceCoverageJPanel = new javax.swing.JPanel();
        sequenceCoverageLayeredPane = new javax.swing.JLayeredPane();
        sequenceCoverageTitledPanel = new javax.swing.JPanel();
        sequenceCoverageInnerPanel = new javax.swing.JPanel();
        sequencePtmsPanel = new javax.swing.JPanel();
        sequenceCoveragetHelpJButton = new javax.swing.JButton();
        exportSequenceCoverageContextJButton = new javax.swing.JButton();
        hideCoverageJButton = new javax.swing.JButton();
        contextMenuSequenceCoverageBackgroundPanel = new javax.swing.JPanel();
        peptidesPsmSpectrumFragmentIonsJSplitPane = new javax.swing.JSplitPane();
        peptidesPsmJSplitPane = new javax.swing.JSplitPane();
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
        hidePeptideAndPsmsJButton = new javax.swing.JButton();
        contextMenuPeptidesBackgroundPanel = new javax.swing.JPanel();
        psmJPanel = new javax.swing.JPanel();
        psmsLayeredPane = new javax.swing.JLayeredPane();
        psmsPanel = new javax.swing.JPanel();
        spectraScrollPane = new javax.swing.JScrollPane();
        psmTable = new JTable() {
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        String tip = (String) psmTableToolTips.get(realIndex);
                        return tip;
                    }
                };
            }
        };
        psmsHelpJButton = new javax.swing.JButton();
        exportPsmsJButton = new javax.swing.JButton();
        hidePeptideAndPsmsJButton2 = new javax.swing.JButton();
        contextMenuPsmsBackgroundPanel = new javax.swing.JPanel();
        spectrumMainJPanel = new javax.swing.JPanel();
        spectrumLayeredPane = new javax.swing.JLayeredPane();
        spectrumMainPanel = new javax.swing.JPanel();
        slidersSplitPane = new javax.swing.JSplitPane();
        spectrumJTabbedPane = new javax.swing.JTabbedPane();
        fragmentIonJPanel = new javax.swing.JPanel();
        fragmentIonsJScrollPane = new javax.swing.JScrollPane();
        ionTableJToolBar = new javax.swing.JToolBar();
        ionTableAnnotationMenuPanel = new javax.swing.JPanel();
        bubblePlotTabJPanel = new javax.swing.JPanel();
        bubbleJPanel = new javax.swing.JPanel();
        bubblePlotJToolBar = new javax.swing.JToolBar();
        bubbleAnnotationMenuPanel = new javax.swing.JPanel();
        spectrumContainerJPanel = new javax.swing.JPanel();
        spectrumJToolBar = new javax.swing.JToolBar();
        spectrumAnnotationMenuPanel = new javax.swing.JPanel();
        spectrumSplitPane = new javax.swing.JSplitPane();
        secondarySpectrumPlotsJPanel = new javax.swing.JPanel();
        spectrumJPanel = new javax.swing.JPanel();
        slidersPanel = new javax.swing.JPanel();
        accuracySlider = new javax.swing.JSlider();
        intensitySlider = new javax.swing.JSlider();
        spectrumHelpJButton = new javax.swing.JButton();
        exportSpectrumJButton = new javax.swing.JButton();
        hideSpectrumPanelJButton = new javax.swing.JButton();
        maximizeSpectrumPanelJButton = new javax.swing.JButton();
        contextMenuSpectrumBackgroundPanel = new javax.swing.JPanel();
        toolBar = new javax.swing.JToolBar();
        showProteinsBeforeSeparator = new javax.swing.JPopupMenu.Separator();
        showProteinsJButton = new javax.swing.JButton();
        showProteinsAfterSeparator = new javax.swing.JPopupMenu.Separator();
        showPeptidesAndPsmsJButton = new javax.swing.JButton();
        showPeptidesAfterSeparator = new javax.swing.JPopupMenu.Separator();
        showSpectrumJButton = new javax.swing.JButton();
        showSpectrumAfterSeparator = new javax.swing.JPopupMenu.Separator();
        showCoverageJButton = new javax.swing.JButton();
        showCoverageAfterSeparator = new javax.swing.JPopupMenu.Separator();

        selectAllMenuItem.setText("Select All");
        selectAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllMenuItemActionPerformed(evt);
            }
        });
        selectJPopupMenu.add(selectAllMenuItem);

        deselectAllMenuItem.setText("Deselect All");
        deselectAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deselectAllMenuItemActionPerformed(evt);
            }
        });
        selectJPopupMenu.add(deselectAllMenuItem);

        setBackground(new java.awt.Color(255, 255, 255));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        overviewJPanel.setBackground(new java.awt.Color(255, 255, 255));
        overviewJPanel.setOpaque(false);
        overviewJPanel.setPreferredSize(new java.awt.Dimension(900, 800));

        overviewJSplitPane.setBorder(null);
        overviewJSplitPane.setDividerLocation(300);
        overviewJSplitPane.setDividerSize(0);
        overviewJSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        overviewJSplitPane.setResizeWeight(0.5);
        overviewJSplitPane.setOpaque(false);

        proteinsJPanel.setOpaque(false);

        proteinsLayeredPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Proteins"));
        proteinsLayeredPanel.setOpaque(false);

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
        proteinTable.getAccessibleContext().setAccessibleName("ProteinTable");

        javax.swing.GroupLayout proteinsLayeredPanelLayout = new javax.swing.GroupLayout(proteinsLayeredPanel);
        proteinsLayeredPanel.setLayout(proteinsLayeredPanelLayout);
        proteinsLayeredPanelLayout.setHorizontalGroup(
            proteinsLayeredPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 938, Short.MAX_VALUE)
            .addGroup(proteinsLayeredPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(proteinsLayeredPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(proteinScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 918, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        proteinsLayeredPanelLayout.setVerticalGroup(
            proteinsLayeredPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 273, Short.MAX_VALUE)
            .addGroup(proteinsLayeredPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(proteinsLayeredPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(proteinScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        proteinsLayeredPanel.setBounds(0, 0, 950, 300);
        proteinsLayeredPane.add(proteinsLayeredPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

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

        hideProteinsJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/hide_grey.png"))); // NOI18N
        hideProteinsJButton.setToolTipText("Hide Proteins (Shift+Ctrl+P)");
        hideProteinsJButton.setBorder(null);
        hideProteinsJButton.setBorderPainted(false);
        hideProteinsJButton.setContentAreaFilled(false);
        hideProteinsJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/hide.png"))); // NOI18N
        hideProteinsJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                hideProteinsJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                hideProteinsJButtonMouseExited(evt);
            }
        });
        hideProteinsJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hideProteinsJButtonActionPerformed(evt);
            }
        });
        hideProteinsJButton.setBounds(910, 0, 10, 25);
        proteinsLayeredPane.add(hideProteinsJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        contextMenuProteinsBackgroundPanel.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout contextMenuProteinsBackgroundPanelLayout = new javax.swing.GroupLayout(contextMenuProteinsBackgroundPanel);
        contextMenuProteinsBackgroundPanel.setLayout(contextMenuProteinsBackgroundPanelLayout);
        contextMenuProteinsBackgroundPanelLayout.setHorizontalGroup(
            contextMenuProteinsBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );
        contextMenuProteinsBackgroundPanelLayout.setVerticalGroup(
            contextMenuProteinsBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        contextMenuProteinsBackgroundPanel.setBounds(910, 0, 40, 20);
        proteinsLayeredPane.add(contextMenuProteinsBackgroundPanel, javax.swing.JLayeredPane.POPUP_LAYER);

        javax.swing.GroupLayout proteinsJPanelLayout = new javax.swing.GroupLayout(proteinsJPanel);
        proteinsJPanel.setLayout(proteinsJPanelLayout);
        proteinsJPanelLayout.setHorizontalGroup(
            proteinsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(proteinsLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 960, Short.MAX_VALUE)
        );
        proteinsJPanelLayout.setVerticalGroup(
            proteinsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(proteinsLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );

        overviewJSplitPane.setTopComponent(proteinsJPanel);

        coverageJSplitPane.setBorder(null);
        coverageJSplitPane.setDividerLocation(350);
        coverageJSplitPane.setDividerSize(0);
        coverageJSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        coverageJSplitPane.setResizeWeight(1.0);
        coverageJSplitPane.setOpaque(false);

        sequenceCoverageJPanel.setOpaque(false);

        sequenceCoverageTitledPanel.setBackground(new java.awt.Color(255, 255, 255));
        sequenceCoverageTitledPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Protein Sequence Coverage"));
        sequenceCoverageTitledPanel.setOpaque(false);

        sequenceCoverageInnerPanel.setBackground(new java.awt.Color(255, 255, 255));
        sequenceCoverageInnerPanel.setLayout(new javax.swing.BoxLayout(sequenceCoverageInnerPanel, javax.swing.BoxLayout.LINE_AXIS));

        sequencePtmsPanel.setBackground(new java.awt.Color(255, 255, 255));
        sequencePtmsPanel.setOpaque(false);
        sequencePtmsPanel.setLayout(new javax.swing.BoxLayout(sequencePtmsPanel, javax.swing.BoxLayout.LINE_AXIS));

        javax.swing.GroupLayout sequenceCoverageTitledPanelLayout = new javax.swing.GroupLayout(sequenceCoverageTitledPanel);
        sequenceCoverageTitledPanel.setLayout(sequenceCoverageTitledPanelLayout);
        sequenceCoverageTitledPanelLayout.setHorizontalGroup(
            sequenceCoverageTitledPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sequenceCoverageTitledPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sequenceCoverageTitledPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sequenceCoverageInnerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 918, Short.MAX_VALUE)
                    .addComponent(sequencePtmsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 918, Short.MAX_VALUE))
                .addContainerGap())
        );
        sequenceCoverageTitledPanelLayout.setVerticalGroup(
            sequenceCoverageTitledPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sequenceCoverageTitledPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sequenceCoverageInnerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(sequencePtmsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 7, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        sequenceCoverageTitledPanel.setBounds(0, 0, 950, 70);
        sequenceCoverageLayeredPane.add(sequenceCoverageTitledPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        sequenceCoveragetHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        sequenceCoveragetHelpJButton.setToolTipText("Help");
        sequenceCoveragetHelpJButton.setBorder(null);
        sequenceCoveragetHelpJButton.setBorderPainted(false);
        sequenceCoveragetHelpJButton.setContentAreaFilled(false);
        sequenceCoveragetHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        sequenceCoveragetHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                sequenceCoveragetHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                sequenceCoveragetHelpJButtonMouseExited(evt);
            }
        });
        sequenceCoveragetHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sequenceCoveragetHelpJButtonActionPerformed(evt);
            }
        });
        sequenceCoveragetHelpJButton.setBounds(930, 0, 10, 25);
        sequenceCoverageLayeredPane.add(sequenceCoveragetHelpJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        exportSequenceCoverageContextJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        exportSequenceCoverageContextJButton.setToolTipText("Copy to Clipboard");
        exportSequenceCoverageContextJButton.setBorder(null);
        exportSequenceCoverageContextJButton.setBorderPainted(false);
        exportSequenceCoverageContextJButton.setContentAreaFilled(false);
        exportSequenceCoverageContextJButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        exportSequenceCoverageContextJButton.setEnabled(false);
        exportSequenceCoverageContextJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame.png"))); // NOI18N
        exportSequenceCoverageContextJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                exportSequenceCoverageContextJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                exportSequenceCoverageContextJButtonMouseExited(evt);
            }
        });
        exportSequenceCoverageContextJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportSequenceCoverageContextJButtonActionPerformed(evt);
            }
        });
        exportSequenceCoverageContextJButton.setBounds(920, 0, 10, 25);
        sequenceCoverageLayeredPane.add(exportSequenceCoverageContextJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        hideCoverageJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/hide_grey.png"))); // NOI18N
        hideCoverageJButton.setToolTipText("Hide Coverage (Shift+Ctrl+E)");
        hideCoverageJButton.setBorder(null);
        hideCoverageJButton.setBorderPainted(false);
        hideCoverageJButton.setContentAreaFilled(false);
        hideCoverageJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/hide.png"))); // NOI18N
        hideCoverageJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                hideCoverageJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                hideCoverageJButtonMouseExited(evt);
            }
        });
        hideCoverageJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hideCoverageJButtonActionPerformed(evt);
            }
        });
        hideCoverageJButton.setBounds(910, 0, 10, 25);
        sequenceCoverageLayeredPane.add(hideCoverageJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        contextMenuSequenceCoverageBackgroundPanel.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout contextMenuSequenceCoverageBackgroundPanelLayout = new javax.swing.GroupLayout(contextMenuSequenceCoverageBackgroundPanel);
        contextMenuSequenceCoverageBackgroundPanel.setLayout(contextMenuSequenceCoverageBackgroundPanelLayout);
        contextMenuSequenceCoverageBackgroundPanelLayout.setHorizontalGroup(
            contextMenuSequenceCoverageBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );
        contextMenuSequenceCoverageBackgroundPanelLayout.setVerticalGroup(
            contextMenuSequenceCoverageBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        contextMenuSequenceCoverageBackgroundPanel.setBounds(900, 0, 40, 20);
        sequenceCoverageLayeredPane.add(contextMenuSequenceCoverageBackgroundPanel, javax.swing.JLayeredPane.POPUP_LAYER);

        javax.swing.GroupLayout sequenceCoverageJPanelLayout = new javax.swing.GroupLayout(sequenceCoverageJPanel);
        sequenceCoverageJPanel.setLayout(sequenceCoverageJPanelLayout);
        sequenceCoverageJPanelLayout.setHorizontalGroup(
            sequenceCoverageJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sequenceCoverageLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 960, Short.MAX_VALUE)
        );
        sequenceCoverageJPanelLayout.setVerticalGroup(
            sequenceCoverageJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sequenceCoverageLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)
        );

        coverageJSplitPane.setRightComponent(sequenceCoverageJPanel);

        peptidesPsmSpectrumFragmentIonsJSplitPane.setBorder(null);
        peptidesPsmSpectrumFragmentIonsJSplitPane.setDividerLocation(450);
        peptidesPsmSpectrumFragmentIonsJSplitPane.setDividerSize(0);
        peptidesPsmSpectrumFragmentIonsJSplitPane.setResizeWeight(0.5);
        peptidesPsmSpectrumFragmentIonsJSplitPane.setOpaque(false);

        peptidesPsmJSplitPane.setBorder(null);
        peptidesPsmJSplitPane.setDividerLocation(175);
        peptidesPsmJSplitPane.setDividerSize(0);
        peptidesPsmJSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        peptidesPsmJSplitPane.setResizeWeight(0.5);
        peptidesPsmJSplitPane.setOpaque(false);

        peptidesJPanel.setOpaque(false);

        peptidesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Peptides"));
        peptidesPanel.setOpaque(false);

        peptideScrollPane.setOpaque(false);

        peptideTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "  ", "PI", "Sequence", "Start", "#Spectra", "Confidence", ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Boolean.class, java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Double.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, false, false, false, false, false, false
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
            .addGap(0, 438, Short.MAX_VALUE)
            .addGroup(peptidesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(peptidesPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(peptideScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        peptidesPanelLayout.setVerticalGroup(
            peptidesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 143, Short.MAX_VALUE)
            .addGroup(peptidesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(peptidesPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(peptideScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        peptidesPanel.setBounds(0, 0, 450, 170);
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
        peptidesHelpJButton.setBounds(430, 0, 10, 25);
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
        exportPeptidesJButton.setBounds(420, 0, 10, 25);
        peptidesLayeredPane.add(exportPeptidesJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        hidePeptideAndPsmsJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/hide_grey.png"))); // NOI18N
        hidePeptideAndPsmsJButton.setToolTipText("Hide Peptides & PSMs (Shift+Ctrl+E)");
        hidePeptideAndPsmsJButton.setBorder(null);
        hidePeptideAndPsmsJButton.setBorderPainted(false);
        hidePeptideAndPsmsJButton.setContentAreaFilled(false);
        hidePeptideAndPsmsJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/hide.png"))); // NOI18N
        hidePeptideAndPsmsJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                hidePeptideAndPsmsJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                hidePeptideAndPsmsJButtonMouseExited(evt);
            }
        });
        hidePeptideAndPsmsJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hidePeptideAndPsmsJButtonActionPerformed(evt);
            }
        });
        hidePeptideAndPsmsJButton.setBounds(410, 0, 10, 25);
        peptidesLayeredPane.add(hidePeptideAndPsmsJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        contextMenuPeptidesBackgroundPanel.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout contextMenuPeptidesBackgroundPanelLayout = new javax.swing.GroupLayout(contextMenuPeptidesBackgroundPanel);
        contextMenuPeptidesBackgroundPanel.setLayout(contextMenuPeptidesBackgroundPanelLayout);
        contextMenuPeptidesBackgroundPanelLayout.setHorizontalGroup(
            contextMenuPeptidesBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );
        contextMenuPeptidesBackgroundPanelLayout.setVerticalGroup(
            contextMenuPeptidesBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        contextMenuPeptidesBackgroundPanel.setBounds(400, 0, 40, 20);
        peptidesLayeredPane.add(contextMenuPeptidesBackgroundPanel, javax.swing.JLayeredPane.POPUP_LAYER);

        javax.swing.GroupLayout peptidesJPanelLayout = new javax.swing.GroupLayout(peptidesJPanel);
        peptidesJPanel.setLayout(peptidesJPanelLayout);
        peptidesJPanelLayout.setHorizontalGroup(
            peptidesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(peptidesLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
        );
        peptidesJPanelLayout.setVerticalGroup(
            peptidesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(peptidesLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)
        );

        peptidesPsmJSplitPane.setTopComponent(peptidesJPanel);

        psmJPanel.setOpaque(false);

        psmsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Peptide-Spectrum Matches"));
        psmsPanel.setOpaque(false);

        spectraScrollPane.setOpaque(false);

        psmTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "  ", "SE", "Sequence", "Charge", "Mass Error", "Confidence", ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Boolean.class, java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Double.class, java.lang.Double.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        psmTable.setOpaque(false);
        psmTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                psmTableMouseReleased(evt);
            }
        });
        psmTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                psmTableMouseMoved(evt);
            }
        });
        psmTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                psmTableKeyReleased(evt);
            }
        });
        spectraScrollPane.setViewportView(psmTable);

        javax.swing.GroupLayout psmsPanelLayout = new javax.swing.GroupLayout(psmsPanel);
        psmsPanel.setLayout(psmsPanelLayout);
        psmsPanelLayout.setHorizontalGroup(
            psmsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 438, Short.MAX_VALUE)
            .addGroup(psmsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(psmsPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(spectraScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        psmsPanelLayout.setVerticalGroup(
            psmsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 143, Short.MAX_VALUE)
    
