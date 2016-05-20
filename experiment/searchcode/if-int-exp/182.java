package com.wordprocessor.core;
/*
 Alex Frier
 5/22/2013
 
 FINAL PROJECT
 This program allows users to write their own documents and save them to their hard drive. When re-running the program,
 users will be able to open these documents and continue editing them, including formatting depending on the file format.
 Multiple documents are permitted, as long as the file name is not the same.
 
 Swing Components:
 JTextArea (Document editing)
 JScrollBar (Scrolling document)
 GridBagLayout (Component resizing based on frame dimensions)
 JMenu
 JMenuItem
 JMenuBar (Save/edit)
 JToolBar (Formatting)
 
 Listeners:
 ActionListener
 CaretListener
 WindowFocusListener
 WindowListener (Save before quit)
 
 I/O:
 File (Saving docs)
 FileWriter
 PrintWriter
 BufferedWriter
 Scanner
 
 */
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.rtf.RTFEditorKit;

import com.wordprocessor.action.BoldAction;
import com.wordprocessor.action.FindNextAction;
import com.wordprocessor.action.ItalicAction;
import com.wordprocessor.action.UnderlineAction;
import com.wordprocessor.filters.HtmlFilter;
import com.wordprocessor.filters.RtfFilter;
import com.wordprocessor.filters.TxtFilter;
import com.wordprocessor.filters.WpFilter;
import com.wordprocessor.listeners.CaretStalker;
import com.wordprocessor.listeners.DocStalker;
import com.wordprocessor.util.ClipboardHandler;
import com.wordprocessor.util.FileUtils;
 
public class WordProcessor implements ActionListener, ItemListener {
	
	public static final String title = "Word Processor"; // Program name
	public static final String newDocName = title + " - New document"; // New doc title

	private static String s = System.getProperty("file.separator");
	private static String os = System.getProperty("os.name").toLowerCase();
	
	private ImageIcon boldIcon = null;
	private ImageIcon italicIcon = null;
	private ImageIcon underlineIcon = null;

	public JFrame frame1 = new JFrame(newDocName); // Make the frame
	public JPanel panel1 = new JPanel(); // Make the panel
	public JTextPane area1 = new JTextPane(); // Make the input area
	public StyledDocument doc = area1.getStyledDocument(); // Make the format displayer
	public JScrollPane pane1 = new JScrollPane(area1); // Make the scroll bar

	public JDialog findDlg = new JDialog(frame1, "Word Processer - Find");
	public JPanel findPnl = new JPanel();
	public JTextField findFld = new JTextField(20);
	public JCheckBox matchCase = new JCheckBox("Match case");
	public JButton findNext = new JButton("Find");
	public JLabel findMsg = new JLabel("");

	// Formatting bar
	public JToolBar formatting = new JToolBar("Formatting"); // Make the formatting toolbar
	public JToggleButton bold = null;
	public JToggleButton italic = null;
	public JToggleButton underline = null;

	// Menu bar
	public JMenuBar bar = new JMenuBar(); // Make the menu bar

	public JMenu file = new JMenu("File");
	public JMenuItem newFile = new JMenuItem("New", KeyEvent.VK_N);
	public JMenuItem saveAsB = new JMenuItem("Save as...", KeyEvent.VK_A);
	public JMenuItem save = new JMenuItem("Save", KeyEvent.VK_S);
	public JMenuItem open = new JMenuItem("Open...", KeyEvent.VK_O);
	public JMenuItem export = new JMenuItem("Export...", KeyEvent.VK_P);
	public JMenuItem exit = new JMenuItem("Exit", KeyEvent.VK_E);

	public JMenu edit = new JMenu("Edit");
	public JMenuItem copy = new JMenuItem(new DefaultEditorKit.CopyAction());
	public JMenuItem paste = new JMenuItem(new DefaultEditorKit.PasteAction());
	public JMenuItem cut = new JMenuItem(new DefaultEditorKit.CutAction());
	public JMenuItem find = new JMenuItem("Find...", KeyEvent.VK_F);
	public JMenuItem select = new JMenuItem("Select all");

