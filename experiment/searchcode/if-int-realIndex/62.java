package eu.isas.peptideshaker.gui.preferencesdialogs;

import com.compomics.util.Util;
import com.compomics.util.examples.BareBonesBrowserLaunch;
import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.io.identifications.IdentificationParametersReader;
import com.compomics.util.gui.dialogs.ProgressDialogParent;
import com.compomics.util.gui.dialogs.ProgressDialogX;
import com.compomics.util.gui.renderers.AlignedListCellRenderer;
import com.compomics.util.gui.dialogs.PtmDialog;
import com.compomics.util.gui.dialogs.PtmDialogParent;
import eu.isas.peptideshaker.gui.HelpDialog;
import eu.isas.peptideshaker.gui.PeptideShakerGUI;
import eu.isas.peptideshaker.preferences.ModificationProfile;
import eu.isas.peptideshaker.preferences.SearchParameters;
import com.compomics.util.pride.CvTerm;
import com.compomics.util.pride.PrideObjectsFactory;
import com.compomics.util.pride.PtmToPrideMap;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import no.uib.jsparklines.extra.HtmlLinksRenderer;
import no.uib.jsparklines.extra.NimbusCheckBoxRenderer;
import no.uib.jsparklines.extra.TrueFalseIconRenderer;
import no.uib.jsparklines.renderers.JSparklinesColorTableCellRenderer;

