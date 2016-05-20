package eu.isas.peptideshaker.gui.tabpanels;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.Identification;
import com.compomics.util.experiment.identification.SpectrumAnnotator;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.PeptideMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Precursor;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import com.compomics.util.gui.dialogs.ProgressDialogParent;
import com.compomics.util.gui.dialogs.ProgressDialogX;
import com.compomics.util.gui.protein.SequenceModificationPanel;
import com.compomics.util.gui.spectrum.SpectrumPanel;
import eu.isas.peptideshaker.gui.PeptideShakerGUI;
import eu.isas.peptideshaker.myparameters.PSParameter;
import eu.isas.peptideshaker.myparameters.PSPtmScores;
import eu.isas.peptideshaker.preferences.AnnotationPreferences;
import eu.isas.peptideshaker.scoring.PtmScoring;
import com.compomics.util.gui.protein.ModificationProfile;
import eu.isas.peptideshaker.export.OutputGenerator;
import eu.isas.peptideshaker.gui.ExportGraphicsDialog;
import eu.isas.peptideshaker.gui.HelpDialog;
import eu.isas.peptideshaker.gui.ProteinInferencePeptideLevelDialog;
import eu.isas.peptideshaker.gui.PtmSiteInferenceDialog;
import eu.isas.peptideshaker.gui.PtmTable;
import eu.isas.peptideshaker.myparameters.PSMaps;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import no.uib.jsparklines.extra.TrueFalseIconRenderer;
import no.uib.jsparklines.renderers.JSparklinesBarChartTableCellRenderer;
import no.uib.jsparklines.renderers.JSparklinesColorTableCellRenderer;
import no.uib.jsparklines.renderers.JSparklinesIntegerColorTableCellRenderer;
import no.uib.jsparklines.renderers.JSparklinesIntervalChartTableCellRenderer;
import no.uib.jsparklines.renderers.util.GradientColorCoding.ColorGradient;
import org.jfree.chart.plot.PlotOrientation;

