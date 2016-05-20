//MainWindow - the main window of the program
package gui;

import main.Program;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;

import java.io.File;
import java.lang.Short;
import java.util.*;

import dict.Dictionary;
import algorithm.Cosine;

//singleton class, final, no sub-classing of main window allowed
public final class MainWindow implements Runnable{

    //reference to the singleton MainWindow
    private static MainWindow mwRef = null;

    //method called to get the instance of the mainWindow
    //call this instead of constructor
    public static MainWindow GetInstance(){
        if(mwRef == null){
            mwRef = new MainWindow();
        }
        return mwRef;
    }
 
	///Event Handlers///
	
	//callback for click event on the exit menu item
    class ExitButtonListener implements ActionListener{
	    //callback handler
	    public void actionPerformed(ActionEvent e){
            //Exit program
            System.exit(0);
	    }
    }

    //callback for click event on the pstem test menu item
    class PStemTestButtonListener implements ActionListener{
	    //callback handler
	    public void actionPerformed(ActionEvent e){
            //open the PStemWindow
            SwingUtilities.invokeLater(new PorterstemWindow());
	    }
    }

    //callback for click event on the invidx test menu item
    class InvIdxWindowButtonListener implements ActionListener{
	    //callback handler
	    public void actionPerformed(ActionEvent e){
            //open the InvIdxStatWindow
            SwingUtilities.invokeLater(new InvertedIndexWindow());
	    }
    }
    
    //callback for click event on the spellCorrect test menu item
    class SpellCorrectWindowButtonListener implements ActionListener{
	    //callback handler
	    public void actionPerformed(ActionEvent e){
            //open the SpellCorectWindow
	    	SwingUtilities.invokeLater(new SpellCorrectWindow());
	    }
    }
    
    //callback for click event on the wildcard test menu item
    class WildcardWindowButtonListener implements ActionListener{
	    //callback handler
	    public void actionPerformed(ActionEvent e){
            //open the WildcardWindow
	    	SwingUtilities.invokeLater(new WildcardWindow());
	    }
    }
    
    //callback for click event on the TermsVsFreq stats menu button
    class termsVsFreqButtonListener implements ActionListener{
	    //callback handler
	    public void actionPerformed(ActionEvent e){
            //open the TermFrequencyChart window
	    	Dictionary.calcTermFrequency();
	    	SwingUtilities.invokeLater(new TermFrequencyChart());
	    }
    }
    
    //callback for click event on the tokensVsTerms stats menu button
    class tokensVsTermsButtonListener implements ActionListener{
	    //callback handler
	    public void actionPerformed(ActionEvent e){
            // show tokens/terms ratio window
            SwingUtilities.invokeLater(new TokenTermChart());
	    }
    }
    
    //callback for click event on the memUsage stats menu button
    class memUsageButtonListener implements ActionListener{
	    //callback handler
	    public void actionPerformed(ActionEvent e){
            // show tokens/terms ratio window
            SwingUtilities.invokeLater(new MemUsageWindow());
	    }
    }
    
    //click on ranking button radio button
    class EnableRankingButtonListener implements ActionListener{
		public void actionPerformed(ActionEvent arg0) {
			MainWindow.GetInstance().ToggleReqAllToks();
		}
    }
    //click on the calculate cosine similarity menu item 
    class CalcCosineSimListener implements ActionListener{
		public void actionPerformed(ActionEvent arg0) {
			int doc1 = MainWindow.GetInstance().GetSelectedDoc1();
			int doc2 = MainWindow.GetInstance().GetSelectedDoc2();
			if(doc1 < 0 || doc2 < 0)
				return;
			Cosine cosSim = new Cosine(doc1, doc2);
			double similarity = cosSim.getSimilarity();
			JOptionPane.showMessageDialog(MainWindow.GetInstance().f,
			"Cos Similarity between DocID: " + doc1 + " and DocID: " +
									doc2 + " is: " + similarity);
		}
    }
    
