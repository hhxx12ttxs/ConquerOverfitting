package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


//SET GUN LISTENER FOR TABLE

public class Window implements WindowListener, ClipboardOwner, ActionListener, ChangeListener, ListSelectionListener
{
	private final Color Background = Color.LIGHT_GRAY;
	private final Color Foreground = new Color(120,20,20);
	
	private java.awt.Container con;
	private gen.Hero output;
	
	private boolean medals;
	
	private JButton saveButtonMedals;
	
	private String oldTalent;
	private String oldArmor;
	private String oldRankCap;
	private String CodeTemporary;
	
	private JFrame windowFrame;
	private JFrame optionsFrame;
	private JFrame codeManagerFrame;
	
	private JLabel xpLabel;
	private JLabel xpLabelDefault;
	private JLabel remLabel;
	private JLabel remLabel2;
	
	private JSlider xpSlider;
	private JSlider xpSliderDefault;
	
	private JCheckBox cobCheckBox;
	private JCheckBox pccCheckBox;
	private JCheckBox lsaCheckBox;
	private JCheckBox mohCheckBox;
	private JCheckBox keyCheckBox;
	private JCheckBox cobCheckBoxDefault;
	private JCheckBox pccCheckBoxDefault;
	private JCheckBox lsaCheckBoxDefault;
	private JCheckBox mohCheckBoxDefault;
	private JCheckBox keyCheckBoxDefault;
	
	private JComboBox classComboBox;
	private JComboBox gunComboBox;
	private JComboBox armorComboBox;
	private JComboBox traitComboBox;
	private JComboBox specComboBox;
	private JComboBox talentComboBox;
	private JComboBox rankComboBox;
	private JComboBox rankcapComboBox;
	private JComboBox rankComboBoxDefault;
	private JComboBox rankcapComboBoxDefault;
	private JComboBox remComboBox;
	private JComboBox remComboBoxDefault;
	
	private JTextField nameTextField;
	private JTextField nameTextFieldDefault;	
	private JTextField codeTextField;
	
	//private JMenu windowMenu;
	private JMenu toolsMenu;
	private JMenuBar menuBar;
	private JMenuItem menuItem;
	private JMenuItem codeManagerMenuItem;
	
	private JRadioButton medalsOnRadioButton;
	private JRadioButton medalsOffRadioButton;
	private ButtonGroup medalToggleButtonGroup;
		
	//private Panel tablePanel;
	private JPanel menuPanel;
	private JPanel medalPanel;
	//JPanel rankPanel;
	private JScrollPane selectedScrollPane;
	
	private Vector<String> classVector;
	private Vector<String> traitVector;
	private Vector<String> specVector;
	private Vector<String> rankVector;
	private Vector<String> optionsVector;
	
	public static void main (String[] args)
	{
		new Window();
		
	}
	
