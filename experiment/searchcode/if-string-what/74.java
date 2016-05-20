/*
 Part of the ReplicatorG project - http://www.replicat.org
 Copyright (c) 2008 Zach Smith

 Forked from Arduino: http://www.arduino.cc

 Based on Processing http://www.processing.org
 Copyright (c) 2004-05 Ben Fry and Casey Reas
 Copyright (c) 2001-04 Massachusetts Institute of Technology

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
 $Id: MainWindow.java 370 2008-01-19 16:37:19Z mellis $
 */

package replicatorg.app.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.prefs.BackingStoreException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;

import org.w3c.dom.Document;

import replicatorg.app.Base;
import replicatorg.app.MachineController;
import replicatorg.app.MachineFactory;
import replicatorg.app.Serial;
import replicatorg.app.Sketchbook;
import replicatorg.app.exceptions.SerialException;
import replicatorg.app.syntax.JEditTextArea;
import replicatorg.app.syntax.PdeKeywords;
import replicatorg.app.syntax.PdeTextAreaDefaults;
import replicatorg.app.syntax.SyntaxDocument;
import replicatorg.app.syntax.TextAreaPainter;
import replicatorg.drivers.EstimationDriver;
import replicatorg.drivers.OnboardParameters;
import replicatorg.drivers.UsesSerial;
import replicatorg.machine.MachineListener;
import replicatorg.machine.MachineProgressEvent;
import replicatorg.machine.MachineState;
import replicatorg.machine.MachineStateChangeEvent;
import replicatorg.machine.MachineToolStatusEvent;
import replicatorg.model.JEditTextAreaSource;

import com.apple.mrj.MRJAboutHandler;
import com.apple.mrj.MRJApplicationUtils;
import com.apple.mrj.MRJOpenDocumentHandler;
import com.apple.mrj.MRJPrefsHandler;
import com.apple.mrj.MRJQuitHandler;

