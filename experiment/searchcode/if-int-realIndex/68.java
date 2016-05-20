package eu.isas.peptideshaker.gui.tabpanels;

import com.compomics.util.Util;
import com.compomics.util.examples.BareBonesBrowserLaunch;
import com.compomics.util.experiment.identification.Identification;
import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.gui.dialogs.ProgressDialogParent;
import com.compomics.util.gui.dialogs.ProgressDialogX;
import com.compomics.util.gui.renderers.AlignedListCellRenderer;
import eu.isas.peptideshaker.gui.ExportGraphicsDialog;
import eu.isas.peptideshaker.gui.HelpDialog;
import eu.isas.peptideshaker.gui.PeptideShakerGUI;
import eu.isas.peptideshaker.gui.tablemodels.ProteinGoTableModel;
import eu.isas.peptideshaker.myparameters.PSParameter;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import no.uib.jsparklines.data.JSparklinesDataSeries;
import no.uib.jsparklines.data.JSparklinesDataset;
import no.uib.jsparklines.data.ValueAndBooleanDataPoint;
import no.uib.jsparklines.data.XYDataPoint;
import no.uib.jsparklines.extra.HtmlLinksRenderer;
import no.uib.jsparklines.extra.TrueFalseIconRenderer;
import no.uib.jsparklines.renderers.JSparklinesBarChartTableCellRenderer;
import no.uib.jsparklines.renderers.JSparklinesTableCellRenderer;
import no.uib.jsparklines.renderers.JSparklinesTwoValueBarChartTableCellRenderer;
import no.uib.jsparklines.renderers.util.BarChartColorRenderer;
import org.apache.commons.math.distribution.HypergeometricDistributionImpl;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryMarker;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.Layer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * The PeptideShaker GO Enrichment Analysis tab.
 *
 * @author Harald Barsnes
 */
public class GOEAPanel extends javax.swing.JPanel implements ProgressDialogParent {

    /**
     * The protein table column header tooltips.
     */
    private ArrayList<String> proteinTableToolTips;
    /**
     * The progress dialog.
     */
    private ProgressDialogX progressDialog;
    /**
     * PeptideShaker GUI parent.
     */
    private PeptideShakerGUI peptideShakerGUI;
    /**
     * The GO mappings table column header tooltips.
     */
    private ArrayList<String> mappingsTableToolTips;
    /**
     * GO table tooltips.
     */
    private TreeMap<String, Integer> totalGoTermUsage;
    /**
     * GO term to protein mapping, key: GO accession number, element: list of
     * proteins.
     */
    private HashMap<String, HashSet<String>> goProteinMappings;
    /**
     * The sequence factory.
     */
    private SequenceFactory sequenceFactory = SequenceFactory.getInstance();
    /**
     * The distribution chart panel.
     */
    private ChartPanel distributionChartPanel = null;
    /**
     * The significance chart panel.
     */
    private ChartPanel signChartPanel = null;
    /**
     * The GO domain map.
     */
    private HashMap<String, String> goDomainMap;
    /**
     * The species map, key: latin name, element: ensembl database name.
     */
    private HashMap<String, String> speciesMap;
    /**
     * The Ensembl versions for the downloaded species.
     */
    private HashMap<String, String> ensemblVersionsMap;
    /**
     * The list of species.
     */
    private Vector<String> species;
    /**
     * The folder where the mapping files are located.
     */
    private String mappingsFolderPath;
    /**
     * If false, the mappings are not loaded and the analysis cannot be
     * performed.
     */
    private boolean goMappingsLoaded = false;
    /**
     * The species separator used in the species combobox.
     */
    private String speciesSeparator = "------------------------------------------------------------";
    /**
     * If true the progress bar is disposed of.
     */
    private static boolean cancelProgress = false;

    /**
     * Creates a new GOEAPanel.
     *
     * @param peptideShakerGUI
     */
    public GOEAPanel(PeptideShakerGUI peptideShakerGUI) {

        this.peptideShakerGUI = peptideShakerGUI;

        initComponents();
        setupGUI();

        mappingsFolderPath = peptideShakerGUI.getJarFilePath() + File.separator 
                + "resources" + File.separator + "conf"
                + File.separator + "gene_ontology" + File.separator;

        // load the go mapping files
        loadSpeciesAndGoDomains();
        speciesJComboBoxActionPerformed(null);
    }