	public Window()
	{	
		JSplitPane optionsSplitPane;
		JScrollPane optionsScrollPane;
		
		JLabel capLabel;		
		JLabel rankLabel;
		JLabel experienceLabel;
		JLabel defaultSettingsLabel;
		JLabel defaultMedalsLabel;
		JLabel rankSeperatorLabel;
		JLabel rankSeperatorLabel2;
		JLabel nameLabel;
		
		JButton genButton;
		JButton copyButton;
		JButton saveButtonDefault;
		
		JList list;
		LayoutManager layout;
		
		
		Vector<Vector<String>> vectors = CreateVectors();
		
		classVector = vectors.get(0);
		traitVector = vectors.get(1);
		specVector = vectors.get(2);
		rankVector = vectors.get(3);
		optionsVector = vectors.get(4);
		
		//RANK CAP OPTION
		capLabel = new JLabel("Rank/Cap:");
		capLabel.setFont(new Font("Arial BOLD", Font.BOLD, 10));
		capLabel.setForeground(Foreground);
		
		nameLabel = new JLabel("Default Name:");
		nameLabel.setFont(new Font("Arial BOLD", Font.BOLD, 10));
		nameLabel.setForeground(Foreground);
		nameLabel.setSize(20,40);
	    
		//SET LAYOUT
		layout = new FlowLayout();
		((FlowLayout)layout).setHgap(10);
		((FlowLayout)layout).setVgap(10);
		
		//MENU BAR
		menuBar = new JMenuBar();
		toolsMenu = new JMenu("Tools");
		toolsMenu.setMnemonic('T');
		toolsMenu.setForeground(Foreground);
		menuBar.add(toolsMenu);	
		menuItem = new JMenuItem("Defaults");
		menuItem.setMnemonic('D');
		menuItem.addActionListener(this);
		menuItem.setForeground(Foreground);
		menuItem.setActionCommand("Defaults");
		toolsMenu.add(menuItem);
		codeManagerMenuItem = new JCheckBoxMenuItem("Code Manager");
		codeManagerMenuItem.setMnemonic('C');
		codeManagerMenuItem.addActionListener(this);
		codeManagerMenuItem.setForeground(Foreground);
		codeManagerMenuItem.setActionCommand("codeManager");
		
		
		//MEDALS WINDOW
		//*************
		remComboBoxDefault = new JComboBox();
		remComboBoxDefault.addItem("0");
		remComboBoxDefault.addItem("I");
		remComboBoxDefault.addItem("II");
		remComboBoxDefault.addItem("III");
		remComboBoxDefault.setMaximumRowCount(300);
		remComboBoxDefault.setPreferredSize(new Dimension(20,11));
		remComboBoxDefault.setForeground(Foreground);
		remComboBoxDefault.setActionCommand("remDefault");
		remComboBoxDefault.addActionListener(this);
		remComboBox = new JComboBox();
		remComboBox.addItem("0");
		remComboBox.addItem("I");
		remComboBox.addItem("II");
		remComboBox.addItem("III");
		remComboBox.setMaximumRowCount(300);
		remComboBox.setPreferredSize(new Dimension(20,11));
		remComboBox.setForeground(Foreground);
		remComboBox.setActionCommand("rem");
		remComboBox.addActionListener(this);
		remLabel = new JLabel("REM: "+remComboBoxDefault.getSelectedItem());
		remLabel.setFont(new Font("Arial BOLD", Font.BOLD, 10));
		remLabel.setForeground(Foreground);
		remLabel2 = new JLabel("REM: "+remComboBox.getSelectedItem());
		remLabel2.setFont(new Font("Arial BOLD", Font.BOLD, 10));
		remLabel2.setForeground(Foreground);
		//In initialization code:
	    cobCheckBox = new JCheckBox("COB"); 
	    cobCheckBox.setFont(new Font("Arial BOLD", Font.BOLD, 10));
	    cobCheckBox.setSelected(true);
	    cobCheckBox.setBackground(Background);
	    cobCheckBox.setForeground(Foreground);
	    pccCheckBox = new JCheckBox("PCC"); 
	    pccCheckBox.setFont(new Font("Arial BOLD", Font.BOLD, 10));
	    pccCheckBox.setSelected(true);
	    pccCheckBox.setBackground(Background);
	    pccCheckBox.setForeground(Foreground);
	    lsaCheckBox = new JCheckBox("LSA"); 
	    lsaCheckBox.setFont(new Font("Arial BOLD", Font.BOLD, 10));
	    lsaCheckBox.setSelected(true);
	    lsaCheckBox.setBackground(Background);
	    lsaCheckBox.setForeground(Foreground);
	    mohCheckBox = new JCheckBox("MOH"); 
	    mohCheckBox.setFont(new Font("Arial BOLD", Font.BOLD, 10));
	    mohCheckBox.setSelected(true);
	    mohCheckBox.setBackground(Background);
	    mohCheckBox.setForeground(Foreground);
	    keyCheckBox = new JCheckBox("KEY");
	    keyCheckBox.setFont(new Font("Arial BOLD", Font.BOLD, 10));
	    keyCheckBox.setSelected(true);
	    keyCheckBox.setBackground(Background);
	    keyCheckBox.setForeground(Foreground);
	    
	    //MAIN WINDOW
	    //***********
		nameTextField = new JTextField(9);
		nameTextField.setForeground(Foreground);
		nameTextField.setText("Enter Name");
		classComboBox = new JComboBox(classVector);
		classComboBox.setActionCommand("Class");
		classComboBox.setMaximumRowCount(300);
		classComboBox.setPreferredSize(new Dimension(114,18));
		classComboBox.addActionListener(this);
		classComboBox.setForeground(Foreground);
		gunComboBox = new JComboBox();
		gunComboBox.addItem("-Weapon-");
		gunComboBox.setMaximumRowCount(300);
		gunComboBox.setPreferredSize(new Dimension(114,18));
		gunComboBox.addActionListener(this);
		gunComboBox.setForeground(Foreground);
		armorComboBox = new JComboBox();
		armorComboBox.setMaximumRowCount(300);
		armorComboBox.setPreferredSize(new Dimension(80,18));
		armorComboBox.addItem("-Armor-");
		armorComboBox.setForeground(Foreground);
		traitComboBox = new JComboBox(traitVector);
		traitComboBox.setMaximumRowCount(300);
		traitComboBox.setPreferredSize(new Dimension(80,18));
		traitComboBox.setForeground(Foreground);
		specComboBox = new JComboBox(specVector);
		specComboBox.setMaximumRowCount(300);
		specComboBox.setPreferredSize(new Dimension(80,18));
		specComboBox.setForeground(Foreground);
		talentComboBox = new JComboBox();
		talentComboBox.addItem("-Talent-");
		talentComboBox.setMaximumRowCount(300);
		talentComboBox.setPreferredSize(new Dimension(80,18));
		talentComboBox.setForeground(Foreground);
		talentComboBox.addActionListener(this);
		rankComboBox = new JComboBox(rankVector);
		rankComboBox.setMaximumRowCount(300);
		rankComboBox.setPreferredSize(new Dimension(40,18));
		rankComboBox.setForeground(Foreground);
		rankComboBox.setActionCommand("Rank");
		rankComboBox.addActionListener(this);
		rankSeperatorLabel = new JLabel("/");
		rankSeperatorLabel.setForeground(Foreground);
		rankSeperatorLabel2 = new JLabel("/");
		rankSeperatorLabel2.setForeground(Foreground);
		rankcapComboBox = new JComboBox();
		rankcapComboBox.addItem("12");
		rankcapComboBox.setMaximumRowCount(300);
		rankcapComboBox.setPreferredSize(new Dimension(40,18));
		rankcapComboBox.setForeground(Foreground);
		rankcapComboBox.setActionCommand("RankCap");
		rankcapComboBox.addActionListener(this);
		rankcapComboBox.setEnabled(false);
		xpSlider = new JSlider(0, 0, 2495, 2495);
		xpSlider.setBackground(Background);
		xpSlider.setForeground(Foreground);
		xpSlider.addChangeListener(this);
		xpSlider.setPreferredSize(new Dimension(130,18));
		xpSlider.setEnabled(false);
		xpLabel = new JLabel();
		xpLabel.setText(""+xpSlider.getValue()/5/5+"%");
		xpLabel.setPreferredSize(new Dimension(25,18));
		genButton = new JButton("Gen!");
		genButton.setActionCommand("Gen");
		genButton.addActionListener(this);
		genButton.setPreferredSize(new Dimension(69,18));
		genButton.setForeground(Foreground);
		copyButton = new JButton("Copy");
		copyButton.setActionCommand("Copy");
		copyButton.addActionListener(this);
		copyButton.setPreferredSize(new Dimension(69,18));
		copyButton.setForeground(Foreground);
		//Code Field (where the code is displayed)
		codeTextField = new JTextField(17);
		codeTextField.setForeground(Foreground);
		
		//DEFAULTS OPTIONS PANE
		//*********************
		defaultSettingsLabel = new JLabel("Basic Defaults");
		defaultSettingsLabel.setForeground(Foreground);
		defaultSettingsLabel.setFont(new Font("Arial BOLD", Font.BOLD, 16));
		nameTextFieldDefault = new JTextField(8);
		nameTextFieldDefault.setForeground(Foreground);
		nameTextFieldDefault.setText("Default Name");
		rankLabel = new JLabel("Rank/cap:");
		rankLabel.setForeground(Foreground);
		rankLabel.setFont(new Font("Arial BOLD", Font.BOLD, 10));
		experienceLabel = new JLabel("Exp:");
		experienceLabel.setForeground(Foreground);
		experienceLabel.setFont(new Font("Arial BOLD", Font.BOLD, 10));
		rankComboBoxDefault = new JComboBox(rankVector);
		rankComboBoxDefault.setMaximumRowCount(300);
		rankComboBoxDefault.setPreferredSize(new Dimension(40,18));
		rankComboBoxDefault.setForeground(Foreground);
		rankComboBoxDefault.setActionCommand("DefaultRank");
		rankComboBoxDefault.addActionListener(this);
		remComboBoxDefault.setSelectedIndex(3);
		rankcapComboBoxDefault = new JComboBox();
		rankcapComboBoxDefault.addItem("12");
		rankcapComboBoxDefault.addItem("11");
		rankcapComboBoxDefault.addItem("10");
		rankcapComboBoxDefault.addItem("9");
		rankcapComboBoxDefault.addItem("6");
		rankcapComboBoxDefault.addItem("3");
		rankcapComboBoxDefault.setMaximumRowCount(300);
		rankcapComboBoxDefault.setPreferredSize(new Dimension(40,18));
		rankcapComboBoxDefault.setForeground(Foreground);
		rankcapComboBoxDefault.setActionCommand("DefaultRank");
		rankcapComboBoxDefault.addActionListener(this);
		//default xp bar
		xpSliderDefault = new JSlider(0, 0, 2495, 2495);
		xpSliderDefault.setForeground(Foreground);
		xpSliderDefault.addChangeListener(this);
		xpSliderDefault.setPreferredSize(new Dimension(68,18));	
		xpSliderDefault.setEnabled(false);
		//default xp label
		xpLabelDefault = new JLabel();
		xpLabelDefault.setText(""+xpSliderDefault.getValue()/5/5+"%");
		xpLabelDefault.setPreferredSize(new Dimension(25,18));
		//medal activation radio buttons
		medalsOnRadioButton = new JRadioButton("Medals On");
		medalsOnRadioButton.setFont(new Font("Arial BOLD", Font.BOLD, 10));
	    medalsOnRadioButton.setForeground(Foreground);
	    medalsOnRadioButton.setSelected(true);
	    medalsOffRadioButton = new JRadioButton("Medals Off");
	    medalsOffRadioButton.setFont(new Font("Arial BOLD", Font.BOLD, 10));
	    medalsOffRadioButton.setForeground(Foreground);
	    medalsOffRadioButton.setSelected(true);
		medalToggleButtonGroup = new ButtonGroup();
	    medalToggleButtonGroup.add(medalsOnRadioButton);
	    medalToggleButtonGroup.add(medalsOffRadioButton);
		saveButtonDefault = new JButton("Save");
		saveButtonDefault.setActionCommand("DefaultSave");
		saveButtonDefault.addActionListener(this);
		saveButtonDefault.setPreferredSize(new Dimension(69,18));
		saveButtonDefault.setForeground(Foreground);
		
		
		//MEDALS OPTIONS PANE
		//*******************
		//label
		defaultMedalsLabel = new JLabel("  Medal Defaults  " );
		defaultMedalsLabel.setFont(new Font("Arial BOLD", Font.BOLD, 16));
		defaultMedalsLabel.setForeground(Foreground);
		//medals
	    cobCheckBoxDefault = new JCheckBox("COB"); 
	    cobCheckBoxDefault.setFont(new Font("Arial BOLD", Font.BOLD, 10));
	    cobCheckBoxDefault.setSelected(true);
	    cobCheckBoxDefault.setForeground(Foreground);
	    pccCheckBoxDefault = new JCheckBox("PCC"); 
	    pccCheckBoxDefault.setFont(new Font("Arial BOLD", Font.BOLD, 10));
	    pccCheckBoxDefault.setSelected(true);
	    pccCheckBoxDefault.setForeground(Foreground);
	    lsaCheckBoxDefault = new JCheckBox("LSA"); 
	    lsaCheckBoxDefault.setFont(new Font("Arial BOLD", Font.BOLD, 10));
	    lsaCheckBoxDefault.setSelected(true);
	    lsaCheckBoxDefault.setForeground(Foreground);
	    mohCheckBoxDefault = new JCheckBox("MOH"); 
	    mohCheckBoxDefault.setFont(new Font("Arial BOLD", Font.BOLD, 10));
	    mohCheckBoxDefault.setSelected(true);
	    mohCheckBoxDefault.setForeground(Foreground);
	    keyCheckBoxDefault = new JCheckBox("KEY");
	    keyCheckBoxDefault.setFont(new Font("Arial BOLD", Font.BOLD, 10));
	    keyCheckBoxDefault.setSelected(true);
	    keyCheckBoxDefault.setForeground(Foreground);
	    //Save Button
		saveButtonMedals = new JButton("Save");
		saveButtonMedals.setActionCommand("DefaultSave");
		saveButtonMedals.addActionListener(this);
		saveButtonMedals.setPreferredSize(new Dimension(69,18));
		saveButtonMedals.setForeground(Foreground);
		
		
		//WINDOWS
		//*******
		//Main Code Gen window
		windowFrame = new JFrame("Amoeba's Gen");
		windowFrame.setSize(400, 230);
		windowFrame.setResizable(false);
		windowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		windowFrame.setLayout(layout);
		windowFrame.add(nameTextField);
		windowFrame.add(classComboBox);
		windowFrame.add(gunComboBox);
		windowFrame.add(armorComboBox);
	    windowFrame.add(traitComboBox);	
		windowFrame.add(specComboBox);
		windowFrame.add(talentComboBox);
		windowFrame.add(xpSlider);
		windowFrame.add(xpLabel);
		windowFrame.add(capLabel);
		windowFrame.add(rankComboBox);
		windowFrame.add(rankSeperatorLabel);
		windowFrame.add(rankcapComboBox);
		windowFrame.add(genButton);
		windowFrame.add(copyButton);
		windowFrame.add(codeTextField);
		windowFrame.add(keyCheckBox);
		windowFrame.add(mohCheckBox);
		windowFrame.add(pccCheckBox);
		windowFrame.add(cobCheckBox);
		windowFrame.add(lsaCheckBox);
		windowFrame.add(remComboBox);
		windowFrame.add(remLabel2);
		windowFrame.setJMenuBar(menuBar);
		
		//Code Manager Window
		codeManagerFrame = new JFrame("Code Manager");
		codeManagerFrame.setSize(500, 210);
		codeManagerFrame.setResizable(true);
		codeManagerFrame.setLayout(layout);
		codeManagerFrame.addWindowListener(this);
		
		//Default Settings Options Pane
		menuPanel = new JPanel(layout);
		menuPanel.add(defaultSettingsLabel);
		menuPanel.add(nameLabel);
		menuPanel.add(nameTextFieldDefault);
		menuPanel.add(rankLabel);
		menuPanel.add(rankComboBoxDefault);
		menuPanel.add(rankSeperatorLabel2);
		menuPanel.add(rankcapComboBoxDefault);
		menuPanel.add(experienceLabel);
		menuPanel.add(xpSliderDefault);
		menuPanel.add(xpLabelDefault);
		menuPanel.add(saveButtonDefault);
		menuPanel.setPreferredSize(new Dimension(100, 150));
		
		//Default Medals Options Pane
		medalPanel = new JPanel(layout);
		medalPanel.add(defaultMedalsLabel);
		medalPanel.add(keyCheckBoxDefault);
		medalPanel.add(mohCheckBoxDefault);
		medalPanel.add(pccCheckBoxDefault);
		medalPanel.add(cobCheckBoxDefault);
		medalPanel.add(lsaCheckBoxDefault);
		medalPanel.add(remComboBoxDefault);
		medalPanel.add(remLabel);
		medalPanel.add(medalsOnRadioButton);
		medalPanel.add(medalsOffRadioButton);
		medalPanel.add(saveButtonMedals);
		medalPanel.setPreferredSize(new Dimension(100, 150));
		
		//JList for optionsPane (part of the options window pane)
		list = new JList(optionsVector);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        list.addListSelectionListener(this);
        list.setForeground(Foreground);
		
        //Options Window Panes
        selectedScrollPane = new JScrollPane();
		selectedScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		optionsScrollPane = new JScrollPane(list);
		optionsSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,optionsScrollPane,selectedScrollPane);
		optionsSplitPane.setDividerLocation(85);
		optionsSplitPane.setOneTouchExpandable(false);
		optionsSplitPane.setEnabled(false);
		optionsSplitPane.setPreferredSize(new Dimension(290, 170));
		