public class MainWindow extends JFrame implements MRJAboutHandler, MRJQuitHandler,
		MRJPrefsHandler, MRJOpenDocumentHandler,
		MachineListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4144538738677712284L;

	static final String WINDOW_TITLE = "ReplicatorG" + " - "
			+ Base.VERSION_NAME;

	// List of most recently opened files names.
	List<String> mruFiles;

	// Preference key name
	final static String MRU_LIST_KEY = "mru_list";

	// p5 icon for the window
	Image icon;

	// our machines.xml document.
	public Document dom;

	MachineController machine;

	// otherwise, if the window is resized with the message label
	// set to blank, it's preferredSize() will be fukered
	static public final String EMPTY = "                                                                     "
			+ "                                                                     "
			+ "                                                                     ";

	static public final KeyStroke WINDOW_CLOSE_KEYSTROKE = KeyStroke
			.getKeyStroke('W', Toolkit.getDefaultToolkit()
					.getMenuShortcutKeyMask());

	static final int HANDLE_NEW = 1;

	static final int HANDLE_OPEN = 2;

	static final int HANDLE_QUIT = 3;

	int checkModifiedMode;

	String handleOpenPath;

	boolean handleNewShift;

	PageFormat pageFormat;

	PrinterJob printerJob;

	MainButtonPanel buttons;

	EditorHeader header;

	//EditorStatus status;

	MachineStatusPanel machineStatusPanel;

	MessagePanel console;

	JSplitPane splitPane;

	JPanel consolePanel;

	JLabel lineNumberComponent;

	// currently opened program
	public Sketch sketch;

	public JEditTextArea textarea;

	// runtime information and window placement
	Point appletLocation;

	public SimulationThread simulationThread;

	public EstimationThread estimationThread;

	JMenuItem saveMenuItem;
	JMenuItem saveAsMenuItem;
	JMenuItem stopItem;
	JMenuItem pauseItem;

	JMenu machineMenu;
	MachineMenuListener machineMenuListener;

	public boolean building;
	public boolean simulating;
	public boolean debugging;

	// boolean presenting;

	// undo fellers
	JMenuItem undoItem, redoItem;

	protected UndoAction undoAction;

	protected RedoAction redoAction;

	UndoManager undo;

	// used internally, and only briefly
	CompoundEdit compoundEdit;

	//

	// SketchHistory history; // TODO re-enable history
	Sketchbook sketchbook;

	FindReplace find;

	public MainWindow() {
		super(WINDOW_TITLE);
		// #@$*(@#$ apple.. always gotta think different
		MRJApplicationUtils.registerAboutHandler(this);
		MRJApplicationUtils.registerPrefsHandler(this);
		MRJApplicationUtils.registerQuitHandler(this);
		MRJApplicationUtils.registerOpenDocumentHandler(this);

		// load up the most recently used files list
		String mruString = Base.preferences.get(MRU_LIST_KEY,null);
		mruFiles = new LinkedList<String>();
		if (mruString != null && mruString.length() != 0) {
			for (String entry : mruString.split(",")) {
				addMRUEntry(entry);
			}
		}

		// set the window icon
		icon = Base.getImage("images/icon.gif", this);
		setIconImage(icon);

		// add listener to handle window close box hit event
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				handleQuitInternal();
			}
		});

		// don't close the window when clicked, the app will take care
		// of that via the handleQuitInternal() methods
		// http://dev.processing.org/bugs/show_bug.cgi?id=440
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		sketchbook = new Sketchbook(this);

		JMenuBar menubar = new JMenuBar();
		menubar.add(buildFileMenu());
		menubar.add(buildEditMenu());
		menubar.add(buildGCodeMenu());
		menubar.add(buildMachineMenu());
		menubar.add(buildHelpMenu());

		setJMenuBar(menubar);

		// for rev 0120, placing things inside a JPanel because
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		JPanel pane = new JPanel();
		pane.setLayout(new BorderLayout());
		contentPane.add(pane, BorderLayout.CENTER);

		Box box = Box.createVerticalBox();
		Box upper = Box.createVerticalBox();

		buttons = new MainButtonPanel(this);
		upper.add(buttons);
		machineStatusPanel = new MachineStatusPanel();
		upper.add(machineStatusPanel);

		header = new EditorHeader(this);
		upper.add(header);

		textarea = new JEditTextArea(new PdeTextAreaDefaults());
		textarea.setRightClickPopup(new TextAreaPopup());
		textarea.setHorizontalOffset(6);
		upper.add(textarea);

		// assemble console panel, consisting of status area and the console
		// itself
		consolePanel = new JPanel();
		consolePanel.setLayout(new BorderLayout());

		//status = new EditorStatus(this);
		//consolePanel.add(status, BorderLayout.NORTH);

		console = new MessagePanel(this);
		// windows puts an ugly border on this guy
		console.setBorder(null);
		consolePanel.add(console, BorderLayout.CENTER);

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upper,
				consolePanel);
		// textarea, consolePanel);

		splitPane.setOneTouchExpandable(true);
		// repaint child panes while resizing
		splitPane.setContinuousLayout(true);
		// if window increases in size, give all of increase to
		// the textarea in the uppper pane
		splitPane.setResizeWeight(1D);

		// to fix ugliness.. normally macosx java 1.3 puts an
		// ugly white border around this object, so turn it off.
		splitPane.setBorder(null);

		// the default size on windows is too small and kinda ugly
		int dividerSize = Base.preferences.getInt("editor.divider.size",0);
		if (dividerSize != 0) {
			splitPane.setDividerSize(dividerSize);
		}

		splitPane.setMinimumSize(new Dimension(600, 600));
		box.add(splitPane);
		pane.add(box);

		pane.setTransferHandler(new TransferHandler() {
			public boolean canImport(JComponent dest, DataFlavor[] flavors) {
				// claim that we can import everything
				return true;
			}

			public boolean importData(JComponent src, Transferable transferable) {
				DataFlavor[] flavors = transferable.getTransferDataFlavors();

				int successful = 0;

				for (int i = 0; i < flavors.length; i++) {
					try {
						// System.out.println(flavors[i]);
						// System.out.println(transferable.getTransferData(flavors[i]));
						Object stuff = transferable.getTransferData(flavors[i]);
						if (!(stuff instanceof java.util.List))
							continue;
						java.util.List list = (java.util.List) stuff;

						for (int j = 0; j < list.size(); j++) {
							Object item = list.get(j);
							if (item instanceof File) {
								File file = (File) item;

								// see if this is a .gcode file to be opened
								String filename = file.getName();
								if (filename.endsWith(".gcode")) {
									String name = filename.substring(0,
											filename.length() - 6);
									File parent = file.getParentFile();
									if (name.equals(parent.getName())) {
										handleOpenFile(file);
										return true;
									}
								}

								if (sketch.addFile(file)) {
									successful++;
								}
							}
						}

					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
				}

				if (successful == 0) {
					error("No files were added to the sketch.");

				} else if (successful == 1) {
					message("One file added to the sketch.");

				} else {
					message(successful + " files added to the sketch.");
				}
				return true;
			}
		});
	}

	/**
	 * Hack for #@#)$(* Mac OS X 10.2. <p/> This appears to only be required on
	 * OS X 10.2, and is not even being called on later versions of OS X or
	 * Windows.
	 */
	public Dimension getMinimumSize() {
		// System.out.println("getting minimum size");
		return new Dimension(500, 550);
	}

	// ...................................................................

	/**
	 * Post-constructor setup for the editor area. Loads the last sketch that
	 * was used (if any), and restores other MainWindow settings. The complement to
	 * "storePreferences", this is called when the application is first
	 * launched.
	 */
	public void restorePreferences() {
		// figure out window placement

		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		boolean windowPositionValid = true;

		if (Base.preferences.getInt("last.screen.height",-1) != -1) {
			// if screen size has changed, the window coordinates no longer
			// make sense, so don't use them unless they're identical
			int screenW = Base.preferences.getInt("last.screen.width",600);
			int screenH = Base.preferences.getInt("last.screen.height",600);

			if ((screen.width != screenW) || (screen.height != screenH)) {
				windowPositionValid = false;
			}
			int windowX = Base.preferences.getInt("last.window.x",200);
			int windowY = Base.preferences.getInt("last.window.y",200);
			if ((windowX < 0) || (windowY < 0) || (windowX > screenW)
					|| (windowY > screenH)) {
				windowPositionValid = false;
			}

		} else {
			windowPositionValid = false;
		}

		if (!windowPositionValid) {
			// System.out.println("using default size");
			int windowH = Base.preferences.getInt("default.window.height",500);
			int windowW = Base.preferences.getInt("default.window.width",600);
			setBounds((screen.width - windowW) / 2,
					(screen.height - windowH) / 2, windowW, windowH);
			// this will be invalid as well, so grab the new value
			Base.preferences.putInt("last.divider.location", splitPane
					.getDividerLocation());
		} else {
			setBounds(Base.preferences.getInt("last.window.x",200), 
					Base.preferences.getInt("last.window.y",200), 
					Base.preferences.getInt("last.window.width",500), 
					Base.preferences.getInt("last.window.height",600));
		}

		// last sketch that was in use, or used to launch the app
		if (Base.openedAtStartup != null) {
			handleOpen2(Base.openedAtStartup);
		} else {
			String sketchPath = Base.preferences.get("last.sketch.path",null);
			// Sketch sketchTemp = new Sketch(sketchPath);

			if ((sketchPath != null) && (new File(sketchPath)).exists()) {
				// don't check modified because nothing is open yet
				handleOpen2(sketchPath);
			} else {
				handleNew2(true);
			}
		}

		// location for the console/editor area divider
		int location = Base.preferences.getInt("last.divider.location",-1);
		if (location != -1) {
			splitPane.setDividerLocation(location);
		} else {
			splitPane.setDividerLocation(0.7);
		}
		
		// read the preferences that are settable in the preferences window
		applyPreferences();
	}

	/**
	 * Read and apply new values from the preferences, either because the app is
	 * just starting up, or the user just finished messing with things in the
	 * Preferences window.
	 */
	public void applyPreferences() {

		// apply the setting for 'use external editor'
		boolean external = Base.preferences.getBoolean("editor.external",false);

		textarea.setEditable(!external);
		saveMenuItem.setEnabled(!external);
		saveAsMenuItem.setEnabled(!external);
		// beautifyMenuItem.setEnabled(!external);

		TextAreaPainter painter = textarea.getPainter();
		if (external) {
			// disable line highlight and turn off the caret when disabling
			Color color = Base.getColorPref("editor.external.bgcolor","#168299");
			painter.setBackground(color);
			painter.setLineHighlightEnabled(false);
			textarea.setCaretVisible(false);

		} else {
			Color color = Base.getColorPref("editor.bgcolor","#ffffff");
			painter.setBackground(color);
			boolean highlight = Base.preferences.getBoolean("editor.linehighlight",true);
			painter.setLineHighlightEnabled(highlight);
			textarea.setCaretVisible(true);
		}

		// apply changes to the font size for the editor
		// TextAreaPainter painter = textarea.getPainter();
		painter.setFont(Base.getFontPref("editor.font","Monospaced,plain,12"));
		// Font font = painter.getFont();
		// textarea.getPainter().setFont(new Font("Courier", Font.PLAIN, 36));

		// in case moved to a new location
		// For 0125, changing to async version (to be implemented later)
		// sketchbook.rebuildMenus();
		sketchbook.rebuildMenusAsync();
	}

	/**
	 * Store preferences about the editor's current state. Called when the
	 * application is quitting.
	 */
	public void storePreferences() {
		// System.out.println("storing preferences");

		// window location information
		Rectangle bounds = getBounds();
		Base.preferences.putInt("last.window.x", bounds.x);
		Base.preferences.putInt("last.window.y", bounds.y);
		Base.preferences.putInt("last.window.width", bounds.width);
		Base.preferences.putInt("last.window.height", bounds.height);

		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Base.preferences.putInt("last.screen.width", screen.width);
		Base.preferences.putInt("last.screen.height", screen.height);

		// last sketch that was in use
		// Preferences.set("last.sketch.name", sketchName);
		// Preferences.set("last.sketch.name", sketch.name);
		if (sketch != null) {
			Base.preferences.put("last.sketch.path", sketch.getMainFilePath());
		}

		// location for the console/editor area divider
		int location = splitPane.getDividerLocation();
		Base.preferences.putInt("last.divider.location", location);

		saveMRUPrefs();
		try {
			Base.preferences.flush();
		} catch (BackingStoreException bse) {
			// Not much we can do about this, so let it go with a stack
			// trace.
			bse.printStackTrace();
		}
	}

	private void saveMRUPrefs() {
		StringBuffer sb = new StringBuffer();
		for (String s : mruFiles) {
			if (sb.length() != 0)
				sb.append(",");
			sb.append(s);
		}
		Base.preferences.put(MRU_LIST_KEY, sb.toString());		
	}
	
	// ...................................................................

	private JMenu serialMenu = null;
	
	private void reloadSerialMenu() {
		if (serialMenu == null) return;
		serialMenu.removeAll();
		if (machine == null || !(machine.driver instanceof UsesSerial))  {
			JMenuItem item = new JMenuItem("Currently selected machine does not use a serial port.");
			item.setEnabled(false);
			serialMenu.add(item);
			return;
		}
		String currentName = null;
		UsesSerial us = (UsesSerial)machine.driver;
		if (us.getSerial() != null) {
			currentName = us.getSerial().getName();
		}
		Vector<Serial.Name> names = Serial.scanSerialNames();
		Collections.sort(names);
		for (Serial.Name name : names) {
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(name.getName());
			item.setEnabled(name.isAvailable());
			item.setSelected(name.getName().equals(currentName));
			final String portName = name.getName();
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						UsesSerial us = (UsesSerial)machine.driver;
						if (us.getSerial() == null ||
								us.getSerial().getName() != portName) {
							us.setSerial(new Serial(portName, us));
							Base.preferences.put("serial.last_selected", portName);
							machine.reset();
						}
					} catch (SerialException se) {
						se.printStackTrace();
					}
				}
			});
			serialMenu.add(item);
		}
		if (names.isEmpty()) {
			JMenuItem item = new JMenuItem("No serial ports detected");
			item.setEnabled(false);
			serialMenu.add(item);			
		}
		serialMenu.addSeparator();
		JMenuItem item = new JMenuItem("Rescan serial ports");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reloadSerialMenu();
			}
		});
		serialMenu.add(item);
	}
	
	private JMenu mruMenu = null;

	private void reloadMruMenu() {
		if (mruMenu == null) {
			return;
		}
		mruMenu.removeAll();
		class FileOpenActionListener implements ActionListener {
			public String path;

			FileOpenActionListener(String path) {
				this.path = path;
			}

			public void actionPerformed(ActionEvent e) {
				handleOpen(path);
			}
		}
		if (mruFiles != null && !mruFiles.isEmpty()) {
			int index = 0;
			for (String fileName : mruFiles) {
				String entry = Integer.toString(index) + ". "
						+ fileName.substring(fileName.lastIndexOf('/') + 1);
				JMenuItem item = new JMenuItem(entry, KeyEvent.VK_0 + index);
				item.addActionListener(new FileOpenActionListener(fileName));
				mruMenu.add(item);
				index = index + 1;
				if (index >= 9) {
					break;
				}
			}
		}
	}

	protected JMenu buildFileMenu() {
		JMenuItem item;
		JMenu menu = new JMenu("File");

		item = newJMenuItem("New", 'N');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleNew(false);
			}
		});
		menu.add(item);
		menu.add(sketchbook.getOpenMenu());

		item = newJMenuItem("Open...", 'O', false);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleOpen(null);
			}
		});
		menu.add(item);

		saveMenuItem = newJMenuItem("Save", 'S');
		saveMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleSave(false);
			}
		});
		menu.add(saveMenuItem);

		saveAsMenuItem = newJMenuItem("Save As...", 'S', true);
		saveAsMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleSaveAs();
			}
		});
		menu.add(saveAsMenuItem);

		menu.addSeparator();

		menu.addSeparator();
		mruMenu = new JMenu("Recent");
		reloadMruMenu();
		menu.add(mruMenu);

		// macosx already has its own preferences and quit menu
		if (!Base.isMacOS()) {
			menu.addSeparator();

			item = newJMenuItem("Preferences", ',');
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					handlePrefs();
				}
			});
			menu.add(item);

			menu.addSeparator();
			item = newJMenuItem("Quit", 'Q');
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					handleQuitInternal();
				}
			});
			menu.add(item);
		}
		return menu;
	}

	protected JMenu buildGCodeMenu() {
		JMenuItem item;
		JMenu menu = new JMenu("GCode");

		item = newJMenuItem("Estimate", 'E');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleEstimate();
			}
		});
		menu.add(item);

		item = newJMenuItem("Simulate", 'L');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleSimulate();
			}
		});
		menu.add(item);

		item = newJMenuItem("Build", 'B');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleBuild();
			}
		});
		menu.add(item);

		pauseItem = newJMenuItem("Pause", 'E');
		pauseItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handlePause();
			}
		});
		pauseItem.setEnabled(false);
		menu.add(pauseItem);

		stopItem = newJMenuItem("Stop", '.');
		stopItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleStop();
			}
		});
		stopItem.setEnabled(false);
		menu.add(stopItem);

		menu.addSeparator();

		// no way to do an 'open in file browser' on other platforms
		// since there isn't any sort of standard
		item = newJMenuItem("Show Sketch Folder", 'K', false);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Base.openFolder(sketch.folder);
			}
		});
		menu.add(item);

		if (!Base.openFolderAvailable()) {
			item.setEnabled(false);
		}

		return menu;
	}

	JMenuItem onboardParamsItem = new JMenuItem("Onboard Preferences");
	
	protected JMenu buildMachineMenu() {
		JMenuItem item;
		JMenu menu = new JMenu("Machine");

		machineMenu = new JMenu("Driver");
		populateMachineMenu();
		menu.add(machineMenu);

		menu.addMenuListener(new MenuListener() {
			public void menuCanceled(MenuEvent e) {
			}

			public void menuDeselected(MenuEvent e) {
			}

			public void menuSelected(MenuEvent e) {
				populateMachineMenu();
			}
		});

		machineMenuListener = new MachineMenuListener();

		serialMenu = new JMenu("Serial Port");
		reloadSerialMenu();
		menu.add(serialMenu);
		
		item = new JCheckBoxMenuItem("Enable autoscan",
					     Base.preferences.getBoolean("autoscan",true));
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    JCheckBoxMenuItem box = 
				(JCheckBoxMenuItem)e.getSource();
			    Base.preferences.putBoolean("autoscan",box.getState());
			}
		    });
		menu.add(item);

		item = new JMenuItem("Control Panel", 'C');
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J,ActionEvent.CTRL_MASK));
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleControlPanel();
			}
		});
		menu.add(item);
		
		onboardParamsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				handleOnboardPrefs();
			}
		});
		onboardParamsItem.setVisible(false);
		menu.add(onboardParamsItem);
		
		return menu;
	}

	class MachineMenuListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (machineMenu == null) {
				System.out.println("machineMenu is null");
				return;
			}

			int count = machineMenu.getItemCount();
			for (int i = 0; i < count; i++) {
				((JCheckBoxMenuItem) machineMenu.getItem(i)).setState(false);
			}

			JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
			item.setState(true);
			String name = item.getText();
			Base.preferences.put("machine.name", name);

			// load it and set it.

			loadMachine(name);
		}
	}

	protected void populateMachineMenu() {
		machineMenu.removeAll();
		boolean empty = true;

		try {
			for (String name : MachineFactory.getMachineNames() ) {
				JMenuItem rbMenuItem = new JCheckBoxMenuItem(name,
						name.equals(Base.preferences
								.get("machine.name",null)));
				rbMenuItem.addActionListener(machineMenuListener);
				machineMenu.add(rbMenuItem);
				empty = false;
			}
			if (!empty) {
				// System.out.println("enabling the machineMenu");
				machineMenu.setEnabled(true);
			}
		} catch (Exception exception) {
			System.out.println("error retrieving machine list");
			exception.printStackTrace();
		}

		if (machineMenu.getItemCount() == 0)
			machineMenu.setEnabled(false);
	}

	protected JMenu buildHelpMenu() {
		JMenu menu = new JMenu("Help");
		JMenuItem item;

		if (!Base.isLinux()) {
			item = newJMenuItem("Getting Started", '1');
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Base.openURL("http://www.replicat.org/getting-started");
				}
			});
			menu.add(item);
		}

		item = newJMenuItem("Hardware Setup", '2');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Base.openURL("http://www.replicat.org/hardware");
			}
		});
		menu.add(item);

		item = newJMenuItem("Troubleshooting", '3');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Base.openURL("http://www.replicat.org/troubleshooting");
			}
		});
		menu.add(item);

		item = newJMenuItem("Reference", '4');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Base.openURL("http://www.replicat.org/reference");
			}
		});
		menu.add(item);

		item = newJMenuItem("Frequently Asked Questions", '5');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Base.openURL("http://www.replicat.org/faq");
			}
		});
		menu.add(item);

		item = newJMenuItem("Visit Replicat.orG", '6');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Base.openURL("http://www.replicat.org/");
			}
		});
		menu.add(item);

		// macosx already has its own about menu
		if (!Base.isMacOS()) {
			menu.addSeparator();
			item = new JMenuItem("About ReplicatorG");
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					handleAbout();
				}
			});
			menu.add(item);
		}

		return menu;
	}

	public JMenu buildEditMenu() {
		JMenu menu = new JMenu("Edit");
		JMenuItem item;

		undoItem = newJMenuItem("Undo", 'Z');
		undoItem.addActionListener(undoAction = new UndoAction());
		menu.add(undoItem);

		redoItem = newJMenuItem("Redo", 'Y');
		redoItem.addActionListener(redoAction = new RedoAction());
		menu.add(redoItem);

		menu.addSeparator();

		// TODO "cut" and "copy" should really only be enabled
		// if some text is currently selected
		item = newJMenuItem("Cut", 'X');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textarea.cut();
				sketch.setModified(true);
			}
		});
		menu.add(item);

		item = newJMenuItem("Copy", 'C');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textarea.copy();
			}
		});
		menu.add(item);

		item = newJMenuItem("Paste", 'V');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textarea.paste();
				sketch.setModified(true);
			}
		});
		menu.add(item);

		item = newJMenuItem("Select All", 'A');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textarea.selectAll();
			}
		});
		menu.add(item);

		menu.addSeparator();

		item = newJMenuItem("Find...", 'F');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (find == null) {
					find = new FindReplace(MainWindow.this);
				}
				// new FindReplace(MainWindow.this).setVisible(true);
				find.setVisible(true);
				// find.setVisible(true);
			}
		});
		menu.add(item);

		// TODO find next should only be enabled after a
		// search has actually taken place
		item = newJMenuItem("Find Next", 'G');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (find != null) {
					// find.find(true);
					// FindReplace find = new FindReplace(MainWindow.this);
					// //.setVisible(true);
					find.find(true);
				}
			}
		});
		menu.add(item);

		return menu;
	}

	/**
	 * Convenience method, see below.
	 */
	static public JMenuItem newJMenuItem(String title, int what) {
		return newJMenuItem(title, what, false);
	}

	/**
	 * A software engineer, somewhere, needs to have his abstraction taken away.
	 * In some countries they jail or beat people for writing the sort of API
	 * that would require a five line helper function just to set the command
	 * key for a menu item.
	 */
	static public JMenuItem newJMenuItem(String title, int what, boolean shift) {
		JMenuItem menuItem = new JMenuItem(title);
		int modifiers = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		if (shift)
			modifiers |= ActionEvent.SHIFT_MASK;
		menuItem.setAccelerator(KeyStroke.getKeyStroke(what, modifiers));
		return menuItem;
	}

	// ...................................................................

	class UndoAction extends AbstractAction {
		public UndoAction() {
			super("Undo");
			this.setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				undo.undo();
			} catch (CannotUndoException ex) {
				// System.out.println("Unable to undo: " + ex);
				// ex.printStackTrace();
			}
			updateUndoState();
			redoAction.updateRedoState();
		}

		protected void updateUndoState() {
			if (undo.canUndo()) {
				this.setEnabled(true);
				undoItem.setEnabled(true);
				undoItem.setText(undo.getUndoPresentationName());
				putValue(Action.NAME, undo.getUndoPresentationName());
				if (sketch != null) {
					sketch.setModified(true); // 0107
				}
			} else {
				this.setEnabled(false);
				undoItem.setEnabled(false);
				undoItem.setText("Undo");
				putValue(Action.NAME, "Undo");
				if (sketch != null) {
					sketch.setModified(false); // 0107
				}
			}
		}
	}

	class RedoAction extends AbstractAction {
		public RedoAction() {
			super("Redo");
			this.setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				undo.redo();
			} catch (CannotRedoException ex) {
				// System.out.println("Unable to redo: " + ex);
				// ex.printStackTrace();
			}
			updateRedoState();
			undoAction.updateUndoState();
		}

		protected void updateRedoState() {
			if (undo.canRedo()) {
				redoItem.setEnabled(true);
				redoItem.setText(undo.getRedoPresentationName());
				putValue(Action.NAME, undo.getRedoPresentationName());
			} else {
				this.setEnabled(false);
				redoItem.setEnabled(false);
				redoItem.setText("Redo");
				putValue(Action.NAME, "Redo");
			}
		}
	}

	// ...................................................................

	// interfaces for MRJ Handlers, but naming is fine
	// so used internally for everything else

	public void handleAbout() {
		final Image image = Base.getImage("images/about.jpg", this);
		int w = image.getWidth(this);
		int h = image.getHeight(this);
		final Window window = new Window(this) {
			public void paint(Graphics g) {
				g.drawImage(image, 0, 0, null);

				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
						RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

				g.setFont(new Font("SansSerif", Font.PLAIN, 13));
				g.setColor(Color.black);
				g.drawString(Base.VERSION_NAME, 166, 85);
			}
		};
		window.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				window.dispose();
			}
		});
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		window.setBounds((screen.width - w) / 2, (screen.height - h) / 2, w, h);
		window.setVisible(true);
	}

	public void handleControlPanel() {
		if (machine == null) {
			JOptionPane.showMessageDialog(
					this,
					"ReplicatorG can't connect to your machine.\nTry checking your settings and resetting your machine.",
					"Can't find machine", JOptionPane.ERROR_MESSAGE);
		} else {
			ControlPanelWindow window = new ControlPanelWindow(machine);
			window.setVisible(true);
		}
	}

	public void handleOnboardPrefs() {
		if (machine == null || 
				!(machine.driver instanceof OnboardParameters)) {
			JOptionPane.showMessageDialog(
					this,
					"ReplicatorG can't connect to your machine or onboard preferences are not supported..\nTry checking your settings and resetting your machine.",
					"Can't run onboard prefs", JOptionPane.ERROR_MESSAGE);
		} else {
			MachineOnboardParameters moo = new MachineOnboardParameters((OnboardParameters)machine.driver);
			moo.setVisible(true);
		}
	}
	
	/**
	 * Show the preferences window.
	 */
	public void handlePrefs() {
		PreferencesWindow preferences = new PreferencesWindow();
		preferences.showFrame(this);
	}

	// ...................................................................

	/**
	 * Get the contents of the current buffer. Used by the Sketch class.
	 */
	public String getText() {
		return textarea.getText();
	}

	/**
	 * Called to update the text but not switch to a different set of code
	 * (which would affect the undo manager).
	 */
	public void setText(String what, int selectionStart, int selectionEnd) {
		beginCompoundEdit();
		textarea.setText(what);
		endCompoundEdit();

		// make sure that a tool isn't asking for a bad location
		selectionStart = Math.max(0, Math.min(selectionStart, textarea
				.getDocumentLength()));
		selectionEnd = Math.max(0, Math.min(selectionStart, textarea
				.getDocumentLength()));
		textarea.select(selectionStart, selectionEnd);

		textarea.requestFocus(); // get the caret blinking
	}

	/**
	 * Switch between tabs, this swaps out the Document object that's currently
	 * being manipulated.
	 */
	public void setCode(SketchCode code) {
		if (code.document == null) { // this document not yet inited
			code.document = new SyntaxDocument();

			// turn on syntax highlighting
			code.document.setTokenMarker(new PdeKeywords());

			// insert the program text into the document object
			try {
				code.document.insertString(0, code.program, null);
			} catch (BadLocationException bl) {
				bl.printStackTrace();
			}

			// set up this guy's own undo manager
			code.undo = new UndoManager();

			// connect the undo listener to the editor
			code.document.addUndoableEditListener(new UndoableEditListener() {
				public void undoableEditHappened(UndoableEditEvent e) {
					if (compoundEdit != null) {
						compoundEdit.addEdit(e.getEdit());

					} else if (undo != null) {
						undo.addEdit(e.getEdit());
						undoAction.updateUndoState();
						redoAction.updateRedoState();
					}
				}
			});
		}

		// update the document object that's in use
		textarea.setDocument(code.document, code.selectionStart,
				code.selectionStop, code.scrollPosition);

		textarea.requestFocus(); // get the caret blinking

		this.undo = code.undo;
		undoAction.updateUndoState();
		redoAction.updateRedoState();
	}

	public void beginCompoundEdit() {
		compoundEdit = new CompoundEdit();
	}

	public void endCompoundEdit() {
		compoundEdit.end();
		undo.addEdit(compoundEdit);
		undoAction.updateUndoState();
		redoAction.updateRedoState();
		compoundEdit = null;
	}

	// ...................................................................

	public void handleEstimate() {
		if (building)
			return;
		if (simulating)
			return;

		// load our simulator machine
		loadSimulator();

		// fire off our thread.
		estimationThread = new EstimationThread(this);
		estimationThread.start();
	}

	public void handleSimulate() {
		if (building)
			return;
		if (simulating)
			return;

		// close stuff.
		doClose();

		// buttons/status.
		simulating = true;
		buttons.activate(MainButtonPanel.SIMULATE);

		// load our simulator machine
		loadSimulator();
		setEditorBusy(true);

		// fire off our thread.
		simulationThread = new SimulationThread(this);
		simulationThread.start();
	}

	// synchronized public void simulationOver()
	public void simulationOver() {
		message("Done simulating.");
		simulating = false;
		setEditorBusy(false);
	}

	// synchronized public void handleBuild()
	public void handleBuild() {
		if (building)
			return;
		if (simulating)
			return;

		if (machine == null) {
			System.err.println("Not ready to build yet.");
		} else {
			// close stuff.
			doClose();

			// build specific stuff
			building = true;
			buttons.activate(MainButtonPanel.BUILD);

			setEditorBusy(true);

			// start our building thread.

			message("Building...");
			buildStart = new Date();
			machine.execute();
		}
	}

	private Date buildStart = null;
	
	public void machineStateChanged(MachineStateChangeEvent evt) {
		if (building) {
			if (evt.getState() == MachineState.READY ||
				evt.getState() == MachineState.STOPPING) {
				final MachineState endState = evt.getState();
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                    	if (endState == MachineState.READY) {
                    		notifyBuildComplete(buildStart, new Date());
                    	} else {
                    		notifyBuildAborted(buildStart, new Date());
                    	}
                        buildingOver();
                    }
                });
			}
		}
		if (evt.getState() == MachineState.READY) {
			reloadSerialMenu();
		}
		onboardParamsItem.setVisible(machine.getDriver() instanceof OnboardParameters &&
				((OnboardParameters)machine.getDriver()).hasFeatureOnboardParameters());
	}

	public void setEditorBusy(boolean isBusy) {
		// variables and stuff.
		stopItem.setEnabled(isBusy);
		pauseItem.setEnabled(isBusy);

		// clear the console on each build, unless the user doesn't want to
		if (isBusy && Base.preferences.getBoolean("console.auto_clear",true)) {
			console.clear();
		}

		// prepare editor window.
		setVisible(true);
		textarea.setEnabled(!isBusy);
		textarea.setEditable(!isBusy);
		if (isBusy) {
			textarea.selectNone();
			textarea.scrollTo(0, 0);
		} else {
			buttons.clear();
		}
	}


	/**
	 * give a prompt and stuff about the build being done with elapsed time,
	 * etc.
	 */
	private void notifyBuildComplete(Date started, Date finished) {
		assert started != null;
		assert finished != null;

		long elapsed = finished.getTime() - started.getTime();

		String message = "Build finished.\n\n";
		message += "Completed in "
				+ EstimationDriver.getBuildTimeString(elapsed);

		Base.showMessage("Build finished", message);
	}

	private void notifyBuildAborted(Date started, Date aborted) {
		assert started != null;
		assert aborted != null;

		long elapsed = aborted.getTime() - started.getTime();

		String message = "Build aborted.\n\n";
		message += "Stopped after "
				+ EstimationDriver.getBuildTimeString(elapsed);

		Base.showMessage("Build aborted", message);
	}

	// synchronized public void buildingOver()
	public void buildingOver() {
		message("Done building.");

		// re-enable the gui and shit.
		textarea.setEnabled(true);

		building = false;
		if (machine.getSimulatorDriver() != null)
			machine.getSimulatorDriver().destroyWindow();
		setEditorBusy(false);
	}

	class SimulationThread extends Thread {
		MainWindow editor;

		public SimulationThread(MainWindow edit) {
			super("Simulation Thread");

			editor = edit;
		}

		public void run() {
			message("Simulating...");
			machine.execute();
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					simulationOver();
				}
			});
		}
	}

	public void handleStop() {
		// if (building || simulating) // can also be called during panel ops
		// called by menu or buttons
		doStop();
		setEditorBusy(false);
	}

	class EstimationThread extends Thread {
		MainWindow editor;

		public EstimationThread(MainWindow edit) {
			super("Estimation Thread");

			editor = edit;
		}

		public void run() {
			message("Estimating...");
			machine.estimate();
			editor.estimationOver();
		}
	}

	public void estimationOver() {
		// stopItem.setEnabled(false);
		// pauseItem.setEnabled(false);
		buttons.clear();
	}

	/**
	 * Stop the applet but don't kill its window.
	 */
	public void doStop() {
		if (machine != null) {
			machine.stop();
		}

		message(EMPTY);

		buttons.clear();

		building = false;
		simulating = false;
	}

	public void handlePause() {
		// called by menu or buttons
		// if (building || simulating) // can also be used during control panel
		// ops
		doPause();
	}

	/**
	 * Pause the applet but don't kill its window.
	 */
	public void doPause() {
		if (machine.isPaused()) {
			machine.unpause();

			if (simulating) {
				buttons.activate(MainButtonPanel.SIMULATE);
				message("Simulating...");
			} else if (building) {
				buttons.activate(MainButtonPanel.BUILD);
				message("Building...");
			}

			buttons.inactivate(MainButtonPanel.PAUSE);
		} else {
			message("Paused.");
			machine.pause();

			buttons.clear();
			buttons.activate(MainButtonPanel.PAUSE);
		}
	}

	/**
	 * Stop the applet and kill its window. When running in presentation mode,
	 * this will always be called instead of doStop().
	 */
	public void doClose() {

		doStop(); // need to stop if runtime error
		if (sketch != null) {
			sketch.cleanup();
		}

		// focus the GCode again after quitting presentation mode
		toFront();
	}

	/**
	 * Check to see if there have been changes. If so, prompt user whether or
	 * not to save first. If the user cancels, just ignore. Otherwise, one of
	 * the other methods will handle calling checkModified2() which will get on
	 * with business.
	 */
	protected void checkModified(int checkModifiedMode) {
		this.checkModifiedMode = checkModifiedMode;

		if (sketch == null || !sketch.modified) {
			checkModified2();
			return;
		}

		String prompt = "Save changes to " + sketch.name + "?  ";

		if (!Base.isMacOS() || Base.javaVersion < 1.5f) {
			int result = JOptionPane.showConfirmDialog(this, prompt,
					"Quit", JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);

			if (result == JOptionPane.YES_OPTION) {
				handleSave(true);
				checkModified2();

			} else if (result == JOptionPane.NO_OPTION) {
				checkModified2();
			}
			// cancel is ignored altogether

		} else {
			// This code is disabled unless Java 1.5 is being used on Mac OS
			// X
			// because of a Java bug that prevents the initial value of the
			// dialog from being set properly (at least on my MacBook Pro).
			// The bug causes the "Don't Save" option to be the highlighted,
			// blinking, default. This sucks. But I'll tell you what doesn't
			// suck--workarounds for the Mac and Apple's snobby attitude
			// about it!

			// adapted from the quaqua guide
			// http://www.randelshofer.ch/quaqua/guide/joptionpane.html
			JOptionPane pane = new JOptionPane("<html> "
					+ "<head> <style type=\"text/css\">"
					+ "b { font: 13pt \"Lucida Grande\" }"
					+ "p { font: 11pt \"Lucida Grande\"; margin-top: 8px }"
					+ "</style> </head>"
					+ "<b>Do you want to save changes to this sketch<BR>"
					+ " before closing?</b>"
					+ "<p>If you don't save, your changes will be lost.",
					JOptionPane.QUESTION_MESSAGE);

			String[] options = new String[] { "Save", "Cancel",
			"Don't Save" };
			pane.setOptions(options);

			// highlight the safest option ala apple hig
			pane.setInitialValue(options[0]);

			// on macosx, setting the destructive property places this
			// option
			// away from the others at the lefthand side
			pane.putClientProperty("Quaqua.OptionPane.destructiveOption",
					new Integer(2));

			JDialog dialog = pane.createDialog(this, null);
			dialog.setVisible(true);

			Object result = pane.getValue();
			if (result == options[0]) { // save (and quit)
				handleSave(true);
				checkModified2();

			} else if (result == options[2]) { // don't save (still quit)
				checkModified2();
			}
		}
	}

	/**
	 * Called by EditorStatus to complete the job and re-dispatch to handleNew,
	 * handleOpen, handleQuit.
	 */
	public void checkModified2() {
		switch (checkModifiedMode) {
		case HANDLE_NEW:
			handleNew2(false);
			break;
		case HANDLE_OPEN:
			handleOpen2(handleOpenPath);
			break;
		case HANDLE_QUIT:
			handleQuit2();
			break;
		}
		checkModifiedMode = 0;
	}

	/**
	 * New was called (by buttons or by menu), first check modified and if
	 * things work out ok, handleNew2() will be called. <p/> If shift is pressed
	 * when clicking the toolbar button, then force the opposite behavior from
	 * sketchbook.prompt's setting
	 */
	public void handleNew(final boolean shift) {
		buttons.activate(MainButtonPanel.NEW);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				doStop();
				handleNewShift = shift;
				checkModified(HANDLE_NEW);
			}
		});
	}

	/**
	 * Extra public method so that Sketch can call this when a sketch is
	 * selected to be deleted, and it won't call checkModified() to prompt for
	 * save as.
	 */
	public void handleNewUnchecked() {
		doStop();
		handleNewShift = false;
		handleNew2(true);
	}

	/**
	 * Does all the plumbing to create a new project then calls handleOpen to
	 * load it up.
	 * 
	 * @param noPrompt
	 *            true to disable prompting for the sketch name, used when the
	 *            app is starting (auto-create a sketch)
	 */
	protected void handleNew2(boolean noPrompt) {
		try {
			String pdePath = sketchbook.handleNew(noPrompt, handleNewShift);
			if (pdePath != null)
				handleOpen2(pdePath);

		} catch (IOException e) {
			// not sure why this would happen, but since there's no way to
			// recover (outside of creating another new setkch, which might
			// just cause more trouble), then they've gotta quit.
			Base.showError("Problem creating a new sketch",
					"An error occurred while creating\n"
							+ "a new sketch. ReplicatorG must now quit.", e);
		}
		buttons.clear();
	}

	/**
	 * This is the implementation of the MRJ open document event, and the
	 * Windows XP open document will be routed through this too.
	 */
	public void handleOpenFile(File file) {
		// System.out.println("handling open file: " + file);
		handleOpen(file.getAbsolutePath());
	}

	/**
	 * Open a sketch given the full path to the .gcode file. Pass in 'null' to
	 * prompt the user for the name of the sketch.
	 */
	public void handleOpen(final String ipath) {
		// haven't run across a case where i can verify that this works
		// because open is usually very fast.
		buttons.activate(MainButtonPanel.OPEN);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				String path = ipath;
				if (path == null) { // "open..." selected from the menu
					path = sketchbook.handleOpen();
					if (path == null)
						return;
				}
				doClose();
				handleOpenPath = path;
				checkModified(HANDLE_OPEN);
			}
		});
	}

	/**
	 * Open a sketch from a particular path, but don't check to save changes.
	 * Used by Sketch.saveAs() to re-open a sketch after the "Save As"
	 */
	public void handleOpenUnchecked(String path, int codeIndex, int selStart,
			int selStop, int scrollPos) {
		doClose();
		handleOpen2(path);

		sketch.setCurrent(codeIndex);
		textarea.select(selStart, selStop);
		// textarea.updateScrollBars();
		textarea.setScrollPosition(scrollPos);
	}

	/**
	 * Second stage of open, occurs after having checked to see if the
	 * modifications (if any) to the previous sketch need to be saved.
	 */
	protected void handleOpen2(String path) {
		if (sketch != null) {
			// if leaving an empty sketch (i.e. the default) do an
			// auto-clean right away
			try {
				// don't clean if we're re-opening the same file
				String oldPath = sketch.code[0].file.getCanonicalPath();
				String newPath = new File(path).getCanonicalPath();
				if (!oldPath.equals(newPath)) {
					if (Base.calcFolderSize(sketch.folder) == 0) {
						Base.removeDir(sketch.folder);
						// sketchbook.rebuildMenus();
						sketchbook.rebuildMenusAsync();
					}
				}
			} catch (Exception e) {
			} // oh well
		}

		try {
			// check to make sure that this .gcode file is
			// in a folder of the same name
			File file = new File(path);
			File parentFile = new File(file.getParent());
			String parentName = parentFile.getName();
			String pdeName = parentName + ".gcode";
			File altFile = new File(file.getParent(), pdeName);

			// System.out.println("path = " + file.getParent());
			// System.out.println("name = " + file.getName());
			// System.out.println("pname = " + parentName);

			if (pdeName.equals(file.getName())) {
				// no beef with this guy

			} else if (altFile.exists()) {
				// but open the .gcode instead
				path = altFile.getAbsolutePath();
				// System.out.println("found alt file in same folder");

			} else if (!path.endsWith(".gcode")) {
				Base.showWarning("Bad file selected",
						"ReplicatorG can only open its own sketches\n"
								+ "and other files ending in .gcode", null);
				throw new Exception();
				// return;

			} else {
				String properParent = file.getName().substring(0,
						file.getName().length() - 6);

				Object[] options = { "OK", "Cancel" };
				String prompt = "The file \"" + file.getName()
						+ "\" needs to be inside\n"
						+ "a sketch folder named \"" + properParent + "\".\n"
						+ "Create this folder, move the file, and continue?";

				int result = JOptionPane
						.showOptionDialog(this, prompt, "Moving",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options,
								options[0]);

				if (result == JOptionPane.YES_OPTION) {
					// create properly named folder
					File properFolder = new File(file.getParent(), properParent);
					if (properFolder.exists()) {
						Base.showWarning("Error", "A folder named \""
								+ properParent + "\" "
								+ "already exists. Can't open sketch.", null);
						return;
					}
					if (!properFolder.mkdirs()) {
						throw new IOException("Couldn't create sketch folder");
					}
					// copy the sketch inside
					File properPdeFile = new File(properFolder, file.getName());
					File origPdeFile = new File(path);
					Base.copyFile(origPdeFile, properPdeFile);

					// remove the original file, so user doesn't get confused
					origPdeFile.delete();

					// update with the new path
					path = properPdeFile.getAbsolutePath();

				} else if (result == JOptionPane.NO_OPTION) {
					return;
				}
			}

			sketch = new Sketch(this, path);
			handleOpenPath = path;
			addMRUEntry(path);
			reloadMruMenu();
			storePreferences(); // Updates MRU list, in case program aborts
			// before quit
			header.rebuild();
			if (Base.preferences.getBoolean("console.auto_clear",true)) {
				console.clear();
			}

		} catch (Exception e) {
			error(e);
		}
	}

	/**
	 * Add a path to the MRU list
	 */
	void addMRUEntry(String path) {
		File f = new File(path);
		String absPath = f.getAbsolutePath();
		mruFiles.remove(absPath);
		mruFiles.add(0,absPath);
		saveMRUPrefs();
	}

	// there is no handleSave1 since there's never a need to prompt
	/**
	 * Actually handle the save command. If 'force' is set to false, this will
	 * happen in another thread so that the message area will update and the
	 * save button will stay highlighted while the save is happening. If 'force'
	 * is true, then it will happen immediately. This is used during a quit,
	 * because invokeLater() won't run properly while a quit is happening. This
	 * fixes <A HREF="http://dev.processing.org/bugs/show_bug.cgi?id=276">Bug
	 * 276</A>.
	 */
	public void handleSave(boolean force) {
		doStop();
		buttons.activate(MainButtonPanel.SAVE);

		if (force) {
			handleSave2();
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					handleSave2();
				}
			});
		}
	}

	public void handleSave2() {
		message("Saving...");
		try {
			if (sketch.save()) {
				message("Done Saving.");
			} else {
				message(EMPTY);
			}
			// rebuild sketch menu in case a save-as was forced
			// Disabling this for 0125, instead rebuild the menu inside
			// the Save As method of the Sketch object, since that's the
			// only one who knows whether something was renamed.
			// sketchbook.rebuildMenus();
			// sketchbook.rebuildMenusAsync();

		} catch (Exception e) {
			// show the error as a message in the window
			error(e);

			// zero out the current action,
			// so that checkModified2 will just do nothing
			checkModifiedMode = 0;
			// this is used when another operation calls a save
		}
		buttons.clear();
	}

	public void handleSaveAs() {
		doStop();
		buttons.activate(MainButtonPanel.SAVE);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				message("Saving...");
				try {
					if (sketch.saveAs()) {
						message("Done Saving.");
						// Disabling this for 0125, instead rebuild the menu
						// inside
						// the Save As method of the Sketch object, since that's
						// the
						// only one who knows whether something was renamed.
						// sketchbook.rebuildMenusAsync();
					} else {
						message("Save Cancelled.");
					}
				} catch (Exception e) {
					// show the error as a message in the window
					error(e);
				}
				buttons.clear();
			}
		});
	}

	/**
	 * Quit, but first ask user if it's ok. Also store preferences to disk just
	 * in case they want to quit. Final exit() happens in MainWindow since it has
	 * the callback from EditorStatus.
	 */
	public void handleQuitInternal() {

		try {
			if (simulationThread != null) {
				simulationThread.interrupt();
				simulationThread.join();
			}
			if (estimationThread != null) {
				estimationThread.interrupt();
				estimationThread.join();
			}
		} catch (InterruptedException e) {
			assert (false);
		}

		// doStop() isn't sufficient with external vm & quit
		// instead use doClose() which will kill the external vm
		doClose();

		// cleanup our machine/driver.
		if (machine != null) {
			if (machine.getDriver() != null)
				machine.getDriver().dispose();
			if (machine.getSimulatorDriver() != null)
				machine.getSimulatorDriver().dispose();
		}

		checkModified(HANDLE_QUIT);
	}

	/**
	 * Method for the MRJQuitHandler, needs to be dealt with differently than
	 * the regular handler because OS X has an annoying implementation <A
	 * HREF="http://developer.apple.com/qa/qa2001/qa1187.html">quirk</A> that
	 * requires an exception to be thrown in order to properly cancel a quit
	 * message.
	 */
	public void handleQuit() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				handleQuitInternal();
			}
		});

		// Throw IllegalStateException so new thread can execute.
		// If showing dialog on this thread in 10.2, we would throw
		// upon JOptionPane.NO_OPTION
		throw new IllegalStateException("Quit Pending User Confirmation");
	}

	/**
	 * Actually do the quit action.
	 */
	protected void handleQuit2() {

		storePreferences();

		sketchbook.clean();
		console.handleQuit();

		// System.out.println("exiting here");
		System.exit(0);
	}

	protected void handleReference() {
		String text = textarea.getSelectedText().trim();

		if (text.length() == 0) {
			message("First select a word to find in the reference.");

		} else {
			String referenceFile = PdeKeywords.getReference(text);
			// System.out.println("reference file is " + referenceFile);
			if (referenceFile == null) {
				message("No reference available for \"" + text + "\"");
			} else {
				Base.showReference(referenceFile + ".html");
			}
		}
	}

	public void highlightLine(int lnum) {
		if (lnum < 0) {
			textarea.select(0, 0);
			return;
		}
		// System.out.println(lnum);
		String s = textarea.getText();
		int len = s.length();
		int st = -1;
		int ii = 0;
		int end = -1;
		int lc = 0;
		if (lnum == 0)
			st = 0;
		for (int i = 0; i < len; i++) {
			ii++;
			// if ((s.charAt(i) == '\n') || (s.charAt(i) == '\r')) {
			boolean newline = false;
			if (s.charAt(i) == '\r') {
				if ((i != len - 1) && (s.charAt(i + 1) == '\n')) {
					i++; // ii--;
				}
				lc++;
				newline = true;
			} else if (s.charAt(i) == '\n') {
				lc++;
				newline = true;
			}
			if (newline) {
				if (lc == lnum)
					st = ii;
				else if (lc == lnum + 1) {
					// end = ii;
					// to avoid selecting entire, because doing so puts the
					// cursor on the next line [0090]
					end = ii - 1;
					break;
				}
			}
		}
		if (end == -1)
			end = len;

		// sometimes KJC claims that the line it found an error in is
		// the last line in the file + 1. Just highlight the last line
		// in this case. [dmose]
		if (st == -1)
			st = len;

		textarea.select(st, end);
	}

	// ...................................................................

	/**
	 * Show an error int the status bar.
	 */
	public void error(String what) {
		System.err.println(what);
	}

	public void error(Exception e) {
		if (e == null) {
			System.err.println("MainWindow.error() was passed a null exception.");
			return;
		}

		// not sure if any RuntimeExceptions will actually arrive
		// through here, but gonna check for em just in case.
		String mess = e.getMessage();
		if (mess != null) {
			String rxString = "RuntimeException: ";
			if (mess.indexOf(rxString) == 0) {
				mess = mess.substring(rxString.length());
			}
			String javaLang = "java.lang.";
			if (mess.indexOf(javaLang) == 0) {
				mess = mess.substring(javaLang.length());
			}
			error(mess);
		}
		e.printStackTrace();
	}

	// synchronized public void message(String msg) {
	public void message(String msg) {
		System.out.println(msg);
		// System.out.println(msg);
	}

	// ...................................................................

	/**
	 * Returns the edit popup menu.
	 */
	class TextAreaPopup extends JPopupMenu {
		// String currentDir = System.getProperty("user.dir");
		String referenceFile = null;

		JMenuItem cutItem, copyItem;

		JMenuItem referenceItem;

		public TextAreaPopup() {
			JMenuItem item;

			cutItem = new JMenuItem("Cut");
			cutItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					textarea.cut();
					sketch.setModified(true);
				}
			});
			this.add(cutItem);

			copyItem = new JMenuItem("Copy");
			copyItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					textarea.copy();
				}
			});
			this.add(copyItem);

			item = new JMenuItem("Paste");
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					textarea.paste();
					sketch.setModified(true);
				}
			});
			this.add(item);

			item = new JMenuItem("Select All");
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					textarea.selectAll();
				}
			});
			this.add(item);

			this.addSeparator();

			referenceItem = new JMenuItem("Find in Reference");
			referenceItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// Base.showReference(referenceFile + ".html");
					handleReference(); // textarea.getSelectedText());
				}
			});
			this.add(referenceItem);
		}

		// if no text is selected, disable copy and cut menu items
		public void show(Component component, int x, int y) {
			if (textarea.isSelectionActive()) {
				cutItem.setEnabled(true);
				copyItem.setEnabled(true);

				String sel = textarea.getSelectedText().trim();
				referenceFile = PdeKeywords.getReference(sel);
				referenceItem.setEnabled(referenceFile != null);

			} else {
				cutItem.setEnabled(false);
				copyItem.setEnabled(false);
				referenceItem.setEnabled(false);
			}
			super.show(component, x, y);
		}
	}

	protected void setMachine(MachineController machine) {
		if (this.machine != null) {
			this.machine.dispose();
		}
		this.machine = machine;
		machine.setCodeSource(new JEditTextAreaSource(textarea));
		machineStatusPanel.setMachine(this.machine);
	}

	public void loadMachine(String name) {
		setMachine(Base.loadMachine(name));
		machine.addMachineStateListener(this);
		machine.addMachineStateListener(machineStatusPanel);
		machine.addMachineStateListener(buttons);
		reloadSerialMenu();
		if (machine.driver instanceof UsesSerial) {
			UsesSerial us = (UsesSerial)machine.driver;
			if (Base.preferences.getBoolean("serial.use_machines",true) &&
					us.isExplicit()) {
				try {
					us.setSerial(new Serial(us.getPortName(),us));
					machine.reset();
				} catch (SerialException e) {
					System.err.println("Could not use serial port specified in machines.xml ("+us.getPortName()+").");
					e.printStackTrace();
				}
			}
			else if (Base.preferences.getBoolean("autoscan",true)) {
				machine.autoscan();
			} else {
				String lastPort = Base.preferences.get("serial.last_selected", null);
				if (lastPort != null) {
					try {
						us.setSerial(new Serial(lastPort,us));
						machine.reset();
					} catch (SerialException e) {
						System.err.println("Could not use most recently selected serial port ("+lastPort+").");
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void loadSimulator() {
		setMachine(MachineFactory.loadSimulator());
	}

	public void machineProgress(MachineProgressEvent event) {
	}

	public void toolStatusChanged(MachineToolStatusEvent event) {
	}
}

