/*
 * ERProcessor.java
 */
package edu.duke.archives.erprocessor;

import edu.duke.archives.erprocessor.exceptions.PreviewException;
import edu.duke.archives.erprocessor.metadata.mets.EAD;
import edu.duke.archives.erprocessor.metadata.mets.METS;
import edu.duke.archives.erprocessor.metadata.mets.MetsDiv;
import edu.duke.archives.erprocessor.metadata.mets.MetsFile;
import edu.duke.archives.erprocessor.metadata.mets.MetsNode;
import edu.duke.archives.erprocessor.treeDisplay.DNDTree;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * The main class of the application.
 */
public class ERProcessor extends SingleFrameApplication {

    private ERProcessorView erpv;
    private File projectFile;
    private METS mets;
    protected PreviewHandler previewHandler = null;
    String browserRequest = "Default";

    protected void loadProject(File projectFile) {
        try {
            if (projectFile == null) {
                mets = new METS();
            } else {
                this.projectFile = projectFile;
                Document doc = new SAXBuilder().build(projectFile);
                mets = new METS(doc);
            }

            erpv.fileTree =
                    new DNDTree(new DefaultTreeModel(mets.getStructMap(METS.ACCESSIONS_STRUCTMAP)));
            erpv.fileTree.addTreeSelectionListener(erpv);
            erpv.fileSP.setViewportView(erpv.fileTree);
            erpv.fileSP.updateUI();

            erpv.seriesTree =
                    new DNDTree(new DefaultTreeModel(mets.getStructMap(METS.COLLECTION_STRUCTMAP)));
            erpv.seriesTree.addTreeSelectionListener(erpv);
            erpv.seriesSP.setViewportView(erpv.seriesTree);
            erpv.seriesTree.updateUI();


            //Right-click actions for JTrees
            erpv.seriesTree.addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        popupAction(e);
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        popupAction(e);
                    }
                }