		//Options Window
		optionsFrame = new JFrame("Defaults");
		optionsFrame.setSize(300,208);
		optionsFrame.setResizable(false);
		optionsFrame.setLayout(layout);
		optionsFrame.add(optionsSplitPane);
		
		//set background color for all windows
		con = windowFrame.getContentPane();
		con.setBackground(Background);
		con = optionsFrame.getContentPane();
		con.setBackground(Background);
		con.setBackground(Background);
		con = codeManagerFrame.getContentPane();
		con.setBackground(Background);
		
		gunComboBox.setEnabled(false);
		armorComboBox.setEnabled(false);
		talentComboBox.setEnabled(false);
		traitComboBox.setEnabled(false);
		specComboBox.setEnabled(false);
		talentComboBox.setEnabled(false);
		
		//Set a Default selectedPane
        String item = ""+list.getSelectedValue();
		if (item.compareTo("Basic")==0)
			selectedScrollPane.setViewportView(menuPanel);
		//if (item.compareTo("Rank Options")==0)
			//selectedPane.setViewportView(rankPanel);
		if (item.compareTo("Medals")==0)
			selectedScrollPane.setViewportView(medalPanel);
		
		remComboBox.setSelectedIndex(3);
		
		//LoadTables();
		ReadDefaults();
		