	public JToolBar stats = new JToolBar("Statistics");
	public JLabel chars = new JLabel("Characters: 0");
	public JLabel charsNoSpaces = new JLabel("Characters (no spaces): 0");
	public JLabel words = new JLabel("Words: 0");
	// Files/directories
	public File processor = new File("");
	public File docs = new File("");
	public File exports = new File("");
	public File currentFile = new File("");
	public File currentExportFile = new File("");

	// Filters
	private FileFilter wp = new WpFilter(); // .wp exclusive format
	private FileFilter rtf = new RtfFilter(); // .rtf format
	private FileFilter txt = new TxtFilter(); // .txt format
	private FileFilter html = new HtmlFilter(); // .html export format
	private FileFilter currentFilter = null;
	private FileFilter currentExportFilter = null;

	public int charCount = 0;
	public int charCountNoSpaces = 0;
	public int wordCount = 0;

	public boolean revisionSaved = true;

	public boolean fileOpen = false;
	public boolean exported = false;

	public boolean lockButtonBold = false;
	public boolean lockButtonItalic = false;
	public boolean lockButtonUnderline = false;

	public boolean findAndMatchCase = false;

    public static void main(String[] args) {
    	new WordProcessor();
    }
    
    public WordProcessor() {
    	makeDirectory();
		
    	try {
    		ClassLoader cl = Thread.currentThread().getContextClassLoader();
			this.boldIcon = new ImageIcon(ImageIO.read(cl.getResourceAsStream("bold.png")));
			this.italicIcon = new ImageIcon(ImageIO.read(cl.getResourceAsStream("italic.png")));
			this.underlineIcon = new ImageIcon(ImageIO.read(cl.getResourceAsStream("underline.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	this.bold = new JToggleButton(new BoldAction(this, this.boldIcon)); 
		bold.setActionCommand("bold_bind");
		bold.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('B', KeyEvent.CTRL_DOWN_MASK), bold.getActionCommand());
		bold.getActionMap().put(bold.getActionCommand(), bold.getAction());
		bold.setToolTipText("Bold (ALT + B)");
		this.italic = new JToggleButton(new ItalicAction(this, this.italicIcon));
		italic.setActionCommand("italic_bind");
		italic.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('I', KeyEvent.CTRL_DOWN_MASK), italic.getActionCommand());
		italic.getActionMap().put(italic.getActionCommand(), italic.getAction());
		italic.setToolTipText("Italic (ALT + I)");
		this.underline = new JToggleButton(new UnderlineAction(this, this.underlineIcon));
		underline.setActionCommand("underline_bind");
		underline.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('U', KeyEvent.CTRL_DOWN_MASK), underline.getActionCommand());
		underline.getActionMap().put(underline.getActionCommand(), underline.getAction());
		underline.setToolTipText("Underline (ALT + U)");
		
		formatting.setFloatable(false);
		
		chars.setToolTipText("The number of characters (including spaces) within the document.");
		charsNoSpaces.setToolTipText("The number of characters (not including spaces) within the document.");
		words.setToolTipText("The number of words within the document.");
		
		stats.setFloatable(false);
		
		newFile.addActionListener(this);
		newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
		newFile.setToolTipText("Creates a new document.");
		file.add(newFile); // New document button
		saveAsB.addActionListener(this);
		saveAsB.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK));
		saveAsB.setToolTipText("Brings up a window where you can save your document with a particular name and file type.");
		file.add(saveAsB); // Save as button
		save.addActionListener(this);
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
		save.setToolTipText("Saves your document under its current name and file type.");
		file.add(save); //Save file button
		open.addActionListener(this);
		open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
		open.setToolTipText("Brings up a window where you can choose a file to open and continue editing.");
		file.add(open); //Open file button
		export.addActionListener(this);
		export.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.ALT_MASK));
		export.setToolTipText("Exports your document to an alternate format. The file cannot be re-opened in this program.");
		file.add(export);
		exit.addActionListener(this);
		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_MASK));
		exit.setToolTipText("Exits Word Processor.");
		file.add(exit); //Exit WP button
		file.setMnemonic(KeyEvent.VK_F); //ALT+F - open File tab
		file.setToolTipText("File functions, like saving and opening.");
		bar.add(file); //Add File tab
		
		copy.setText("Copy");
		copy.addActionListener(this);
		copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK));
		copy.setToolTipText("Copies selected text to the clipboard.");
		edit.add(copy);
		copy.setEnabled(false);
		cut.setText("Cut");
		cut.addActionListener(this);
		cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK));
		cut.setToolTipText("Cuts selected text.");
		edit.add(cut);
		cut.setEnabled(false);
		paste.setText("Paste");
		paste.addActionListener(this);
		paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK));
		paste.setToolTipText("Pastes text in the clipboard.");
		edit.add(paste);
		find.addActionListener(this);
		find.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK));
		find.setToolTipText("Finds input text in the current document.");
		edit.add(find);
		select.addActionListener(this);
		select.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK));
		select.setToolTipText("Selects all text in the document.");
		edit.add(select);
		edit.setMnemonic(KeyEvent.VK_E);
		edit.setToolTipText("Edit functions for modifying or interacting with the document.");
		bar.add(edit);
		
		frame1.setJMenuBar(bar); //Add the menu bar and its Components.
    	addGridBagsAndComponents(); //Add the GridBagLayout to Components. Enables components to be resized when resizing frame.
    	
    	Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		Style regular = doc.addStyle("regular", def);
		
		Style s = doc.addStyle("bold", regular);
		StyleConstants.setBold(s, true); //Bold is allowed
		
		s = doc.addStyle("italic", regular);
		StyleConstants.setItalic(s, true);
		
		s = doc.addStyle("underline", regular);
		StyleConstants.setUnderline(s, true);
		
    	frame1.add(panel1); //Add the parent layout
    	
    	area1.setDragEnabled(true);
    	area1.setTransferHandler(new ClipboardHandler());
    	area1.addCaretListener(new CaretStalker(this));
    	doc.addDocumentListener(new DocStalker(this));
    	
    	frame1.addWindowFocusListener(new WindowAdapter() {
			public void windowGainedFocus(WindowEvent e) {
				area1.requestFocusInWindow();
			}
		});
    	
    	frame1.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame1.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				prepareToClose();
			}
		});
		frame1.setMinimumSize(new Dimension(400, 500));
		frame1.setExtendedState(frame1.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		frame1.pack();
		frame1.setVisible(true);
    }
    
    public void addGridBagsAndComponents() {
    	GridBagLayout gridbag = new GridBagLayout(); //Make the layout
    	GridBagConstraints c = new GridBagConstraints(); //Make the constraints
    	
    	panel1.setLayout(gridbag); //Add layout to parent panel
    	
    	c.fill = GridBagConstraints.BOTH; // Change Component size when resizing in BOTH directions
    	c.insets = new Insets(20, 20, 20, 20); //Set spacing between Components to 20 pixels
    	
    	c.gridx = 0; // Coords (0, 0)
    	c.gridy = 0;
    	c.weightx = c.weighty = 0.0; // Do not take up leftover space
    	c.anchor = GridBagConstraints.PAGE_START;
    	
    	formatting.add(bold);
    	formatting.add(italic);
    	formatting.add(underline);
    	panel1.add(formatting, c);
    	
    	c.gridx = 0; //Coords (0, -1)
    	c.gridy = 1;
    	c.weightx = c.weighty = 1.0; //Take up leftover space
    	c.anchor = GridBagConstraints.CENTER;
  
    	pane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); //ALWAYS show scroll bar
    	panel1.add(pane1, c); //Add text area and scroll bar
    	
    	c.gridx = 0; // Coords (0, -2)
    	c.gridy = 2;
    	c.weightx = c.weighty = 0.0; // Do not take up leftover space
    	c.anchor = GridBagConstraints.PAGE_END;
    	
    	stats.add(chars);
    	stats.addSeparator();
    	stats.add(charsNoSpaces);
    	stats.addSeparator();
    	stats.add(words);
    	panel1.add(stats, c);
    }
    
    public void makeDirectory() {
		if (os.contains("win")) {
			String appData = System.getenv("APPDATA");
			String mainDir = appData + s + "WordProcessor" + s;
			processor = new File(mainDir);

			String docsDir = mainDir + s + "docs" + s;
			docs = new File(docsDir);
			
			String exportsDir = mainDir + s + "exports" + s;
			exports = new File(exportsDir);

			if (!processor.exists() && !processor.mkdirs()) {
				throw new RuntimeException();
			}

			if (!docs.exists() && !docs.mkdirs()) {
				throw new RuntimeException();
			}
			
			if (!exports.exists() && !exports.mkdirs())
			{
				throw new RuntimeException();
			}
			
		} else {
			throw new UnsupportedOperationException(
					"Only Windows OS is supported!");
		}
	}
	
	public void prepareToClose() {
		if (!revisionSaved) {
			int a = JOptionPane.showConfirmDialog(frame1, "This document has unsaved changes. Would you like to save before exiting?", title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if (a == JOptionPane.YES_OPTION) {
				save.doClick();
			}
			else if (a == JOptionPane.CANCEL_OPTION) {
				return;
			}
			System.exit(0);
		}
		else {
			System.exit(0);
		}
	}
	
	public void clearDocument(boolean newDoc) {
		if (newDoc) {
			frame1.setTitle(newDocName);
			fileOpen = false;
			currentFile = new File("");
		}
		else {
			fileOpen = true;
		}
		area1.setText("");
		bold.setSelected(false);
		italic.setSelected(false);
		underline.setSelected(false);
		revisionSaved = true;
	}
	
	public boolean saveAs() {
		String oldTitle = frame1.getTitle();
		frame1.setTitle(title + " - Saving...");
		JFileChooser saveAs = new JFileChooser();
		saveAs.setAcceptAllFileFilterUsed(false);
		saveAs.addChoosableFileFilter(wp);
		saveAs.addChoosableFileFilter(rtf);
		saveAs.addChoosableFileFilter(txt);
		if (!fileOpen) {
			saveAs.setCurrentDirectory(docs);
		}
		else {
			saveAs.setCurrentDirectory(currentFile.getParentFile());
			saveAs.setSelectedFile(currentFile);
			saveAs.setFileFilter(currentFilter);
		}
		int returnVal = saveAs.showSaveDialog(frame1);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			
			try {
				File saveLocation = saveAs.getSelectedFile();
				String absPath = saveLocation.getAbsolutePath();
				if (FileUtils.getExtension(saveLocation) == null || !saveAs.getFileFilter().accept(saveLocation)) {
					if (!absPath.endsWith(".") || !saveAs.getFileFilter().accept(saveLocation)) {
						saveLocation = new File(absPath + "." + saveAs.getFileFilter().toString());
					}
					else {
						saveLocation = new File(absPath + saveAs.getFileFilter().toString());
					}
				}
				
				if (!saveLocation.exists()) {
					saveLocation.createNewFile();
					if (FileUtils.getExtension(saveLocation).equals("rtf") || FileUtils.getExtension(saveLocation).equals("wp")) {
						BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(saveLocation));
						RTFEditorKit rtf = new RTFEditorKit();
						rtf.write(out, doc, doc.getStartPosition().getOffset(), doc.getLength());
						out.close();
					}
					else {
						int a = JOptionPane.showConfirmDialog(frame1, "WARNING! This file format will not save any formatting you have applied to your text. Would you like to continue?", title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
						if (a == JOptionPane.YES_OPTION) {
							PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(saveLocation)));
							writer.write(area1.getText());
							writer.close();
						}
						else {
							frame1.setTitle(oldTitle);
							return false;
						}
					}
				}
				else {
					int a = JOptionPane.showConfirmDialog(frame1, "This file already exists. Are you sure you would like to write over it?", title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if (a == JOptionPane.YES_OPTION) {
						if (FileUtils.getExtension(saveLocation).equals("rtf") || FileUtils.getExtension(saveLocation).equals("wp")) {
							BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(saveLocation));
							RTFEditorKit rtf = new RTFEditorKit();
							rtf.write(out, doc, doc.getStartPosition().getOffset(), doc.getLength());
							out.close();
						}
						else {
							int c = JOptionPane.showConfirmDialog(frame1, "WARNING! This file format will not save any formatting you have applied to your text. Would you like to continue?", title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
							if (c == JOptionPane.YES_OPTION) {
								PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(saveLocation)));
								writer.write(area1.getText());
								writer.close();
							}
							else {
								frame1.setTitle(oldTitle);
								return false;
							}
						}
					}
					else {
						frame1.setTitle(oldTitle);
						return false;
					}
				}
				
				currentFilter = saveAs.getFileFilter();
				revisionSaved = true;
				
				JOptionPane.showMessageDialog(frame1, "Document successfully saved.", title, JOptionPane.INFORMATION_MESSAGE);
				currentFile = saveLocation;
				frame1.setTitle(title + " - " + currentFile.getName());
				fileOpen = true;
			}
			catch (FileNotFoundException fnfe) {
				JOptionPane.showMessageDialog(frame1, "Your document could not be saved. Please try a different file name.", title, JOptionPane.ERROR_MESSAGE);
				return false;
			}
			catch (IOException ioe) {
				JOptionPane.showMessageDialog(frame1, "Your document could not be saved. Try a different name. File too large?", title, JOptionPane.ERROR_MESSAGE);
				return false;
			}
			catch (SecurityException se) {
				JOptionPane.showMessageDialog(frame1, "Access denied. Try saving to a different directory or file.", title, JOptionPane.ERROR_MESSAGE);
				return false;
			}
			catch (BadLocationException ble) {
				JOptionPane.showMessageDialog(frame1, "The document's starting or ending point could not be found. Try again.", title, JOptionPane.ERROR_MESSAGE);
				return false;
			}
			catch (Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(frame1, "An unknown error has occurred!", title, JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		else {
			frame1.setTitle(oldTitle);
			return false;
		}
		return true;
	}
	
	public void updateStats() {
		int lastCharCount = charCount;
		int lastCharNoSpaceCount = charCountNoSpaces;
		int lastWordCount = wordCount;
		charCount = 0;
		charCountNoSpaces = 0;
		wordCount = 0;
		
		for (int i = doc.getStartPosition().getOffset(); i < doc.getLength(); i++) {
			try {
				String character = doc.getText(i, 1);
				if (!character.equals("\n") && !character.equals("\t")) {
					charCount++;
					if (!character.equals(" ")) {
						charCountNoSpaces++;
					}
				}
				
				if (i >= 1) {
					String lastCharacter = doc.getText(i - 1, 1);
					if (lastCharacter != null 
						&& !character.equals(" ") && !character.equals("\n") && !character.equals("\t")
						&& (lastCharacter.equals(" ") || lastCharacter.equals("\n") || lastCharacter.equals("\t"))) {
						wordCount++;
					}
				}
			}
			catch (BadLocationException e) {
				charCount = lastCharCount;
				charCountNoSpaces = lastCharNoSpaceCount;
				wordCount = lastWordCount;
				e.printStackTrace();
				break;
			}
		}
		chars.setText("Characters: " + charCount);
		charsNoSpaces.setText("Characters (no spaces): " + charCountNoSpaces);
		words.setText("Words: " + wordCount);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(this.newFile)) {
			if (!this.revisionSaved) {
				int a = JOptionPane.showConfirmDialog(frame1, "This document has unsaved changes. Would you like to save before creating a new document?", title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				if (a == JOptionPane.YES_OPTION) {
					this.save.doClick();
				}
				else if (a == JOptionPane.CANCEL_OPTION) {
					return;
				}
			}
			this.clearDocument(true);
		}
		else if (e.getSource().equals(this.saveAsB)) {
			this.saveAs();
		}
		else if (e.getSource().equals(this.save)) {
			if (!this.fileOpen) {
				this.saveAsB.doClick();
				return;
			}
			else {
				frame1.setTitle(title + " - " + currentFile.getName() + " - Saving...");
				try {
					RTFEditorKit rtf;
					
					BufferedOutputStream out;
					
					if (FileUtils.getExtension(currentFile).equals("rtf") || FileUtils.getExtension(currentFile).equals("wp")) {
						out = new BufferedOutputStream(new FileOutputStream(currentFile));
						rtf = new RTFEditorKit();
						rtf.write(out, doc, doc.getStartPosition().getOffset(), doc.getLength());
						out.close();
					}
					else {
						PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(currentFile)));
						writer.write(area1.getText());
						writer.close();
					}
				}
				catch (FileNotFoundException fnfe) {
					JOptionPane.showMessageDialog(frame1, "Your document could not be saved. Please try a different file name.", title, JOptionPane.ERROR_MESSAGE);
				}
				catch (IOException ioe) {
					JOptionPane.showMessageDialog(frame1, "Your document could not be saved. Try a different name. File too large?", title, JOptionPane.ERROR_MESSAGE);
				}
				catch (SecurityException se) {
					JOptionPane.showMessageDialog(frame1, "Access denied. Try saving to a different directory or file.", title, JOptionPane.ERROR_MESSAGE);
				}
				catch (BadLocationException ble) {
					JOptionPane.showMessageDialog(frame1, "The document's starting or ending point could not be found. Try again.", title, JOptionPane.ERROR_MESSAGE);
				}
				catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(frame1, "An unknown error has occurred!", title, JOptionPane.ERROR_MESSAGE);
				}
				revisionSaved = true;
				frame1.setTitle(title + " - " + currentFile.getName());
			}
		}
		else if (e.getSource().equals(open)) {
			if (!revisionSaved) {
				int a = JOptionPane.showConfirmDialog(frame1, "This document has unsaved changes. Would you like to save before opening another file?", title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				if (a == JOptionPane.YES_OPTION) {
					save.doClick();
				}
				else if (a == JOptionPane.CANCEL_OPTION) {
					return;
				}
			}
			JFileChooser o = new JFileChooser();
			o.setAcceptAllFileFilterUsed(false);
			o.addChoosableFileFilter(wp);
			o.addChoosableFileFilter(rtf);
			o.addChoosableFileFilter(txt);
			if (!fileOpen) {
				o.setCurrentDirectory(docs);
			}
			else {
				o.setCurrentDirectory(currentFile.getParentFile());
				o.setSelectedFile(currentFile);
				o.setFileFilter(currentFilter);
			}
			int returnVal = o.showOpenDialog(frame1);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					File openLocation = o.getSelectedFile();
					if (!openLocation.exists()) {
						JOptionPane.showMessageDialog(frame1, "The file " + openLocation.getName() + " does not exist in this directory!", title, JOptionPane.ERROR_MESSAGE);
						o.setVisible(false);
						return;
					}
					clearDocument(false);
					if (FileUtils.getExtension(openLocation).equals("rtf") || FileUtils.getExtension(openLocation).equals("wp")) {
						RTFEditorKit kit = new RTFEditorKit();
						BufferedInputStream in = new BufferedInputStream(new FileInputStream(openLocation));
						kit.read(in, doc, doc.getStartPosition().getOffset());
						in.close();
					}
					else {
						Scanner reader = new Scanner(openLocation);
						reader.useDelimiter("\\Z");
						String text = reader.next();
						reader.close();
						area1.setText(text);
					}
					o.setVisible(false);
					currentFilter = o.getFileFilter();
					revisionSaved = true;
					currentFile = openLocation;
					frame1.setTitle(title + " - " + currentFile.getName());
					fileOpen = true;
				}
				catch (FileNotFoundException fnfe) {
					JOptionPane.showMessageDialog(frame1, "Your document could not be found. Did you enter the correct name?", title, JOptionPane.ERROR_MESSAGE);
				}
				catch (IOException ioe) {
					JOptionPane.showMessageDialog(frame1, "Your document could not be opened. File too large?", title, JOptionPane.ERROR_MESSAGE);
				}
				catch (SecurityException se) {
					JOptionPane.showMessageDialog(frame1, "Access denied. Try opening the file in a different directory or file.", title, JOptionPane.ERROR_MESSAGE);
				}
				catch (BadLocationException ble) {
					JOptionPane.showMessageDialog(frame1, "The document's starting point could not be found. Try again.", title, JOptionPane.ERROR_MESSAGE);
				}
				catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(frame1, "An unknown error has occurred!", title, JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		else if (e.getSource().equals(export)) {
			if (!fileOpen) {
				int a = JOptionPane.showConfirmDialog(frame1, "Your document must be saved in a standard format before exporting. Would you like to save now?", title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (a == JOptionPane.YES_OPTION) {
					boolean saveSuccessful = this.saveAs();
					if (!saveSuccessful) {
						return;
					}
				}
				else {
					return;
				}
			}
			else {
				save.doClick();
			}
			
			JFileChooser exp = new JFileChooser();
			exp.setAcceptAllFileFilterUsed(false);
			exp.addChoosableFileFilter(html);
			if (!exported) {
				exp.setCurrentDirectory(exports);
			}
			else {
				exp.setCurrentDirectory(currentExportFile.getParentFile());
				exp.setSelectedFile(currentExportFile);
				exp.setFileFilter(currentExportFilter);
			}
			int returnVal = exp.showDialog(frame1, "Export");
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					HTMLEditorKit html;
					BufferedOutputStream out;
					
					File expLocation = exp.getSelectedFile();
					String absPath = expLocation.getAbsolutePath();
					if (FileUtils.getExtension(expLocation) == null || !exp.getFileFilter().accept(expLocation)) {
						if (!absPath.endsWith(".") || !exp.getFileFilter().accept(expLocation)) {
							expLocation = new File(absPath + "." + exp.getFileFilter().toString());
						}
						else {
							expLocation = new File(absPath + exp.getFileFilter().toString());
						}
					}
					
					if (!expLocation.exists()) {
						expLocation.createNewFile();
						if (FileUtils.getExtension(expLocation).equals("html")) {
							out = new BufferedOutputStream(new FileOutputStream(expLocation));
							html = new HTMLEditorKit();
							html.write(out, doc, doc.getStartPosition().getOffset(), doc.getLength());
							out.close();
						}
					}
					else {
						int a = JOptionPane.showConfirmDialog(frame1, "This file already exists. Are you sure you would like to write over it?", title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
						if (a == JOptionPane.YES_OPTION) {
							if (FileUtils.getExtension(expLocation).equals("html")) {
								out = new BufferedOutputStream(new FileOutputStream(expLocation));
								html = new HTMLEditorKit();
								html.write(out, doc, doc.getStartPosition().getOffset(), doc.getLength());
								out.close();
							}
						}
						else {
							return;
						}
					}
					
					currentExportFilter = exp.getFileFilter();
					
					JOptionPane.showMessageDialog(frame1, "Document successfully exported.", title, JOptionPane.INFORMATION_MESSAGE);
					
					currentExportFile = expLocation;
					exported = true;
				}
				catch (FileNotFoundException fnfe) {
					JOptionPane.showMessageDialog(frame1, "Your document could not be exported. Please try a different file name.", title, JOptionPane.ERROR_MESSAGE);
				}
				catch (IOException ioe) {
					JOptionPane.showMessageDialog(frame1, "Your document could not be exported. Try a different name. File too large?", title, JOptionPane.ERROR_MESSAGE);
				}
				catch (SecurityException se) {
					JOptionPane.showMessageDialog(frame1, "Access denied. Try exporting to a different directory or file.", title, JOptionPane.ERROR_MESSAGE);
				}
				catch (BadLocationException ble) {
					JOptionPane.showMessageDialog(frame1, "The document's starting or ending point could not be found. Try again.", title, JOptionPane.ERROR_MESSAGE);
				}
				catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(frame1, "An unknown error has occurred!", title, JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		else if (e.getSource().equals(exit)) {
			prepareToClose();
		}
		else if (e.getSource().equals(find)) {
			findDlg = new JDialog(frame1, "Find");
			findPnl = new JPanel();
			findFld = new JTextField(20);
			matchCase = new JCheckBox("Match case");
			findNext = new JButton(new FindNextAction(this));
			
			findPnl.setLayout(new GridLayout(0, 3));
			findPnl.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
			findPnl.add(findFld);
			matchCase.addItemListener(this);
			matchCase.setActionCommand("matchCase");
			matchCase.setMnemonic(KeyEvent.VK_M);
			findPnl.add(matchCase);
			findNext.setText("Find Next");
			findNext.setMnemonic(KeyEvent.VK_F);
			findNext.setActionCommand("findNext_bind");
			findNext.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), findNext.getActionCommand());
			findNext.getActionMap().put(findNext.getActionCommand(), findNext.getAction());
			findPnl.add(findNext);
			findPnl.add(findMsg);
			findDlg.add(findPnl);
			findDlg.pack();
			findDlg.setResizable(false);
			findDlg.setLocationRelativeTo(frame1);
			findDlg.setVisible(true);
		}
		else if (e.getSource().equals(select)) {
			area1.requestFocus();
			area1.selectAll();
		}
	}
	
	public void itemStateChanged(ItemEvent e) {
		int change = e.getStateChange();
		if (e.getSource() instanceof JCheckBox) {
			JCheckBox btn = (JCheckBox)e.getSource();
			if (btn.getActionCommand().equals("matchCase")) {
				if (change == ItemEvent.SELECTED) {
					findAndMatchCase = true;
				}
				else {
					findAndMatchCase = false;
				}
			}
		}
	}
}