/**
 * The PTM tab.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class PtmPanel extends javax.swing.JPanel implements ProgressDialogParent {

    /**
     * The progress dialog.
     */
    private ProgressDialogX progressDialog;
    /**
     * If true the progress bar is disposed of.
     */
    private static boolean cancelProgress = false;

    /**
     * Indexes for the data tables.
     */
    private enum TableIndex {

        MODIFIED_PEPTIDES_TABLE, RELATED_PEPTIDES_TABLE, MODIFIED_PSMS_TABLE, RELATED_PSMS_TABLE
    };
    /**
     * The currently selected row in the PTM table.
     */
    private int currentPtmRow = -1;
    /**
     * The selected peptides table column header tooltips.
     */
    private ArrayList<String> selectedPeptidesTableToolTips;
    /**
     * The related peptides table column header tooltips.
     */
    private ArrayList<String> relatedPeptidesTableToolTips;
    /**
     * The selected psms table column header tooltips.
     */
    private ArrayList<String> selectedPsmsTableToolTips;
    /**
     * The related psms table column header tooltips.
     */
    private ArrayList<String> relatedPsmsTableToolTips;
    /**
     * PTM table column header tooltips.
     */
    private ArrayList<String> ptmTableToolTips;
    /**
     * The spectrum annotator for the first spectrum
     */
    private SpectrumAnnotator annotator = new SpectrumAnnotator();
    /**
     * The main GUI
     */
    private PeptideShakerGUI peptideShakerGUI;
    /**
     * Map of all peptide keys indexed by their modification status
     */
    private HashMap<String, ArrayList<String>> peptideMap = new HashMap<String, ArrayList<String>>();
    /**
     * The modification name for no modification
     */
    public final static String NO_MODIFICATION = "no modification";
    /**
     * The displayed identification
     */
    private Identification identification;
    /**
     * The keys of the peptides currently displayed
     */
    private ArrayList<String> displayedPeptides = new ArrayList<String>();
    /**
     * The keys of the related peptides currently displayed
     */
    private ArrayList<String> relatedPeptides = new ArrayList<String>();
    /**
     * boolean indicating whether the related peptide is selected
     */
    private boolean relatedSelected = false;
    /**
     * The current spectrum panel for the upper psm.
     */
    private SpectrumPanel spectrum;
    /**
     * The current spectrum key.
     */
    private String currentSpectrumKey = "";
    /**
     * Protein inference map, key: pi type, element: pi as a string.
     */
    private HashMap<Integer, String> proteinInferenceTooltipMap;
    /**
     * PTM confidence tooltip map, key: ptm confidence type, element: ptm
     * confidence as a string.
     */
    private HashMap<Integer, String> ptmConfidenceTooltipMap;
    /**
     * The ptm factory
     */
    private PTMFactory ptmFactory = PTMFactory.getInstance();

    /**
     * Creates a new PTM tab.
     *
     * @param peptideShakerGUI the PeptideShaker parent frame
     */
    public PtmPanel(PeptideShakerGUI peptideShakerGUI) {
        this.peptideShakerGUI = peptideShakerGUI;
        initComponents();
        formComponentResized(null);

        // add Delta and A score gradient color panels
        addGradientScoreColors();

        peptidesTable.setAutoCreateRowSorter(true);
        relatedPeptidesTable.setAutoCreateRowSorter(true);
        selectedPsmsTable.setAutoCreateRowSorter(true);
        relatedPsmsTable.setAutoCreateRowSorter(true);

        relatedPeptidesTableJScrollPane.getViewport().setOpaque(false);
        psmsModifiedTableJScrollPane.getViewport().setOpaque(false);
        psmsRelatedTableJScrollPane.getViewport().setOpaque(false);
        peptidesTableJScrollPane.getViewport().setOpaque(false);
        ptmJScrollPane.getViewport().setOpaque(false);
        ptmTableJScrollPane.getViewport().setOpaque(false);
        psmAScoresScrollPane.getViewport().setOpaque(false);
        psmDeltaScrollPane.getViewport().setOpaque(false);

        // @TODO: remove when the ptm table is implemented
        //spectrumTabbedPane.setEnabledAt(0, false);

        setTableProperties();

        // make the tabs in the spectrum tabbed pane go from right to left
        spectrumTabbedPane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    }
    
    /**
     * Adds the gradient score colors for the A and Delta score tables. 
     */
    private void addGradientScoreColors() {
        
        final Color startColor = Color.WHITE;
        final Color endColor = Color.BLUE;
        
        JPanel deltaScoreGradientJPanel = new JPanel() {

            @Override
            protected void paintComponent(Graphics grphcs) {
                Graphics2D g2d = (Graphics2D) grphcs;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                        0, getHeight()/2,
                        startColor,
                        getWidth(), getHeight()/2,
                        endColor);
                        
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                super.paintComponent(grphcs);
            }
        };

        deltaScoreGradientJPanel.setOpaque(false);
        deltaScoreGradientPanel.add(deltaScoreGradientJPanel);
        
        JPanel aScoreGradientJPanel = new JPanel() {

            @Override
            protected void paintComponent(Graphics grphcs) {
                Graphics2D g2d = (Graphics2D) grphcs;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                        0, getHeight()/2,
                        startColor,
                        getWidth(), getHeight()/2,
                        endColor);
                        

                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                super.paintComponent(grphcs);
            }
        };

        aScoreGradientJPanel.setOpaque(false);
        aScoreGradientPanel.add(aScoreGradientJPanel);
    }

    /**
     * Set up the properties of the tables.
     */
    private void setTableProperties() {

        ptmJTable.getColumn("  ").setMaxWidth(35);
        ptmJTable.getColumn("  ").setMinWidth(35);

        peptidesTable.getColumn("   ").setMaxWidth(30);
        peptidesTable.getColumn("   ").setMinWidth(30);
        peptidesTable.getColumn(" ").setMaxWidth(45);
        peptidesTable.getColumn(" ").setMinWidth(45);
        peptidesTable.getColumn("PTM").setMaxWidth(45);
        peptidesTable.getColumn("PTM").setMinWidth(45);
        peptidesTable.getColumn("  ").setMaxWidth(30);
        peptidesTable.getColumn("  ").setMinWidth(30);
        peptidesTable.getColumn("PI").setMaxWidth(37);
        peptidesTable.getColumn("PI").setMinWidth(37);
        peptidesTable.getColumn("Peptide").setMaxWidth(80);
        peptidesTable.getColumn("Peptide").setMinWidth(80);

        relatedPeptidesTable.getColumn("   ").setMaxWidth(30);
        relatedPeptidesTable.getColumn("   ").setMinWidth(30);
        relatedPeptidesTable.getColumn(" ").setMaxWidth(45);
        relatedPeptidesTable.getColumn(" ").setMinWidth(45);
        relatedPeptidesTable.getColumn("PI").setMaxWidth(37);
        relatedPeptidesTable.getColumn("PI").setMinWidth(37);
        relatedPeptidesTable.getColumn("  ").setMaxWidth(30);
        relatedPeptidesTable.getColumn("  ").setMinWidth(30);
        relatedPeptidesTable.getColumn("PTM").setMaxWidth(45);
        relatedPeptidesTable.getColumn("PTM").setMinWidth(45);
        relatedPeptidesTable.getColumn("Peptide").setMaxWidth(80);
        relatedPeptidesTable.getColumn("Peptide").setMinWidth(80);

        selectedPsmsTable.getColumn("   ").setMaxWidth(30);
        selectedPsmsTable.getColumn("   ").setMinWidth(30);
        selectedPsmsTable.getColumn(" ").setMaxWidth(45);
        selectedPsmsTable.getColumn(" ").setMinWidth(45);
        selectedPsmsTable.getColumn("PTM").setMaxWidth(45);
        selectedPsmsTable.getColumn("PTM").setMinWidth(45);
        selectedPsmsTable.getColumn("Charge").setMaxWidth(90);
        selectedPsmsTable.getColumn("Charge").setMinWidth(90);
        selectedPsmsTable.getColumn("  ").setMaxWidth(30);
        selectedPsmsTable.getColumn("  ").setMinWidth(30);

        relatedPsmsTable.getColumn("   ").setMaxWidth(30);
        relatedPsmsTable.getColumn("   ").setMinWidth(30);
        relatedPsmsTable.getColumn(" ").setMaxWidth(45);
        relatedPsmsTable.getColumn(" ").setMinWidth(45);
        relatedPsmsTable.getColumn("PTM").setMaxWidth(45);
        relatedPsmsTable.getColumn("PTM").setMinWidth(45);
        relatedPsmsTable.getColumn("Charge").setMaxWidth(90);
        relatedPsmsTable.getColumn("Charge").setMinWidth(90);
        relatedPsmsTable.getColumn("  ").setMaxWidth(30);
        relatedPsmsTable.getColumn("  ").setMinWidth(30);

        peptidesTable.getTableHeader().setReorderingAllowed(false);
        relatedPeptidesTable.getTableHeader().setReorderingAllowed(false);
        selectedPsmsTable.getTableHeader().setReorderingAllowed(false);
        relatedPsmsTable.getTableHeader().setReorderingAllowed(false);
        psmAScoresTable.getTableHeader().setReorderingAllowed(false);
        psmDeltaScoresTable.getTableHeader().setReorderingAllowed(false);

        // centrally align the column headers 
        TableCellRenderer renderer = psmAScoresTable.getTableHeader().getDefaultRenderer();
        JLabel label = (JLabel) renderer;
        label.setHorizontalAlignment(JLabel.CENTER);
        renderer = psmDeltaScoresTable.getTableHeader().getDefaultRenderer();
        label = (JLabel) renderer;
        label.setHorizontalAlignment(JLabel.CENTER);

        // set up the protein inference color map
        HashMap<Integer, Color> proteinInferenceColorMap = new HashMap<Integer, Color>();
        proteinInferenceColorMap.put(PSParameter.NOT_GROUP, peptideShakerGUI.getSparklineColor()); // NOT_GROUP
        proteinInferenceColorMap.put(PSParameter.ISOFORMS, Color.YELLOW); // ISOFORMS
        proteinInferenceColorMap.put(PSParameter.ISOFORMS_UNRELATED, Color.BLUE); // ISOFORMS_UNRELATED
        proteinInferenceColorMap.put(PSParameter.UNRELATED, Color.RED); // UNRELATED

        // set up the protein inference tooltip map
        proteinInferenceTooltipMap = new HashMap<Integer, String>();
        proteinInferenceTooltipMap.put(PSParameter.NOT_GROUP, "Unique to a single protein");
        proteinInferenceTooltipMap.put(PSParameter.ISOFORMS, "Belongs to a group of isoforms");
        proteinInferenceTooltipMap.put(PSParameter.ISOFORMS_UNRELATED, "Belongs to a group of isoforms and unrelated proteins");
        proteinInferenceTooltipMap.put(PSParameter.UNRELATED, "Belongs to unrelated proteins");

        peptidesTable.getColumn("PI").setCellRenderer(new JSparklinesIntegerColorTableCellRenderer(Color.lightGray, proteinInferenceColorMap, proteinInferenceTooltipMap));
        relatedPeptidesTable.getColumn("PI").setCellRenderer(new JSparklinesIntegerColorTableCellRenderer(Color.lightGray, proteinInferenceColorMap, proteinInferenceTooltipMap));
        peptidesTable.getColumn("   ").setCellRenderer(new TrueFalseIconRenderer(
                new ImageIcon(this.getClass().getResource("/icons/star_yellow.png")),
                new ImageIcon(this.getClass().getResource("/icons/star_grey.png")),
                new ImageIcon(this.getClass().getResource("/icons/star_grey.png")),
                "Starred", null, null));
        relatedPeptidesTable.getColumn("   ").setCellRenderer(new TrueFalseIconRenderer(
                new ImageIcon(this.getClass().getResource("/icons/star_yellow.png")),
                new ImageIcon(this.getClass().getResource("/icons/star_grey.png")),
                new ImageIcon(this.getClass().getResource("/icons/star_grey.png")),
                "Starred", null, null));

        // set up the PTM confidence color map
        HashMap<Integer, Color> ptmConfidenceColorMap = new HashMap<Integer, Color>();
        ptmConfidenceColorMap.put(PtmScoring.NOT_FOUND, Color.lightGray);
        ptmConfidenceColorMap.put(PtmScoring.RANDOM, Color.RED);
        ptmConfidenceColorMap.put(PtmScoring.DOUBTFUL, Color.ORANGE);
        ptmConfidenceColorMap.put(PtmScoring.CONFIDENT, Color.YELLOW);
        ptmConfidenceColorMap.put(PtmScoring.VERY_CONFIDENT, peptideShakerGUI.getSparklineColor());

        // set up the PTM confidence tooltip map
        ptmConfidenceTooltipMap = new HashMap<Integer, String>();
        ptmConfidenceTooltipMap.put(-1, "(No PTMs)");
        ptmConfidenceTooltipMap.put(PtmScoring.RANDOM, "Random Assignment");
        ptmConfidenceTooltipMap.put(PtmScoring.DOUBTFUL, "Doubtful Assignment");
        ptmConfidenceTooltipMap.put(PtmScoring.CONFIDENT, "Confident Assignment");
        ptmConfidenceTooltipMap.put(PtmScoring.VERY_CONFIDENT, "Very Confident Assignment");

        peptidesTable.getColumn("PTM").setCellRenderer(new JSparklinesIntegerColorTableCellRenderer(Color.lightGray, ptmConfidenceColorMap, ptmConfidenceTooltipMap));
        relatedPeptidesTable.getColumn("PTM").setCellRenderer(new JSparklinesIntegerColorTableCellRenderer(Color.lightGray, ptmConfidenceColorMap, ptmConfidenceTooltipMap));
        selectedPsmsTable.getColumn("PTM").setCellRenderer(new JSparklinesIntegerColorTableCellRenderer(Color.lightGray, ptmConfidenceColorMap, ptmConfidenceTooltipMap));
        relatedPsmsTable.getColumn("PTM").setCellRenderer(new JSparklinesIntegerColorTableCellRenderer(Color.lightGray, ptmConfidenceColorMap, ptmConfidenceTooltipMap));

        peptidesTable.getColumn("Peptide").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100.0, peptideShakerGUI.getSparklineColor()));
        ((JSparklinesBarChartTableCellRenderer) peptidesTable.getColumn("Peptide").getCellRenderer()).showNumberAndChart(
                true, peptideShakerGUI.getLabelWidth() - 20, peptideShakerGUI.getScoreAndConfidenceDecimalFormat());
        peptidesTable.getColumn("  ").setCellRenderer(new TrueFalseIconRenderer(
                new ImageIcon(this.getClass().getResource("/icons/accept.png")),
                new ImageIcon(this.getClass().getResource("/icons/Error_3.png")),
                "Validated", "Not Validated"));

        relatedPeptidesTable.getColumn("Peptide").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100.0, peptideShakerGUI.getSparklineColor()));
        ((JSparklinesBarChartTableCellRenderer) relatedPeptidesTable.getColumn("Peptide").getCellRenderer()).showNumberAndChart(
                true, peptideShakerGUI.getLabelWidth() - 20, peptideShakerGUI.getScoreAndConfidenceDecimalFormat());
        relatedPeptidesTable.getColumn("  ").setCellRenderer(new TrueFalseIconRenderer(
                new ImageIcon(this.getClass().getResource("/icons/accept.png")),
                new ImageIcon(this.getClass().getResource("/icons/Error_3.png")),
                "Validated", "Not Validated"));

        selectedPsmsTable.getColumn("Charge").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 10.0, peptideShakerGUI.getSparklineColor()));
        ((JSparklinesBarChartTableCellRenderer) selectedPsmsTable.getColumn("Charge").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth() - 30);
        selectedPsmsTable.getColumn("  ").setCellRenderer(new TrueFalseIconRenderer(
                new ImageIcon(this.getClass().getResource("/icons/accept.png")),
                new ImageIcon(this.getClass().getResource("/icons/Error_3.png")),
                "Validated", "Not Validated"));
        selectedPsmsTable.getColumn("RT").setCellRenderer(new JSparklinesIntervalChartTableCellRenderer(PlotOrientation.HORIZONTAL, 0d,
                1000d, 10d, peptideShakerGUI.getSparklineColor(), peptideShakerGUI.getSparklineColor()));
        ((JSparklinesIntervalChartTableCellRenderer) selectedPsmsTable.getColumn("RT").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth() + 5);
        ((JSparklinesIntervalChartTableCellRenderer) selectedPsmsTable.getColumn("RT").getCellRenderer()).showReferenceLine(true, 0.02, java.awt.Color.BLACK);
        selectedPsmsTable.getColumn("   ").setCellRenderer(new TrueFalseIconRenderer(
                new ImageIcon(this.getClass().getResource("/icons/star_yellow.png")),
                new ImageIcon(this.getClass().getResource("/icons/star_grey.png")),
                new ImageIcon(this.getClass().getResource("/icons/star_grey.png")),
                "Starred", null, null));

        relatedPsmsTable.getColumn("Charge").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 10.0, peptideShakerGUI.getSparklineColor()));
        ((JSparklinesBarChartTableCellRenderer) relatedPsmsTable.getColumn("Charge").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth() - 30);
        relatedPsmsTable.getColumn("  ").setCellRenderer(new TrueFalseIconRenderer(
                new ImageIcon(this.getClass().getResource("/icons/accept.png")),
                new ImageIcon(this.getClass().getResource("/icons/Error_3.png")),
                "Validated", "Not Validated"));
        relatedPsmsTable.getColumn("RT").setCellRenderer(new JSparklinesIntervalChartTableCellRenderer(PlotOrientation.HORIZONTAL, 0d,
                1000d, 10d, peptideShakerGUI.getSparklineColor(), peptideShakerGUI.getSparklineColor()));
        ((JSparklinesIntervalChartTableCellRenderer) relatedPsmsTable.getColumn("RT").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth() + 5);
        ((JSparklinesIntervalChartTableCellRenderer) relatedPsmsTable.getColumn("RT").getCellRenderer()).showReferenceLine(true, 0.02, java.awt.Color.BLACK);
        relatedPsmsTable.getColumn("   ").setCellRenderer(new TrueFalseIconRenderer(
                new ImageIcon(this.getClass().getResource("/icons/star_yellow.png")),
                new ImageIcon(this.getClass().getResource("/icons/star_grey.png")),
                new ImageIcon(this.getClass().getResource("/icons/star_grey.png")),
                "Starred", null, null));

        // ptm color coding
        ptmJTable.getColumn("  ").setCellRenderer(new JSparklinesColorTableCellRenderer());

        // set up the table header tooltips
        selectedPeptidesTableToolTips = new ArrayList<String>();
        selectedPeptidesTableToolTips.add(null);
        selectedPeptidesTableToolTips.add("Starred");
        selectedPeptidesTableToolTips.add("Peptide Inference Class");
        selectedPeptidesTableToolTips.add("Peptide Sequence");
        selectedPeptidesTableToolTips.add("PTM Location Confidence");
        selectedPeptidesTableToolTips.add("Peptide Confidence");
        selectedPeptidesTableToolTips.add("Peptide Validated");

        relatedPeptidesTableToolTips = new ArrayList<String>();
        relatedPeptidesTableToolTips.add(null);
        relatedPeptidesTableToolTips.add("Starred");
        relatedPeptidesTableToolTips.add("Peptide Inference Class");
        relatedPeptidesTableToolTips.add("Peptide Sequence");
        relatedPeptidesTableToolTips.add("PTM Location Confidence");
        relatedPeptidesTableToolTips.add("Peptide Confidence");
        relatedPeptidesTableToolTips.add("Peptide Validated");

        selectedPsmsTableToolTips = new ArrayList<String>();
        selectedPsmsTableToolTips.add(null);
        selectedPsmsTableToolTips.add("Starred");
        selectedPsmsTableToolTips.add("Peptide Sequence");
        selectedPsmsTableToolTips.add("PTM Location Confidence");
        selectedPsmsTableToolTips.add("Precursor Charge");
        selectedPsmsTableToolTips.add("Precursor Retention Time");
        selectedPsmsTableToolTips.add("PSM Validated");

        relatedPsmsTableToolTips = new ArrayList<String>();
        relatedPsmsTableToolTips.add(null);
        relatedPsmsTableToolTips.add("Starred");
        relatedPsmsTableToolTips.add("Peptide Sequence");
        relatedPsmsTableToolTips.add("PTM Location Confidence");
        relatedPsmsTableToolTips.add("Precursor Charge");
        relatedPsmsTableToolTips.add("Precursor Retention Time");
        relatedPsmsTableToolTips.add("PSM Validated");

        ptmTableToolTips = new ArrayList<String>();
        ptmTableToolTips.add("PTM Color");
        ptmTableToolTips.add("PTM Name");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ptmAndPeptideSelectionPanel = new javax.swing.JPanel();
        ptmPanel = new javax.swing.JPanel();
        ptmLayeredLayeredPane = new javax.swing.JLayeredPane();
        ptmLayeredPanel = new javax.swing.JPanel();
        ptmJScrollPane = new javax.swing.JScrollPane();
        ptmJTable = new JTable() {
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        String tip = (String) ptmTableToolTips.get(realIndex);
                        return tip;
                    }
                };
            }
        };
        ptmSelectionHelpJButton = new javax.swing.JButton();
        contextMenuPtmBackgroundPanel = new javax.swing.JPanel();
        peptideTablesJSplitPane = new javax.swing.JSplitPane();
        modifiedPeptidesPanel = new javax.swing.JPanel();
        modifiedPeptidesLayeredPane = new javax.swing.JLayeredPane();
        selectedPeptidesJPanel = new javax.swing.JPanel();
        selectedPeptidesJSplitPane = new javax.swing.JSplitPane();
        peptidesTableJScrollPane = new javax.swing.JScrollPane();
        peptidesTable =         new JTable() {

            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {

                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        String tip = (String) selectedPeptidesTableToolTips.get(realIndex);
                        return tip;
                    }
                };
            }

            /**
            * Returns false to indicate that <strong class="highlight">horizontal</strong> scrollbars are required
            * to display the table while honoring perferred column widths. Returns
            * true if the table can be displayed in viewport without <strong class="highlight">horizontal</strong>
            * scrollbars.
            *
            * @return true if an auto-resizing mode is enabled
            *   and the viewport width is larger than the table's
            *   preferred size, otherwise return false.
            * @see Scrollable#getScrollableTracksViewportWidth
            */
            public boolean getScrollableTracksViewportWidth() {
                if (autoResizeMode != AUTO_RESIZE_OFF) {
                    if (getParent() instanceof JViewport) {
                        return (((JViewport) getParent()).getWidth() > getPreferredSize().width);
                    }
                }
                return false;
            }
        };
        modificationProfileSelectedPeptideJPanel = new javax.swing.JPanel();
        modificationProfileHelpJButton = new javax.swing.JButton();
        exportModifiedPeptideProfileJButton = new javax.swing.JButton();
        contextMenuModifiedPeptidesBackgroundPanel = new javax.swing.JPanel();
        relatedPeptidesJPanel = new javax.swing.JPanel();
        relatedPeptiesLayeredPane = new javax.swing.JLayeredPane();
        relatedPeptidesPanel = new javax.swing.JPanel();
        relatedPeptidesJSplitPane = new javax.swing.JSplitPane();
        relatedPeptidesTableJScrollPane = new javax.swing.JScrollPane();
        relatedPeptidesTable =         new JTable() {

            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {

                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        String tip = (String) relatedPeptidesTableToolTips.get(realIndex);
                        return tip;
                    }
                };
            }

            /**
            * Returns false to indicate that <strong class="highlight">horizontal</strong> scrollbars are required
            * to display the table while honoring perferred column widths. Returns
            * true if the table can be displayed in viewport without <strong class="highlight">horizontal</strong>
            * scrollbars.
            *
            * @return true if an auto-resizing mode is enabled
            *   and the viewport width is larger than the table's
            *   preferred size, otherwise return false.
            * @see Scrollable#getScrollableTracksViewportWidth
            */
            public boolean getScrollableTracksViewportWidth() {
                if (autoResizeMode != AUTO_RESIZE_OFF) {
                    if (getParent() instanceof JViewport) {
                        return (((JViewport) getParent()).getWidth() > getPreferredSize().width);
                    }
                }
                return false;
            }
        };
        modificationProfileRelatedPeptideJPanel = new javax.swing.JPanel();
        relatedProfileHelpJButton = new javax.swing.JButton();
        exportRelatedPeptideProfileJButton = new javax.swing.JButton();
        contextMenuRelatedPeptidesBackgroundPanel = new javax.swing.JPanel();
        psmSpectraSplitPane = new javax.swing.JSplitPane();
        spectrumAndFragmentIonJPanel = new javax.swing.JPanel();
        spectrumLayeredPane = new javax.swing.JLayeredPane();
        spectrumAndFragmentIonPanel = new javax.swing.JPanel();
        slidersSplitPane = new javax.swing.JSplitPane();
        spectrumTabbedPane = new javax.swing.JTabbedPane();
        ptmTableJPanel = new javax.swing.JPanel();
        ptmTableJScrollPane = new javax.swing.JScrollPane();
        ptmTableJToolBar = new javax.swing.JToolBar();
        ptmTableAnnotationMenuPanel = new javax.swing.JPanel();
        psmAScoresJPanel = new javax.swing.JPanel();
        psmAScoresScrollPane = new javax.swing.JScrollPane();
        psmAScoresTable = new javax.swing.JTable();
        aScoreMinValueJLabel = new javax.swing.JLabel();
        aScoreGradientPanel = new javax.swing.JPanel();
        aScoreMaxValueJLabel = new javax.swing.JLabel();
        psmDeltaScoresJPanel = new javax.swing.JPanel();
        psmDeltaScrollPane = new javax.swing.JScrollPane();
        psmDeltaScoresTable = new javax.swing.JTable();
        deltaScoreGradientPanel = new javax.swing.JPanel();
        deltaScoreMinValueJLabel = new javax.swing.JLabel();
        deltaScoreMaxValueJLabel = new javax.swing.JLabel();
        spectrumJPanel = new javax.swing.JPanel();
        spectrumJToolBar = new javax.swing.JToolBar();
        spectrumAnnotationMenuPanel = new javax.swing.JPanel();
        spectrumChartJPanel = new javax.swing.JPanel();
        sliderPanel = new javax.swing.JPanel();
        accuracySlider = new javax.swing.JSlider();
        intensitySlider = new javax.swing.JSlider();
        spectrumHelpJButton = new javax.swing.JButton();
        exportSpectrumJButton = new javax.swing.JButton();
        contextMenuSpectrumBackgroundPanel = new javax.swing.JPanel();
        psmSplitPane = new javax.swing.JSplitPane();
        modPsmsPanel = new javax.swing.JPanel();
        psmsModPeptidesLayeredPane = new javax.swing.JLayeredPane();
        modsPsmsLayeredPanel = new javax.swing.JPanel();
        psmsModifiedTableJScrollPane = new javax.swing.JScrollPane();
        selectedPsmsTable = new JTable() {
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        String tip = (String) selectedPsmsTableToolTips.get(realIndex);
                        return tip;
                    }
                };
            }
        };
        modifiedPsmsHelpJButton = new javax.swing.JButton();
        exportModifiedPsmsJButton = new javax.swing.JButton();
        contextMenuModPsmsBackgroundPanel = new javax.swing.JPanel();
        relatedPsmsJPanel = new javax.swing.JPanel();
        psmsRelatedPeptidesJLayeredPane = new javax.swing.JLayeredPane();
        relatedPsmsPanel = new javax.swing.JPanel();
        psmsRelatedTableJScrollPane = new javax.swing.JScrollPane();
        relatedPsmsTable = new JTable() {
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        String tip = (String) selectedPsmsTableToolTips.get(realIndex);
                        return tip;
                    }
                };
            }
        };
        relatedPsmsHelpJButton = new javax.swing.JButton();
        exportRelatedPsmsJButton = new javax.swing.JButton();
        contextMenuRelatedPsmsBackgroundPanel = new javax.swing.JPanel();

        setBackground(new java.awt.Color(255, 255, 255));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        ptmAndPeptideSelectionPanel.setOpaque(false);

        ptmPanel.setOpaque(false);

        ptmLayeredPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Variable Modifications"));
        ptmLayeredPanel.setOpaque(false);

        ptmJScrollPane.setOpaque(false);

        ptmJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "  ", "PTM"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class
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
        ptmJTable.setOpaque(false);
        ptmJTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        ptmJTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                ptmJTableMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                ptmJTableMouseReleased(evt);
            }
        });
        ptmJTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                ptmJTableMouseMoved(evt);
            }
        });
        ptmJTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                ptmJTableKeyReleased(evt);
            }
        });
        ptmJScrollPane.setViewportView(ptmJTable);

        javax.swing.GroupLayout ptmLayeredPanelLayout = new javax.swing.GroupLayout(ptmLayeredPanel);
        ptmLayeredPanel.setLayout(ptmLayeredPanelLayout);
        ptmLayeredPanelLayout.setHorizontalGroup(
            ptmLayeredPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 248, Short.MAX_VALUE)
            .addGroup(ptmLayeredPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(ptmLayeredPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(ptmJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        ptmLayeredPanelLayout.setVerticalGroup(
            ptmLayeredPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 373, Short.MAX_VALUE)
            .addGroup(ptmLayeredPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(ptmLayeredPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(ptmJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        ptmLayeredPanel.setBounds(0, 0, 260, 400);
        ptmLayeredLayeredPane.add(ptmLayeredPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        ptmSelectionHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        ptmSelectionHelpJButton.setToolTipText("Help");
        ptmSelectionHelpJButton.setBorder(null);
        ptmSelectionHelpJButton.setBorderPainted(false);
        ptmSelectionHelpJButton.setContentAreaFilled(false);
        ptmSelectionHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        ptmSelectionHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                ptmSelectionHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                ptmSelectionHelpJButtonMouseExited(evt);
            }
        });
        ptmSelectionHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ptmSelectionHelpJButtonActionPerformed(evt);
            }
        });
        ptmSelectionHelpJButton.setBounds(240, 0, 10, 25);
        ptmLayeredLayeredPane.add(ptmSelectionHelpJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        contextMenuPtmBackgroundPanel.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout contextMenuPtmBackgroundPanelLayout = new javax.swing.GroupLayout(contextMenuPtmBackgroundPanel);
        contextMenuPtmBackgroundPanel.setLayout(contextMenuPtmBackgroundPanelLayout);
        contextMenuPtmBackgroundPanelLayout.setHorizontalGroup(
            contextMenuPtmBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );
        contextMenuPtmBackgroundPanelLayout.setVerticalGroup(
            contextMenuPtmBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        contextMenuPtmBackgroundPanel.setBounds(230, 0, 20, 20);
        ptmLayeredLayeredPane.add(contextMenuPtmBackgroundPanel, javax.swing.JLayeredPane.POPUP_LAYER);

        javax.swing.GroupLayout ptmPanelLayout = new javax.swing.GroupLayout(ptmPanel);
        ptmPanel.setLayout(ptmPanelLayout);
        ptmPanelLayout.setHorizontalGroup(
            ptmPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ptmLayeredLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
        );
        ptmPanelLayout.setVerticalGroup(
            ptmPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ptmLayeredLayeredPane)
        );

        peptideTablesJSplitPane.setBorder(null);
        peptideTablesJSplitPane.setDividerLocation(170);
        peptideTablesJSplitPane.setDividerSize(0);
        peptideTablesJSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        peptideTablesJSplitPane.setResizeWeight(0.5);
        peptideTablesJSplitPane.setOpaque(false);

        modifiedPeptidesPanel.setOpaque(false);

        selectedPeptidesJPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Modified Peptides"));
        selectedPeptidesJPanel.setOpaque(false);

        selectedPeptidesJSplitPane.setBorder(null);
        selectedPeptidesJSplitPane.setDividerLocation(400);
        selectedPeptidesJSplitPane.setDividerSize(0);
        selectedPeptidesJSplitPane.setResizeWeight(0.5);
        selectedPeptidesJSplitPane.setOpaque(false);

        peptidesTableJScrollPane.setOpaque(false);

        peptidesTable.setModel(new PeptideTable());
        peptidesTable.setOpaque(false);
        peptidesTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        peptidesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                peptidesTableMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                peptidesTableMouseReleased(evt);
            }
        });
        peptidesTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                peptidesTableMouseMoved(evt);
            }
        });
        peptidesTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                peptidesTableKeyReleased(evt);
            }
        });
        peptidesTableJScrollPane.setViewportView(peptidesTable);

        selectedPeptidesJSplitPane.setLeftComponent(peptidesTableJScrollPane);

        modificationProfileSelectedPeptideJPanel.setBackground(new java.awt.Color(255, 255, 255));
        modificationProfileSelectedPeptideJPanel.setOpaque(false);
        modificationProfileSelectedPeptideJPanel.setLayout(new java.awt.GridLayout(3, 1));
        selectedPeptidesJSplitPane.setRightComponent(modificationProfileSelectedPeptideJPanel);

        javax.swing.GroupLayout selectedPeptidesJPanelLayout = new javax.swing.GroupLayout(selectedPeptidesJPanel);
        selectedPeptidesJPanel.setLayout(selectedPeptidesJPanelLayout);
        selectedPeptidesJPanelLayout.setHorizontalGroup(
            selectedPeptidesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, selectedPeptidesJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(selectedPeptidesJSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 778, Short.MAX_VALUE)
                .addContainerGap())
        );
        selectedPeptidesJPanelLayout.setVerticalGroup(
            selectedPeptidesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(selectedPeptidesJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(selectedPeptidesJSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                .addContainerGap())
        );

        selectedPeptidesJPanel.setBounds(0, 0, 810, 170);
        modifiedPeptidesLayeredPane.add(selectedPeptidesJPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        modificationProfileHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        modificationProfileHelpJButton.setToolTipText("Help");
        modificationProfileHelpJButton.setBorder(null);
        modificationProfileHelpJButton.setBorderPainted(false);
        modificationProfileHelpJButton.setContentAreaFilled(false);
        modificationProfileHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        modificationProfileHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                modificationProfileHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                modificationProfileHelpJButtonMouseExited(evt);
            }
        });
        modificationProfileHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modificationProfileHelpJButtonActionPerformed(evt);
            }
        });
        modificationProfileHelpJButton.setBounds(747, 0, 10, 25);
        modifiedPeptidesLayeredPane.add(modificationProfileHelpJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        exportModifiedPeptideProfileJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        exportModifiedPeptideProfileJButton.setToolTipText("Export");
        exportModifiedPeptideProfileJButton.setBorder(null);
        exportModifiedPeptideProfileJButton.setBorderPainted(false);
        exportModifiedPeptideProfileJButton.setContentAreaFilled(false);
        exportModifiedPeptideProfileJButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        exportModifiedPeptideProfileJButton.setEnabled(false);
        exportModifiedPeptideProfileJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame.png"))); // NOI18N
        exportModifiedPeptideProfileJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                exportModifiedPeptideProfileJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                exportModifiedPeptideProfileJButtonMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                exportModifiedPeptideProfileJButtonMouseReleased(evt);
            }
        });
        exportModifiedPeptideProfileJButton.setBounds(730, 0, 10, 25);
        modifiedPeptidesLayeredPane.add(exportModifiedPeptideProfileJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        contextMenuModifiedPeptidesBackgroundPanel.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout contextMenuModifiedPeptidesBackgroundPanelLayout = new javax.swing.GroupLayout(contextMenuModifiedPeptidesBackgroundPanel);
        contextMenuModifiedPeptidesBackgroundPanel.setLayout(contextMenuModifiedPeptidesBackgroundPanelLayout);
        contextMenuModifiedPeptidesBackgroundPanelLayout.setHorizontalGroup(
            contextMenuModifiedPeptidesBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );
        contextMenuModifiedPeptidesBackgroundPanelLayout.setVerticalGroup(
            contextMenuModifiedPeptidesBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        contextMenuModifiedPeptidesBackgroundPanel.setBounds(730, 0, 30, 20);
        modifiedPeptidesLayeredPane.add(contextMenuModifiedPeptidesBackgroundPanel, javax.swing.JLayeredPane.POPUP_LAYER);

        javax.swing.GroupLayout modifiedPeptidesPanelLayout = new javax.swing.GroupLayout(modifiedPeptidesPanel);
        modifiedPeptidesPanel.setLayout(modifiedPeptidesPanelLayout);
        modifiedPeptidesPanelLayout.setHorizontalGroup(
            modifiedPeptidesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 812, Short.MAX_VALUE)
            .addGroup(modifiedPeptidesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(modifiedPeptidesLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 812, Short.MAX_VALUE))
        );
        modifiedPeptidesPanelLayout.setVerticalGroup(
            modifiedPeptidesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 170, Short.MAX_VALUE)
            .addGroup(modifiedPeptidesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(modifiedPeptidesLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE))
        );

        peptideTablesJSplitPane.setLeftComponent(modifiedPeptidesPanel);

        relatedPeptidesJPanel.setOpaque(false);

        relatedPeptidesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Related Peptides"));
        relatedPeptidesPanel.setOpaque(false);

        relatedPeptidesJSplitPane.setBorder(null);
        relatedPeptidesJSplitPane.setDividerLocation(400);
        relatedPeptidesJSplitPane.setDividerSize(0);
        relatedPeptidesJSplitPane.setOpaque(false);

        relatedPeptidesTableJScrollPane.setOpaque(false);

        relatedPeptidesTable.setModel(new RelatedPeptidesTable());
        relatedPeptidesTable.setOpaque(false);
        relatedPeptidesTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        relatedPeptidesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                relatedPeptidesTableMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                relatedPeptidesTableMouseReleased(evt);
            }
        });
        relatedPeptidesTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                relatedPeptidesTableMouseMoved(evt);
            }
        });
        relatedPeptidesTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                relatedPeptidesTableKeyReleased(evt);
            }
        });
        relatedPeptidesTableJScrollPane.setViewportView(relatedPeptidesTable);

        relatedPeptidesJSplitPane.setLeftComponent(relatedPeptidesTableJScrollPane);

        modificationProfileRelatedPeptideJPanel.setBackground(new java.awt.Color(255, 255, 255));
        modificationProfileRelatedPeptideJPanel.setOpaque(false);
        modificationProfileRelatedPeptideJPanel.setLayout(new java.awt.GridLayout(3, 1));
        relatedPeptidesJSplitPane.setRightComponent(modificationProfileRelatedPeptideJPanel);

        javax.swing.GroupLayout relatedPeptidesPanelLayout = new javax.swing.GroupLayout(relatedPeptidesPanel);
        relatedPeptidesPanel.setLayout(relatedPeptidesPanelLayout);
        relatedPeptidesPanelLayout.setHorizontalGroup(
            relatedPeptidesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 798, Short.MAX_VALUE)
            .addGroup(relatedPeptidesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(relatedPeptidesPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(relatedPeptidesJSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 778, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        relatedPeptidesPanelLayout.setVerticalGroup(
            relatedPeptidesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 203, Short.MAX_VALUE)
            .addGroup(relatedPeptidesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(relatedPeptidesPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(relatedPeptidesJSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        relatedPeptidesPanel.setBounds(0, 0, 810, 230);
        relatedPeptiesLayeredPane.add(relatedPeptidesPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        relatedProfileHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        relatedProfileHelpJButton.setToolTipText("Help");
        relatedProfileHelpJButton.setBorder(null);
        relatedProfileHelpJButton.setBorderPainted(false);
        relatedProfileHelpJButton.setContentAreaFilled(false);
        relatedProfileHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        relatedProfileHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                relatedProfileHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                relatedProfileHelpJButtonMouseExited(evt);
            }
        });
        relatedProfileHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                relatedProfileHelpJButtonActionPerformed(evt);
            }
        });
        relatedProfileHelpJButton.setBounds(750, 0, 10, 25);
        relatedPeptiesLayeredPane.add(relatedProfileHelpJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        exportRelatedPeptideProfileJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        exportRelatedPeptideProfileJButton.setToolTipText("Export");
        exportRelatedPeptideProfileJButton.setBorder(null);
        exportRelatedPeptideProfileJButton.setBorderPainted(false);
        exportRelatedPeptideProfileJButton.setContentAreaFilled(false);
        exportRelatedPeptideProfileJButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame_grey.png"))); // NOI18N
        exportRelatedPeptideProfileJButton.setEnabled(false);
        exportRelatedPeptideProfileJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/export_no_frame.png"))); // NOI18N
        exportRelatedPeptideProfileJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                exportRelatedPeptideProfileJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                exportRelatedPeptideProfileJButtonMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                exportRelatedPeptideProfileJButtonMouseReleased(evt);
            }
        });
        exportRelatedPeptideProfileJButton.setBounds(740, 0, 10, 25);
        relatedPeptiesLayeredPane.add(exportRelatedPeptideProfileJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        contextMenuRelatedPeptidesBackgroundPanel.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout contextMenuRelatedPeptidesBackgroundPanelLayout = new javax.swing.GroupLayout(contextMenuRelatedPeptidesBackgroundPanel);
        contextMenuRelatedPeptidesBackgroundPanel.setLayout(contextMenuRelatedPeptidesBackgroundPanelLayout);
        contextMenuRelatedPeptidesBackgroundPanelLayout.setHorizontalGroup(
            contextMenuRelatedPeptidesBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );
        contextMenuRelatedPeptidesBackgroundPanelLayout.setVerticalGroup(
            contextMenuRelatedPeptidesBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        contextMenuRelatedPeptidesBackgroundPanel.setBounds(730, 0, 30, 20);
        relatedPeptiesLayeredPane.add(contextMenuRelatedPeptidesBackgroundPanel, javax.swing.JLayeredPane.POPUP_LAYER);

        javax.swing.GroupLayout relatedPeptidesJPanelLayout = new javax.swing.GroupLayout(relatedPeptidesJPanel);
        relatedPeptidesJPanel.setLayout(relatedPeptidesJPanelLayout);
        relatedPeptidesJPanelLayout.setHorizontalGroup(
            relatedPeptidesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(relatedPeptiesLayeredPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 812, Short.MAX_VALUE)
        );
        relatedPeptidesJPanelLayout.setVerticalGroup(
            relatedPeptidesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(relatedPeptiesLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
        );

        peptideTablesJSplitPane.setRightComponent(relatedPeptidesJPanel);

        javax.swing.GroupLayout ptmAndPeptideSelectionPanelLayout = new javax.swing.GroupLayout(ptmAndPeptideSelectionPanel);
        ptmAndPeptideSelectionPanel.setLayout(ptmAndPeptideSelectionPanelLayout);
        ptmAndPeptideSelectionPanelLayout.setHorizontalGroup(
            ptmAndPeptideSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ptmAndPeptideSelectionPanelLayout.createSequentialGroup()
                .addComponent(ptmPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(peptideTablesJSplitPane))
        );
        ptmAndPeptideSelectionPanelLayout.setVerticalGroup(
            ptmAndPeptideSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ptmPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(peptideTablesJSplitPane)
        );

        psmSpectraSplitPane.setBorder(null);
        psmSpectraSplitPane.setDividerLocation(500);
        psmSpectraSplitPane.setDividerSize(0);
        psmSpectraSplitPane.setResizeWeight(0.5);
        psmSpectraSplitPane.setOpaque(false);

        spectrumAndFragmentIonJPanel.setOpaque(false);
        spectrumAndFragmentIonJPanel.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                spectrumAndFragmentIonJPanelMouseWheelMoved(evt);
            }
        });

        spectrumAndFragmentIonPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Spectrum & Fragment Ions"));
        spectrumAndFragmentIonPanel.setOpaque(false);

        slidersSplitPane.setBorder(null);
        slidersSplitPane.setDividerLocation(500);
        slidersSplitPane.setDividerSize(0);
        slidersSplitPane.setOpaque(false);

        spectrumTabbedPane.setBackground(new java.awt.Color(255, 255, 255));
        spectrumTabbedPane.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        spectrumTabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spectrumTabbedPaneStateChanged(evt);
            }
        });

        ptmTableJPanel.setBackground(new java.awt.Color(255, 255, 255));

        ptmTableJScrollPane.setOpaque(false);
        ptmTableJScrollPane.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                ptmTableJScrollPaneMouseWheelMoved(evt);
            }
        });

        ptmTableJToolBar.setBackground(new java.awt.Color(255, 255, 255));
        ptmTableJToolBar.setBorder(null);
        ptmTableJToolBar.setFloatable(false);
        ptmTableJToolBar.setRollover(true);
        ptmTableJToolBar.setBorderPainted(false);

        ptmTableAnnotationMenuPanel.setLayout(new javax.swing.BoxLayout(ptmTableAnnotationMenuPanel, javax.swing.BoxLayout.LINE_AXIS));
        ptmTableJToolBar.add(ptmTableAnnotationMenuPanel);

        javax.swing.GroupLayout ptmTableJPanelLayout = new javax.swing.GroupLayout(ptmTableJPanel);
        ptmTableJPanel.setLayout(ptmTableJPanelLayout);
        ptmTableJPanelLayout.setHorizontalGroup(
            ptmTableJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ptmTableJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ptmTableJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(ptmTableJScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE)
                    .addComponent(ptmTableJToolBar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE))
                .addContainerGap())
        );
        ptmTableJPanelLayout.setVerticalGroup(
            ptmTableJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ptmTableJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ptmTableJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ptmTableJToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        spectrumTabbedPane.addTab("PTM Table", ptmTableJPanel);

        psmAScoresJPanel.setOpaque(false);

        psmAScoresScrollPane.setOpaque(false);

        psmAScoresTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        psmAScoresTable.setOpaque(false);
        psmAScoresScrollPane.setViewportView(psmAScoresTable);

        aScoreMinValueJLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        aScoreMinValueJLabel.setText("0%");

        aScoreGradientPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        aScoreGradientPanel.setToolTipText("A-Score Certainty");
        aScoreGradientPanel.setLayout(new java.awt.BorderLayout());

        aScoreMaxValueJLabel.setText("100%");

        javax.swing.GroupLayout psmAScoresJPanelLayout = new javax.swing.GroupLayout(psmAScoresJPanel);
        psmAScoresJPanel.setLayout(psmAScoresJPanelLayout);
        psmAScoresJPanelLayout.setHorizontalGroup(
            psmAScoresJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(psmAScoresJPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(aScoreMinValueJLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(aScoreGradientPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(aScoreMaxValueJLabel)
                .addGap(20, 20, 20))
            .addGroup(psmAScoresJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(psmAScoresScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE)
                .addContainerGap())
        );

        psmAScoresJPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {aScoreMaxValueJLabel, aS
