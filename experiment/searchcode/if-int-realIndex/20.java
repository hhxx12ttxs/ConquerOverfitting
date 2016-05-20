package eu.isas.peptideshaker.gui.tabpanels;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.Identification;
import com.compomics.util.experiment.identification.PeptideAssumption;
import com.compomics.util.experiment.identification.SpectrumAnnotator;
import com.compomics.util.experiment.identification.advocates.SearchEngine;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.io.identifications.IdfileReaderFactory;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Precursor;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import com.compomics.util.gui.dialogs.ProgressDialogParent;
import com.compomics.util.gui.dialogs.ProgressDialogX;
import com.compomics.util.gui.renderers.AlignedListCellRenderer;
import com.compomics.util.gui.spectrum.SpectrumPanel;
import com.googlecode.charts4j.Color;
import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.VennDiagram;
import eu.isas.peptideshaker.export.OutputGenerator;
import eu.isas.peptideshaker.gui.ExportGraphicsDialog;
import eu.isas.peptideshaker.gui.HelpDialog;
import eu.isas.peptideshaker.gui.PeptideShakerGUI;
import eu.isas.peptideshaker.myparameters.PSMaps;
import eu.isas.peptideshaker.myparameters.PSParameter;
import eu.isas.peptideshaker.preferences.AnnotationPreferences;
import eu.isas.peptideshaker.utils.IdentificationFeaturesGenerator;
import java.awt.Component;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import no.uib.jsparklines.extra.HtmlLinksRenderer;
import no.uib.jsparklines.extra.TrueFalseIconRenderer;
import no.uib.jsparklines.renderers.JSparklinesBarChartTableCellRenderer;
import no.uib.jsparklines.renderers.JSparklinesIntegerColorTableCellRenderer;
import no.uib.jsparklines.renderers.JSparklinesIntervalChartTableCellRenderer;
import org.jfree.chart.plot.PlotOrientation;