		windowFrame.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {		
		String Action = e.getActionCommand();
		String Name = (String) nameTextField.getText();
		String Class = (String) classComboBox.getSelectedItem();
		String Talent = (String) talentComboBox.getSelectedItem();
		String Armor = (String) armorComboBox.getSelectedItem();
		String Rank = (String) rankComboBox.getSelectedItem();
		String Weapon = (String) gunComboBox.getSelectedItem();
		String RankCap = (String) rankcapComboBox.getSelectedItem();
		String DefaultRank = (String) rankComboBoxDefault.getSelectedItem();
		boolean MOH = mohCheckBox.isSelected();
		boolean KEY = keyCheckBox.isSelected();
		boolean LSA = lsaCheckBox.isSelected();
		boolean COB = cobCheckBox.isSelected();
		boolean PCC = pccCheckBox.isSelected();
		//SetREM
		int REM = remComboBox.getSelectedIndex();
		
		if(Action.compareTo("rem")==0)	
		{
			remLabel2.setText("REM: "+remComboBox.getSelectedItem());
		}
		if(Action.compareTo("remDefault")==0)	
		{
			remLabel.setText("REM: "+remComboBoxDefault.getSelectedItem());
		}
		
		if(Action.compareTo("Add Row")==0)
		{
			
		}
		if(Action.compareTo("Remove Row")==0)
		{
			
		}
		if(Action.compareTo("Save Data")==0)
		{
			
		}
	
		
		
		if(Action.compareTo("DefaultSave")==0)
		{
			CreateDefaults();
			ReadDefaults();
		}
		
		
		
		if(Action.compareTo("DefaultSave")==0)
		{
			CreateDefaults();
			ReadDefaults();
		}
					
		//OPTIONS
		if(Action.compareTo("Defaults")==0)
		{
			optionsFrame.setLocation(windowFrame.getLocation());
			optionsFrame.setVisible(true);
		}
		
		//RANKS
		if(Action.compareTo("DefaultRank")==0)
		{			
			updateDefaultXpSlider(DefaultRank);
			updateRankCap((String)rankComboBoxDefault.getSelectedItem(), (String)rankcapComboBoxDefault.getSelectedItem(), rankcapComboBoxDefault);
		}
		
		if(Action.compareTo("RankCap")==0)
		{
			updateMedals();
		}
		
		if(Action.compareTo("Rank")==0)
		{	
			updateXpSlider(Rank);
			updateRankCap(Rank, RankCap, rankcapComboBox);
			updateMedals();
		}
		
		
		
		
		
		//CLASS
		if(Action.compareTo("Class")==0)
		{			
			if (Class.compareTo("-Class-")!=0)
			{
				armorComboBox.setEnabled(true);
				talentComboBox.setEnabled(true);
				traitComboBox.setEnabled(true);
				specComboBox.setEnabled(true);
				gunComboBox.setEnabled(true);
			}
			updateGunArmorAndTalent(Class,Armor,Talent);
			if (Class.compareTo("-Class-")==0)
			{
				armorComboBox.setEnabled(false);
				talentComboBox.setEnabled(false);
				traitComboBox.setEnabled(false);
				specComboBox.setEnabled(false);
				gunComboBox.setEnabled(false);
			}
			
		}
		if (Action.compareTo("Copy")==0)
		{
			if(CodeTemporary!=null)
				setClipboardContents(CodeTemporary);
		}
		
		
		//GEN
		if (Action.compareTo("Gen")==0)
		{
			output = new gen.Hero();	
			output.setKEY(KEY);
			output.setMOH(MOH);
			output.setPCC(PCC);
			output.setCOB(COB);
			output.setLSA(LSA);
			output.setREM(REM);
			output.setPlayer(Name);
			output.setArmor(Armor);
			String Trait = (String) traitComboBox.getSelectedItem();
			output.setTrait(Trait);
			String Spec = (String) specComboBox.getSelectedItem();
			output.setSpec(Spec);
			output.setRank(Rank);
			output.setXp(xpSlider.getValue());
			output.setGun(Weapon);
			output.setType(Class);
			output.setTalent(Talent);
			output.setRankcap(RankCap);
			if (Name.length()<3)
			{
				JOptionPane.showMessageDialog(null, "Name must be at least three characters long.", "ERROR", JOptionPane.ERROR_MESSAGE);
				codeTextField.setText("Invalid code");
			}
			else if (Name.compareTo("")==0 || Name.compareTo("Enter Name")==0)
			{
				codeTextField.setText("Enter Name");
			}
			else
			{
				CodeTemporary = output.getCode();
				codeTextField.setText(CodeTemporary);
				
			}
			
				
			
		}
	}