    class CalcAuthorCosineSimListener implements ActionListener{
    	JFileChooser directoryChooser;
	    //constructor
	    public CalcAuthorCosineSimListener(){
            directoryChooser = new JFileChooser(".");
            directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            directoryChooser.addActionListener(new FileChooserListener(ChooserType.DIRECTORY));
	    }
	    //callback handler
	    public void actionPerformed(ActionEvent e){
		    //now that the button has been clicked, show the jFileChooser
            directoryChooser.showOpenDialog(MainWindow.GetInstance().f);
	    }
    }

	//type enum for the file/directory chooser listener
	public enum ChooserType { FILE, DIRECTORY }
    
    //callback for events on the filechooser window
    class FileChooserListener implements ActionListener{
    	private ChooserType type;
    	
    	FileChooserListener(ChooserType type){
    		this.type = type;
    	}
        //the action listener function
        public void actionPerformed(ActionEvent actionEvent){
            //alias to the file chooser to enable access to its data
            JFileChooser fileChooser = (JFileChooser) actionEvent.getSource();

            String command = actionEvent.getActionCommand();
            if(command.equals(JFileChooser.APPROVE_SELECTION)){
                //get the selected filename
                File selectedFile = fileChooser.getSelectedFile();
                String directory = selectedFile.getParent();
                String filename = selectedFile.getName();
                String FName = directory+"/"+filename;
                
                boolean indexDirectory;
                IndexFileWorker ifWorker;
                
                if(type == ChooserType.FILE){
	                //update main window texts
	                MainWindow.GetInstance().SetStatusLabel("Building Dictionary for: " + filename);
	                MainWindow.GetInstance().SetFile(filename);
	                indexDirectory = false;
	                //create, register callbacks and run the asynchronous worker
	                ifWorker = new IndexFileWorker(FName, indexDirectory);
                }
                else{ //type == ChooserType.Directory (generate the cosine similarity report)
                	MainWindow.GetInstance().SetStatusLabel("Generating Cosine Report for directory: " + filename);
                	indexDirectory = true;
	                //create, register callbacks and run the asynchronous worker
	                ifWorker = new IndexFileWorker(FName, indexDirectory);
                }



                //callback for asynchronous update of the progress bar
    		    ifWorker.addPropertyChangeListener(new PropertyChangeListener(){
    			    public void propertyChange(PropertyChangeEvent evt){
    				    if("progress".equals(evt.getPropertyName())){
				    		MainWindow.GetInstance().SetProgressBar(
                                                (Integer)evt.getNewValue());
    				    }
    			    }
    		    });
                ifWorker.execute(); //schedule asynchronous run
            }
        }
    }

    //choose file click event
    class ChooseFileButtonListener implements ActionListener{
        JFileChooser fileChooser;
	    //constructor
	    public ChooseFileButtonListener(){
            fileChooser = new JFileChooser(".");
            fileChooser.addActionListener(new FileChooserListener(ChooserType.FILE));
	    }
	    //callback handler
	    public void actionPerformed(ActionEvent e){
		    //now that the button has been clicked, show the jFileChooser
            fileChooser.showOpenDialog(MainWindow.GetInstance().f);
	    }
    }


    //search button click event
    class SearchButtonListener implements ActionListener{
	    //references to search worker input
	    JTextField textField;
        //references to search worker output
	    JProgressBar progressBar;
	    ReturnedDocsTable retDocsModel;

	    boolean reqAllToks = false;
        MainWindow mw;