    /**
     * Set up the GUI details.
     */
    private void setupGUI() {

        JTableHeader header = goMappingsTable.getTableHeader();
        header.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if (peptideShakerGUI.getIdentification() != null) {
                    updateGoPlots();
                }
            }
        });

        speciesJComboBox.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));

        goMappingsTable.getTableHeader().setReorderingAllowed(false);
        proteinTable.getTableHeader().setReorderingAllowed(false);
        goMappingsTable.setAutoCreateRowSorter(true);
        proteinTable.setAutoCreateRowSorter(true);

        // make sure that the scroll panes are see-through
        proteinGoMappingsScrollPane.getViewport().setOpaque(false);
        proteinsScrollPane.getViewport().setOpaque(false);

        // the index column
        goMappingsTable.getColumn("").setMaxWidth(60);
        goMappingsTable.getColumn("").setMinWidth(60);
        goMappingsTable.getColumn("  ").setMaxWidth(30);
        goMappingsTable.getColumn("  ").setMinWidth(30);

        double significanceLevel = 0.05;

        if (onePercentRadioButton.isSelected()) {
            significanceLevel = 0.01;
        }

        // cell renderers
        goMappingsTable.getColumn("GO Accession").setCellRenderer(new HtmlLinksRenderer(peptideShakerGUI.getSelectedRowHtmlTagFontColor(), peptideShakerGUI.getNotSelectedRowHtmlTagFontColor()));
        goMappingsTable.getColumn("Frequency All (%)").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100.0, Color.RED));
        ((JSparklinesBarChartTableCellRenderer) goMappingsTable.getColumn("Frequency All (%)").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth());
        goMappingsTable.getColumn("Frequency Dataset (%)").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100.0, peptideShakerGUI.getSparklineColor()));
        ((JSparklinesBarChartTableCellRenderer) goMappingsTable.getColumn("Frequency Dataset (%)").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth());
        goMappingsTable.getColumn("p-value").setCellRenderer(
                new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 1.0, peptideShakerGUI.getSparklineColor(), Color.lightGray, significanceLevel));
        ((JSparklinesBarChartTableCellRenderer) goMappingsTable.getColumn("p-value").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth());
        goMappingsTable.getColumn("Log2 Diff").setCellRenderer(new JSparklinesBarChartTableCellRenderer(
                PlotOrientation.HORIZONTAL, -10.0, 10.0, Color.RED, peptideShakerGUI.getSparklineColor(), Color.lightGray, 0));
        ((JSparklinesBarChartTableCellRenderer) goMappingsTable.getColumn("Log2 Diff").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth());
        goMappingsTable.getColumn("Frequency (%)").setCellRenderer(new JSparklinesTableCellRenderer(
                JSparklinesTableCellRenderer.PlotType.barChart,
                PlotOrientation.HORIZONTAL, 0.0, 100.0));
        goMappingsTable.getColumn("  ").setCellRenderer(new TrueFalseIconRenderer(
                new ImageIcon(this.getClass().getResource("/icons/selected_green.png")),
                null,
                "Selected", null));

        setProteinGoTableProperties();

        // make the tabs in the tabbed pane go from right to left
        goPlotsTabbedPane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        // set up the table header tooltips
        mappingsTableToolTips = new ArrayList<String>();
        mappingsTableToolTips.add(null);
        mappingsTableToolTips.add("Gene Ontology Accession");
        mappingsTableToolTips.add("Gene Ontology Term");
        mappingsTableToolTips.add("Gene Ontology Domain");
        mappingsTableToolTips.add("Frequency All (%)");
        mappingsTableToolTips.add("Frequency Dataset (%)");
        mappingsTableToolTips.add("Frequency (%) (All & Dataset))");
        mappingsTableToolTips.add("Log2 Difference (Dataset / All)");
        mappingsTableToolTips.add("<html>Hypergeometic Test<br>FDR-Corrected</html>");
        mappingsTableToolTips.add("Selected for Plots");

        proteinTableToolTips = new ArrayList<String>();
        proteinTableToolTips.add(null);
        proteinTableToolTips.add("Protein Accession Number");
        proteinTableToolTips.add("Protein Description");
        proteinTableToolTips.add("Protein Seqeunce Coverage (%) (Observed / Possible)");
        proteinTableToolTips.add("Number of Peptides (Validated / Total)");
        proteinTableToolTips.add("Number of Spectra (Validated / Total)");
        proteinTableToolTips.add("MS2 Quantification");
        proteinTableToolTips.add("Protein Confidence");
        proteinTableToolTips.add("Validated");
    }

    /**
     * Set the properties of the GO protein table.
     */
    private void setProteinGoTableProperties() {
        proteinTable.getColumn(" ").setMaxWidth(60);
        proteinTable.getColumn(" ").setMinWidth(60);
        proteinTable.getColumn("  ").setMaxWidth(30);
        proteinTable.getColumn("  ").setMinWidth(30);
        proteinTable.getColumn("Confidence").setMaxWidth(90);
        proteinTable.getColumn("Confidence").setMinWidth(90);

        // set the preferred size of the accession column
        int width = peptideShakerGUI.getPreferredColumnWidth(proteinTable, proteinTable.getColumn("Accession").getModelIndex(), 6);
        proteinTable.getColumn("Accession").setMinWidth(width);
        proteinTable.getColumn("Accession").setMaxWidth(width);

        proteinTable.getColumn("Accession").setCellRenderer(new HtmlLinksRenderer(peptideShakerGUI.getSelectedRowHtmlTagFontColor(), peptideShakerGUI.getNotSelectedRowHtmlTagFontColor()));
        proteinTable.getColumn("#Peptides").setCellRenderer(new JSparklinesTwoValueBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100.0,
                peptideShakerGUI.getSparklineColor(), peptideShakerGUI.getSparklineColorNonValidated(), false));
        ((JSparklinesTwoValueBarChartTableCellRenderer) proteinTable.getColumn("#Peptides").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth(), new DecimalFormat("0"));
        proteinTable.getColumn("#Spectra").setCellRenderer(new JSparklinesTwoValueBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100.0,
                peptideShakerGUI.getSparklineColor(), peptideShakerGUI.getSparklineColorNonValidated(), false));
        ((JSparklinesTwoValueBarChartTableCellRenderer) proteinTable.getColumn("#Spectra").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth(), new DecimalFormat("0"));
        proteinTable.getColumn("MS2 Quant.").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 10.0, peptideShakerGUI.getSparklineColor()));
        ((JSparklinesBarChartTableCellRenderer) proteinTable.getColumn("MS2 Quant.").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth());
        proteinTable.getColumn("Confidence").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100.0, peptideShakerGUI.getSparklineColor()));
        ((JSparklinesBarChartTableCellRenderer) proteinTable.getColumn("Confidence").getCellRenderer()).showNumberAndChart(
                true, peptideShakerGUI.getLabelWidth() - 20, peptideShakerGUI.getScoreAndConfidenceDecimalFormat());
        proteinTable.getColumn("  ").setCellRenderer(new TrueFalseIconRenderer(
                new ImageIcon(this.getClass().getResource("/icons/accept.png")),
                new ImageIcon(this.getClass().getResource("/icons/Error_3.png")),
                "Validated", "Not Validated"));
        proteinTable.getColumn("Coverage").setCellRenderer(new JSparklinesTwoValueBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100.0,
                peptideShakerGUI.getSparklineColor(), peptideShakerGUI.getUserPreferences().getSparklineColorNotFound(), true));
        ((JSparklinesTwoValueBarChartTableCellRenderer) proteinTable.getColumn("Coverage").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth(), new DecimalFormat("0.00"));
    }

    /**
     * Load the mapping files.
     */
    private void loadSpeciesAndGoDomains() {

        try {

            File speciesFile = new File(mappingsFolderPath + "species");
            File ensemblVersionsFile = new File(mappingsFolderPath + "ensembl_versions");
            File goDomainsFile = new File(mappingsFolderPath + "go_domains");

            goDomainMap = new HashMap<String, String>();
            species = new Vector<String>();
            speciesMap = new HashMap<String, String>();
            ensemblVersionsMap = new HashMap<String, String>();

            if (!goDomainsFile.exists()) {
                JOptionPane.showMessageDialog(this, "GO domains file \"" + goDomainsFile.getName() + "\" not found!\n"
                        + "Continuing without GO domains.", "File Not Found", JOptionPane.ERROR_MESSAGE);
            } else {

                // read the GO domains
                FileReader r = new FileReader(goDomainsFile);
                BufferedReader br = new BufferedReader(r);

                String line = br.readLine();

                while (line != null) {
                    String[] elements = line.split("\\t");
                    goDomainMap.put(elements[0], elements[1]);
                    line = br.readLine();
                }

                br.close();
                r.close();
            }

            if (ensemblVersionsFile.exists()) {

                // read the Ensembl versions
                FileReader r = new FileReader(ensemblVersionsFile);
                BufferedReader br = new BufferedReader(r);

                String line = br.readLine();

                while (line != null) {
                    String[] elements = line.split("\\t");
                    ensemblVersionsMap.put(elements[0], elements[1]);
                    line = br.readLine();
                }

                br.close();
                r.close();
            }


            if (!speciesFile.exists()) {
                JOptionPane.showMessageDialog(this, "GO species file \"" + speciesFile.getName() + "\" not found!\n"
                        + "GO Analysis Canceled.", "File Not Found", JOptionPane.ERROR_MESSAGE);
                goMappingsLoaded = false;
            } else {

                // read the species list
                FileReader r = new FileReader(speciesFile);
                BufferedReader br = new BufferedReader(r);

                String line = br.readLine();

                species.add("-- Select Species --");
                species.add(speciesSeparator);

                while (line != null) {
                    String[] elements = line.split("\\t");
                    speciesMap.put(elements[0], elements[1]);

                    if (species.size() == 5) {
                        species.add(speciesSeparator);
                    }

                    if (ensemblVersionsMap.containsKey(elements[1])) {
                        species.add(elements[0] + " [" + ensemblVersionsMap.get(elements[1]) + "]");
                    } else {
                        species.add(elements[0] + " [N/A]");
                    }

                    line = br.readLine();
                }

                br.close();
                r.close();

                speciesJComboBox.setModel(new DefaultComboBoxModel(species));
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occured when loading the species and GO domain file.\n"
                    + "GO Analysis Canceled.", "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Update the GO mappings.
     */
    public void displayResults() {

        if (peptideShakerGUI.getIdentification() != null) {

            String selectedSpecies = (String) speciesJComboBox.getSelectedItem();

            if (selectedSpecies.indexOf("[") != -1) {
                selectedSpecies = selectedSpecies.substring(0, selectedSpecies.indexOf("[") - 1);
            }

            String speciesDatabase = speciesMap.get(selectedSpecies);
            String goMappingsPath = mappingsFolderPath + speciesDatabase;

            final File goMappingsFile = new File(goMappingsPath);

            if (goMappingsFile.exists()) {

                progressDialog = new ProgressDialogX(peptideShakerGUI, this, true);
                progressDialog.setIndeterminate(true);

                new Thread(new Runnable() {

                    public void run() {
                        try {
                            progressDialog.setVisible(true);
                        } catch (IndexOutOfBoundsException e) {
                            // ignore
                        }
                        progressDialog.setTitle("Getting GO Mapping Files. Please Wait...");
                    }
                }, "ProgressDialog").start();

                new Thread("GoThread") {

                    @Override
                    public void run() {

                        // change the peptide shaker icon to a "waiting version"
                        peptideShakerGUI.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/peptide-shaker-orange.gif")));

                        // clear old table
                        DefaultTableModel dm = (DefaultTableModel) goMappingsTable.getModel();
                        dm.getDataVector().removeAllElements();
                        dm.fireTableDataChanged();

                        if (!goMappingsFile.exists()) {
                            progressDialog.dispose();
                            JOptionPane.showMessageDialog(peptideShakerGUI, "Mapping file \"" + goMappingsFile.getName() + "\" not found!",
                                    "File Not Found", JOptionPane.ERROR_MESSAGE);

                            // return the peptide shaker icon to the standard version
                            peptideShakerGUI.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/peptide-shaker.gif")));
                            return;
                        }

                        totalGoTermUsage = new TreeMap<String, Integer>();
                        goProteinMappings = new HashMap<String, HashSet<String>>();
                        TreeMap<String, Integer> datasetGoTermUsage = new TreeMap<String, Integer>();
                        HashMap<String, String> goTermToAccessionMap = new HashMap<String, String>();
                        HashMap<String, ArrayList<String>> proteinToGoMappings = new HashMap<String, ArrayList<String>>();

                        int totalNumberOfProteins = 0;

                        try {

                            progressDialog.setTitle("Getting GO Mappings. Please Wait...");

                            // read the GO mappings
                            FileReader r = new FileReader(goMappingsFile);
                            BufferedReader br = new BufferedReader(r);

                            // read and ignore the header
                            br.readLine();

                            String line = br.readLine();
                            
                            PSParameter proteinPSParameter = new PSParameter();
                            PSParameter probabilities = new PSParameter();

                            while (line != null && !cancelProgress) {

                                String[] elements = line.split("\\t");

                                if (elements.length == 3) {

                                    String proteinAccession = elements[0];
                                    String goAccession = elements[1];
                                    String goTerm = elements[2].toLowerCase();

                                    if (proteinAccession.length() > 0) {

                                        if (!proteinToGoMappings.containsKey(proteinAccession)) {
                                            ArrayList<String> proteinGoMappings = new ArrayList<String>();
                                            proteinGoMappings.add(goTerm);
                                            proteinToGoMappings.put(proteinAccession, proteinGoMappings);
                                        } else {
                                            proteinToGoMappings.get(proteinAccession).add(goTerm);
                                        }

                                        goTermToAccessionMap.put(goTerm, goAccession);

                                        if (totalGoTermUsage.containsKey(goTerm)) {
                                            totalGoTermUsage.put(goTerm, totalGoTermUsage.get(goTerm) + 1);
                                        } else {
                                            totalGoTermUsage.put(goTerm, 1);
                                        }

                                        totalNumberOfProteins++;

                                        // store the go term to protein mappings
                                        if (peptideShakerGUI.getIdentification().matchExists(proteinAccession)) { // @TODO: this might be slow?? but i don't see i way around this?

                                            proteinPSParameter = (PSParameter) peptideShakerGUI.getIdentification().getMatchParameter(proteinAccession, proteinPSParameter);
                                            probabilities = (PSParameter) peptideShakerGUI.getIdentification().getMatchParameter(proteinAccession, probabilities);

                                            if (proteinPSParameter.isValidated() && !ProteinMatch.isDecoy(proteinAccession) && !probabilities.isHidden()) {

                                                if (goProteinMappings.containsKey(goAccession)) {
                                                    if (!goProteinMappings.get(goAccession).contains(proteinAccession)) {
                                                        goProteinMappings.get(goAccession).add(proteinAccession);
                                                    }
                                                } else {
                                                    HashSet<String> tempProteinList = new HashSet<String>();
                                                    tempProteinList.add(proteinAccession);
                                                    goProteinMappings.put(goAccession, tempProteinList);
                                                }
                                            }
                                        }
                                    }
                                }

                                line = br.readLine();
                            }


                            // get go terms for dataset
                            Identification identification = peptideShakerGUI.getIdentification();
                            int totalNumberOfGoMappedProteinsInProject = 0;

                            progressDialog.setTitle("Mapping GO Terms. Please Wait...");
                            progressDialog.setIndeterminate(false);
                            progressDialog.setValue(0);
                            progressDialog.setMax(identification.getProteinIdentification().size());

                            for (String matchKey : identification.getProteinIdentification()) {

                                if (cancelProgress) {
                                    break;
                                }

                                progressDialog.incrementValue();

                                try {
                                    proteinPSParameter = (PSParameter) identification.getMatchParameter(matchKey, proteinPSParameter);
                                    probabilities = (PSParameter) peptideShakerGUI.getIdentification().getMatchParameter(matchKey, probabilities);

                                    if (proteinPSParameter.isValidated() && !ProteinMatch.isDecoy(matchKey) && !probabilities.isHidden()) {

                                        String mainAccession;

                                        if (ProteinMatch.getNProteins(matchKey) > 1) {
                                            mainAccession = identification.getProteinMatch(matchKey).getMainMatch();
                                        } else {
                                            mainAccession = matchKey;
                                        }
                                        
                                        if (proteinToGoMappings.containsKey(mainAccession)) {

                                            ArrayList<String> goTerms = proteinToGoMappings.get(mainAccession);

                                            for (int j = 0; j < goTerms.size(); j++) {
                                                if (datasetGoTermUsage.containsKey(goTerms.get(j))) {
                                                    datasetGoTermUsage.put(goTerms.get(j), datasetGoTermUsage.get(goTerms.get(j)) + 1);
                                                } else {
                                                    datasetGoTermUsage.put(goTerms.get(j), 1);
                                                }
                                            }

                                            totalNumberOfGoMappedProteinsInProject++;
                                        } else {
                                            // ignore, does not map to any GO terms in the current GO slim
                                        }
                                    }
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();

                                    progressDialog.dispose();
                                    // return the peptide shaker icon to the standard version
                                    peptideShakerGUI.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/peptide-shaker.gif")));
                                }
                            }

                            progressDialog.setTitle("Creating GO Plots. Please Wait...");
                            progressDialog.setValue(0);
                            progressDialog.setMax(totalGoTermUsage.entrySet().size());


                            // update the table
                            Double maxLog2Diff = 0.0;
                            ArrayList<Integer> indexes = new ArrayList<Integer>();
                            ArrayList<Double> pValues = new ArrayList<Double>();
                            
                            // display the number of go mapped proteins
                            goProteinCountLabel.setText("[GO Proteins: Ensembl: " + proteinToGoMappings.size() 
                                    + ", Project: " + totalNumberOfGoMappedProteinsInProject + "]");

                            for (Map.Entry<String, Integer> entry : totalGoTermUsage.entrySet()) {

                                if (cancelProgress) {
                                    break;
                                }

                                progressDialog.incrementValue();

                                String goTerm = entry.getKey();
                                Integer frequencyAll = entry.getValue();

                                String goAccession = goTermToAccessionMap.get(goTerm);
                                Integer frequencyDataset = 0;
                                Double percentDataset = 0.0;

                                if (datasetGoTermUsage.get(goTerm) != null) {
                                    frequencyDataset = datasetGoTermUsage.get(goTerm);
                                    percentDataset = ((double) frequencyDataset / totalNumberOfGoMappedProteinsInProject) * 100;
                                }
                                
                                Double percentAll = ((double) frequencyAll / proteinToGoMappings.size()) * 100;
                                Double pValue = new HypergeometricDistributionImpl(
                                        proteinToGoMappings.size(), // population size
                                        frequencyAll, // number of successes
                                        totalNumberOfGoMappedProteinsInProject // sample size
                                        ).probability(frequencyDataset);
                                Double log2Diff = Math.log(percentDataset / percentAll) / Math.log(2);

                                if (!log2Diff.isInfinite() && Math.abs(log2Diff) > maxLog2Diff) {
                                    maxLog2Diff = Math.abs(log2Diff);
                                }

                                String goDomain;

                                if (goDomainMap.get(goAccession) != null) {
                                    goDomain = goDomainMap.get(goAccession);
                                } else {

                                    // URL a GO Term in OBO xml format
                                    URL u = new URL("http://www.ebi.ac.uk/QuickGO/GTerm?id=" + goAccession + "&format=oboxml");

                                    // connect
                                    HttpURLConnection urlConnection = (HttpURLConnection) u.openConnection();

                                    // parse an XML document from the connection
                                    InputStream inputStream = urlConnection.getInputStream();
                                    Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
                                    inputStream.close();

                                    // XPath is here used to locate parts of an XML document
                                    XPath xpath = XPathFactory.newInstance().newXPath();

                                    // locate the domain
                                    goDomain = xpath.compile("/obo/term/namespace").evaluate(xml);

                                    goDomainMap.put(goAccession, goDomain);

                                    File goDomainsFile = new File(mappingsFolderPath + File.separator + "go_domains");

                                    if (!goDomainsFile.exists()) {
                                        JOptionPane.showMessageDialog(peptideShakerGUI, "GO domains file \"" + goDomainsFile.getName() + "\" not found!\n"
                                                + "Continuing without GO domains.", "File Not Found", JOptionPane.ERROR_MESSAGE);
                                    } else {

                                        // read the GO domains
                                        FileWriter fr = new FileWriter(goDomainsFile, true);
                                        BufferedWriter dbr = new BufferedWriter(fr);
                                        dbr.write(goAccession + "\t" + goDomain + "\n");

                                        dbr.close();
                                        fr.close();
                                    }
                                }

                                // add the data points for the first data series 
                                ArrayList<Double> dataAll = new ArrayList<Double>();
                                dataAll.add(percentAll);
                                ArrayList<Double> dataDataset = new ArrayList<Double>();
                                dataDataset.add(percentDataset);

                                // create a JSparklineDataSeries  
                                JSparklinesDataSeries sparklineDataseriesAll = new JSparklinesDataSeries(dataAll, Color.RED, "All");
                                JSparklinesDataSeries sparklineDataseriesDataset = new JSparklinesDataSeries(dataDataset, peptideShakerGUI.getSparklineColor(), "Dataset");

                                // add the data series to JSparklineDataset 
                                ArrayList<JSparklinesDataSeries> sparkLineDataSeries = new ArrayList<JSparklinesDataSeries>();
                                sparkLineDataSeries.add(sparklineDataseriesAll);
                                sparkLineDataSeries.add(sparklineDataseriesDataset);

                                JSparklinesDataset dataset = new JSparklinesDataset(sparkLineDataSeries);

                                pValues.add(pValue);
                                indexes.add(goMappingsTable.getRowCount());

                                ((DefaultTableModel) goMappingsTable.getModel()).addRow(new Object[]{
                                            goMappingsTable.getRowCount() + 1,
                                            addGoLink(goAccession),
                                            goTerm,
                                            goDomain,
                                            percentAll,
                                            percentDataset,
                                            dataset,
                                            new ValueAndBooleanDataPoint(log2Diff, false),
                                            pValue,
                                            true
                                        });
                            }

                            int significantCounter = 0;
                            double significanceLevel = 0.05;

                            if (onePercentRadioButton.isSelected()) {
                                significanceLevel = 0.01;
                            }

                            if (!cancelProgress) {

                                ((DefaultTableModel) goMappingsTable.getModel()).fireTableDataChanged();

                                // correct the p-values for multiple testing using benjamini-hochberg
                                sortPValues(pValues, indexes);

                                ((ValueAndBooleanDataPoint) ((DefaultTableModel) goMappingsTable.getModel()).getValueAt(
                                        indexes.get(0), goMappingsTable.getColumn("Log2 Diff").getModelIndex())).setSignificant(
                                        pValues.get(0) < significanceLevel);
                                ((DefaultTableModel) goMappingsTable.getModel()).setValueAt(new XYDataPoint(pValues.get(0), pValues.get(0)), indexes.get(0), 
                                        goMappingsTable.getColumn("p-value").getModelIndex());

                                if (pValues.get(0) < significanceLevel) {
                                    significantCounter++;
                                }

                                for (int i = 1; i < pValues.size(); i++) {

                                    if (cancelProgress) {
                                        break;
                                    }

                                    double tempPvalue = pValues.get(i) * pValues.size() / (pValues.size() - i);

                                    ((ValueAndBooleanDataPoint) ((DefaultTableModel) goMappingsTable.getModel()).getValueAt(
                                            indexes.get(i), goMappingsTable.getColumn("Log2 Diff").getModelIndex())).setSignificant(tempPvalue < significanceLevel);
                                    ((DefaultTableModel) goMappingsTable.getModel()).setValueAt(new XYDataPoint(tempPvalue, tempPvalue), indexes.get(i), 
                                            goMappingsTable.getColumn("p-value").getModelIndex());

                                    if (tempPvalue < significanceLevel) {
                                        significantCounter++;
                                    }
                                }

                                br.close();
                                r.close();
                            }

                            if (!cancelProgress) {

                                ((TitledBorder) mappingsPanel.getBorder()).setTitle("Gene Ontology Mappings (" + significantCounter + "/" + goMappingsTable.getRowCount() + ")");
                                mappingsPanel.repaint();

                                progressDialog.setIndeterminate(true);

                                // invoke later to give time for components to update
                                SwingUtilities.invokeLater(new Runnable() {

                                    public void run() {
                                        // set the preferred size of the accession column
                                        int width = peptideShakerGUI.getPreferredColumnWidth(goMappingsTable, goMappingsTable.getColumn("GO Accession").getModelIndex(), 6);
                                        goMappingsTable.getColumn("GO Accession").setMinWidth(width);
                                        goMappingsTable.getColumn("GO Accession").setMaxWidth(width);
                                    }
                                });

                                maxLog2Diff = Math.ceil(maxLog2Diff);

                                goMappingsTable.getColumn("Log2 Diff").setCellRenderer(new JSparklinesBarChartTableCellRenderer(
                                        PlotOrientation.HORIZONTAL, -maxLog2Diff, maxLog2Diff, Color.RED, peptideShakerGUI.getSparklineColor(), Color.lightGray, 0));
                                ((JSparklinesBarChartTableCellRenderer) goMappingsTable.getColumn("Log2 Diff").getCellRenderer()).showNumberAndChart(true, peptideShakerGUI.getLabelWidth());

                                // update the plots
                                updateGoPlots();

                                // enable the contextual export options
                                exportMappingsJButton.setEnabled(true);
                                exportPlotsJButton.setEnabled(true);

                                peptideShakerGUI.setUpdated(PeptideShakerGUI.GO_ANALYSIS_TAB_INDEX, true);
                            }

                            progressDialog.dispose();

                            // return the peptide shaker icon to the standard version
                            peptideShakerGUI.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/peptide-shaker.gif")));

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();

                            if (progressDialog != null) {

                                progressDialog.dispose();
                                // return the peptide shaker icon to the standard version
                                peptideShakerGUI.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/peptide-shaker.gif")));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();

                            if (progressDialog != null) {

                                progressDialog.dispose();
                                // return the peptide shaker icon to the standard version
                                peptideShakerGUI.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/peptide-shaker.gif")));
                            }
                        } catch (IndexOutOfBoundsException e) {
                            e.printStackTrace();

                            if (progressDialog != null) {
                                progressDialog.dispose();
                                // return the peptide shaker icon to the standard version
                                peptideShakerGUI.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/peptide-shaker.gif")));
                            }
                        } catch (ParserConfigurationException e) {
                            e.printStackTrace();

                            if (progressDialog != null) {

                                progressDialog.dispose();
                                // return the peptide shaker icon to the standard version
                                peptideShakerGUI.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/peptide-shaker.gif")));
                            }
                        } catch (XPathExpressionException e) {
                            e.printStackTrace();

                            if (progressDialog != null) {

                                progressDialog.dispose();
                                // return the peptide shaker icon to the standard version
                                peptideShakerGUI.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/peptide-shaker.gif")));
                            }
                        } catch (HeadlessException e) {
                            e.printStackTrace();

                            if (progressDialog != null) {
                                progressDialog.dispose();
                                // return the peptide shaker icon to the standard version
                                peptideShakerGUI.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/peptide-shaker.gif")));
                            }
                        } catch (SAXException e) {
                            e.printStackTrace();

                            if (progressDialog != null) {

                                progressDialog.dispose();
                                // return the peptide shaker icon to the standard version
                                peptideShakerGUI.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/peptide-shaker.gif")));
                            }
                        }

                        cancelProgress = false;
                    }
                }.start();
            }
        }
    }

    /**
     * Update the GO plots.
     */
    private void updateGoPlots() {

        DefaultCategoryDataset frquencyPlotDataset = new DefaultCategoryDataset();
        DefaultCategoryDataset significancePlotDataset = new DefaultCategoryDataset();
        ArrayList<Color> significanceColors = new ArrayList<Color>();
        Double maxLog2Diff = 0.0;

        for (int i = 0; i < goMappingsTable.getRowCount(); i++) {

            boolean selected = (Boolean) goMappingsTable.getValueAt(i, goMappingsTable.getColumn("  ").getModelIndex());
            boolean significant = ((ValueAndBooleanDataPoint) goMappingsTable.getValueAt(i, goMappingsTable.getColumn("Log2 Diff").getModelIndex())).isSignificant();

            if (selected) {

                String goTerm = (String) goMappingsTable.getValueAt(i, goMappingsTable.getColumn("GO Term").getModelIndex());
                Double percentAll = (Double) goMappingsTable.getValueAt(i, goMappingsTable.getColumn("Frequency All (%)").getModelIndex());
                Double percentDataset = (Double) goMappingsTable.getValueAt(i, goMappingsTable.getColumn("Frequency Dataset (%)").getModelIndex());
                Double log2Diff = ((ValueAndBooleanDataPoint) goMappingsTable.getValueAt(i, goMappingsTable.getColumn("Log2 Diff").getModelIndex())).getValue();

                frquencyPlotDataset.addValue(percentAll, "All", goTerm);
                frquencyPlotDataset.addValue(percentDataset, "Dataset", goTerm);

                if (!log2Diff.isInfinite()) {
                    significancePlotDataset.addValue(log2Diff, "Difference", goTerm);
                } else {
                    significancePlotDataset.addValue(0, "Difference", goTerm);
                }

                if (significant) {
                    if (log2Diff > 0) {
                        significanceColors.add(peptideShakerGUI.getSparklineColor());
                    } else {
                        significanceColors.add(new Color(255, 51, 51));
                    }
                } else {
                    significanceColors.add(Color.lightGray);
                }

                if (!log2Diff.isInfinite() && Math.abs(log2Diff) > maxLog2Diff) {
                    maxLog2Diff = Math.abs(log2Diff);
                }
            }
        }

        maxLog2Diff = Math.ceil(maxLog2Diff);


        JFreeChart distributionChart = ChartFactory.createBarChart(null, "GO Terms", "Frequency (%)", frquencyPlotDataset, PlotOrientation.VERTICAL, false, true, true);
        distributionChartPanel = new ChartPanel(distributionChart);

        ((CategoryPlot) distributionChartPanel.getChart().getPlot()).getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_90);

        BarRenderer3D renderer = new BarRenderer3D(0, 0);
        renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesPaint(1, peptideShakerGUI.getSparklineColor());
        distributionChart.getCategoryPlot().setRenderer(renderer);

        // add mouse listener
        distributionChartPanel.addChartMouseListener(new ChartMouseListener() {

            @Override
            public void chartMouseClicked(ChartMouseEvent cme) {

                if (cme.getEntity() instanceof CategoryItemEntity) {
                    CategoryItemEntity categoryItem = (CategoryItemEntity) cme.getEntity();
                    String columnKey = (String) categoryItem.getColumnKey();

                    // select and highlight category
                    boolean categoryFound = false;

                    for (int i = 0; i < goMappingsTable.getRowCount() && !categoryFound; i++) {
                        if (((String) goMappingsTable.getValueAt(
                                i, goMappingsTable.getColumn("GO Term").getModelIndex())).equalsIgnoreCase(columnKey)) {
                            goMappingsTable.setRowSelectionInterval(i, i);
                            goMappingsTable.scrollRectToVisible(goMappingsTable.getCellRect(i, 0, false));
                            goMappingsTableMouseReleased(null);
                        }
                    }
                }
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent cme) {
                // do nothing
            }
        });

        // set background color
        distributionChart.getPlot().setBackgroundPaint(Color.WHITE);
        distributionChart.setBackgroundPaint(Color.WHITE);
        distributionChartPanel.setBackground(Color.WHITE);

        // hide the outline
        distributionChart.getPlot().setOutlineVisible(false);

        goFrequencyPlotPanel.removeAll();
        goFrequencyPlotPanel.add(distributionChartPanel);
        goFrequencyPlotPanel.revalidate();
        goFrequencyPlotPanel.repaint();


        JFreeChart significanceChart = ChartFactory.createBarChart(null, "GO Terms", "Log2 Difference", significancePlotDataset, PlotOrientation.VERTICAL, false, true, true);
        signChartPanel = new ChartPanel(significanceChart);

        ((CategoryPlot) signChartPanel.getChart().getPlot()).getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_90);

        ((CategoryPlot) signChartPanel.getChart().getPlot()).getRangeAxis().setUpperBound(maxLog2Diff);
        ((CategoryPlot) signChartPanel.getChart().getPlot()).getRangeAxis().setLowerBound(-maxLog2Diff);

        BarChartColorRenderer signRenderer = new BarChartColorRenderer(significanceColors);
        signRenderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
        significanceChart.getCategoryPlot().setRenderer(signRenderer);


        // add mouse listener
        signChartPanel.addChartMouseListener(new ChartMouseListener() {

            @Override
            public void chartMouseClicked(ChartMouseEvent cme) {

                if (cme.getEntity() instanceof CategoryItemEntity) {
                    CategoryItemEntity categoryItem = (CategoryItemEntity) cme.getEntity();
                    String columnKey = (String) categoryItem.getColumnKey();

                    // select and highlight category
                    boolean categoryFound = false;

                    for (int i = 0; i < goMappingsTable.getRowCount() && !categoryFound; i++) {
                        if (((String) goMappingsTable.getValueAt(
                                i, goMappingsTable.getColumn("GO Term").getModelIndex())).equalsIgnoreCase(columnKey)) {
                            goMappingsTable.setRowSelectionInterval(i, i);
                            goMappingsTable.scrollRectToVisible(goMappingsTable.getCellRect(i, 0, false));
                            goMappingsTableMouseReleased(null);
                        }
                    }
                }
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent cme) {
                // do nothing
            }
        });

        // set background color
        significanceChart.getPlot().setBackgroundPaint(Color.WHITE);
        significanceChart.setBackgroundPaint(Color.WHITE);
        signChartPanel.setBackground(Color.WHITE);

        // hide the outline
        significanceChart.getPlot().setOutlineVisible(false);

        goSignificancePlotPanel.removeAll();
        goSignificancePlotPanel.add(signChartPanel);
        goSignificancePlotPanel.revalidate();
        goSignificancePlotPanel.repaint();

        updatePlotMarkers();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        selectTermsJPopupMenu = new javax.swing.JPopupMenu();
        selectAllMenuItem = new javax.swing.JMenuItem();
        deselectAllMenuItem = new javax.swing.JMenuItem();
        selectSignificantMenuItem = new javax.swing.JMenuItem();
        significanceLevelButtonGroup = new javax.swing.ButtonGroup();
        mappingsTableLayeredPane = new javax.swing.JLayeredPane();
        mappingsPanel = new javax.swing.JPanel();
        proteinGoMappingsScrollPane = new javax.swing.JScrollPane();
        goMappingsTable = new JTable() {
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        String tip = (String) mappingsTableToolTips.get(realIndex);
                        return tip;
                    }
                };
            }
        };
        goMappingsFileJLabel = new javax.swing.JLabel();
        speciesJComboBox = new javax.swing.JComboBox();
        significanceJLabel = new javax.swing.JLabel();
        downloadButton = new javax.swing.JButton();
        updateButton = new javax.swing.JButton();
        biasWarningLabel = new javax.swing.JLabel();
        unknownSpeciesLabel = new javax.swing.JLabel();
        fivePercentRadioButton = new javax.swing.JRadioButton();
        onePercentRadioButton = new javax.swing.JRadioButton();
        ensemblVersionLabel = new javax.swing.JLabel();
        goProteinCountLabel = new javax.swing.JLabel();
        mappingsHelpJButton = new javax.swing.JButton();
        exportMappingsJButton = new javax.swing.JButton();
        contextMenuMappingsBackgroundPanel = new javax.swing.JPanel();
        plotLayeredPane = new javax.swing.JLayeredPane();
        plotPanel = new javax.swing.JPanel();
        goPlotsTabbedPane = new javax.swing.JTabbedPane();
        proteinsPanel = new javax.swing.JPanel();
        proteinsScrollPane = new javax.swing.JScrollPane();
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
        goFrequencyPlotPanel = new javax.swing.JPanel();
        goSignificancePlotPanel = new javax.swing.JPanel();
        plotHelpJButton = new javax.swing.JButton();
        exportPlotsJButton = new javax.swing.JButton();
        contextMenuPlotsBackgroundPanel = new javax.swing.JPanel();

        selectAllMenuItem.setText("Select All");
        selectAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllMenuItemActionPerformed(evt);
            }
        });
        selectTermsJPopupMenu.add(selectAllMenuItem);

        deselectAllMenuItem.setText("Deselect All");
        deselectAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deselectAllMenuItemActionPerformed(evt);
            }
        });
        selectTermsJPopupMenu.add(deselectAllMenuItem);

        selectSignificantMenuItem.setText("Select Significant");
        selectSignificantMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectSignificantMenuItemActionPerformed(evt);
            }
        });
        selectTermsJPopupMenu.add(selectSignificantMenuItem);

        setBackground(new java.awt.Color(255, 255, 255));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        mappingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Gene Ontology Mappings"));
        mappingsPanel.setOpaque(false);

        proteinGoMappingsScrollPane.setOpaque(false);

        goMappingsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "", "GO Accession", "GO Term", "GO Domain", "Frequency All (%)", "Frequency Dataset (%)", "Frequency (%)", "Log2 Diff", "p-value", "  "
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Double.class, java.lang.Double.class, ValueAndBooleanDataPoint.class, java.lang.Object.class, java.lang.Double.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean[]{
                false, false, false, false, false, false, false, false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        goMappingsTable.setOpaque(false);
        goMappingsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        goMappingsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                goMappingsTableMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                goMappingsTableMouseReleased(evt);
            }
        });
        goMappingsTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                goMappingsTableMouseMoved(evt);
            }
        });
        goMappingsTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                goMappingsTableKeyReleased(evt);
            }
        });
        proteinGoMappingsScrollPane.setViewportView(goMappingsTable);

        goMappingsFileJLabel.setText("Species:");

        speciesJComboBox.setMaximumRowCount(30);
        speciesJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        speciesJComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                speciesJComboBoxActionPerformed(evt);
            }
        });

        significanceJLabel.setText("Significance Level:");

        downloadButton.setText("Download");
        downloadButton.setToolTipText("Download GO Mappings");
        downloadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downloadButtonActionPerformed(evt);
            }
        });

        updateButton.setText("Update");
        updateButton.setToolTipText("Update the GO Mappings");
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });

        biasWarningLabel.setFont(biasWarningLabel.getFont().deriveFont((biasWarningLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        biasWarningLabel.setText("Note that the statistical analysis above is only correct as long as the selected protein set is unbiased.");

        unknownSpeciesLabel.setFont(unknownSpeciesLabel.getFont().deriveFont((unknownSpeciesLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        unknownSpeciesLabel.setText("<html><a href>Species not in list?</a></html>");
        unknownSpeciesLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                unknownSpeciesLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                unknownSpeciesLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                unknownSpeciesLabelMouseExited(evt);
            }
        });

        significanceLevelButtonGroup.add(fivePercentRadioButton);
        fivePercentRadioButton.setSelected(true);
        fivePercentRadioButton.setText("0.05");
        fivePercentRadioButton.setOpaque(false);
        fivePercentRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fivePercentRadioButtonActionPerformed(evt);
            }
        });

        significanceLevelButtonGroup.add(onePercentRadioButton);
        onePercentRadioButton.setText("0.01");
        onePercentRadioButton.setOpaque(false);
        onePercentRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onePercentRadioButtonActionPerformed(evt);
            }
        });

        ensemblVersionLabel.setFont(ensemblVersionLabel.getFont().deriveFont((ensemblVersionLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        ensemblVersionLabel.setText("<html><a href>Ensembl version?</a></html>");
        ensemblVersionLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ensemblVersionLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                ensemblVersionLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                ensemblVersionLabelMouseExited(evt);
            }
        });

        goProteinCountLabel.setText("[GO Proteins: Ensembl: -, Project: -]");
        goProteinCountLabel.setToolTipText("Number of GO mapped proteins");

        javax.swing.GroupLayout mappingsPanelLayout = new javax.swing.GroupLayout(mappingsPanel);
        mappingsPanel.setLayout(mappingsPanelLayout);
        mappingsPanelLayout.setHorizontalGroup(
            mappingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mappingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mappingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(proteinGoMappingsScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 988, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, mappingsPanelLayout.createSequentialGroup()
                        .addComponent(goMappingsFileJLabel)
                        .addGap(18, 18, 18)
                        .addComponent(speciesJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 355, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(downloadButton)
                        .addPreferredGap(javax
