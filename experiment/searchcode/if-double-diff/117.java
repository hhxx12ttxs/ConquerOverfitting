import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner.ListEditor;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class MainFrame extends JFrame 
implements WindowListener, KeyListener, ActionListener {
	
	private static final int CONSOLE_FONT_SIZE = 11;
	private static final String TABLE_ENTER_ACTION = "TABLE_ENTER_ACTION";
	private static final int SEPARATOR_WIDTH = 6;

	int rows, cols;
	DigitImage img;
	
	DigitImageCanvas digitImageCanvas;
	TLPCanvas tlpCanvas;
	RecognitionResultCanvas resultCanvas;

	JScrollPane consolePane, tlpPane;
	JSplitPane windowPane, leftPane, rightPane;
	JPanel learnPanel, testPanel;
	JTabbedPane tlpTabPane, datalistTabPane;
	
	JTable learnTable;
	DefaultTableModel learnTableModel;
	JComboBox digitComboBox;
	JButton changeLearnSaveDir, saveImageButton, saveImageAsButton, removeLearnImageButton; 
	JButton overwriteImageButton, addImageDirButton, clearImageButton;
	JTextField learnSaveDirField;
	JTextArea console;
	JPanel tlpPanel;
	
	JTable testTable;
	DefaultTableModel testTableModel;
	JComboBox testDigitComboBox;
	JButton changeTestSaveDirButton, saveTestImageButton, saveTestImageAsButton, removeTestImageButton;
	JButton overwriteTestImageButton, addTestDirButton, clearTestDirButton, doTestButton;
	JTextField testSaveDirField;
	
	JButton learntlpButton, resettlpButton, savetlpButton, loadtlpButton;
	JTextField alphaField, middleSizeField, errorField;
	JComboBox drawMiddleIdxComboBox;

	TwoLayerPerceptron tlp;
	
	public MainFrame(){
		super("Handwritten Digit Recognition using Neural Network - ver " + Main.VERSION);
		
		addWindowListener(this);
		addKeyListener(this);
	}
	
	public void makeLearnTable(){
		String colname[] = {"file", "number"};
		learnTableModel = new DefaultTableModel(colname, 0){
			@Override public boolean isCellEditable(int row, int col){ return false; }
		};
		learnTable = new JTable(learnTableModel);
		learnTable.addMouseListener(new TableMouseAdapter());
		learnTable.getActionMap().put(TABLE_ENTER_ACTION, new TableAction());
		learnTable.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), TABLE_ENTER_ACTION);
		learnTable.getColumnModel().getColumn(0).setPreferredWidth(400);
	}
	
	public void makeTestTable(){
		String testcolname[] = {"file", "number", "result"};
		testTableModel = new DefaultTableModel(testcolname, 0){
			@Override public boolean isCellEditable(int row, int col){ return false; }
		};
		testTable = new JTable(testTableModel);
		testTable.addMouseListener(new TestTableMouseAdapter());
		testTable.getActionMap().put(TABLE_ENTER_ACTION, new TestTableAction());
		testTable.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), TABLE_ENTER_ACTION);
		testTable.getColumnModel().getColumn(0).setPreferredWidth(400);
	}
	
	public void makeTLPPanel(double alpha, double error, int middleSize, int drawMiddleIdx){
		JPanel tlpControlPanel = new JPanel();
		tlpControlPanel.setLayout(new BoxLayout(tlpControlPanel, BoxLayout.X_AXIS));
		tlpControlPanel.add(new JLabel("alpha:"));
		
		alphaField = new JTextField();
		alphaField.setText(""+alpha);
		tlpControlPanel.add(alphaField);
		
		tlpControlPanel.add(new JLabel("err:"));
		
		errorField = new JTextField();
		errorField.setText(""+error);
		tlpControlPanel.add(errorField);
		
		learntlpButton = new JButton("Learn");
		learntlpButton.addActionListener(this);
		tlpControlPanel.add(learntlpButton);
		
		tlpControlPanel.add(new JLabel("middle:"));
		
		middleSizeField = new JTextField();
		middleSizeField.setText("" + middleSize);
		tlpControlPanel.add(middleSizeField);
		
		resettlpButton = new JButton("Reset");
		resettlpButton.addActionListener(this);
		tlpControlPanel.add(resettlpButton);
		
		savetlpButton = new JButton("Save");
		savetlpButton.addActionListener(this);
		tlpControlPanel.add(savetlpButton);
		
		loadtlpButton = new JButton("Load");
		loadtlpButton.addActionListener(this);
		tlpControlPanel.add(loadtlpButton);
		
		tlpControlPanel.add(new JLabel("draw:"));
		
		drawMiddleIdxComboBox = new JComboBox();
		drawMiddleIdxComboBox.addActionListener(this);
		for(int i=0;i<middleSize;i++){ drawMiddleIdxComboBox.addItem("" + i); }
		drawMiddleIdxComboBox.setSelectedIndex(drawMiddleIdx);
		tlpControlPanel.add(drawMiddleIdxComboBox);
		
		tlpPanel = new JPanel();
		tlpPanel.setLayout(new BorderLayout());
		tlpPanel.add(tlpControlPanel, BorderLayout.NORTH);
		tlpPanel.add(tlpCanvas, BorderLayout.CENTER);
	}

	public JPanel makeLearnControlPanel(String learnSaveDir){
		JPanel savePanel = new JPanel();
		savePanel.setLayout(new BoxLayout(savePanel, BoxLayout.X_AXIS));
		
		digitComboBox = new JComboBox();
		for(int i=0;i<10;i++){ digitComboBox.addItem("" + i); }
		savePanel.add(digitComboBox);
		
		learnSaveDirField = new JTextField(learnSaveDir);
		learnSaveDirField.setText(learnSaveDir);
		savePanel.add(learnSaveDirField);
		
		changeLearnSaveDir = new JButton("..");
		changeLearnSaveDir.addActionListener(this);
		savePanel.add(changeLearnSaveDir);
		
		/*
		saveImageButton = new JButton("Save");
		saveImageButton.addActionListener(this);
		savePanel.add(saveImageButton);
		*/
		
		saveImageAsButton = new JButton("Save");
		saveImageAsButton.addActionListener(this);
		savePanel.add(saveImageAsButton);
		
		overwriteImageButton = new JButton("Overwrite");
		overwriteImageButton.addActionListener(this);
		savePanel.add(overwriteImageButton);
		
		addImageDirButton = new JButton("Import");
		addImageDirButton.addActionListener(this);
		savePanel.add(addImageDirButton);
		
		removeLearnImageButton = new JButton("Remove");
		removeLearnImageButton.addActionListener(this);
		savePanel.add(removeLearnImageButton);
		
		clearImageButton = new JButton("Clear");
		clearImageButton.addActionListener(this);
		savePanel.add(clearImageButton);
		
		return savePanel;
	}
	
	public JPanel makeLearnPanel(JScrollPane learnScroll, JPanel learnControlPanel){
		JPanel learnPanel = new JPanel();
		learnPanel.setLayout(new BorderLayout());
		learnPanel.add(learnScroll, BorderLayout.CENTER);
		learnPanel.add(learnControlPanel, BorderLayout.SOUTH);
		return learnPanel;
	}
	
	public JPanel makeTestControlPanel(String testSaveDir){
		JPanel testControlPanel = new JPanel();
		testControlPanel.setLayout(new BoxLayout(testControlPanel, BoxLayout.X_AXIS));

		testDigitComboBox = new JComboBox();
		for(int i=0;i<10;i++){ testDigitComboBox.addItem("" + i); }
		testControlPanel.add(testDigitComboBox);

		testSaveDirField = new JTextField(testSaveDir);
		testSaveDirField.setText(testSaveDir);
		testControlPanel.add(testSaveDirField);

		changeTestSaveDirButton = new JButton("..");
		changeTestSaveDirButton.addActionListener(this);
		testControlPanel.add(changeTestSaveDirButton);

		/*
		saveTestImageButton = new JButton("Save");
		saveTestImageButton.addActionListener(this);
		testControlPanel.add(saveTestImageButton);
		*/

		saveTestImageAsButton = new JButton("Save");
		saveTestImageAsButton.addActionListener(this);
		testControlPanel.add(saveTestImageAsButton);

		overwriteTestImageButton = new JButton("Overwrite");
		overwriteTestImageButton.addActionListener(this);
		testControlPanel.add(overwriteTestImageButton);

		addTestDirButton = new JButton("Import");
		addTestDirButton.addActionListener(this);
		testControlPanel.add(addTestDirButton);

		removeTestImageButton = new JButton("Remove");
		removeTestImageButton.addActionListener(this);
		testControlPanel.add(removeTestImageButton);

		clearTestDirButton = new JButton("Clear");
		clearTestDirButton.addActionListener(this);
		testControlPanel.add(clearTestDirButton);

		doTestButton = new JButton("Test");
		doTestButton.addActionListener(this);
		testControlPanel.add(doTestButton);

		return testControlPanel;
	}
	
	public JPanel makeTestPanel(JComponent testScroll, JPanel testControlPanel){
		JPanel testPanel = new JPanel();
		testPanel.setLayout(new BorderLayout());
		testPanel.add(testScroll, BorderLayout.CENTER);
		testPanel.add(testControlPanel, BorderLayout.SOUTH);
		return testPanel;
	}
	
	public void makeRightPane(){
		tlpTabPane = new JTabbedPane();
		tlpTabPane.add("Perceptron Output", resultCanvas);
		tlpTabPane.add("Perceptron Controller", tlpPane);
		
		datalistTabPane = new JTabbedPane();
		datalistTabPane.add("Learn", learnPanel);
		datalistTabPane.add("Test", testPanel);

		rightPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		rightPane.setDividerLocation(195);
		rightPane.setDividerSize(SEPARATOR_WIDTH);
		rightPane.setTopComponent(tlpTabPane);
		rightPane.setBottomComponent(datalistTabPane);
	}
	
	public void makeLeftPane(){
		leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		leftPane.setDividerLocation(300);
		leftPane.setDividerSize(SEPARATOR_WIDTH);
		leftPane.setTopComponent(digitImageCanvas);
		leftPane.setBottomComponent(consolePane);
	}
	
	public void makeWindowPane(){
		windowPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		windowPane.setDividerLocation(450);
		windowPane.setDividerSize(SEPARATOR_WIDTH);
		windowPane.setLeftComponent(leftPane);
		windowPane.setRightComponent(rightPane);
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object source = arg0.getSource();
		if( source == saveImageButton ){
			saveAndAddImage();
		}else if( source == saveImageAsButton ){
			saveImageAs();
		}else if( source == addImageDirButton ){ 
			importLearnImage();
		}else if( source == learntlpButton ){
			learnAllImage();
		}else if( source == resettlpButton ){
			resetTLP();
		}else if( source == savetlpButton ){
			saveTLP();
		}else if( source == loadtlpButton ){
			loadTLP();
		}else if( source == removeLearnImageButton ){
			removeLearnImage();
		}else if( source == clearImageButton ){
			clearLearnImageList();
		}else if( source == overwriteImageButton ){
			overwriteSelectedImage();
		}else if( source == saveTestImageButton ){
			saveAndAddImage();
		}else if( source == saveTestImageAsButton ){
			saveTestImageAs();
		}else if( source == changeTestSaveDirButton ){
			changeTestSaveDir();
		}else if( source == overwriteTestImageButton ){
			overwriteCurrentTestImage();
		}else if( source == addTestDirButton ){
			importTestImage();
		}else if( source == doTestButton ){
			testAllImage();
		}else if( source == changeLearnSaveDir ){
			changeLearnSaveDir();
		}else if( source == removeTestImageButton ){
			removeTestImage();
		}else if( source == clearTestDirButton ){
			clearTestImage();
		}else if( source == drawMiddleIdxComboBox ){
			drawTLPMiddleLayer();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		char ch = e.getKeyChar();
		if( ch == 'c' ){
			clearImage();
		}else if( ch == 's' ){
			saveAndAddImage();
		}else if( ch == 'S' ){
			saveAndAddImage();
			clearImage();
		}else if( '0' <= ch && ch <= '9' ){
			changeImageDigit(ch);
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
	
	public JPanel makeTable(TwoLayerPerceptron tlp, boolean toMiddle){
		JPanel ret = null;
		
		return ret;
	}
	
	public void start(){
		setSize(1200, 900);

		String learnSaveDir = "data" + File.separator + "my-learn";
		String testSaveDir = "data" + File.separator + "my-test";
		int initialMiddle = 20;
		double initialAlpha = 0.1;
		double initialError = 0.1;
		int initialDrawMiddleIdx = 0;
		
		rows = cols = 28;
		img = new DigitImage(rows, cols);
		tlp = new TwoLayerPerceptron(rows * cols, initialMiddle, 10);

		digitImageCanvas = new DigitImageCanvas(img);
		resultCanvas = new RecognitionResultCanvas(tlp);
		repaintJudgeCanvas();
		tlpCanvas = new TLPCanvas(tlp, rows, cols);
		
		// changeLookAndFeel(); 

		makeLearnTable();
		JScrollPane learnScroll = new JScrollPane(learnTable);

		makeTestTable();
		JScrollPane testScroll = new JScrollPane(testTable);
		
		JPanel learnControlPanel = makeLearnControlPanel(learnSaveDir);
		learnPanel = makeLearnPanel(learnScroll, learnControlPanel);
		
		JPanel testControlPanel = makeTestControlPanel(testSaveDir);
		testPanel = makeTestPanel(testScroll, testControlPanel);
		
		makeTLPPanel(initialAlpha, initialError, initialMiddle, initialDrawMiddleIdx);
		tlpPane = new JScrollPane(tlpPanel);

		Font consoleFont = new Font("Courier New", Font.PLAIN, CONSOLE_FONT_SIZE);
		console = new JTextArea();
		console.setFont(consoleFont);
		consolePane = new JScrollPane(console);
		
		makeLeftPane();
		makeRightPane();
		makeWindowPane();
		add(windowPane, BorderLayout.CENTER);

		setVisible(true);
	}
	
	@Override
	public void windowActivated(WindowEvent arg0) {
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		System.exit(0);		
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		dispose();		
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
	}
	
	public void repaintJudgeCanvas(){
		double z[] = tlp.calculateOutput(img.toArray());
		resultCanvas.paintResult(z);
	}
	
	public void clearImage(){
		img.clear();
		digitImageCanvas.repaint();
		repaintJudgeCanvas();
	}
	
	public void saveImageAs(){
		saveImageAs(defaultSaveName(getImageDigit()));
	}

	public void saveTestImageAs(){
		saveImageAs(defaultSaveName(getTestImageDigit()));
	}

	public void saveImageAs(String defaultName){
		JFileChooser jfc = new JFileChooser(".");
		jfc.setSelectedFile( new File(defaultName) );
		int ret = jfc.showSaveDialog(this);
		if( ret != JFileChooser.CANCEL_OPTION ){
			saveImage(jfc.getSelectedFile().getPath());
		}			
	}

	public void changeImageDigit(char ch){
		Object comp = datalistTabPane.getSelectedComponent();
		if( comp == learnPanel ){
			digitComboBox.selectWithKeyChar(ch);
		}else{
			testDigitComboBox.selectWithKeyChar(ch);
		}
	}
	
	public int getImageDigit(){
		return Integer.valueOf((String)digitComboBox.getSelectedItem());
	}
	
	public int getTestImageDigit(){
		return Integer.valueOf((String)testDigitComboBox.getSelectedItem());
	}
	
	public void setImage(DigitImage img){
		digitImageCanvas.setImage(img);
		this.img = img;
	}
	
	public void loadImage(String path){
		DigitImage img = new DigitImage(rows, cols);
		img.setDigit(Main.getDigitFromName(path));
		if( img.readFile(path) ){
			setImage(img);
			digitImageCanvas.repaint();
			Main.repaintJudgeCanvas();
		}else{
			Main.log("<error> failed to load " + path);
		}
	}
	
	public void loadSelectedImage(){
		int selectedCnt = learnTable.getSelectedRowCount();
		if( selectedCnt != 1 ){
			return;
		}
		int r = learnTable.getSelectedRow();
		String path = (String)learnTableModel.getValueAt(r, 0);
		loadImage(path);
		Main.log("[Load] loaded " + path);
	}
	
	public void overwriteSelectedImage(){
		int selectedCnt = learnTable.getSelectedRowCount();
		if( selectedCnt != 1 ){
			Main.log("<error> failed to overwrite: no file is selected");
			return;
		}
		int r = learnTable.getSelectedRow();
		String path = (String)learnTableModel.getValueAt(r, 0);
		saveImage(path);
	}
	
	public void changeLearnSaveDir(){
	    JFileChooser dlg = new JFileChooser("./");
	    dlg.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

	    int selected = dlg.showOpenDialog(this);
	    if (selected == JFileChooser.APPROVE_OPTION){
	      learnSaveDirField.setText(dlg.getSelectedFile().getPath());
	    }			
	}
	
	public String defaultSaveName(int n){
		return n + "-" + Main.currentDateString() + ".dat";		
	}
	
	public void saveAndAddImage(){
		int num;
		String dir;
		DefaultTableModel tm;
		Object comp = datalistTabPane.getSelectedComponent();
		if( comp == learnPanel ){
			num = getImageDigit();
			dir = learnSaveDirField.getText();
			tm = learnTableModel;
		}else{
			num = getTestImageDigit();
			dir = testSaveDirField.getText();
			tm = testTableModel;
		}
		String path = dir + File.separator + defaultSaveName(num);
		if( saveImage(path) ){
			addImage(tm, path, num);
		}
	}
	
	public void addImage(DefaultTableModel tm, String filename, int n){
		tm.addRow(new String[]{filename, ""+n});
	}
	
	public boolean saveImage(String path){
		boolean ret = img.writeFile(path);
		if( ret ){
			Main.log("[Save] saved to " + path);
		}else{
			Main.log("<ERROR> failed to save " + path);
		}
		return ret;
	}
	
	public void addLearnFile(String path){
		int n = Main.getDigitFromName(path);
		if( n == -1 )
			return;
		if( path.length() < 4 || !path.endsWith(".dat") ){
			return;
		}
		learnTableModel.addRow(new String[]{path, ""+n});
	}
	
	public void importLearnImage(String dirpath){
		File d = new File(dirpath);
		if( d.isDirectory() ){
			File[] files = d.listFiles();
			for(File f : files){
				addLearnFile(f.getPath());
			}
			Main.log("[Import] " + dirpath);
		}
	}
	
	public void importLearnImage(){
	    JFileChooser dlg = new JFileChooser("./");
	    dlg.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

	    int selected = dlg.showOpenDialog(this);
	    if (selected == JFileChooser.APPROVE_OPTION){
	      importLearnImage(dlg.getSelectedFile().getPath());
	    }			
	}
	
	public void removeImage(JTable table, DefaultTableModel tm, boolean delete){
		int rows[] = table.getSelectedRows();
		for(int i=rows.length-1;i>=0;i--){
			int r = rows[i];
			String path = (String)tm.getValueAt(r, 0);
			tm.removeRow(r);
			if( delete ){
				File f = new File(path);
				if( f.delete() ){
					Main.log("Deleted " + path);
				}else{
					Main.log("<error> failed to delete " + path);
				}
			}
		}
	}
	
	public int removeDialog(int selnum){
		if( selnum == 0 ){
			JOptionPane.showMessageDialog(this, "no file is selected.");
			return JOptionPane.CLOSED_OPTION;
		}else{
			String selectvalues[] = {"Remove from list", "Remove from list, and delete", "Cancel"};
			return JOptionPane.showOptionDialog(this, "Remove selected files?", "Confirm", 
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null, selectvalues, selectvalues[0]);
		}
	}
	
	public void removeLearnImage(){
		int select = removeDialog(learnTable.getSelectedRowCount());
		if (select != JOptionPane.CLOSED_OPTION && select != 2 ){
			removeImage(learnTable, learnTableModel, select == 1);
		}
	}
	
	private void clearLearnImageList() {
		for(int i=learnTableModel.getRowCount()-1;i>=0;i--){
			learnTableModel.removeRow(i);
		}
	}

	public void addTestImage(String path){
		int n = Main.getDigitFromName(path);
		if( n == -1 )
			return;
		if( path.length() < 4 || !path.endsWith(".dat") ){
			return;
		}
		testTableModel.addRow(new String[]{path, ""+n, ""});
	}

	public void importTestImage(String dirpath){
		File d = new File(dirpath);
		if( d.isDirectory() ){
			File[] files = d.listFiles();
			for(File f : files){
				addTestImage(f.getPath());
			}
			Main.log("[Import to Test] " + dirpath);
		}
	}
	
	public void testAllImage(){
		class TestThread extends Thread{
			public void run(){
				disableTestButton();
				
				int imgNum = testTableModel.getRowCount();
				int correct = 0;
				for(int r=0;r<imgNum;r++){
					testTableModel.setValueAt("", r, 2);
				}
				Main.log("[Test] start : # of images = " + imgNum);
				for(int r=0;r<imgNum;r++){
					if( r > 0 && r % 1000 == 0 ){
						Main.log("[Test] " + r + " images completed.");
					}
					String path = (String)testTableModel.getValueAt(r, 0);
					int n = Integer.valueOf((String)testTableModel.getValueAt(r, 1));
					DigitImage img = new DigitImage(rows, cols);
					img.readFile(path);
					String result = "OK";
					int guessDigit = Main.testImage(tlp, img); 	
					if( guessDigit != n ){
						result = "FAIL:" + guessDigit; 
					}else{
						correct++;
					}
					testTableModel.setValueAt(result, r, 2);
				}
				Main.log("[Test] # of images: " + imgNum);
				Main.log("[Test] # of correct: " + correct);
				Main.log("[Test] correctness: " + ((double)correct/imgNum));

				enableTestButton();
			}
		}

		TestThread thread = new TestThread();
		thread.start();
	}

	
	public void overwriteCurrentTestImage(){
		int selectedNum = testTable.getSelectedRowCount();
		if( selectedNum != 1 ){
			Main.log("<error> failed to overwrite: no file is selected");
			return;
		}
		int r = testTable.getSelectedRow();
		String path = (String)testTableModel.getValueAt(r, 0);
		saveImage(path);
	}
	
	public void changeTestSaveDir(){
	    JFileChooser dlg = new JFileChooser("./");
	    dlg.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

	    int selected = dlg.showOpenDialog(this);
	    if (selected == JFileChooser.APPROVE_OPTION){
	    	testSaveDirField.setText(dlg.getSelectedFile().getPath());
	    }			
	}
	
	public void importTestImage(){
	    JFileChooser dlg = new JFileChooser("./");
	    dlg.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

	    int selected = dlg.showOpenDialog(this);
	    if (selected == JFileChooser.APPROVE_OPTION){
	      importTestImage(dlg.getSelectedFile().getPath());
	    }			
	}
	
	public void removeTestImage(){
		int select = removeDialog(testTable.getSelectedRowCount());
		if (select != JOptionPane.CLOSED_OPTION && select != 2 ){
			removeImage(testTable, testTableModel, select == 1);
		}
	}
	
	public void clearTestImage(){
		for(int i=testTableModel.getRowCount()-1;i>=0;i--){
			testTableModel.removeRow(i);
		}
	}
	
	public void loadSelectedTestImage(){
		int selectedNum = testTable.getSelectedRowCount();
		if( selectedNum != 1 ){
			return;
		}
		int r = testTable.getSelectedRow();
		String path = (String)testTableModel.getValueAt(r, 0);
		loadImage(path);
		Main.log("[Load] loaded " + path);
	}
	
	public DigitImage[] loadLearnImages(){
		int n = learnTableModel.getRowCount();
		String[] paths = new String[n];
		for(int i=0;i<n;i++){
			paths[i] = (String)learnTableModel.getValueAt(i, 0);
		}
		return Main.loadDigitImages(paths);
	}
	
	public void learnAllImage(){
		
		class LearnThread extends Thread{
			@Override
			public void run(){
				disableLearnButton();
				disableResetButton();

				double odata[][] = new double[10][10];
				for(int k=0;k<10;k++){
					for(int j=0;j<10;j++){
						double tmp = 0.1;
						if( k == j ){ tmp = 0.9; }
						odata[k][j] = tmp;
					}
				}

				DigitImage imgs[] = loadLearnImages();
				int imgNum = imgs.length;
				if( imgNum == 0 ){
					Main.log("<error> no entry in learn-list");
				}else{
					Example exset[] = Main.prepareExample(imgs);
					double alpha = Double.valueOf(alphaField.getText());
					double diff = Double.valueOf(errorField.getText());
					int max = 100000;
					Main.log("[Learn] start : alpha = " + alpha + ", diff = " + diff + ", max = " + max);
					tlp.learn(exset, alpha, diff, max);
					Main.log("[Learn] completed.");
				}
				tlpCanvas.repaint();
				enableLearnButton();
				enableResetButton();
			}
		}

		Thread thread = new LearnThread();
		thread.start();
	}

	public void reflectTLP(){
		int m = tlp.getMiddleSize();
		middleSizeField.setText(""+m);
		drawMiddleIdxComboBox.removeAllItems();
		for(int i=0;i<m;i++){ drawMiddleIdxComboBox.addItem("" + i); }
		tlpCanvas.setTLP(tlp);
		tlpCanvas.repaint();
	}
	
	public void resetTLP(){
		try{
			int m = Integer.valueOf(middleSizeField.getText());
			if( m > 0 ){
				tlp = new TwoLayerPerceptron(rows * cols, m, 10);
			}
		}catch(Exception e){}
		reflectTLP();
	}
	
	public void saveTLP(){
		String outname = "tlp-" + Main.currentDateString() + ".dat"; 
	    JFileChooser dlg = new JFileChooser(".");
	    dlg.setSelectedFile(new File(outname));

	    int selected = dlg.showSaveDialog(this);
	    if (selected == JFileChooser.APPROVE_OPTION){
	    	String path = dlg.getSelectedFile().getPath();
	    	tlp.writeFile(path);
	    	Main.log("TLP saved: " + path);
	    }			
	}
	
	public void loadTLP(){
	    JFileChooser dlg = new JFileChooser(".");
	    int selected = dlg.showOpenDialog(this);
	    if (selected == JFileChooser.APPROVE_OPTION){
	    	String path = dlg.getSelectedFile().getPath();
	    	tlp = TwoLayerPerceptron.readFile(path);
	    	if( tlp != null ){
	    		reflectTLP();
	    		Main.log("TLP loaded: " + path);
	    	}else{
	    		Main.log("<error> failed to load TLP: " + path);
	    	}
	    }			
	}
	
	public void drawTLPMiddleLayer(){
		Object sel = drawMiddleIdxComboBox.getSelectedItem();
		if( sel != null ){
			tlpCanvas.setDrawMiddleIdx(Integer.valueOf((String)sel));
			tlpCanvas.repaint();
		}
	}
	
	public void log(String msg) {
		console.append(msg + "\n");
		console.setCaretPosition(console.getText().length());
	}
	
	public void enableLearnButton(){ learntlpButton.setEnabled(true); }
	public void disableLearnButton(){ learntlpButton.setEnabled(false); }
	
	public void enableResetButton(){ resettlpButton.setEnabled(true); }
	public void disableResetButton(){ resettlpButton.setEnabled(false); }
	
	public void enableTestButton(){	doTestButton.setEnabled(true); }
	public void disableTestButton(){ doTestButton.setEnabled(false); }
	
	class TableMouseAdapter extends MouseAdapter {
		@Override public void mouseClicked(MouseEvent me) {
		    if(me.getClickCount() == 2) {
		    	loadSelectedImage();
		    }
		}
	}

	class TableAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			loadSelectedImage();
		}		
	}

	class TestTableMouseAdapter extends MouseAdapter {
		@Override public void mouseClicked(MouseEvent me) {
		    if(me.getClickCount() == 2) {
		    	loadSelectedTestImage();
		    }
		}
	}

	class TestTableAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			loadSelectedTestImage();
		}		
	}

	public void changeLookAndFeel(){
		String lafClassName = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
		try{
			UIManager.setLookAndFeel(lafClassName);
			SwingUtilities.updateComponentTreeUI(this);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	


}