	    //constructor
	    public SearchButtonListener(JTextField textFieldIn,
                                    JProgressBar progressBarIn,
                                    ReturnedDocsTable retDocsModelIn,
                                    MainWindow mwIn){
		    textField = textFieldIn;
		    progressBar = progressBarIn;
            retDocsModel = retDocsModelIn;
            mw = mwIn;
	    }
	    public void ToggleReqAllToks(){
	    	if(reqAllToks)
	    		reqAllToks = false;
	    	else
	    		reqAllToks = true;
	    }
	    //callback handler
	    public void actionPerformed(ActionEvent e){
		    //now that the button has been clicked, get the text in the text field
		    String text = textField.getText();
            //set main window text
            MainWindow.GetInstance().SetStatusLabel("Searching for: " + text);

		    //create, register callbacks, and asynchronously run the search
		    Search search = new Search(text, reqAllToks);
		    search.addPropertyChangeListener(new PropertyChangeListener(){
			    public void propertyChange(PropertyChangeEvent evt){
				    if("progress".equals(evt.getPropertyName())){
					    MainWindow.GetInstance().SetProgressBar((Integer)evt.getNewValue());
				    }
                    if("document".equals(evt.getPropertyName())){
                    	DocScore returnedDoc = (DocScore)evt.getNewValue();
                        if(returnedDoc.docID == -1){
                           retDocsModel.AddDocument(-1, 0);
                        }
                        else{
                           retDocsModel.AddDocument(returnedDoc.docID,
                        		   					returnedDoc.score);
                        }
                    }
			    }
		    });
            //clear previous search results
            retDocsModel.Clear();
		    search.execute(); //schedule for asynchronous execution
	    }
    }

    //enter key search event (delegate's search functionality)
    private class EntryEnterKeyListener extends KeyAdapter{
        SearchButtonListener searchButtonListener;
        public EntryEnterKeyListener(SearchButtonListener sblIn){
            searchButtonListener = sblIn;
        }
        public void keyReleased(KeyEvent e){
            if(e.getKeyCode() == KeyEvent.VK_ENTER)
                searchButtonListener.actionPerformed(
                    new ActionEvent(this, 0, ""));
        }
    }

    //click on a displayed returned document
    class RetDocsClickListener extends MouseAdapter{
        //references to input
        JTable list;
        ReturnedDocsTable listModel;

        public RetDocsClickListener(JTable listIn,
        						ReturnedDocsTable listModelIn){
            list = listIn;
            listModel = listModelIn;
        }

        public void mouseClicked(MouseEvent e){
            if(e.getClickCount() == 2){
                int i = list.getSelectedRow();
                int docID = (Integer)listModel.getValueAt(i,0);
                String docIDString = "Document number: " + docID;
                if(docID > -1){
                    String doc = Dictionary.documents.elementAt(docID-1);
                    //spawn a new document window
                    SwingUtilities.invokeLater(
                    					new DisplayDocWindow(docIDString, doc));
                }
            }
        }
    }
    
    
    //table model for the JTable display
    class ReturnedDocsTable extends AbstractTableModel{
    	private static final long serialVersionUID = 1L; //stop warn message
    	public Vector<Integer> docIDs = new Vector<Integer>();
    	public Vector<Double> ranks = new Vector<Double>();
    	public String getColumnName(int col){
    		if(col == 0)
    			return "DocID";
    		else
    			return "Rank";
    	}
    	public Object getValueAt(int row, int col){
			if(col == 0)
			{
				int retVal = docIDs.elementAt(row);
				if(retVal == -1)
					return "No Documents Returned";
				else
					return retVal;
			}
    		else
    			return ranks.elementAt(row);
    	}
    	public int getRowCount(){return docIDs.size();}
    	public int getColumnCount(){return 2;}
    	public boolean isCellEditable(int row, int col){return false;}
    	public void AddDocument(int docID, double rank){
			docIDs.add(docID);
			ranks.add(rank);
			int newRow = docIDs.size()-1;
			//tell the renderer we've inserted a row
    		fireTableRowsInserted(newRow, newRow);
    	}
    	public void Clear(){
    		docIDs.clear();
    		ranks.clear();
    		//update display
    		fireTableDataChanged();
    	}
    }

    ///class attributes///
    
    String selectedFile = "None";
    
    //search returned documents list model
    private final ReturnedDocsTable retDocsListModel =
    								new ReturnedDocsTable();

