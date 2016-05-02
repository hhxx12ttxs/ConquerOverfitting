package com.lab111.labworkS4L2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * GUI class of diagram drawer. Responsible for View and, partly, Controller
 * @author wizzardich
 *
 */
public class MainFrame{
	/**
	 * Class responsible for graphics handling
	 * @author wizzardich
	 *
	 */
	private class Canvas extends JPanel{
		//Constants
		private static final long serialVersionUID = 42L;
	    private final Color bg = Color.white;
	    //Fields
		public Drawer d;
		
		public Canvas(BorderLayout borderLayout) {
			super(borderLayout);
			this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			this.addMouseListener(new MouseListener(){

				@Override
				public void mouseClicked(MouseEvent arg0) {
					double max = 0;
					String tabName = MainFrame.this.getCurrentTabName();
					String[][] data = MainFrame.this.dm.getElementByName(tabName).getDataTable();
					for(int i = 0;i<data.length;i++){
						double s = 0;
						s = Double.parseDouble(data[i][1]);
						if (s>max) max = s;
					}
					max+=0.1*max;
					int row = d.getBar(arg0.getX());
					int column = 1;
					double x = (0.95*Canvas.this.getHeight())-arg0.getY();
					double y = (0.95*Canvas.this.getHeight());
					String whereTo = Double.toString((double)((x/y)*max));
					MainFrame.this.updateElement(tabName, row, column, whereTo);
				}

				@Override
				public void mouseEntered(MouseEvent arg0) {
				}

				@Override
				public void mouseExited(MouseEvent arg0) {
				}

				@Override
				public void mousePressed(MouseEvent arg0) {
				}

				@Override
				public void mouseReleased(MouseEvent arg0) {
				}
				
			});
		}
		
		/**
		 * Paints the Diagram using the encapsulated drawer
		 */
		public void paintComponent(Graphics g) {
			 super.paintComponent(g);
			 d.setSize(canvas.getSize());
	         d.draw(g);
	         setBackground(bg);
	    }
	}
	
	//Constants
	public static final int BAR_DIAGRAM = 0;
	private static JMenuItem[] items1 = {
		new JMenuItem("New",KeyEvent.VK_G),
		new JMenuItem("Save...",KeyEvent.VK_B),
		new JMenuItem("Load...",KeyEvent.VK_C),
		new JMenuItem("Save graphics...",KeyEvent.VK_D),
		new JMenuItem("Add raw",KeyEvent.VK_A),
		new JMenuItem("Close file",KeyEvent.VK_X)
	};
	private static JMenuItem[] items2 = {
		new JMenuItem("Undo",KeyEvent.VK_Z),
		new JMenuItem("Redo",KeyEvent.VK_Y)
	};
	private static final String[][] nullData = {
		{"Uno", "5"},
		{"Duo", "10"},
		{"Tres","15"}
	};
	
	//Fields
	private JMenuBar menuBar = new JMenuBar();
	private JMenu fileMenu = new JMenu("File...");
	private JMenu editMenu = new JMenu("Edit...");
	private Canvas canvas;
	private JFrame window = new JFrame("Testing Design...");
	private JTabbedPane tp;
	private JDataModel dm;
	private JToolBar tb;
	private static MainFrame object;
	
	/**
	 * Initializes the GUI construction
	 */
	private MainFrame(){
		createGUI();
	}
	
	/**
	 * Initiates the global access point to this class
	 * @return the singleton instance of this class
	 */
	public static MainFrame getInstance(){
		if(object==null){
			object = new MainFrame();
		}
		return object;
	}

	/**
	 * Parameterizes which drawer to use. In this version : only BarDiagramDrawer available
	 * @param i integer constant that indicates which strategy to use 
	 */
	public void setDrawer(int i){
		if(i == MainFrame.BAR_DIAGRAM){
			this.canvas.d = new BarDiagramDrawer(340,300);
		}
	}
	
	/**
	 * Sets new set of data as the table content. Paints the diagram
	 * @param data new set of Strings
	 */
	public void setData(String[][] data){
		this.canvas.d.setData(data);
		this.canvas.repaint();
	}
		
	/**
	 * Sets the UI look and feel to system defaults
	 */
	private void setLookAndFeel(){
		String s = UIManager.getSystemLookAndFeelClassName();
		try{
			UIManager.setLookAndFeel(s);
		}
		catch(Exception e){
			System.err.println("Unable to locate" + s);
		}
	}
	