	public Vector<Vector<String>> CreateVectors()
	{
		Vector<String> Classes = new Vector<String>();
		Classes.add("-Class-");
		Classes.add("Sniper");
		Classes.add("Medic");
		Classes.add("Tactician");
		Classes.add("Psychologist");
		Classes.add("Maverick");
		Classes.add("Heavy Ordinance");
		Classes.add("Demolitions");
		Classes.add("Cyborg");
		Classes.add("Pyrotechnician");
		Classes.add("Watchman");
		Classes.add("Tech Ops");
		Classes.add("Umbrella Clone");
		
		Vector<String> Traits = new Vector<String>();
		Traits.add("-Trait-");
		Traits.add("Skilled");
		Traits.add("Gifted");
		Traits.add("Survivalist");
		Traits.add("Dragoon");
		Traits.add("Acrobat");
		Traits.add("Swift Learner");
		Traits.add("Healer");
		Traits.add("Flower Child");
		Traits.add("Chem Reliant");
		Traits.add("Rad Resistant");
		Traits.add("Gadgeteer");
		Traits.add("Prowler");
		Traits.add("Energizer");
		Traits.add("Pack Rat");
		Traits.add("Engineer");
		Traits.add("Reckless");
		
		Vector<String> Specs = new Vector<String>();
		Specs.add("-Spec-");
		Specs.add("Weaponry");
		Specs.add("Power Armor");
		Specs.add("Energy Cells");
		Specs.add("Cybernetics");
		Specs.add("Triage");
		Specs.add("Chemistry");
		Specs.add("Leadership");
		Specs.add("Robotics");
		Specs.add("Espionage");
		
		Vector<String> Ranks = new Vector<String>();
		Ranks.add("12");
		Ranks.add("11");
		Ranks.add("10");
		Ranks.add("9");
		Ranks.add("8");
		Ranks.add("7");
		Ranks.add("6");
		Ranks.add("5");
		Ranks.add("4");
		Ranks.add("3");
		Ranks.add("2");
		Ranks.add("1");
		
		Vector<String> OptionsList = new Vector<String>();
		OptionsList.add("Basic");
		OptionsList.add("Medals");
		
		Vector<Vector<String>> Vectors = new Vector<Vector<String>>();
		Vectors.add(Classes);
		Vectors.add(Traits);
		Vectors.add(Specs);
		Vectors.add(Ranks);
		Vectors.add(OptionsList);
				
		return Vectors;
	}
	