/**
 * A dialog for displaying and editing the search preferences.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class SearchPreferencesDialog extends javax.swing.JDialog implements PtmDialogParent, ProgressDialogParent {

    /**
     * The tooltips for the expected variable mods.
     */
    private Vector<String> expectedVariableModsTableToolTips;
    /**
     * The tooltips for the available mods.
     */
    private Vector<String> availableModsTableToolTips;
    /**
     * The search parameters needed by PeptideShaker.
     */
    private SearchParameters searchParameters;
    /**
     * The enzyme factory.
     */
    private EnzymeFactory enzymeFactory = EnzymeFactory.getInstance();
    /**
     * The PeptideShakerGUI.
     */
    private PeptideShakerGUI peptideShakerGUI;
    /**
     * The compomics PTM factory.
     */
    private PTMFactory ptmFactory = PTMFactory.getInstance();
    /**
     * A map of all loaded PTMs.
     */
    private HashMap<String, PTM> ptms = new HashMap<String, PTM>();
    /**
     * The selected ptms.
     */
    private ArrayList<String> modificationList = new ArrayList<String>();
    /**
     * File containing the modification profile. By default default.psm in the
     * conf folder.
     */
    private File profileFile;
    /**
     * A simple progress dialog.
     */
    private static ProgressDialogX progressDialog;
    /**
     * If true the progress bar is disposed of.
     */
    private static boolean cancelProgress = false;
    /**
     * boolean indicating whether import-related data can be edited.
     */
    private boolean editable;
    /**
     * The ptm to pride map.
     */
    private PtmToPrideMap ptmToPrideMap;

    /**
     * Create a new SearchPreferencesDialog.
     *
     * @param parent the PeptideShaker parent
     * @param editable
     */
    public SearchPreferencesDialog(PeptideShakerGUI parent, boolean editable) {
        super(parent, true);

        this.editable = editable;
        this.peptideShakerGUI = parent;
        this.searchParameters = parent.getSearchParameters();
        this.profileFile = parent.getModificationProfileFile();

        initComponents();
        setUpGui();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Set up the GUI.
     */
    private void setUpGui() {

        loadModifications();
        ptmToPrideMap = peptideShakerGUI.loadPrideToPtmMap();

        // set the cell renderers
        expectedModificationsTable.getColumn("  ").setCellRenderer(new JSparklinesColorTableCellRenderer());
        expectedModificationsTable.getColumn("PSI-MOD").setCellRenderer(new HtmlLinksRenderer(
                peptideShakerGUI.getSelectedRowHtmlTagFontColor(), peptideShakerGUI.getNotSelectedRowHtmlTagFontColor()));
        availableModificationsTable.getColumn("PSI-MOD").setCellRenderer(new HtmlLinksRenderer(
                peptideShakerGUI.getSelectedRowHtmlTagFontColor(), peptideShakerGUI.getNotSelectedRowHtmlTagFontColor()));
        expectedModificationsTable.getColumn("U.M.").setCellRenderer(new NimbusCheckBoxRenderer());
        availableModificationsTable.getColumn("U.M.").setCellRenderer(new NimbusCheckBoxRenderer());
        expectedModificationsTable.getColumn("U.M.").setCellRenderer(new TrueFalseIconRenderer(
                new ImageIcon(this.getClass().getResource("/icons/selected_green.png")),
                null,
                "User Modification", null));
        availableModificationsTable.getColumn("U.M.").setCellRenderer(new TrueFalseIconRenderer(
                new ImageIcon(this.getClass().getResource("/icons/selected_green.png")),
                null,
                "User Modification", null));

        // set table properties
        expectedModificationsTable.getTableHeader().setReorderingAllowed(false);
        availableModificationsTable.getTableHeader().setReorderingAllowed(false);

        availableModificationsTable.getColumn(" ").setMaxWidth(40);
        availableModificationsTable.getColumn(" ").setMinWidth(40);
        availableModificationsTable.getColumn("U.M.").setMaxWidth(40);
        availableModificationsTable.getColumn("U.M.").setMinWidth(40);

        expectedModificationsTable.getColumn(" ").setMaxWidth(40);
        expectedModificationsTable.getColumn(" ").setMinWidth(40);
        expectedModificationsTable.getColumn("  ").setMaxWidth(40);
        expectedModificationsTable.getColumn("  ").setMinWidth(40);
        expectedModificationsTable.getColumn("U.M.").setMaxWidth(40);
        expectedModificationsTable.getColumn("U.M.").setMinWidth(40);

        availableModificationsTable.getColumn("PSI-MOD").setMaxWidth(100);
        availableModificationsTable.getColumn("PSI-MOD").setMinWidth(100);
        expectedModificationsTable.getColumn("PSI-MOD").setMaxWidth(100);
        expectedModificationsTable.getColumn("PSI-MOD").setMinWidth(100);

        expectedVariableModsTableToolTips = new Vector<String>();
        expectedVariableModsTableToolTips.add(null);
        expectedVariableModsTableToolTips.add("Modification Color");
        expectedVariableModsTableToolTips.add("Modification Name");
        expectedVariableModsTableToolTips.add("Modification Family Name");
        expectedVariableModsTableToolTips.add("Modification Short Name");
        expectedVariableModsTableToolTips.add("User Defined Modification");
        expectedVariableModsTableToolTips.add("The PSI-MOD CV Term Mapping");

        availableModsTableToolTips = new Vector<String>();
        availableModsTableToolTips.add(null);
        availableModsTableToolTips.add("Modification Name");
        availableModsTableToolTips.add("User Defined Modification");
        availableModsTableToolTips.add("The PSI-MOD CV Term Mapping");

        // make sure that the scroll panes are see-through
        expectedModsScrollPane.getViewport().setOpaque(false);
        availableModsScrollPane.getViewport().setOpaque(false);

        modificationList = new ArrayList<String>(searchParameters.getModificationProfile().getUtilitiesNames());
        Collections.sort(modificationList);
        enzymesCmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        ion1Cmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        ion2Cmb.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        precursorUnit.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        loadValues();
        updateModificationLists();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        expectedPtmPopupMenu = new javax.swing.JPopupMenu();
        removeExpectedPtmJMenuItem = new javax.swing.JMenuItem();
        editExpectedPtmJMenuItem = new javax.swing.JMenuItem();
        availablePtmPopupMenu = new javax.swing.JPopupMenu();
        addAvailablePtmJMenuItem = new javax.swing.JMenuItem();
        editAvailablePtmJMenuItem = new javax.swing.JMenuItem();
        backgroundPanel = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        enzymeAndFragmentIonsPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        fragmentIonAccuracyTxt = new javax.swing.JTextField();
        enzymesCmb = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        missedCleavagesTxt = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        ion1Cmb = new javax.swing.JComboBox();
        ion2Cmb = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        precursorAccuracy = new javax.swing.JTextField();
        precursorUnit = new javax.swing.JComboBox();
        modProfilePanel = new javax.swing.JPanel();
        addModifications = new javax.swing.JButton();
        removeModification = new javax.swing.JButton();
        expectedModsScrollPane = new javax.swing.JScrollPane();
        expectedModificationsTable = new JTable() {
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        String tip = (String) expectedVariableModsTableToolTips.get(realIndex);
                        return tip;
                    }
                };
            }
        };
        expectedModsLabel = new javax.swing.JLabel();
        availableModsLabel = new javax.swing.JLabel();
        clearProfileBtn = new javax.swing.JButton();
        saveAsProfileBtn = new javax.swing.JButton();
        loadProfileBtn = new javax.swing.JButton();
        availableModsScrollPane = new javax.swing.JScrollPane();
        availableModificationsTable =         new JTable() {

            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {

                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        String tip = (String) availableModsTableToolTips.get(realIndex);
                        return tip;
                    }
                };
            }
        };
        loadAvailableModsButton = new javax.swing.JButton();
        searchGuiParamsPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        fileTxt = new javax.swing.JTextField();
        loadButton = new javax.swing.JButton();
        helpLineLabel = new javax.swing.JLabel();
        searchPreferencesHelpJButton = new javax.swing.JButton();

        removeExpectedPtmJMenuItem.setText("Remove Selected Modifications");
        removeExpectedPtmJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeExpectedPtmJMenuItemActionPerformed(evt);
            }
        });
        expectedPtmPopupMenu.add(removeExpectedPtmJMenuItem);

        editExpectedPtmJMenuItem.setText("Edit");
        editExpectedPtmJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editExpectedPtmJMenuItemActionPerformed(evt);
            }
        });
        expectedPtmPopupMenu.add(editExpectedPtmJMenuItem);

        addAvailablePtmJMenuItem.setText("Add Selected Modifications");
        addAvailablePtmJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addAvailablePtmJMenuItemActionPerformed(evt);
            }
        });
        availablePtmPopupMenu.add(addAvailablePtmJMenuItem);

        editAvailablePtmJMenuItem.setText("Edit");
        editAvailablePtmJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editAvailablePtmJMenuItemActionPerformed(evt);
            }
        });
        availablePtmPopupMenu.add(editAvailablePtmJMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Search Parameters");
        setResizable(false);

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        enzymeAndFragmentIonsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Enzyme and Fragment Ions"));
        enzymeAndFragmentIonsPanel.setOpaque(false);

        jLabel1.setText("MS/MS Tol. (Da):");
        jLabel1.setToolTipText("Fragment ion tolerance");

        fragmentIonAccuracyTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        fragmentIonAccuracyTxt.setToolTipText("Fragment ion tolerance");

        enzymesCmb.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        enzymesCmb.setToolTipText("Enzyme used");

        jLabel5.setText("Enzyme:");
        jLabel5.setToolTipText("Enzyme used");

        missedCleavagesTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        missedCleavagesTxt.setText("1");
        missedCleavagesTxt.setToolTipText("Max number of missed cleavages");

        jLabel7.setText("Missed Cleavages:");
        jLabel7.setToolTipText("Max number of missed cleavages");

        jLabel2.setText("Fragment Ion Types:");

        ion1Cmb.setModel(new DefaultComboBoxModel(searchParameters.getIons()));
        ion1Cmb.setToolTipText("Fragment ion types");

        ion2Cmb.setModel(new DefaultComboBoxModel(searchParameters.getIons()));
        ion2Cmb.setToolTipText("Fragment ion types");

        jLabel9.setText("Prec. Tol.:");
        jLabel9.setToolTipText("Precursor tolerance");

        precursorAccuracy.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        precursorAccuracy.setToolTipText("Precursor tolerance");

        precursorUnit.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ppm", "Da" }));
        precursorUnit.setToolTipText("Precursor tolerance type");

        javax.swing.GroupLayout enzymeAndFragmentIonsPanelLayout = new javax.swing.GroupLayout(enzymeAndFragmentIonsPanel);
        enzymeAndFragmentIonsPanel.setLayout(enzymeAndFragmentIonsPanelLayout);
        enzymeAndFragmentIonsPanelLayout.setHorizontalGroup(
            enzymeAndFragmentIonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(enzymeAndFragmentIonsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(enzymeAndFragmentIonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel9))
                .addGap(18, 18, 18)
                .addGroup(enzymeAndFragmentIonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(enzymeAndFragmentIonsPanelLayout.createSequentialGroup()
                        .addComponent(precursorAccuracy, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(precursorUnit, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fragmentIonAccuracyTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(enzymesCmb, 0, 308, Short.MAX_VALUE))
                .addGap(59, 59, 59)
                .addGroup(enzymeAndFragmentIonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(18, 18, 18)
                .addGroup(enzymeAndFragmentIonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(missedCleavagesTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, enzymeAndFragmentIonsPanelLayout.createSequentialGroup()
                        .addComponent(ion1Cmb, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(ion2Cmb, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        enzymeAndFragmentIonsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {ion1Cmb, ion2Cmb});

        enzymeAndFragmentIonsPanelLayout.setVerticalGroup(
            enzymeAndFragmentIonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(enzymeAndFragmentIonsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(enzymeAndFragmentIonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(enzymesCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(missedCleavagesTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(enzymeAndFragmentIonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(precursorAccuracy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(precursorUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(ion1Cmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ion2Cmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(fragmentIonAccuracyTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        modProfilePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Modification Profile"));
        modProfilePanel.setOpaque(false);

        addModifications.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/arrowUp_grey.png"))); // NOI18N
        addModifications.setText("Add");
        addModifications.setToolTipText("Add to list of expected modifications");
        addModifications.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        addModifications.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/arrowUp.png"))); // NOI18N
        addModifications.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addModificationsActionPerformed(evt);
            }
        });

        removeModification.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/arrowDown_grey.png"))); // NOI18N
        removeModification.setText("Remove");
        removeModification.setToolTipText("Remove from list of selected modifications");
        removeModification.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        removeModification.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/arrowDown.png"))); // NOI18N
        removeModification.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeModificationActionPerformed(evt);
            }
        });

        expectedModificationsTable.setModel(new ModificationTable());
        expectedModificationsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                expectedModificationsTableMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                expectedModificationsTableMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                expectedModificationsTableMouseReleased(evt);
            }
        });
        expectedModificationsTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                expectedModificationsTableMouseMoved(evt);
            }
        });
        expectedModsScrollPane.setViewportView(expectedModificationsTable);

        expectedModsLabel.setFont(expectedModsLabel.getFont().deriveFont((expectedModsLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        expectedModsLabel.setText("Expected Variable Modifications");

        availableModsLabel.setFont(availableModsLabel.getFont().deriveFont((availableModsLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        availableModsLabel.setText("Available Modifications");

        clearProfileBtn.setText("Clear");
        clearProfileBtn.setToolTipText("Clear the list of expected modifications");
        clearProfileBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearProfileBtnActionPerformed(evt);
            }
        });

        saveAsProfileBtn.setText("Save");
        saveAsProfileBtn.setToolTipText("Save the modification profile to a psm file");
        saveAsProfileBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsProfileBtnActionPerformed(evt);
            }
        });

        loadProfileBtn.setText("Load");
        loadProfileBtn.setToolTipText("Load a modification profile from a psm file");
        loadProfileBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadProfileBtnActionPerformed(evt);
            }
        });

        availableModificationsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "Name", "U.M.", "PSI-MOD"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Boolean.class, java.lang.String.class
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
        availableModificationsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                availableModificationsTableMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                availableModificationsTableMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                availableModificationsTableMouseReleased(evt);
            }
        });
        availableModificationsTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                availableModificationsTableMouseMoved(evt);
            }
        });
        availableModsScrollPane.setViewportView(availableModificationsTable);

        loadAvailableModsButton.setText("Load");
        loadAvailableModsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadAvailableModsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout modProfilePanelLayout = new javax.swing.GroupLayout(modProfilePanel);
        modProfilePanel.setLayout(modProfilePanelLayout);
        modProfilePanelLayout.setHorizontalGroup(
            modProfilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(modProfilePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(modProfilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, modProfilePanelLayout.createSequentialGroup()
                        .addGroup(modProfilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(modProfilePanelLayout.createSequentialGroup()
                                .addComponent(availableModsLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(addModifications, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(removeModification, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(expectedModsScrollPane))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(modProfilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(saveAsProfileBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE)
                            .addComponent(loadProfileBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(clearProfileBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(modProfilePanelLayout.createSequentialGroup()
                        .addComponent(expectedModsLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(modProfilePanelLayout.createSequentialGroup()
                        .addComponent(availableModsScrollPane)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(loadAvailableModsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        modProfilePanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addModifications, removeModification});

        modProfilePanelLayout.setVerticalGroup(
            modProfilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(modProfilePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(expectedModsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(modProfilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(expectedModsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(modProfilePanelLayout.createSequentialGroup()
                        .addComponent(loadProfileBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveAsProfileBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(clearProfileBtn)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(modProfilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(modProfilePanelLayout.createSequentialGroup()
                        .addGroup(modProfilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(removeModification, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(addModifications, javax.swing.GroupLayout.PREFERRED_SIZE, 0, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(14, 14, 14))
                    .addComponent(availableModsLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(modProfilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(availableModsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(loadAvailableModsButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        modProfilePanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {availableModsScrollPane, expectedModsScrollPane});

        modProfilePanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {addModifications, clearProfileBtn, loadAvailableModsButton, loadProfileBtn, removeModification, saveAsProfileBtn});

        searchGuiParamsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("SearchGUI Parameters File"));
        searchGuiParamsPanel.setOpaque(false);

        jLabel4.setText("SearchGUI File:");

        fileTxt.setEditable(false);

        loadButton.setText("Load");
        loadButton.setToolTipText("Load parameters from a SearchGUI parameters file");
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout searchGuiParamsPanelLayout = new javax.swing.GroupLayout(searchGuiParamsPanel);
        searchGuiParamsPanel.setLayout(searchGuiParamsPanelLayout);
        searchGuiParamsPanelLayout.setHorizontalGroup(
            searchGuiParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchGuiParamsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(fileTxt)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(loadButton, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        searchGuiParamsPanelLayout.setVerticalGroup(
            searchGuiParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchGuiParamsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(searchGuiParamsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(fileTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(loadButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        helpLineLabel.setFont(helpLineLabel.getFont().deriveFont((helpLineLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        helpLineLabel.setText("Edit the search parameters and the modification profile and click OK to save.");

        searchPreferencesHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help.GIF"))); // NOI18N
        searchPreferencesHelpJButton.setToolTipText("Help");
        searchPreferencesHelpJButton.setBorder(null);
        searchPreferencesHelpJButton.setBorderPainted(false);
        searchPreferencesHelpJButton.setContentAreaFilled(false);
        searchPreferencesHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                searchPreferencesHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                searchPreferencesHelpJButtonMouseExited(evt);
            }
        });
        searchPreferencesHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchPreferencesHelpJButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(searchPreferencesHelpJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(helpLineLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addComponent(searchGuiParamsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(enzymeAndFragmentIonsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(modProfilePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        backgroundPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, okButton});

        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(enzymeAndFragmentIonsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(modProfilePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchGuiParamsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(helpLineLabel)
                    .addComponent(searchPreferencesHelpJButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(okButton)
                    .addComponent(cancelButton))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Saves the settings and closes the dialog.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (validateInput()) {
            try {
                PrideObjectsFactory prideObjectsFactory = PrideObjectsFactory.getInstance();
                prideObjectsFactory.setPtmToPrideMap(ptmToPrideMap);
            } catch (Exception e) {
                peptideShakerGUI.catchException(e);
            }
            //@TODO: the displayed data ought to be updated here if any change was made
            searchParameters.setFragmentIonAccuracy(new Double(fragmentIonAccuracyTxt.getText()));
            searchParameters.setnMissedCleavages(new Integer(missedCleavagesTxt.getText()));
            searchParameters.setEnzyme(enzymeFactory.getEnzyme((String) enzymesCmb.getSelectedItem()));
            searchParameters.setIonSearched1((String) ion1Cmb.getSelectedItem());
            searchParameters.setIonSearched2((String) ion2Cmb.getSelectedItem());

            if (((String) precursorUnit.getSelectedItem()).equalsIgnoreCase("ppm")) {
                searchParameters.setPrecursorAccuracyType(SearchParameters.PrecursorAccuracyType.PPM);
            } else { // Da
                searchParameters.setPrecursorAccuracyType(SearchParameters.PrecursorAccuracyType.DA);
            }

            searchParameters.setPrecursorAccuracy(new Double(precursorAccuracy.getText()));

            if (!searchParameters.getEnzyme().enzymeCleaves()) {

                // create an empty label to put the message in
                JLabel label = new JLabel();

                // html content 
                JEditorPane ep = new JEditorPane("text/html", "<html><body bgcolor=\"#" + Util.color2Hex(label.getBackground()) + "\">"
                        + "The cleavage site of the selected enzyme is not configured.<br><br>"
                        + "PeptideShaker functionalities will be limited.<br><br>"
                        + "Edit enzyme configuration in:<br>"
                        + "<i>peptideshaker_enzymes.xml</i> located in the conf folder.<br><br>"
                        + "For more information on enzymes, contact us via:<br>"
                        + "<a href=\"http://groups.google.com/group/peptide-shaker\">http://groups.google.com/group/peptide-shaker</a>."
                        + "</body></html>");

                // handle link events 
                ep.addHyperlinkListener(new HyperlinkListener() {

                    @Override
                    public void hyperlinkUpdate(HyperlinkEvent e) {
                        if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                            BareBonesBrowserLaunch.openURL(e.getURL().toString());
                        }
                    }
                });

                ep.setBorder(null);
                ep.setEditable(false);
                
                JOptionPane.showMessageDialog(this, ep, "Enzyme Not Configured", JOptionPane.WARNING_MESSAGE);
            }

            peptideShakerGUI.setSearchParameters(searchParameters);
            peptideShakerGUI.updateAnnotationPreferencesFromSearchSettings();
            peptideShakerGUI.setModificationProfileFile(profileFile);
            peptideShakerGUI.setDataSaved(false); //@TODO this should be set to false only if a change was made
            this.dispose();
        }
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Closes the dialog.
     *
     * @param evt
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * Adds a modification to the list.
     *
     * @param evt
     */
    private void addModificationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addModificationsActionPerformed

        int[] selectedRows = availableModificationsTable.getSelectedRows();

        for (int i = selectedRows.length - 1; i >= 0; i--) {
            String name = (String) availableModificationsTable.getValueAt(selectedRows[i], 1);
            if (!searchParameters.getModificationProfile().getPeptideShakerNames().contains(name)) {
                ArrayList<String> conflicts = new ArrayList<String>();
                PTM oldPTM;
                for (String oldModification : searchParameters.getModificationProfile().getUtilitiesNames()) {
                    oldPTM = ptmFactory.getPTM(oldModification);
                    if (Math.abs(oldPTM.getMass() - ptmFactory.getPTM(name).getMass()) < 0.01) {
                        conflicts.add(oldModification);
                    }
                }
                int index = name.length();
                if (name.lastIndexOf(" ") > 0) {
                    index = name.indexOf(" ");
                }
                if (name.lastIndexOf("-") > 0) {
                    index = Math.min(index, name.indexOf("-"));
                }
                searchParameters.getModificationProfile().setShortName(name, name.substring(0, index));
                searchParameters.getModificationProfile().setColor(name, Color.lightGray);
                if (!conflicts.isEmpty()) {
                    String report = name + " might be difficult to distinguish from ";
                    boolean first = true;
                    int cpt = 0;
                    for (String conflict : conflicts) {
                        cpt++;
                        if (first) {
                            first = false;
                        } else if (cpt == conflicts.size()) {
                            report += " and ";
                        } else {
                            report += ", ";
                        }
                        report += conflict;
                    }
                    report += ".\nIt is avised to group them into the same modification family.";
                    JOptionPane.showMessageDialog(this, report, "Modification Conflict", JOptionPane.WARNING_MESSAGE);
                }
            }
            searchParameters.getModificationProfile().setPeptideShakerName(name, name);
            modificationList.add(name);
        }

        updateModificationLists();
    }//GEN-LAST:event_addModificationsActionPerformed

    /**
     * Removes a modification from the list.
     *
     * @param evt
     */
    private void removeModificationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeModificationActionPerformed

        ArrayList<String> toRemove = new ArrayList<String>();

        int[] selectedRows = expectedModificationsTable.getSelectedRows();
        for (int selectedRow : selectedRows) {
            toRemove.add((String) expectedModificationsTable.getValueAt(selectedRow, 2));
        }

        for (String name : toRemove) {
            modificationList.remove(name);
            searchParameters.getModificationProfile().remove(name);
        }

        updateModificationLists();
    }//GEN-LAST:event_removeModificationActionPerformed

    /**
     * Loads the search preferences from a SearchGUI file.
     *
     * @param evt
     */
    private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadButtonActionPerformed
        JFileChooser fc = new JFileChooser(peptideShakerGUI.getLastSelectedFolder());

        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File myFile) {
                return myFile.getName().toLowerCase().endsWith("properties") || myFile.isDirectory();
            }

            @Override
            public String getDescription() {
                return "(SearchGUI properties file) *.properties";
            }
        };

        fc.setFileFilter(filter);

        int result = fc.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                Properties props = IdentificationParametersReader.loadProperties(file);
                setScreenProps(props);
                searchParameters.setParametersFile(file);
                fileTxt.setText(file.getAbsolutePath());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, file.getName() + " not found.", "File Not Found", JOptionPane.WARNING_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "An error occured while reading " + file.getName() + ".\n"
                        + "Please verify the version compatibility.", "File Import Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }//GEN-LAST:event_loadButtonActionPerformed

    /**
     * Clears the list of selected proteins.
     *
     * @param evt
     */
    private void clearProfileBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearProfileBtnActionPerformed
        searchParameters.setModificationProfile(new ModificationProfile());
        modificationList.clear();
        expectedModificationsTable.revalidate();
        expectedModificationsTable.repaint();
        expectedModsLabel.setText("Expected Variable Modifications");
    }//GEN-LAST:event_clearProfileBtnActionPerformed

    /**
     * Loads a modification profile.
     *
     * @param evt
     */
    private void loadProfileBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadProfileBtnActionPerformed
        JFileChooser fc = new JFileChooser(peptideShakerGUI.getLastSelectedFolder());

        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File myFile) {
                return myFile.getName().toLowerCase().endsWith("psm") || myFile.isDirectory();
            }

            @Override
            public String getDescription() {
                return "(Profile psm file) *.psm";
            }
        };

        fc.setFileFilter(filter);

        int result = fc.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            loadModificationProfile(file);
        }
    }//GEN-LAST:event_loadProfileBtnActionPerformed

    /**
     * Save a modification profile.
     *
     * @param evt
     */
    private void saveAsProfileBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsProfileBtnActionPerformed

        final JFileChooser fileChooser = new JFileChooser(peptideShakerGUI.getLastSelectedFolder());
        fileChooser.setDialogTitle("Save As...");
        fileChooser.setMultiSelectionEnabled(false);

        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File myFile) {
                return myFile.getName().toLowerCase().endsWith("psm") || myFile.isDirectory();
            }

            @Override
            public String getDescription() {
                return "(PeptideShaker Modifications) *.psm";
            }
        };

        fileChooser.setFileFilter(filter);

        int returnVal = fileChooser.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {

            progressDialog = new ProgressDialogX(peptideShakerGUI, this, true); // note: not really possible to cancel this one...

            final PeptideShakerGUI tempRef = peptideShakerGUI; // needed due to threading issues

            new Thread(new Runnable() {

                public void run() {
                    progressDialog.setIndeterminate(true);
                    progressDialog.setTitle("Saving. Please Wait...");
                    try {
                        progressDialog.setVisible(true);
                    } catch (IndexOutOfBoundsException e) {
                        // ignore
                    }
                }
            }, "ProgressDialog").start();

            new Thread("SaveThread") {

                @Override
                public void run() {

                    String selectedFile = fileChooser.getSelectedFile().getPath();

                    if (!selectedFile.endsWith(".psm")) {
                        selectedFile += ".psm";
                    }

                    File newFile = new File(selectedFile);
                    int outcome = JOptionPane.YES_OPTION;

                    if (newFile.exists()) {
                        outcome = JOptionPane.showConfirmDialog(progressDialog,
                                "Should " + selectedFile + " be overwritten?", "Selected File Already Exists",
                                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    }

                    if (outcome != JOptionPane.YES_OPTION) {
                        progressDialog.dispose();
                        return;
                    }

                    try {

                        FileOutputStream fos = new FileOutputStream(newFile);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                        ObjectOutputStream oos = new ObjectOutputStream(bos);
                        oos.writeObject(searchParameters.getModificationProfile());
                        oos.close();
                        bos.close();
                        fos.close();
                        profileFile = newFile;
                        expectedModsLabel.setText("Expected Variable Modifications (" + newFile.getName().substring(0, newFile.getName().lastIndexOf(".")) + ")");
                        progressDialog.dispose();

                    } catch (Exception e) {
                        progressDialog.dispose();
                        JOptionPane.showMessageDialog(tempRef, "Failed saving the file.", "Error", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }//GEN-LAST:event_saveAsProfileBtnActionPerformed

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void searchPreferencesHelpJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchPreferencesHelpJButtonMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_searchPreferencesHelpJButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void searchPreferencesHelpJButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchPreferencesHelpJButtonMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_searchPreferencesHelpJButtonMouseExited

    /**
     * Open the help dialog.
     *
     * @param evt
     */
    private void searchPreferencesHelpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchPreferencesHelpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(peptideShakerGUI, getClass().getResource("/helpFiles/SearchPreferencesDialog.html"));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_searchPreferencesHelpJButtonActionPerformed

    /**
     * Changes the cursor to a hand cursor if over the color or PSI-MOD column.
     *
     * @param evt
     */
    private void expectedModificationsTableMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_expectedModificationsTableMouseMoved

        int row = expectedModificationsTable.rowAtPoint(evt.getPoint());
        int column = expectedModificationsTable.columnAtPoint(evt.getPoint());

        if (row != -1) {

            if (column == expectedModificationsTable.getColumn("  ").getModelIndex()) {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            } else if (column == expectedModificationsTable.getColumn("PSI-MOD").getModelIndex() && expectedModificationsTable.getValueAt(row, column) != null) {

                this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            } else {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
        }
    }//GEN-LAST:event_expectedModificationsTableMouseMoved

    /**
     * Changes the cursor back to the default cursor.
     *
     * @param evt
     */
    private void expectedModificationsTableMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_expectedModificationsTableMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_expectedModificationsTableMouseExited

    /**
     * Load the available modifications from file.
     *
     * @param evt
     */
    private void loadAvailableModsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadAvailableModsButtonActionPerformed
        JFileChooser fc = new JFileChooser(peptideShakerGUI.getLastSelectedFolder());

        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File myFile) {
                return myFile.getName().toLowerCase().endsWith("usermods.xml") || myFile.isDirectory();
            }

            @Override
            public String getDescription() {
                return "(user modification file) *usermods.xml";
            }
        };

        fc.setFileFilter(filter);

        int result = fc.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                ptmFactory.importModifications(file, true);
                loadModifications();
                updateModificationLists();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "An error occurred while importing " + file.getName() + ".", "User Modification File Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }//GEN-LAST:event_loadAvailableModsButtonActionPerformed

    /**
     * Opens a file chooser where the color for the ptm can be changed.
     *
     * @param evt
     */
    private void expectedModificationsTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_expectedModificationsTableMouseReleased
        int row = expectedModificationsTable.rowAtPoint(evt.getPoint());
        int column = expectedModificationsTable.columnAtPoint(evt.getPoint());

        if (row != -1) {
            int ptmIndex = expectedModificationsTable.convertRowIndexToModel(row);
            if (column == expectedModificationsTable.getColumn("  ").getModelIndex()) {
                Color newColor = JColorChooser.showDialog(this, "Pick a Color", (Color) expectedModificationsTable.getValueAt(ptmIndex, column));

                if (newColor != null) {
                    searchParameters.getModificationProfile().setColor(searchParameters.getModificationProfile().getPeptideShakerName(modificationList.get(ptmIndex)), newColor);
                    expectedModificationsTable.repaint();
                }
            } else if (column == expectedModificationsTable.getColumn("PSI-MOD").getModelIndex()) {
                // open protein link in web browser
                if (column == expectedModificationsTable.getColumn("PSI-MOD").getModelIndex() && evt != null && evt.getButton() == MouseEvent.BUTTON1) {
                    if (((String) expectedModificationsTable.getValueAt(ptmIndex, column)).lastIndexOf("<html>") != -1) {
                        String link = (String) expectedModificationsTable.getValueAt(ptmIndex, column);
                        link = link.substring(link.indexOf("\"") + 1);
                        link = link.substring(0, link.indexOf("\""));

                        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
                        BareBonesBrowserLaunch.openURL(link);
                        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                    } else {
                        new PtmDialog(this, this, ptmToPrideMap, ptmFactory.getPTM(modificationList.get(ptmIndex)));
                    }
                }
            }
        }
    }//GEN-LAST:event_expectedModificationsTableMouseReleased

    /**
     * Changes the cursor to a hand cursor if over the color or PSI-MOD column.
     *
     * @param evt
     */
    private void availableModificationsTableMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_availableModificationsTableMouseMoved
        int row = availableModificationsTable.rowAtPoint(evt.getPoint());
        int column = availableModificationsTable.columnAtPoint(evt.getPoint());

        if (row != -1) {
            if (column == availableModificationsTable.getColumn("PSI-MOD").getModelIndex() && availableModificationsTable.getValueAt(row, column) != null) {

                String tempValue = (String) availableModificationsTable.getValueAt(row, column);

                if (tempValue.lastIndexOf("<html>") != -1) {
                    this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                } else {
                    this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                }

            } else {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
        }
    }//GEN-LAST:event_availableModificationsTableMouseMoved

    /**
     * Changes the cursor back to the default cursor.
     *
     * @param evt
     */
    private void availableModificationsTableMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_availableModificationsTableMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_availableModificationsTableMouseExited

    /**
     * Opens a file chooser where the color for the ptm can be changed.
     *
     * @param evt
     */
    private void availableModificationsTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_availableModificationsTableMouseReleased
        int row = availableModificationsTable.rowAtPoint(evt.getPoint());
        int column = availableModificationsTable.columnAtPoint(evt.getPoint());

        if (row != -1) {
            int ptmIndex = availableModificationsTable.convertRowIndexToModel(row);
            if (column == availableModificationsTable.getColumn("PSI-MOD").getModelIndex()) {

                // open protein link in web browser
                if (column == availableModificationsTable.getColumn("PSI-MOD").getModelIndex() && evt != null && evt.getButton() == MouseEvent.BUTTON1
                        && ((String) availableModificationsTable.getValueAt(row, column)).lastIndexOf("<html>") != -1) {
                    if (((String) availableModificationsTable.getValueAt(ptmIndex, column)).lastIndexOf("<html>") != -1) {
                        String link = (String) availableModificationsTable.getValueAt(ptmIndex, column);
                        link = link.substring(link.indexOf("\"") + 1);
                        link = link.substring(0, link.indexOf("\""));

                        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
                        BareBonesBrowserLaunch.openURL(link);
                        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                    } else {
                        new PtmDialog(this, this, p