/**
 * The Spectrum ID panel.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class SpectrumIdentificationPanel extends javax.swing.JPanel implements ProgressDialogParent {

    /**
     * The progress dialog.
     */
    private ProgressDialogX progressDialog;
    /**
     * If true the progress bar is disposed of.
     */
    private static boolean cancelProgress = false;
    /**
     * Needed in order to not update the file selection too many times.
     */
    private boolean updateSelection = true;

    /**
     * Indexes for the three main data tables.
     */
    private enum TableIndex {

        SEARCH_ENGINE_PERFORMANCE, SPECTRUM_FILES, PSM_TABLES
    };
    /**
     * Static index for the search engine agreement: no psm found.
     */
    public static final int NO_ID = 0;
    /**
     * Static index for the search engine agreement: the search engines have
     * different top ranking peptides.
     */
    public static final int CONFLICT = 1;
    /**
     * Static index for the search engine agreement: one or more of the search
     * engines did not identifie the spectrum, while one or more of the others
     * did.
     */
    public static final int PARTIALLY_MISSING = 2;
    /**
     * Static index for the search engine agreement: the search engines all have
     * the same top ranking peptide.
     */
    public static final int AGREEMENT = 3;
    /**
     * The peptide sequence tooltips for the OMSSA table.
     */
    private HashMap<Integer, String> omssaTablePeptideTooltips = null;
    /**
     * The peptide sequence tooltips for the XTandem table.
     */
    private HashMap<Integer, String> xTandemTablePeptideTooltips = null;
    /**
     * The peptide sequence tooltips for the Mascot table.
     */
    private HashMap<Integer, String> mascotTablePeptideTooltips = null;
    /**
     * The peptide sequence tooltips for the OMSSA table.
     */
    private String peptideShakerJTablePeptideTooltip = null;
    /**
     * The current spectrum key.
     */
    private String currentSpectrumKey = "";
    /**
     * The search engine table column header tooltips.
     */
    private ArrayList<String> searchEngineTableToolTips;
    /**
     * The spectrum table column header tooltips.
     */
    private ArrayList<String> spectrumTableToolTips;
    /**
     * The peptide shaker table column header tooltips.
     */
    private ArrayList<String> peptideShakerTableToolTips;
    /**
     * The OMSSA table column header tooltips.
     */
    private ArrayList<String> omssaTableToolTips;
    /**
     * The X!Tandem table column header tooltips.
     */
    private ArrayList<String> xTandemTableToolTips;
    /**
     * The Mascot table column header tooltips.
     */
    private ArrayList<String> mascotTableToolTips;
    /**
     * The spectrum annotator for search engine specific results
     */
    private SpectrumAnnotator specificAnnotator = new SpectrumAnnotator();
    /**
     * The list of OMSSA peptide keys.
     */
    private HashMap<Integer, String> omssaPeptideKeys = new HashMap<Integer, String>();
    /**
     * The list of X!Tandem peptide keys.
     */
    private HashMap<Integer, String> xtandemPeptideKeys = new HashMap<Integer, String>();
    /**
     * The list of Mascot peptide keys.
     */
    private HashMap<Integer, String> mascotPeptideKeys = new HashMap<Integer, String>();
    /**
     * The main GUI
     */
    private PeptideShakerGUI peptideShakerGUI;
    /**
     * The identification
     */
    private Identification identification;
    /**
     * The file currently selected
     */
    private String fileSelected = null;
    /**
     * The spectrum factory
     */
    private SpectrumFactory spectrumFactory = SpectrumFactory.getInstance();
    /**
     * Shows if OMSSA is used as part of the search.
     */
    private static boolean omssaUsed = false;
    /**
     * Shows if X!Tandem is used as part of the search.
     */
    private static boolean xtandemUsed = false;
    /**
     * Shows if Mascot is used as part of the search.
     */
    private static boolean mascotUsed = false;

    /**
     * Create a new SpectrumIdentificationPanel.
     *
     * @param peptideShakerGUI the PeptideShaker parent frame
     */
    public SpectrumIdentificationPanel(PeptideShakerGUI peptideShakerGUI) {
        this.peptideShakerGUI = peptideShakerGUI;
        initComponents();
        formComponentResized(null);

        searchEnginetableJScrollPane.getViewport().setOpaque(false);
        spectrumTableJScrollPane.getViewport().setOpaque(false);
        peptideShakerJScrollPane.getViewport().setOpaque(false);
        xTandemTableJScrollPane.getViewport().setOpaque(false);
        mascotTableJScrollPane.getViewport().setOpaque(false);
        omssaTableJScrollPane.getViewport().setOpaque(false);

        fileNamesCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));

        setTableProperties();
    }

    /**
     * Set up the properties of the tables.
     */
    private void setTableProperties() {

        peptideShakerJTable.getColumn("  ").setCellRenderer(new TrueFalseIconRenderer(
                new ImageIcon(this.getClass().getResource("/icons/accept.png")),
                new ImageIcon(this.getClass().getResource("/icons/Error_3.png")),
                "Validated", "Not Validated"));

        searchEngineTable.getTableHeader().setReorderingAllowed(false);
        peptideShakerJTable.getTableHeader().setReorderingAllowed(false);
        spectrumTable.getTableHeader().setReorderingAllowed(false);
        omssaTable.getTableHeader().setReorderingAllowed(false);
        mascotTable.getTableHeader().setReorderingAllowed(false);
        xTandemTable.getTableHeader().setReorderingAllowed(false);

        //spectrumTable.setAutoCreateRowSorter(true); // @TODO: perhaps this should be enabled later
        searchEngineTable.setAutoCreateRowSorter(true);

        peptideShakerJTable.getColumn(" ").setMinWidth(30);
        peptideShakerJTable.getColumn(" ").setMaxWidth(30);
        peptideShakerJTable.getColumn("  ").setMinWidth(30);
        peptideShakerJTable.getColumn("  ").setMaxWidth(30);
        searchEngineTable.getColumn(" ").setMinWidth(30);
        searchEngineTable.getColumn(" ").setMaxWidth(30);
        spectrumTable.getColumn(" ").setMinWidth(50);
        spectrumTable.getColumn(" ").setMaxWidth(50);

        omssaTable.getColumn(" ").setMinWidth(30);
        omssaTable.getColumn(" ").setMaxWidth(30);
        mascotTable.getColumn(" ").setMinWidth(30);
        mascotTable.getColumn(" ").setMaxWidth(30);
        xTandemTable.getColumn(" ").setMinWidth(30);
        xTandemTable.getColumn(" ").setMaxWidth(30);

        peptideShakerJTable.getColumn("SE").setMaxWidth(37);
        peptideShakerJTable.getColumn("SE").setMinWidth(37);
        spectrumTable.getColumn("SE").setMaxWidth(37);
        spectrumTable.getColumn("SE").setMinWidth(37);

        peptideShakerJTable.getColumn("Confidence").setMaxWidth(90);
        peptideShakerJTable.getColumn("Confidence").setMinWidth(90);
        peptideShakerJTable.getColumn("Score").setMaxWidth(90);
        peptideShakerJTable.getColumn("Score").setMinWidth(90);
        omssaTable.getColumn("Confidence").setMaxWidth(90);
        omssaTable.getColumn("Confidence").setMinWidth(90);
        mascotTable.getColumn("Confidence").setMaxWidth(90);
        mascotTable.getColumn("Confidence").setMinWidth(90);
        xTandemTable.getColumn("Confidence").setMaxWidth(90);
        xTandemTable.getColumn("Confidence").setMinWidth(90);

        // set up the psm color map
        HashMap<Integer, java.awt.Color> searchEngineColorMap = new HashMap<Integer, java.awt.Color>();
        searchEngineColorMap.put(AGREEMENT, peptideShakerGUI.getSparklineColor()); // search engines agree
        searchEngineColorMap.put(CONFLICT, java.awt.Color.YELLOW); // search engines don't agree
        searchEngineColorMap.put(PARTIALLY_MISSING, java.awt.Color.ORANGE); // some search engines id'ed some didn't

        // set up the psm tooltip map
        HashMap<Integer, String> searchEngineTooltipMap = new HashMap<Integer, String>();
        searchEngineTooltipMap.put(AGREEMENT, "Search Engines Agree");
        searchEngineTooltipMap.put(CONFLICT, "Search Engines Disagree");
        searchEngineTooltipMap.put(PARTIALLY_MISSING, "First Hit(s) Missing");

        peptideShakerJTable.getColumn("SE").setCellRenderer(new JSparklinesIntegerColorTableCellRenderer(java.awt.Color.lightGray, searchEngineColorMap, searchEngineTooltipMap));
        peptideShakerJTable.getColumn("Protein(s)").setCellRenderer(new HtmlLinksRenderer(peptideShakerGUI.getSelectedRowHtmlTagFontColor(), peptideShakerGUI.getNotSelectedRowHtmlTagFontColor()));
        peptideShakerJTable.getColumn("Score").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100d, peptideShakerGUI.getSparklineColor()));
        peptideShakerJTable.getColumn("Confidence").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100d, peptideShakerGUI.getSparklineColor()));
        ((JSparklinesBarChartTableCellRenderer) peptideShakerJTable.getColumn("Score").getCellRenderer()).showNumberAndChart(
                true, peptideShakerGUI.getLabelWidth() - 20, peptideShakerGUI.getScoreAndConfidenceDecimalFormat());
        ((JSparklinesBarChartTableCellRenderer) peptideShakerJTable.getColumn("Confidence").getCellRenderer()).showNumberAndChart(
                true, peptideShakerGUI.getLabelWidth() - 20, peptideShakerGUI.getScoreAndConfidenceDecimalFormat());

        omssaTable.getColumn("Protein(s)").setCellRenderer(new HtmlLinksRenderer(peptideShakerGUI.getSelectedRowHtmlTagFontColor(), peptideShakerGUI.getNotSelectedRowHtmlTagFontColor()));
        xTandemTable.getColumn("Protein(s)").setCellRenderer(new HtmlLinksRenderer(peptideShakerGUI.getSelectedRowHtmlTagFontColor(), peptideShakerGUI.getNotSelectedRowHtmlTagFontColor()));
        mascotTable.getColumn("Protein(s)").setCellRenderer(new HtmlLinksRenderer(peptideShakerGUI.getSelectedRowHtmlTagFontColor(), peptideShakerGUI.getNotSelectedRowHtmlTagFontColor()));

        omssaTable.getColumn("Confidence").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100d, peptideShakerGUI.getSparklineColor()));
        ((JSparklinesBarChartTableCellRenderer) omssaTable.getColumn("Confidence").getCellRenderer()).showNumberAndChart(
                true, peptideShakerGUI.getLabelWidth() - 20, peptideShakerGUI.getScoreAndConfidenceDecimalFormat());
        xTandemTable.getColumn("Confidence").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100d, peptideShakerGUI.getSparklineColor()));
        ((JSparklinesBarChartTableCellRenderer) xTandemTable.getColumn("Confidence").getCellRenderer()).showNumberAndChart(
                true, peptideShakerGUI.getLabelWidth() - 20, peptideShakerGUI.getScoreAndConfidenceDecimalFormat());
        mascotTable.getColumn("Confidence").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100d, peptideShakerGUI.getSparklineColor()));
        ((JSparklinesBarChartTableCellRenderer) mascotTable.getColumn("Confidence").getCellRenderer()).showNumberAndChart(
                true, peptideShakerGUI.getLabelWidth() - 20, peptideShakerGUI.getScoreAndConfidenceDecimalFormat());

        omssaTable.getColumn("Charge").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 10d, peptideShakerGUI.getSparklineColor()));
        ((JSparklinesBarChartTableCellRenderer) omssaTable.getColumn("Charge").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth() - 30);
        xTandemTable.getColumn("Charge").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 10d, peptideShakerGUI.getSparklineColor()));
        ((JSparklinesBarChartTableCellRenderer) xTandemTable.getColumn("Charge").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth() - 30);
        mascotTable.getColumn("Charge").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 10d, peptideShakerGUI.getSparklineColor()));
        ((JSparklinesBarChartTableCellRenderer) mascotTable.getColumn("Charge").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth() - 30);

        searchEngineTable.getColumn("Validated PSMs").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100d, peptideShakerGUI.getSparklineColor()));
        searchEngineTable.getColumn("Unique PSMs").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100d, peptideShakerGUI.getSparklineColor()));
        searchEngineTable.getColumn("OMSSA").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100d, peptideShakerGUI.getSparklineColor()));
        searchEngineTable.getColumn("X!Tandem").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100d, peptideShakerGUI.getSparklineColor()));
        searchEngineTable.getColumn("Mascot").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100d, peptideShakerGUI.getSparklineColor()));
        searchEngineTable.getColumn("All").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100d, peptideShakerGUI.getSparklineColor()));
        searchEngineTable.getColumn("Unassigned").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100d, peptideShakerGUI.getSparklineColor()));
        ((JSparklinesBarChartTableCellRenderer) searchEngineTable.getColumn("Validated PSMs").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth());
        ((JSparklinesBarChartTableCellRenderer) searchEngineTable.getColumn("Unique PSMs").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth());
        ((JSparklinesBarChartTableCellRenderer) searchEngineTable.getColumn("OMSSA").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth());
        ((JSparklinesBarChartTableCellRenderer) searchEngineTable.getColumn("X!Tandem").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth());
        ((JSparklinesBarChartTableCellRenderer) searchEngineTable.getColumn("Mascot").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth());
        ((JSparklinesBarChartTableCellRenderer) searchEngineTable.getColumn("All").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth());
        ((JSparklinesBarChartTableCellRenderer) searchEngineTable.getColumn("Unassigned").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth());

        // set up the psm color map
        HashMap<Integer, java.awt.Color> searchEngineSpectrumLevelColorMap = new HashMap<Integer, java.awt.Color>();
        searchEngineSpectrumLevelColorMap.put(AGREEMENT, peptideShakerGUI.getSparklineColor()); // search engines agree
        searchEngineSpectrumLevelColorMap.put(CONFLICT, java.awt.Color.YELLOW); // search engines don't agree
        searchEngineSpectrumLevelColorMap.put(PARTIALLY_MISSING, java.awt.Color.ORANGE); // some search engines id'ed some didn't
        searchEngineSpectrumLevelColorMap.put(NO_ID, java.awt.Color.lightGray); // no psm

        // set up the psm tooltip map
        HashMap<Integer, String> searchEngineSpectrumLevelTooltipMap = new HashMap<Integer, String>();
        searchEngineSpectrumLevelTooltipMap.put(AGREEMENT, "Search Engines Agree");
        searchEngineSpectrumLevelTooltipMap.put(CONFLICT, "Search Engines Disagree");
        searchEngineSpectrumLevelTooltipMap.put(PARTIALLY_MISSING, "Search Engine(s) Missing");
        searchEngineSpectrumLevelTooltipMap.put(NO_ID, "(No PSM)");

        spectrumTable.getColumn("SE").setCellRenderer(new JSparklinesIntegerColorTableCellRenderer(java.awt.Color.lightGray, searchEngineSpectrumLevelColorMap, searchEngineSpectrumLevelTooltipMap));
        spectrumTable.getColumn("m/z").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100d, peptideShakerGUI.getSparklineColor()));
        spectrumTable.getColumn("Charge").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 4d, peptideShakerGUI.getSparklineColor()));
        spectrumTable.getColumn("RT").setCellRenderer(new JSparklinesIntervalChartTableCellRenderer(PlotOrientation.HORIZONTAL, 0d,
                1000d, 10d, peptideShakerGUI.getSparklineColor(), peptideShakerGUI.getSparklineColor()));
        ((JSparklinesBarChartTableCellRenderer) spectrumTable.getColumn("m/z").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth());
        ((JSparklinesBarChartTableCellRenderer) spectrumTable.getColumn("Charge").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth() - 30);
        ((JSparklinesIntervalChartTableCellRenderer) spectrumTable.getColumn("RT").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth() + 5);
        ((JSparklinesIntervalChartTableCellRenderer) spectrumTable.getColumn("RT").getCellRenderer()).showReferenceLine(true, 0.02, java.awt.Color.BLACK);

        // set up the table header tooltips
        searchEngineTableToolTips = new ArrayList<String>();
        searchEngineTableToolTips.add(null);
        searchEngineTableToolTips.add("Search Engine");
        searchEngineTableToolTips.add("Validated Peptide-Spectrum Matches");
        searchEngineTableToolTips.add("Unique Pepttide-Spectrum Matches");
        searchEngineTableToolTips.add("Overlapping Peptide-Spectrum Matches with OMSSA");
        searchEngineTableToolTips.add("Overlapping Peptide-Spectrum Matches with X!Tandem");
        searchEngineTableToolTips.add("Overlapping Peptide-Spectrum Matches with Mascot");
        searchEngineTableToolTips.add("Overlapping Peptide-Spectrum Matches All Search Engines");
        searchEngineTableToolTips.add("Unassigned Spectra");

        spectrumTableToolTips = new ArrayList<String>();
        spectrumTableToolTips.add(null);
        spectrumTableToolTips.add("Search Engine Agreement");
        spectrumTableToolTips.add("Spectrum Title");
        spectrumTableToolTips.add("Precursor m/z");
        spectrumTableToolTips.add("Precursor Charge");
        spectrumTableToolTips.add("Precursor Retention Time");

        peptideShakerTableToolTips = new ArrayList<String>();
        peptideShakerTableToolTips.add(null);
        peptideShakerTableToolTips.add("Search Engine Agreement");
        peptideShakerTableToolTips.add("Mapping Protein(s)");
        peptideShakerTableToolTips.add("Peptide Sequence");
        peptideShakerTableToolTips.add("Peptide Score");
        peptideShakerTableToolTips.add("Peptide Confidence");
        peptideShakerTableToolTips.add("Validated");

        omssaTableToolTips = new ArrayList<String>();
        omssaTableToolTips.add("Search Engine Peptide Rank");
        omssaTableToolTips.add("Mapping Protein(s)");
        omssaTableToolTips.add("Peptide Sequence");
        omssaTableToolTips.add("Precursor Charge");
        omssaTableToolTips.add("Peptide e-value");
        omssaTableToolTips.add("Peptide Confidence");

        xTandemTableToolTips = new ArrayList<String>();
        xTandemTableToolTips.add("Search Engine Peptide Rank");
        xTandemTableToolTips.add("Mapping Protein(s)");
        xTandemTableToolTips.add("Peptide Sequence");
        xTandemTableToolTips.add("Precursor Charge");
        xTandemTableToolTips.add("Peptide e-value");
        xTandemTableToolTips.add("Peptide Confidence");

        mascotTableToolTips = new ArrayList<String>();
        mascotTableToolTips.add("Search Engine Peptide Rank");
        mascotTableToolTips.add("Mapping Protein(s)");
        mascotTableToolTips.add("Peptide Sequence");
        mascotTableToolTips.add("Precursor Charge");
        mascotTableToolTips.add("Peptide e-value");
        mascotTableToolTips.add("Peptide Confidence");
    }

    /**
     * Displays or hide sparklines in the tables.
     *
     * @param showSparkLines boolean indicating whether sparklines shall be
     * displayed or hidden
     */
    public void showSparkLines(boolean showSparkLines) {
        ((JSparklinesBarChartTableCellRenderer) searchEngineTable.getColumn("Validated PSMs").getCellRenderer()).showNumbers(!showSparkLines);
        ((JSparklinesBarChartTableCellRenderer) searchEngineTable.getColumn("Unique PSMs").getCellRenderer()).showNumbers(!showSparkLines);
        ((JSparklinesBarChartTableCellRenderer) searchEngineTable.getColumn("OMSSA").getCellRenderer()).showNumbers(!showSparkLines);
        ((JSparklinesBarChartTableCellRenderer) searchEngineTable.getColumn("X!Tandem").getCellRenderer()).showNumbers(!showSparkLines);
        ((JSparklinesBarChartTableCellRenderer) searchEngineTable.getColumn("Mascot").getCellRenderer()).showNumbers(!showSparkLines);
        ((JSparklinesBarChartTableCellRenderer) searchEngineTable.getColumn("All").getCellRenderer()).showNumbers(!showSparkLines);

        ((JSparklinesBarChartTableCellRenderer) spectrumTable.getColumn("m/z").getCellRenderer()).showNumbers(!showSparkLines);
        ((JSparklinesBarChartTableCellRenderer) spectrumTable.getColumn("Charge").getCellRenderer()).showNumbers(!showSparkLines);
        ((JSparklinesIntervalChartTableCellRenderer) spectrumTable.getColumn("RT").getCellRenderer()).showNumbers(!showSparkLines);

        ((JSparklinesBarChartTableCellRenderer) peptideShakerJTable.getColumn("Score").getCellRenderer()).showNumbers(!showSparkLines);
        ((JSparklinesBarChartTableCellRenderer) peptideShakerJTable.getColumn("Confidence").getCellRenderer()).showNumbers(!showSparkLines);

        ((JSparklinesBarChartTableCellRenderer) omssaTable.getColumn("Confidence").getCellRenderer()).showNumbers(!showSparkLines);
        ((JSparklinesBarChartTableCellRenderer) xTandemTable.getColumn("Confidence").getCellRenderer()).showNumbers(!showSparkLines);
        ((JSparklinesBarChartTableCellRenderer) mascotTable.getColumn("Confidence").getCellRenderer()).showNumbers(!showSparkLines);
        ((JSparklinesBarChartTableCellRenderer) omssaTable.getColumn("Charge").getCellRenderer()).showNumbers(!showSparkLines);
        ((JSparklinesBarChartTableCellRenderer) xTandemTable.getColumn("Charge").getCellRenderer()).showNumbers(!showSparkLines);
        ((JSparklinesBarChartTableCellRenderer) mascotTable.getColumn("Charge").getCellRenderer()).showNumbers(!showSparkLines);

        searchEngineTable.revalidate();
        searchEngineTable.repaint();

        spectrumTable.revalidate();
        spectrumTable.repaint();

        peptideShakerJTable.revalidate();
        peptideShakerJTable.repaint();

        omssaTable.revalidate();
        omssaTable.repaint();

        xTandemTable.revalidate();
        xTandemTable.repaint();

        mascotTable.revalidate();
        mascotTable.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        searchEnginesJPanel = new javax.swing.JPanel();
        searchEnginesJLayeredPane = new javax.swing.JLayeredPane();
        searchEnginesPanel = new javax.swing.JPanel();
        searchEnginetableJScrollPane = new javax.swing.JScrollPane();
        searchEngineTable = new JTable() {
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        String tip = (String) searchEngineTableToolTips.get(realIndex);
                        return tip;
                    }
                };
            }
        };
        vennDiagramButton = new javax.swing.JButton();
        searchEnginesHelpJButton = new javax.swing.JButton();
        exportSearchEnginePerformanceJButton = new javax.swing.JButton();
        contextMenuSearchEnginesBackgroundPanel = new javax.swing.JPanel();
        psmsJPanel = new javax.swing.JPanel();
        psmsLayeredPane = new javax.swing.JLayeredPane();
        psmsPanel = new javax.swing.JPanel();
        peptideShakerJScrollPane = new javax.swing.JScrollPane();
        peptideShakerJTable = new JTable() {
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        String tip = (String) peptideShakerTableToolTips.get(realIndex);
                        return tip;
                    }
                };
            }
        };
        jLabel1 = new javax.swing.JLabel();
        omssaPanel = new javax.swing.JPanel();
        omssaTableJScrollPane = new javax.swing.JScrollPane();
        omssaTable = new JTable() {
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        String tip = (String) omssaTableToolTips.get(realIndex);
                        return tip;
                    }
                };
            }
        };
        jLabel3 = new javax.swing.JLabel();
        xTandemPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        xTandemTableJScrollPane = new javax.swing.JScrollPane();
        xTandemTable = new JTable() {
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        String tip = (String) xTandemTableToolTips.get(realIndex);
                        return tip;
                    }
                };
            }
        };
        mascotPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        mascotTableJScrollPane = new javax.swing.JScrollPane();
        mascotTable = new JTable() {
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        String tip = (String) mascotTableToolTips.get(realIndex);
                        return tip;
                    }
                };
            }
        };
        psmsHelpJButton = new javax.swing.JButton();
        exportPsmsJButton = new javax.swing.JButton();
        contextMenuPsmsBackgroundPanel = new javax.swing.JPanel();
        spectrumJSplitPane = new javax.swing.JSplitPane();
        spectrumSelectionJPanel = new javax.swing.JPanel();
        spectrumSelectionLayeredPane = new javax.swing.JLayeredPane();
        spectrumSelectionPanel = new javax.swing.JPanel();
        fileNamesCmb = new javax.swing.JComboBox();
        spectrumTableJScrollPane = new javax.swing.JScrollPane();
        spectrumTable = new JTable() {
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        String tip = (String) spectrumTableToolTips.get(realIndex);
                        return tip;
                    }
                };
            }
        };
        spectrumSelectionHelpJButton = new javax.swing.JButton();
        exportSpectrumSelectionJButton = new javax.swing.JButton();
        contextMenuSpectrumSelectionBackgroundPanel = new javax.swing.JPanel();
        spectrumJPanel = new javax.swing.JPanel();
        spectrumLayeredPane = new javax.swing.JLayeredPane();
        spectrumPanel = new javax.swing.JPanel();
        slidersSplitPane = new javax.swing.JSplitPane();
        slidersPanel = new javax.swing.JPanel();
        accuracySlider = new javax.swing.JSlider();
        intensitySlider = new javax.swing.JSlider();
        spectrumJPanel1 = new javax.swing.JPanel();
        spectrumJToolBar = new javax.swing.JToolBar();
        spectrumAnnotationMenuPanel = new javax.swing.JPanel();
        spectrumChartPanel = new javax.swing.JPanel();
        spectrumHelpJButton = new javax.swing.JButton();
        exportSpectrumJButton = new javax.swing.JButton();
        contextMenuSpectrumBackgroundPanel = new javax.swing.JPanel();

        setBackground(new java.awt.Color(255, 255, 255));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        searchEnginesJPanel.setOpaque(false);

        searchEnginesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Search Engine Performance"));
        searchEnginesPanel.setOpaque(false);

        searchEnginetableJScrollPane.setOpaque(false);

        searchEngineTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "Search Engine", "Validated PSMs", "Unique PSMs", "OMSSA", "X!Tandem", "Mascot", "All", "Unassigned"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        searchEngineTable.setOpaque(false);
        searchEnginetableJScrollPane.setViewportView(searchEngineTable);

        vennDiagramButton.setBackground(new java.awt.Color(255, 255, 255));
        vennDiagramButton.setBorderPainted(false);
        vennDiagramButton.setContentAreaFilled(false);
        vennDiagramButton.setFocusable(false);

        javax.swing.GroupLayout searchEnginesPanelLayout = new javax.swing.GroupLayout(searchEnginesPanel);
        searchEnginesPanel.setLayout(searchEnginesPanelLayout);
        searchEnginesPanelLayout.setHorizontalGroup(
            searchEnginesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1318, Short.MAX_VALUE)
            .addGroup(searchEnginesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, searchEnginesPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(searchEnginetableJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1115, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(vennDiagramButton, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );
        searchEnginesPanelLayout.setVerticalGroup(
            searchEnginesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 123, Short.MAX_VALUE)
            .addGroup(searchEnginesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, searchEnginesPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(searchEnginesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(searchEnginetableJScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                        .addComponent(vennDiagramButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE))
                    .addContainerGap()))
        );

        searchEnginesPanel.setBounds(0, 0, 1330, 150);
        searchEnginesJLayeredPane.add(searchEnginesPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        searchEnginesHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        searchEnginesHelpJButton.setToolTipText("Help");
        searchEnginesHelpJButton.setBorder(null);
        searchEnginesHelpJButton.setBorderPainted(false);
        searchEnginesHelpJButton.setContentAreaFilled(false);
        searchEnginesHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        searchEnginesHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                searchEnginesHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                searchEnginesHelpJButtonMouseExited(evt);
            }
        });
        searchEnginesHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchEnginesHelpJButtonActionPerformed(evt);
            }
        });
        searchEnginesHelpJButton.setBounds(1290, 0, 10, 25);
        searchEnginesJLayeredPane.add(searchEnginesHelpJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        exportSearchEnginePerformanceJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        exportSearchEnginePerformanceJButton.setToolTipText("Copy to File");
        exportSearchEnginePerformanceJButton.setBorder(null);
        exportSearchEnginePerformanceJButton.setBorderPainted(false);
        exportSearchEnginePerformanceJButton.setContentAreaFilled(false);
        exportSearchEnginePerformanceJButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        exportSearchEnginePerformanceJButton.setEnabled(false);
        exportSearchEnginePerformanceJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame.png"))); // NOI18N
        exportSearchEnginePerformanceJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                exportSearchEnginePerformanceJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                exportSearchEnginePerformanceJButtonMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                exportSearchEnginePerformanceJButtonMouseReleased(evt);
            }
        });
        exportSearchEnginePerformanceJButton.setBounds(1280, 0, 10, 25);
        searchEnginesJLayeredPane.add(exportSearchEnginePerformanceJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        contextMenuSearchEnginesBackgroundPanel.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout contextMenuSearchEnginesBackgroundPanelLayout = new javax.swing.GroupLayout(contextMenuSearchEnginesBackgroundPanel);
        contextMenuSearchEnginesBackgroundPanel.setLayout(contextMenuSearchEnginesBackgroundPanelLayout);
        contextMenuSearchEnginesBackgroundPanelLayout.setHorizontalGroup(
            contextMenuSearchEnginesBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );
        contextMenuSearchEnginesBackgroundPanelLayout.setVerticalGroup(
            contextMenuSearchEnginesBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        contextMenuSearchEnginesBackgroundPanel.setBounds(1280, 0, 30, 20);
        searchEnginesJLayeredPane.add(contextMenuSearchEnginesBackgroundPanel, javax.swing.JLayeredPane.POPUP_LAYER);

        javax.swing.GroupLayout searchEnginesJPanelLayout = new javax.swing.GroupLayout(searchEnginesJPanel);
        searchEnginesJPanel.setLayout(searchEnginesJPanelLayout);
        searchEnginesJPanelLayout.setHorizontalGroup(
            searchEnginesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(searchEnginesJLayeredPane)
        );
        searchEnginesJPanelLayout.setVerticalGroup(
            searchEnginesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(searchEnginesJLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
        );

        psmsJPanel.setOpaque(false);

        psmsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Peptide-Spectrum Matches"));
        psmsPanel.setOpaque(false);

        peptideShakerJScrollPane.setOpaque(false);

        peptideShakerJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "SE", "Protein(s)", "Sequence", "Score", "Confidence", "  "
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Double.class, java.lang.Double.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        peptideShakerJTable.setFocusable(false);
        peptideShakerJTable.setOpaque(false);
        peptideShakerJTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                peptideShakerJTableMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                peptideShakerJTableMouseReleased(evt);
            }
        });
        peptideShakerJTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                peptideShakerJTableMouseMoved(evt);
            }
        });
        peptideShakerJScrollPane.setViewportView(peptideShakerJTable);

        jLabel1.setFont(jLabel1.getFont().deriveFont((jLabel1.getFont().getStyle() | java.awt.Font.ITALIC)));
        jLabel1.setText("PeptideShaker");

        omssaPanel.setOpaque(false);

        omssaTableJScrollPane.setOpaque(false);

        omssaTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "Protein(s)", "Sequence", "Charge", "e-value", "Confidence"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Double.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        omssaTable.setOpaque(false);
        omssaTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        omssaTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                omssaTableMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                omssaTableMouseReleased(evt);
            }
        });
        omssaTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                omssaTableMouseMoved(evt);
            }
        });
        omssaTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                omssaTableKeyReleased(evt);
            }
        });
        omssaTableJScrollPane.setViewportView(omssaTable);

        jLabel3.setFont(jLabel3.getFont().deriveFont((jLabel3.getFont().getStyle() | java.awt.Font.ITALIC)));
        jLabel3.setText("OMSSA");

        javax.swing.GroupLayout omssaPanelLayout = new javax.swing.GroupLayout(omssaPanel);
        omssaPanel.setLayout(omssaPanelLayout);
        omssaPanelLayout.setHorizontalGroup(
            omssaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(omssaPanelLayout.createSequentialGroup()
                .addComponent(jLabel3)
                .addContainerGap(389, Short.MAX_VALUE))
            .addComponent(omssaTableJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
        );
        omssaPanelLayout.setVerticalGroup(
            omssaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(omssaPanelLayout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(omssaTableJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE))
        );

        xTandemPanel.setOpaque(false);

        jLabel4.setFont(jLabel4.getFont().deriveFont((jLabel4.getFont().getStyle() | java.awt.Font.ITALIC)));
        jLabel4.setText("X!Tandem");

        xTandemTableJScrollPane.setOpaque(false);

        xTandemTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "Protein(s)", "Sequence", "Charge", "e-value", "Confidence"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Double.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        xTandemTable.setOpaque(false);
        xTandemTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        xTandemTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                xTandemTableMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                xTandemTableMouseReleased(evt);
            }
        });
        xTandemTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                xTandemTableMouseMoved(evt);
            }
        });
        xTandemTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                xTandemTableKeyReleased(evt);
            }
        });
        xTandemTableJScrollPane.setViewportView(xTandemTable);

        javax.swing.GroupLayout xTandemPanelLayout = new javax.swing.GroupLayout(xTandemPanel);
        xTandemPanel.setLayout(xTandemPanelLayout);
        xTandemPanelLayout.setHorizontalGroup(
            xTandemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(xTandemPanelLayout.createSequentialGroup()
                .addComponent(jLabel4)
                .addContainerGap(389, Short.MAX_VALUE))
            .addComponent(xTandemTableJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE)
        );
        xTandemPanelLayout.setVerticalGroup(
            xTandemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(xTandemPanelLayout.createSequentialGroup()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(xTandemTableJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE))
        );

        mascotPanel.setOpaque(false);

        jLabel2.setFont(jLabel2.getFont().deriveFont((jLabel2.getFont().getStyle() | java.awt.Font.ITALIC)));
        jLabel2.setText("Mascot");

        mascotTableJScrollPane.setMinimumSize(new java.awt.Dimension(23, 87));
        mascotTableJScrollPane.setOpaque(false);

        mascotTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "Protein(s)", "Sequence", "Charge", "e-value", "Confidence"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Double.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        mascotTable.setOpaque(false);
        mascotTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        mascotTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mascotTableMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                mascotTableMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                mascotTableMouseReleased(evt);
            }
        });
        mascotTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                mascotTableMouseMoved(evt);
            }
        });
        mascotTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                mascotTableKeyReleased(evt);
            }
        });
        mascotTableJScrollPane.setViewportView(mascotTable);

        javax.swing.GroupLayout mascotPanelLayout = new javax.swing.GroupLayout(mascotPanel);
        mascotPanel.setLayout(mascotPanelLayout);
        mascotPanelLayout.setHorizontalGroup(
            mascotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mascotPanelLayout.createSequentialGroup()
                .addComponent(jLabel2)
                .addContainerGap(391, Short.MAX_VALUE))
            .addComponent(mascotTableJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 425, Short.MAX_VALUE)
        );
        mascotPanelLayout.setVerticalGroup(
            mascotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mascotPanelLayout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mascotTableJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout psmsPanelLayout = new javax.swing.GroupLayout(psmsPanel);
        psmsPanel.setLayout(psmsPanelLayout);
        psmsPanelLayout.setHorizontalGroup(
            psmsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1318, Short.MAX_VALUE)
            .addGroup(psmsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(psmsPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(psmsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(peptideShakerJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1298, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addGroup(psmsPanelLayout.createSequentialGroup()
                            .addComponent(omssaPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(xTandemPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(mascotPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addContainerGap()))
        );
        psmsPanelLayout.setVerticalGroup(
            psmsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 273, Short.MAX_VALUE)
            .addGroup(psmsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(psmsPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel1)
                    .addGap(9, 9, 9)
                    .addComponent(peptideShakerJScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addGroup(psmsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(xTandemPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(mascotPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(omssaPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addContainerGap()))
        );

        psmsPanel.setBounds(0, 0, 1330, 300);
        psmsLayeredPane.add(psmsPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        psmsHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        psmsHelpJButton.setToolTipText("Help");
        psmsHelpJButton.setBorder(null);
        psmsHelpJButton.setBorderPainted(false);
        psmsHelpJButton.setContentAreaFilled(false);
        psmsHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        psmsHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                psmsHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                psmsHelpJButtonMouseExited(evt);
            }
        });
        psmsHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                psmsHelpJButtonActionPerformed(evt);
            }
        });
        psmsHelpJButton.setBounds(1290, 0, 10, 25);
        psmsLayeredPane.add(psmsHelpJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        exportPsmsJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        exportPsmsJButton.setToolTipText("Copy to File");
        exportPsmsJButton.setBorder(null);
        exportPsmsJButton.setBorderPainted(false);
        exportPsmsJButton.setContentAreaFilled(false);
        exportPsmsJButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        exportPsmsJButton.setEnabled(false);
        exportPsmsJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame.png"))); // NOI18N
        exportPsmsJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                exportPsmsJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                exportPsmsJButtonMouseExited(evt);
            }
        });
        exportPsmsJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportPsmsJButtonActionPerformed(evt);
            }
        });
        exportPsmsJButton.setBounds(1280, 0, 10, 25);
        psmsLayeredPane.add(exportPsmsJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        contextMenuPsmsBackgroundPanel.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout contextMenuPsmsBackgroundPanelLayout = new javax.swing.GroupLayout(contextMenuPsmsBackgroundPanel);
        contextMenuPsmsBackgroundPanel.setLayout(contextMenuPsmsBackgroundPanelLayout);
        contextMenuPsmsBackgroundPanelLayout.setHorizontalGroup(
            contextMenuPsmsBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );
        contextMenuPsmsBackgroundPanelLayout.setVerticalGroup(
            contextMenuPsmsBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        contextMenuPsmsBackgroundPanel.setBounds(1280, 0, 30, 20);
        psmsLayeredPane.add(contextMenuPsmsBackgroundPanel, javax.swing.JLayeredPane.POPUP_LAYER);

        javax.swing.GroupLayout psmsJPanelLayout = new javax.swing.GroupLayout(psmsJPanel);
        psmsJPanel.setLayout(psmsJPanelLayout);
        psmsJPanelLayout.setHorizontalGroup(
            psmsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(psmsLayeredPane)
        );
        psmsJPanelLayout.setVerticalGroup(
            psmsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(psmsLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
        );

        spectrumJSplitPane.setBorder(null);
        spectrumJSplitPane.setDividerLocation(700);
        spectrumJSplitPane.setDividerSize(0);
        spectrumJSplitPane.setResizeWeight(0.5);
        spectrumJSplitPane.setOpaque(false);

        spectrumSelectionJPanel.setOpaque(false);

        spectrumSelectionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Spectrum Selection"));
        spectrumSelectionPanel.setOpaque(false);

        fileNamesCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileNamesCmbActionPerformed(evt);
            }
        });

        spectrumTableJScrollPane.setOpaque(false);

        spectrumTable.setModel(new SpectrumTable());
        spectrumTable.setOpaque(false);
        spectrumTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        spectrumTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                spectrumTableMouseReleased(evt);
            }
        });
        spectrumTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                spectrumTableKeyReleased(evt);
            }
        });
        spectrumTableJScrollPane.setViewportView(spectrumTable);

        javax.swing.GroupLayout spectrumSelectionPanelLayout = new javax.swing.GroupLayout(spectrumSelectionPanel);
        spectrumSelectionPanel.setLayout(spectrumSelectionPanelLayout);
        spectrumSelectionPanelLayout.setHorizontalGroup(
            spectrumSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 678, Short.MAX_VALUE)
            .addGroup(spectrumSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(spectrumSelectionPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(spectrumSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(spectrumTableJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 658, Short.MAX_VALUE)
                        .addComponent(fileNamesCmb, 0, 658, Short.MAX_VALUE))
                    .addContainerGap()))
        );
        spectrumSelectionPanelLayout.setVerticalGroup(
            spectrumSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 323, Short.MAX_VALUE)
            .addGroup(spectrumSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, spectrumSelectionPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(fileNamesCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(spectrumTableJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        spectrumSelectionPanel.setBounds(0, 0, 690, 350);
        spectrumSelectionLayeredPane.add(spectrumSelectionPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        spectrumSelectionHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        spectrumSelectionHelpJButton.setToolTipText("Help");
        spectrumSelectionHelpJButton.setBorder(null);
        spectrumSelectionHelpJButton.setBorderPainted(false);
        spectrumSelectionHelpJButton.setContentAreaFilled(false);
        spectrumSelectionHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        spectrumSelectionHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                spectrumSelectionHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                spectrumSelectionHelpJButtonMouseExited(evt);
            }
        });
        spectrumSelectionHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.even