	public void CreateDefaults()
	{
		
		try 
		{
			PrintStream fout = new PrintStream(new File("defaults.txt"));
			
			
			String DefaultNameInput = nameTextFieldDefault.getText();
			fout.println(DefaultNameInput);
			
			String DefaultRankInput = (String) rankComboBoxDefault.getSelectedItem();
			fout.println(DefaultRankInput);
			
			int DefaultExperienceInput = xpSliderDefault.getValue();
			fout.println(DefaultExperienceInput);
			
			if (medalsOnRadioButton.isSelected() == true)
			{
			    fout.println(true);
			    boolean DefaultKEY = keyCheckBoxDefault.isSelected();
				fout.println(DefaultKEY);
				boolean DefaultMOH = mohCheckBoxDefault.isSelected();
				fout.println(DefaultMOH);
				boolean DefaultPCC = pccCheckBoxDefault.isSelected();
				fout.println(DefaultPCC);
				boolean DefaultCOB = cobCheckBoxDefault.isSelected();
				fout.println(DefaultCOB);
				boolean DefaultLSA = lsaCheckBoxDefault.isSelected();
				fout.println(DefaultLSA);
				int DefaultREM = remComboBoxDefault.getSelectedIndex();
				if (DefaultREM > 0)
					fout.println(true);
				else 
					fout.println(false);
				
				if (remComboBoxDefault.getSelectedIndex()==1)
					fout.println("I");
				else if (remComboBoxDefault.getSelectedIndex()==2)	
					fout.println("II");
				else if (remComboBoxDefault.getSelectedIndex()==3)
					fout.println("III");
			}	
			if (medalsOffRadioButton.isSelected() == true)
			{
				fout.println(false);
				fout.println(false);
				fout.println(false);
				fout.println(false);
				fout.println(false);
				fout.println(false);
				fout.println(false);
				fout.println("OFF");
			}
			fout.println((String) rankcapComboBoxDefault.getSelectedItem());
			fout.println((int)windowFrame.getX());
			fout.println((int)windowFrame.getY());
			fout.close();	
		} catch (FileNotFoundException e) {
			
		}
		
	}
	
	public void ReadDefaults()
	{
		
		try 
		{
			Scanner fin = new Scanner(new File("defaults.txt"));
			
			//read in NAME
			String DefaultNameInput = fin.nextLine();
			nameTextField.setText(DefaultNameInput);
			nameTextFieldDefault.setText(DefaultNameInput);
			
			//read in RANK
			String DefaultRankInput = fin.nextLine();
			rankComboBox.setSelectedItem(DefaultRankInput);
			rankComboBoxDefault.setSelectedItem(DefaultRankInput);
			
			//read in EXP
			int DefaultExperienceInput = fin.nextInt();
			xpSlider.setValue(DefaultExperienceInput);
			xpSliderDefault.setValue(DefaultExperienceInput);
			
			
			
			//read in MEDAL STATUS
			boolean DefaultMedals = fin.nextBoolean();
			
			//if disabled
			if (!DefaultMedals)
			{
				medals = false;
				//saveButtonMedals.setEnabled(false);
				medalsOffRadioButton.setSelected(true);
				keyCheckBox.setSelected(false);
				keyCheckBoxDefault.setSelected(false);
				mohCheckBox.setSelected(false);
				mohCheckBoxDefault.setSelected(false);
				pccCheckBox.setSelected(false);
				pccCheckBoxDefault.setSelected(false);
				cobCheckBox.setSelected(false);
				cobCheckBoxDefault.setSelected(false);
				lsaCheckBoxDefault.setSelected(false);
				lsaCheckBox.setSelected(false);
				remComboBox.setSelectedIndex(0);
				remComboBoxDefault.setSelectedIndex(0);
				
				keyCheckBox.setEnabled(false);
				keyCheckBoxDefault.setEnabled(false);
				mohCheckBox.setEnabled(false);
				mohCheckBoxDefault.setEnabled(false);
				pccCheckBox.setEnabled(false);
				pccCheckBoxDefault.setEnabled(false);
				cobCheckBox.setEnabled(false);
				cobCheckBoxDefault.setEnabled(false);
				lsaCheckBoxDefault.setEnabled(false);
				lsaCheckBox.setEnabled(false);
				remComboBox.setEnabled(false);
				remComboBoxDefault.setEnabled(false);
				remLabel.setEnabled(false);
				remLabel2.setEnabled(false);
			}
			else 
			{
				medals = true;
				//saveButtonMedals.setEnabled(true);
				medalsOnRadioButton.setSelected(true);
				keyCheckBox.setEnabled(true);
				keyCheckBoxDefault.setEnabled(true);
				mohCheckBox.setEnabled(true);
				mohCheckBoxDefault.setEnabled(true);
				pccCheckBox.setEnabled(true);
				pccCheckBoxDefault.setEnabled(true);
				cobCheckBox.setEnabled(true);
				cobCheckBoxDefault.setEnabled(true);
				lsaCheckBoxDefault.setEnabled(true);
				lsaCheckBox.setEnabled(true);
				remComboBox.setEnabled(true);
				remComboBoxDefault.setEnabled(true);
				remLabel.setEnabled(true);
				remLabel2.setEnabled(true);
					
				
				//read in DEFAULT MEDALS
				boolean DefaultKEYInput = fin.nextBoolean();
				if (!DefaultKEYInput)
				{
					keyCheckBox.setSelected(false);
					keyCheckBoxDefault.setSelected(false);
				}
				else
				{
					keyCheckBox.setSelected(true);
					keyCheckBoxDefault.setSelected(true);
				}
				boolean DefaultMOHInput = fin.nextBoolean();
				if (!DefaultMOHInput)
				{
					mohCheckBox.setSelected(false);
					mohCheckBoxDefault.setSelected(false);
				}
				else
				{
					mohCheckBox.setSelected(true);
					mohCheckBoxDefault.setSelected(true);
				}
				
				boolean DefaultPCCInput = fin.nextBoolean();
				if (!DefaultPCCInput)
				{
					pccCheckBox.setSelected(false);
					pccCheckBoxDefault.setSelected(false);
				}
				else
				{
					pccCheckBox.setSelected(true);
					pccCheckBoxDefault.setSelected(true);
				}
				boolean DefaultCOBInput = fin.nextBoolean();
				if (!DefaultCOBInput)
				{	
					cobCheckBox.setSelected(false);
					cobCheckBoxDefault.setSelected(false);
				}			
				else
				{
					cobCheckBox.setSelected(true);
					cobCheckBoxDefault.setSelected(true);
				}
				boolean DefaultLSAInput = fin.nextBoolean();
				if (!DefaultLSAInput)
				{
					lsaCheckBoxDefault.setSelected(false);
					lsaCheckBox.setSelected(false);
				}
				else
				{
					lsaCheckBox.setSelected(true);
					lsaCheckBoxDefault.setSelected(true);
				}
				boolean DefaultREMInput = fin.nextBoolean();
				if (!DefaultREMInput)
				{
					remComboBox.setSelectedIndex(0);
					remComboBoxDefault.setSelectedIndex(0);
				}
				else
				{
					fin.nextLine();
					String DefaultREMLevelInput = fin.nextLine();
					
					if (DefaultREMLevelInput.compareTo("I")==0)
					{						
						remComboBox.setSelectedIndex(1);
						remComboBoxDefault.setSelectedIndex(1);
					}
					if (DefaultREMLevelInput.compareTo("II")==0)
					{						
						remComboBox.setSelectedIndex(2);
						remComboBoxDefault.setSelectedIndex(2);
					}			
					if (DefaultREMLevelInput.compareTo("III")==0)
					{						
						remComboBox.setSelectedIndex(3);
						remComboBoxDefault.setSelectedIndex(3);
					}		
				}
			}
			if(fin.hasNext())
			{
				String defaultRankCap =fin.next();
				rankcapComboBox.setSelectedItem(defaultRankCap);
				rankcapComboBoxDefault.setSelectedItem(defaultRankCap);
			}
			if(fin.hasNextInt())
			{
				int x = fin.nextInt();
				int y = fin.nextInt();
				windowFrame.setLocation(x, y);
			}
			
			fin.close();
			updateMedals();
		} 
		catch (FileNotFoundException e) 
		{
				//e.printStackTrace();
		}
	}
	