	/**
	 * Initializes menu items & creates the menu bar itself
	 */
	private void menuInit(){
		for(int i=0;i<items1.length;i++){
			fileMenu.add(items1[i]);
		}
		items1[0].addActionListener(ActionsHandler.getCommand(ActionsHandler.NEW_COMMAND));
		items1[1].addActionListener(ActionsHandler.getCommand(ActionsHandler.SAVE_COMMAND));
		items1[2].addActionListener(ActionsHandler.getCommand(ActionsHandler.OPEN_COMMAND));
		items1[4].addActionListener(ActionsHandler.getCommand(ActionsHandler.ADD_COMMAND));
		items1[5].addActionListener(ActionsHandler.getCommand(ActionsHandler.CLOSE_COMMAND));
		items2[0].addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				ActionsHandler.unDo();
				
			}
			
		});
		items2[1].addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				ActionsHandler.reDo();
				
			}
			
		});
		editMenu.add(items2[0]);
		editMenu.add(items2[1]);
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
	}
	
	/**
	 * Creates and returns ImageIcon
	 * @param path to the icon
	 * @return ImageIcon, if everything went right
	 */
    private ImageIcon createImageIcon(String path) {
            return new ImageIcon(path);
    }
    
    
    /**
     * Initializes toolbar, adds the actions listeners to a toolbar buttons
     */
    private void initializeToolBar(){
    	tb = new JToolBar("Toolbar");
		tb.setFloatable(false);
		JButton btn = new JButton(createImageIcon("images/new.png"));
		btn.setToolTipText("New");
		btn.addActionListener(ActionsHandler.getCommand(ActionsHandler.NEW_COMMAND));
		tb.add(btn);
		btn = new JButton(createImageIcon("images/open.png"));
		btn.setToolTipText("Open");
		btn.addActionListener(ActionsHandler.getCommand(ActionsHandler.OPEN_COMMAND));
		tb.add(btn);
		btn = new JButton(createImageIcon("images/save.png"));
		btn.setToolTipText("Save");
		btn.addActionListener(ActionsHandler.getCommand(ActionsHandler.SAVE_COMMAND));
		tb.add(btn);
		btn = new JButton(createImageIcon("images/add.png"));
		btn.setToolTipText("Add row");
		btn.addActionListener(ActionsHandler.getCommand(ActionsHandler.ADD_COMMAND));
		tb.add(btn);
		btn = new JButton(createImageIcon("images/remove.png"));
		btn.setToolTipText("Remove row");
		btn.addActionListener(ActionsHandler.getCommand(ActionsHandler.REMOVE_COMMAND));
		tb.add(btn);
		btn = new JButton(createImageIcon("images/close.png"));
		btn.setToolTipText("Close");
		btn.addActionListener(ActionsHandler.getCommand(ActionsHandler.CLOSE_COMMAND));
		tb.add(btn);
		
    }
    
    /**
     * Initializes and packs window. Sets sizes and operations on close
     */
    private void initializeWindow(){
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setPreferredSize(new Dimension(583,420));
		window.setMinimumSize(new Dimension(430,300));
		window.pack();
		window.setVisible(true);
	}
    
    
	/**
	 * Creates GUI. Creates all the components and adds it to the frame.
	 */
	private void createGUI() {
		//Constant
		this.setLookAndFeel();
		this.menuInit();
		
		//Canvas initialization
		canvas = new Canvas(new BorderLayout());
		canvas.setSize(new Dimension(340,300));

		//Table initialization and data-parameterization
		this.setDrawer(BAR_DIAGRAM);
		this.setData(nullData);
		
		//TabbedPane initialization
		tp = new JTabbedPane();
		tp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		
		dm = new JDataModel();
		addNewTab("Untitled"); 
		
		tp.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent arg0) {
				if(arg0.getSource().getClass() == JTabbedPane.class){
					String tabName = tp.getTitleAt(tp.getSelectedIndex());
					MainFrame.this.setData(MainFrame.this.dm.getElementByName(tabName).getDataTable());
				}
			}
		});
		
		//Toolbar initialization
		initializeToolBar();
		JPanel pan = new JPanel(new BorderLayout());
		pan.add(tb);
		
		
		//Window initialization
		window.setJMenuBar(menuBar);
		
		//Layout Manager initialization. Setting constraints for each element;
		window.getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints cons = new GridBagConstraints();
		cons.gridx = 0;
		cons.gridy = 1;
		cons.gridheight = 1;
		cons.gridwidth = 1;
		cons.fill = GridBagConstraints.VERTICAL;
		cons.weightx = 0;
		cons.weighty = 0;
		cons.anchor = GridBagConstraints.EAST;
		window.getContentPane().add(tp,cons);
		cons.fill = GridBagConstraints.BOTH;
		cons.gridx=1;
		cons.weightx = 100;
		cons.weighty = 100;
		cons.anchor = GridBagConstraints.WEST;
		window.getContentPane().add(canvas,cons);
		cons.gridx = 0;
		cons.gridy = 0;
		cons.weightx = 100;
		cons.weighty = 0;
		cons.gridheight = 1;
		cons.gridwidth = 2;
		cons.fill = GridBagConstraints.HORIZONTAL;
		window.getContentPane().add(pan,cons);
		this.initializeWindow();
	}
	
	/**
	 * Adds Tab and puts the data from the specified file in it
	 * @param fileName name of file to work with
	 */
	public void addTab(String fileName){
		dm.addElement(fileName);
		String tabName = fileName.substring(fileName.lastIndexOf('/')+1, fileName.length());
		JTable dataGrid = dm.getElementByName(tabName).getGrid();		
		dataGrid.setFillsViewportHeight(true);
		dataGrid.getModel().addTableModelListener(new TableHandler());
		JScrollPane sp = new JScrollPane(dataGrid);
		sp.setPreferredSize(new Dimension(170,100));
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(sp);
		tp.addTab(tabName, p);
	}
	
	/**
	 * Removes Tab which contains data from the specified file
	 * @param fileName name of file, which should disappear
	 */
	public void removeTab(String fileName){
		String tabName = fileName.substring(fileName.lastIndexOf('/')+1, fileName.length());
		int index = tp.indexOfTab(tabName);
		if (index != -1){
			tp.remove(index);
		}
		dm.removeElement(tabName);
	}
	
	/**
	 * Removes the tab that is selected
	 * @return fileName of DataElement of the removed Tab
	 */
	public String removeCurrentTab(){
		String fileName = null;
		if (tp.getTabCount()>1){
			String tabName = tp.getTitleAt(tp.getSelectedIndex());
			tp.removeTabAt(tp.getSelectedIndex());
			fileName = dm.getElementByName(tabName).getFileName();
			dm.removeElement(tabName);
		}
		return fileName;
	}
	
	/**
	 * Creates new temporary file and adds a new Tab with it
	 * @param tabName name of tab
	 */
	public void addNewTab(String tabName){
		dm.createElement(tabName, nullData);
		JTable dataGrid = dm.getElementByName(tabName).getGrid();
		dataGrid.setFillsViewportHeight(true);
		dataGrid.getModel().addTableModelListener(new TableHandler());
		JScrollPane sp = new JScrollPane(dataGrid);
		sp.setPreferredSize(new Dimension(170,100));
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(sp);
		tp.addTab(tabName, p);
	}
	
	/**
	 * Returns the String data of the current tab
	 * @return
	 */
	public String[][] getCurrentTabData(){
		return dm.getElementByName(tp.getTitleAt(tp.getSelectedIndex())).getDataTable();
	}
	
	/**
	 * Renames tab(actually deletes the old one and creates new)
	 * @param fileName name of file from which the data should be uploaded
	 */
	public void setCurrentTabName(String fileName){
		this.addTab(fileName);
		this.removeCurrentTab();
	}

	/**
	 * Notifies the model to add new row to the specified element
	 * @param tabName element
	 * @param rowData data to be added
	 * @return number of row
	 */
	public int addRowToTab(String tabName,String[] rowData){
		tp.setSelectedIndex(tp.indexOfTab(tabName));
		JDataElement de = dm.getElementByName(tabName);
		return de.addRow(rowData);
	}
	
	/**
	 * Notifies model to remove the row from the element
	 * @param tabName specifies element
	 * @param index number of the row to be removed
	 */
	public void removeRowFromTab(String tabName,int index){
		tp.setSelectedIndex(tp.indexOfTab(tabName));
		JDataElement de = dm.getElementByName(tabName);
		de.removeRow(index);
	}

	/**
	 * Notifies model to delete selected row
	 * @return data from that row
	 */
	public String[] removeSelectedRow() {
		JDataElement de = dm.getElementByName(tp.getTitleAt(tp.getSelectedIndex()));
		return de.removeSelectedRow();
	}

	/**
	 * Asks TabbedPane to give the name of the selected Tab
	 * @return current tab title
	 */
	public String getCurrentTabName() {
		return tp.getTitleAt(tp.getSelectedIndex());
	}

	/**
	 * Notifies model to update Table Model. 
	 * @param tabName in which element
	 * @param i cell row
	 * @param j cell column
	 * @param whereTo new value
	 */
	public void updateElement(String tabName, int i, int j, String whereTo) {
		this.dm.updateElementByName(tabName, i, j, whereTo);
	}

}