                protected void popupAction(MouseEvent e) {
                    erpv.collectionPopup = new JPopupMenu();
                    JMenuItem mi;

                    TreePath path = erpv.seriesTree.getPathForLocation(e.getX(),
                            e.getY());

                    if (path == null) {
                        //Did not r-click on tree indicating no selection
                        erpv.seriesTree.removeSelectionPaths(erpv.seriesTree.getSelectionPaths());
                    }
                    if (erpv.seriesTree.getSelectionCount() < 2) {
                        if ((path == null) ||
                                (erpv.seriesTree.getLastSelectedPathComponent() instanceof MetsDiv)) {
                            mi = new JMenuItem("Insert a sub-division");
                            mi.addActionListener(erpv);
                            mi.setActionCommand("insertCollDiv");
                            erpv.collectionPopup.add(mi);

                        }
                    }
                    if (path != null) {
                        mi = new JMenuItem("Remove");
                        mi.addActionListener(erpv);
                        mi.setActionCommand("removeCollectionltem");
                        erpv.collectionPopup.add(mi);

                        if (erpv.seriesTree.getLastSelectedPathComponent() instanceof MetsFile && previewHandler.getAvailableBrowsers().
                                size() > 0) {
                            erpv.collectionPopup.addSeparator();
                            for (Object text : previewHandler.getAvailableBrowsers()) {
                                //TODO:Use action map?
                                mi = new JMenuItem(text.toString() + " preview");
                                mi.addActionListener(erpv);
                                mi.setActionCommand("preview:" + text.toString());
                                erpv.collectionPopup.add(mi);
                            }
                        }
                    }

                    erpv.collectionPopup.show((JComponent) e.getSource(),
                            e.getX(), e.getY());
                }
            });
            erpv.fileTree.addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        popupAction(e);
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        popupAction(e);
                    }
                }

                protected void popupAction(MouseEvent e) {
                    erpv.collectionPopup = new JPopupMenu();
                    JMenuItem mi;

                    TreePath path = erpv.fileTree.getPathForLocation(e.getX(),
                            e.getY());

                    if (path == null) {
                        //Did not r-click on tree indicating no selection
                        erpv.fileTree.removeSelectionPaths(erpv.fileTree.getSelectionPaths());
                    }
                    if (path != null) {
                        if (erpv.fileTree.getLastSelectedPathComponent() instanceof MetsFile && previewHandler.getAvailableBrowsers().
                                size() > 0) {
                            for (Object text : previewHandler.getAvailableBrowsers()) {
                                //TODO:Use action map?
                                mi = new JMenuItem(text.toString() + " preview");
                                mi.addActionListener(erpv);
                                mi.setActionCommand("preview:" + text.toString());
                                erpv.collectionPopup.add(mi);
                            }
                        }
                    }
                    erpv.collectionPopup.show((JComponent) e.getSource(),
                            e.getX(), e.getY());
                }
            });
            erpv.getFrame().repaint();
        } catch (Exception ex) {
            Logger.getLogger(ERProcessor.class.getName()).
                    log(Level.SEVERE, null, ex);
            System.err.println(ex.getLocalizedMessage());
            JOptionPane.showMessageDialog(erpv.getFrame(), "Unable to load " +
                    projectFile.getAbsolutePath(), "Could not load project file",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    protected void saveProject(File projectFile) throws Exception {
        projectFile.getParentFile().mkdirs();
        FileOutputStream stream = new FileOutputStream(projectFile);
        OutputStreamWriter osw = new OutputStreamWriter(stream, "UTF-8");
        PrintWriter output = new PrintWriter(osw);
        Format format = Format.getCompactFormat();
        XMLOutputter outputter = new XMLOutputter(format);
        outputter.output(mets.getDoc(), output);

        output.close();
        osw.close();
        stream.close();
        return;
    }

    protected void saveProject() throws Exception {
        if (projectFile == null) {
            JFileChooser jfc = new JFileChooser();
            if (jfc.showSaveDialog(erpv.getFrame()) ==
                    JFileChooser.APPROVE_OPTION) {
                projectFile = jfc.getSelectedFile();
                saveProject(projectFile);
            }
        } else {
            saveProject(projectFile);
        }
        return;
    }

    protected void closeProject() {
        projectFile = null;
        mets = null;
        return;
    }

    protected void loadAccession(File accessionFile) {
        try {
            if (mets == null) { //New project file
                this.loadProject(null);
            }
            mets.loadAccession(accessionFile);
            erpv.fileTree.updateUI();
            erpv.fileTree.expandRow(erpv.fileTree.getRowCount());
            erpv.fileSP.setViewportView(erpv.fileTree);
            erpv.fileSP.updateUI();
        } catch (java.lang.OutOfMemoryError oome) {
            JOptionPane.showMessageDialog(erpv.getFrame(),
                    "Unable to load " +
                    accessionFile.getAbsolutePath() +
                    ". The system ran out of memory. Please run the program with more memory alloted to the Java virtual machine.",
                    "Could not add accession file",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    static public String prettySize(String sizeString) {
        try {
            int size = Integer.parseInt(sizeString);
            return prettySize(size);
        } catch (NumberFormatException nfe) {
            return "";
        }
    }

    static public String prettySize(long size) {
        String prettySize = "";
        String[] measures = {"B", "KB", "MB", "GB", "TB", "EB", "ZB", "YB"};

        int power = measures.length - 1;
        //Cycle each measure starting with the smallest
        for (int i = 0; i < measures.length; i++) {
            //Test for best fit 
            if ((size / (Math.pow(1024, i))) < 1024) {
                power = i;
                break;
            }
        }
        DecimalFormat twoPlaces = new DecimalFormat("#,##0.##");
        Double newSize = (size / (Math.pow(1024, power)));
        prettySize = twoPlaces.format(newSize) + " " + measures[power];
        return prettySize;
    }

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup() {
        try {
            previewHandler =
                    PreviewHandler.getHandler();
        } catch (Exception ex) {
            System.err.println("Unable to setup the preview handler! " +
                    "Moving on without it...");
            Logger.getLogger(ERProcessor.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
        erpv = new ERProcessorView(this);
        show(erpv);
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override
    protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of ERProcessor
     */
    public static ERProcessor getApplication() {
        return Application.getInstance(ERProcessor.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(ERProcessor.class, args);
    }

    public void createEAD() {

        EAD ead = new EAD();
        ead.setDateGranularity(EAD.YEAR_MONTH);
        if (erpv.seriesTree == null) {
            return;
        }
        String code = ead.convert2EAD(
                ((MetsNode) erpv.seriesTree.getModel().getRoot()).getMETS());
        //Display EAD code in new window with text area.
        JTextArea code_area = new JTextArea(code, 30, 80);
        JScrollPane code_scroll = new JScrollPane(code_area);

        JFrame frame = new JFrame("EAD Parts");
        JPanel center = new JPanel(new BorderLayout());
        center.add(code_scroll, BorderLayout.CENTER);
        frame.setContentPane(center);
        frame.pack();
        frame.setVisible(true);
//        System.out.println(code);
    }

    @Action
    public Task previewFile() {
        return new PreviewFileTask(org.jdesktop.application.Application.getInstance(edu.duke.archives.erprocessor.ERProcessor.class));
    }

    @Action
    public Task previewFile(String browser) {
        browserRequest = browser;
        return new PreviewFileTask(org.jdesktop.application.Application.getInstance(edu.duke.archives.erprocessor.ERProcessor.class));
    }

    private class PreviewFileTask extends org.jdesktop.application.Task<Object, Void> {

        PreviewFileTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to PreviewFileTask fields, here.
            super(app);
        }

        @Override
        protected Object doInBackground() {
            try {
                //Setup handler
                if (!previewHandler.isBaseSet()) {
                    JFileChooser jfc = new JFileChooser();
                    jfc.setMultiSelectionEnabled(false);
                    jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    jfc.setApproveButtonText("Select");
                    jfc.setApproveButtonToolTipText("Select as base (accessions) directory.");
                    jfc.setDialogTitle("Select Base (Accessions) Directory");
                    if (jfc.showSaveDialog(erpv.getFrame()) ==
                            JFileChooser.APPROVE_OPTION) {
                        previewHandler.setBase(jfc.getSelectedFile());
                    } else {
                        return null;
                    }
                }
                //Get selected file
                Object node = null;
                if (erpv.fileTree.getSelectionCount() > 0) {
                    node = erpv.fileTree.getSelectionPath().
                            getLastPathComponent();
                } else if (erpv.seriesTree.getSelectionCount() > 0) {
                    node = erpv.seriesTree.getSelectionPath().
                            getLastPathComponent();
                } else {
                    return null;
                }
                //Try to launch
                if (node != null && node instanceof MetsFile) {
                    erpv.setMessage("Previewing " + node.toString());
                    previewHandler.previewFile((MetsFile) node, browserRequest);
                }
            } catch (PreviewException pe) {
                Logger.getLogger(ERProcessor.class.getName()).
                        log(Level.SEVERE, null, pe);
                //Notify user
                JOptionPane.showMessageDialog(erpv.getFrame(),
                        "Cannot preview file!\n" + pe.getMessage(),
                        "Cannot preview file!", JOptionPane.ERROR_MESSAGE);
            } catch (FileNotFoundException fnfe) {
                Logger.getLogger(ERProcessor.class.getName()).
                        log(Level.SEVERE, null, fnfe);
                //Notify user
                JOptionPane.showMessageDialog(erpv.getFrame(),
                        "Cannot preview file!\n" + fnfe.getMessage(),
                        "Cannot preview file!", JOptionPane.ERROR_MESSAGE);
            }
            return null;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
            browserRequest = PreviewHandler.VIEWER_DEFAULT;
            erpv.setMessage("");
        }
    }
}