	//window components
    JFrame f = new JFrame("Mocha Search Engine");
    //Search Components
    private final JLabel selectedFileLabel =
    							new JLabel("Indexed File: " + selectedFile);
    private final JLabel searchFieldLabel = new JLabel("Mocha Search:");
	private final JTextField textField = new JTextField("");
    private final JButton searchButton = new JButton("Search");
    private final JLabel returnedDocsLabel = new JLabel("Returned Documents:");
	private final JTable retDocsList =
							new JTable(retDocsListModel);
    private final JScrollPane scrollPane = new JScrollPane(retDocsList);
    //status components
	private final JProgressBar progressBar = new JProgressBar(0,100);
	private final JLabel statusLabel = new JLabel("Status: Idle");
	//private final JLabel memUsageLabel = new JLabel("MemUsage: 0 MB");
	//Layout boxes
    private final Box fileBox = new Box(BoxLayout.X_AXIS);
    private final Box searchLabelBox = new Box(BoxLayout.Y_AXIS);
    private final Box searchBox = new Box(BoxLayout.X_AXIS);
    private final Box resultLabelBox = new Box(BoxLayout.X_AXIS);
    private final Box statusBox = new Box(BoxLayout.X_AXIS);
    private final Box mainBox = new Box(BoxLayout.Y_AXIS);
    //Menu Components
    private final JMenuBar menuBar = new JMenuBar();
    private final JMenu fileMenu = new JMenu("File");
    private final JMenu statisticsMenu = new JMenu("Statistics");
    private final JMenu testingMenu = new JMenu("Testing");
    private final JMenu optionsMenu = new JMenu("Options");

    //shared actionListeners
    private SearchButtonListener searchButtonListener =
    				new SearchButtonListener(
    						textField, progressBar, retDocsListModel, this);

    //private to preserve singleton class property
    private MainWindow(){
    	//build the file menu
    	JMenuItem idxFile = new JMenuItem("Open File");
    	JMenuItem ext = new JMenuItem("Exit");
    	idxFile.addActionListener(new ChooseFileButtonListener());
    	ext.addActionListener(new ExitButtonListener());
    	fileMenu.add(idxFile);
    	fileMenu.add(ext);
    	
    	//build the stats menu
    	JMenuItem invStat = new JMenuItem("Inverted Index");
    	JMenuItem tVF = new JMenuItem("Terms vs Frequency");
    	JMenuItem tVT = new JMenuItem("Tokens vs Terms");
    	JMenuItem mUW = new JMenuItem("Estimated Mem Usage");
    	invStat.addActionListener(new InvIdxWindowButtonListener());
    	tVF.addActionListener(new termsVsFreqButtonListener());
    	tVT.addActionListener(new tokensVsTermsButtonListener());
    	mUW.addActionListener(new memUsageButtonListener());
    	statisticsMenu.add(invStat);
    	statisticsMenu.add(tVF);
    	statisticsMenu.add(tVT);
    	statisticsMenu.add(mUW);
    	
    	//build the testing menu
    	JMenuItem pStem = new JMenuItem("Porter Stemmer");
    	JMenuItem sPCorr = new JMenuItem("Spelling Correction");
    	JMenuItem wCQuery = new JMenuItem("Wildcard Query");
    	pStem.addActionListener(new PStemTestButtonListener());
    	sPCorr.addActionListener(new SpellCorrectWindowButtonListener());
    	wCQuery.addActionListener(new WildcardWindowButtonListener());
    	testingMenu.add(pStem);
    	testingMenu.add(sPCorr);
    	testingMenu.add(wCQuery);
    	
    	//build the options menu
    	JCheckBoxMenuItem enableTokReqCheck =
    					new JCheckBoxMenuItem("Require All Query Tokens");
    	JMenuItem calcCosineSimButton =
    					new JMenuItem("Calculate Cosine Similarity");
    	JMenuItem calcAuthorCosineSimButton  =
    				new JMenuItem("Calculate Author Cosine Similarity");
    	enableTokReqCheck.addActionListener(new EnableRankingButtonListener());
    	calcCosineSimButton.addActionListener(new CalcCosineSimListener());
    	calcAuthorCosineSimButton.addActionListener(new CalcAuthorCosineSimListener()); 
    	optionsMenu.add(enableTokReqCheck);
    	optionsMenu.add(calcCosineSimButton);
    	optionsMenu.add(calcAuthorCosineSimButton);
    	
    	menuBar.add(fileMenu);
    	menuBar.add(statisticsMenu);
    	menuBar.add(testingMenu);
    	menuBar.add(optionsMenu);
    }
	