	private void updateRankCap(String Rank, String RankCap, JComboBox rankcapComboBox) {
		
		oldRankCap = RankCap;
		rankcapComboBox.setActionCommand("other");
		rankcapComboBox.removeItem("12");
		rankcapComboBox.removeItem("11");
		rankcapComboBox.removeItem("10");
		rankcapComboBox.removeItem("9");
		rankcapComboBox.removeItem("6");
		rankcapComboBox.removeItem("3");
		rankcapComboBox.setEnabled(true);
		rankcapComboBox.setActionCommand("RankCap");
		
		Vector<String> rankcapVector = new gen.Rank(Rank).getRankcapVector();
		for (int i=0; i <rankcapVector.size() ; i++)
		{
			rankcapComboBox.addItem(rankcapVector.elementAt(i));
		}
		
		int rankInt = Integer.parseInt(Rank);
		
		if (rankInt==11 || rankInt==12)
		{
			rankcapComboBox.setEnabled(false);
		}

		for(int i=0; i<rankcapComboBox.getItemCount();i++)
		{
			if (oldRankCap !=null && oldRankCap.compareTo((String) rankcapComboBox.getItemAt(i))==0)
			{
				rankcapComboBox.setSelectedItem(rankcapComboBox.getItemAt(i));
			}
			
		}
		
		//if old rank is less than 6 and new rank is greater than old rank and oldrankcap is 6 then 
		if(oldRankCap!=null && Integer.parseInt(Rank)>Integer.parseInt(oldRankCap))
		{
			rankcapComboBox.setSelectedItem(rankcapComboBox.getItemAt(rankcapComboBox.getItemCount()-1));
		}
		
	}
		
	private void updateGunArmorAndTalent(String Class, String Armor, String Talent) 
	{
		oldTalent = Talent;
		oldArmor = Armor;
	
		talentComboBox.setEnabled(true);		
		armorComboBox.setEnabled(true);
		
		gunComboBox.setEnabled(false);
		gunComboBox.removeItem("-Weapon-");
		gunComboBox.removeItem("Assault Rifle");
		gunComboBox.removeItem("Sniper Rifle");
		gunComboBox.removeItem("Chaingun");
		gunComboBox.removeItem("Vindicator");
		gunComboBox.removeItem("Rocket Launcher");
		gunComboBox.removeItem("Flamethrower");
		gunComboBox.removeItem("Laser Rifle");
		gunComboBox.removeItem("Gatling Laser");
		gunComboBox.removeItem("Pistols");
		
		talentComboBox.removeItem("-Talent-");
		talentComboBox.removeItem("Courage");
		talentComboBox.removeItem("Running");
		talentComboBox.removeItem("Hacking");
		talentComboBox.removeItem("Toughness");
		talentComboBox.removeItem("Tinkering");
		talentComboBox.removeItem("Wiring");
		talentComboBox.removeItem("Spotting");
		
		armorComboBox.removeItem("-Armor-");
		armorComboBox.removeItem("Light");
		armorComboBox.removeItem("Medium");
		armorComboBox.removeItem("Heavy");
		armorComboBox.removeItem("Advanced");
		
		if (Class.compareTo("-Class-")==0)
		{
			gunComboBox.addItem("-Weapon-");
			talentComboBox.addItem("-Talent-");
			armorComboBox.addItem("-Armor-");
		}
		else
		{
			Vector<String> talentVector = new gen.Class(Class).getTalent();
			Vector<String> armorVector = new gen.Class(Class).getArmor();
			Vector<String> weaponVector = new gen.Class(Class).getWeapon();
			for (int i=0; i <weaponVector.size() ; i++)
			{
				gunComboBox.addItem(weaponVector.elementAt(i));
			}
			for (int i=0; i <talentVector.size() ; i++)
			{
				talentComboBox.addItem(talentVector.elementAt(i));
			}
			for (int i=0; i < armorVector.size(); i++)
			{
				armorComboBox.addItem(armorVector.elementAt(i));
			}
		}
		
		if (Class.compareTo("Cyborg")==0)
		{
			armorComboBox.setEnabled(false);
		}
		else if (Class.compareTo("Watchman")==0)
		{
			armorComboBox.setEnabled(false);
			gunComboBox.setEnabled(true);
		}
		else if (Class.compareTo("Maverick")==0)
		{
			gunComboBox.setEnabled(true);
		}
		
		for(int i=0; i<talentComboBox.getItemCount();i++)
		{
			if (oldTalent !=null && oldTalent.compareTo((String) talentComboBox.getItemAt(i))==0)
			{
				talentComboBox.setSelectedItem(talentComboBox.getItemAt(i));
			}
		}
		
		for(int i=0; i<armorComboBox.getItemCount();i++)
		{
			if (oldArmor !=null && oldArmor.compareTo((String) armorComboBox.getItemAt(i))==0)
			{
				armorComboBox.setSelectedItem(armorComboBox.getItemAt(i));
			}
		}
		
	}

	private void updateDefaultXpSlider(String DefaultRank) 
	{
		if (DefaultRank.compareTo("11")==0 || DefaultRank.compareTo("12")==0)
		{
			xpSliderDefault.setEnabled(false);
			xpSliderDefault.setValue(2495);
		}
		else
		{
			xpSliderDefault.setEnabled(true);
		}
	}
			
	private void updateXpSlider(String Rank) 
	{
		if (Rank.compareTo("11")==0|| Rank.compareTo("12")==0)
		{
			xpSlider.setEnabled(false);
			xpSlider.setValue(2495);
			xpLabel.setText("");
		}
		else
		{
			xpSlider.setEnabled(true);
			xpLabel.setText(""+(xpSlider.getValue()/5)/5+"%");
		}
	}

	private void updateMedals()
	{
		if (medals)
		{
			keyCheckBox.setEnabled(true);
			mohCheckBox.setEnabled(true);
			pccCheckBox.setEnabled(true);
			lsaCheckBox.setEnabled(true);
			cobCheckBox.setEnabled(true);
			remComboBox.setEnabled(true);
			
			if(rankComboBox.getSelectedItem()!=null
					&&rankcapComboBox.getSelectedItem()!=null
					&&Integer.parseInt((String) rankComboBox.getSelectedItem())<4
					&&Integer.parseInt((String) rankcapComboBox.getSelectedItem())<4)
			{
				keyCheckBox.setEnabled(false);
				mohCheckBox.setEnabled(false);
				pccCheckBox.setEnabled(false);
				lsaCheckBox.setEnabled(false);
				cobCheckBox.setEnabled(false);
				remComboBox.setEnabled(false);
				keyCheckBox.setSelected(false);
				mohCheckBox.setSelected(false);
				pccCheckBox.setSelected(false);
				lsaCheckBox.setSelected(false);
				cobCheckBox.setSelected(false);
				remComboBox.setSelectedIndex(0);
			}
			if(rankComboBox.getSelectedItem()!=null
					&&rankcapComboBox.getSelectedItem()!=null
					&&Integer.parseInt((String) rankComboBox.getSelectedItem())<10
					&&Integer.parseInt((String) rankcapComboBox.getSelectedItem())<10)
			{
				keyCheckBox.setEnabled(false);
				mohCheckBox.setEnabled(false);
				pccCheckBox.setEnabled(false);
				lsaCheckBox.setEnabled(false);
				cobCheckBox.setEnabled(false);
				keyCheckBox.setSelected(false);
				mohCheckBox.setSelected(false);
				pccCheckBox.setSelected(false);
				lsaCheckBox.setSelected(false);
				cobCheckBox.setSelected(false);
			}
			else if(rankComboBox.getSelectedItem()!=null
					&&rankcapComboBox.getSelectedItem()!=null
					&&Integer.parseInt((String) rankComboBox.getSelectedItem())<10
					&&Integer.parseInt((String) rankcapComboBox.getSelectedItem())<12)
			{
				keyCheckBox.setEnabled(false);
				mohCheckBox.setEnabled(false);
				pccCheckBox.setEnabled(false);
				keyCheckBox.setSelected(false);
				mohCheckBox.setSelected(false);
				pccCheckBox.setSelected(false);
			}
			else if(rankComboBox.getSelectedItem()!=null
					&&rankcapComboBox.getSelectedItem()!=null
					&&Integer.parseInt((String) rankComboBox.getSelectedItem())<12
					&&Integer.parseInt((String) rankcapComboBox.getSelectedItem())<12)
			{
				keyCheckBox.setEnabled(false);
				mohCheckBox.setEnabled(false);
				keyCheckBox.setSelected(false);
				mohCheckBox.setSelected(false);
			}
		}
	}
	
	public void stateChanged(ChangeEvent e) {
		xpLabel.setText(""+(xpSlider.getValue()/5)/5+"%"); 
		xpLabelDefault.setText(""+(xpSliderDefault.getValue()/5)/5+"%");
	}

	public void valueChanged(ListSelectionEvent e) 
			{
				JList list = (JList)e.getSource();
				String Selection = ""+list.getSelectedValue();
				if (Selection.compareTo("Basic")==0)
					selectedScrollPane.setViewportView(menuPanel);
				if (Selection.compareTo("Medals")==0)
					selectedScrollPane.setViewportView(medalPanel);
			}

	public static void setClipboardContents( String aString ){
			    StringSelection stringSelection = new StringSelection( aString );
			    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			    clipboard.setContents( stringSelection, stringSelection );
			  }

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		
		if (e.getWindow()==codeManagerFrame)
			codeManagerMenuItem.setSelected(false);
		
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
		
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
}