	public void run(){
        //set the icon image and layout manager of the rootPane
        f.setIconImage(Program.windowIcon);
        f.setLayout(new BorderLayout());

        //dimensions used for empty fillers
        Dimension minDim = new Dimension(25, 10);
        Dimension prefDim = new Dimension(150, 10);
        Dimension maxDim = new Dimension(Short.MAX_VALUE, 10);

        //configure component sizes
        textField.setMaximumSize(new Dimension(500, 30));
        textField.setPreferredSize(new Dimension(500, 30));
        textField.setMinimumSize(new Dimension(50, 30));
        
        //border adjustments
        mainBox.setBorder(new EmptyBorder(new Insets(0,10,10,10)));
        selectedFileLabel.setBorder(new EmptyBorder(new Insets(0,0,5,0)));
        searchBox.setBorder(new EmptyBorder(new Insets(0,0,10,0)));
        searchFieldLabel.setBorder(new EmptyBorder(new Insets(0,0,5,0)));
        returnedDocsLabel.setBorder(new EmptyBorder(new Insets(0,0,5,0)));
        statusBox.setBorder(new EmptyBorder(new Insets(10,0,0,0)));
        fileBox.setBorder(new EmptyBorder(new Insets(10,0,0,10)));
        resultLabelBox.setBorder(new EmptyBorder(new Insets(0,0,0,10)));
        
        //alignment adjustments (all components must have the same alignment
        //when using box layout to prevent left aligning to the center of
        //center aligned components)
        fileBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchLabelBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultLabelBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		//connect all of the window components (and add padding)
        fileBox.add(selectedFileLabel);
        fileBox.add(new Box.Filler(minDim, prefDim, maxDim));

        searchLabelBox.add(searchFieldLabel);

		searchBox.add(textField);
        searchBox.add(new Box.Filler(minDim, prefDim, maxDim));
        searchBox.add(searchButton);

        resultLabelBox.add(returnedDocsLabel);

        statusBox.add(statusLabel);
        statusBox.add(new Box.Filler(minDim, prefDim, maxDim));

        //add everything to the main box
        mainBox.add(fileBox);
        mainBox.add(searchLabelBox);
        mainBox.add(searchBox);
        mainBox.add(resultLabelBox);
		mainBox.add(scrollPane);
		mainBox.add(statusBox);

		f.add(menuBar, BorderLayout.NORTH);
        f.add(mainBox, BorderLayout.CENTER);
		f.add(progressBar, BorderLayout.PAGE_END);
		
		//register event listeners
        textField.addKeyListener(
            new EntryEnterKeyListener(searchButtonListener));
		searchButton.addActionListener(searchButtonListener);
        retDocsList.addMouseListener(
              new RetDocsClickListener(retDocsList, retDocsListModel));

		//finish setting up the window
		f.pack();
        f.setSize(400, 350);
        f.setLocationByPlatform(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}

    //update the selected file JLabel text
    public void SetFile(String fileName){
        selectedFile = fileName;
        selectedFileLabel.setText("Selected file: " + selectedFile);
    }
    //update the progress bar
    public void SetProgressBar(int val){
        if(val > 99)
            progressBar.setValue(0);
        else
            progressBar.setValue(val);
    }
    //functions for updating the status texts
    public void SetStatusLabel(String in){
    	statusLabel.setText("Status: " + in);
    }
    public void SetStatusIdle(){
    	SetStatusLabel("Idle");
    }
    
    public void ToggleReqAllToks(){
    	searchButtonListener.ToggleReqAllToks();
    }
    
    public int GetSelectedDoc1(){
    	int row = retDocsList.getSelectedRow();
    	if(row < 0)
    		return -1;
    	return (Integer)retDocsListModel.getValueAt(row, 0);
    }
    public int GetSelectedDoc2(){
    	int[] selectedRows = retDocsList.getSelectedRows();
    	if(selectedRows.length > 0)
    		return (Integer)retDocsListModel.getValueAt(
    					selectedRows[selectedRows.length-1], 0);
    	else
    		return -1;
    }
}


