package view;

import jade.gui.GuiEvent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JToolTip;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import model.WorkFlow;
import agents.ViewAgent;

public class View extends JFrame implements PropertyChangeListener {

	private class FileSaver extends JFileChooser {
		public FileSaver() {
			super();
			super.setDialogType(SAVE_DIALOG);
		}

		@Override
		public File getSelectedFile() {
			FileFilter fileFilter = super.getFileFilter();
			File selectedFile = super.getSelectedFile();
			if ((fileFilter instanceof FileNameExtensionFilter)
					&& (selectedFile != null)) {
				FileNameExtensionFilter fileFilterExtension = (FileNameExtensionFilter) fileFilter;
				String name = selectedFile.getName();
				boolean foundExtension = false;
				for (String rawExtension : fileFilterExtension.getExtensions()) {
					String extension = "." + rawExtension;
					if (name.endsWith(extension)) {
						foundExtension = true;
						break;
					}
				}
				if (!foundExtension) {
					String firstExtension = "."
							+ fileFilterExtension.getExtensions()[0];
					selectedFile = new File(selectedFile.getParentFile(), name
							+ firstExtension);
				}
			}

			return selectedFile;
		}

		@Override
		public void approveSelection() {
			int dialogType = getDialogType();
			if (dialogType == SAVE_DIALOG) {
				File selectedFile = getSelectedFile();
				if ((selectedFile != null) && selectedFile.exists()) {
					int response = JOptionPane
							.showConfirmDialog(
									this,
									"The file "
											+ selectedFile.getName()
											+ " already exists. Do you want to replace the existing file?",
									"Overwrite file",
									JOptionPane.YES_NO_OPTION,
									JOptionPane.WARNING_MESSAGE);
					if (response != JOptionPane.YES_OPTION) {
						return;
					}
				}
			}

			super.approveSelection();
		}
	}

	private ViewAgent viewagent;
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	private final JFileChooser choose = new JFileChooser();
	private final FileSaver saver = new FileSaver();
	private final Export export = new Export();
	private final JMenuBar bar = new JMenuBar();
	private final JMenu fileMenu = new JMenu("File");
	private final JMenuItem help = new JMenu("Help");
	private final JMenuItem newFile = new JMenuItem("New");
	private final JMenuItem oppenFile = new JMenuItem("Open");
	private final JMenuItem save = new JMenuItem("Save");
	private final JMenuItem saveAs = new JMenuItem("Save as");
	private final JMenuItem exportMenu = new JMenuItem("Export in BPMN file");
	private final JMenuItem quit = new JMenuItem("Quit");
	private final StyleContext context = new StyleContext();
	private final StyledDocument document = new DefaultStyledDocument(context);
	private final JTextPane historic = new JTextPane(document);
	private final JScrollPane conversationContent = new JScrollPane(historic);
	private final GraphContainer graphContent = new GraphContainer();
	private final JPanel graph = new JPanel();
	private final JPanel pane = new JPanel(new GridBagLayout());
	private final JPanel conversation = new JPanel(new GridBagLayout());
	private final JButton micro = new JButton(new ImageIcon((((new ImageIcon(
			"doc/micro.png")).getImage()).getScaledInstance(45, 45,
			java.awt.Image.SCALE_SMOOTH))));
	private final JTextArea text = new JTextArea(3, 2);
	private final JLabel verifLabel = new JLabel(" Enter a command");
	private final JLabel status = new JLabel(" Status :");
	private final JScrollPane actions = new JScrollPane(text);
	boolean isMicroOn = false;
	private String fileName = null;
	private WorkFlow wf;
	private boolean hasBeenModified = false;
	private JDialog d2 = new JDialog(this);
	protected JScrollPane jsp;
	final Style right;

	public View(ViewAgent a) {
		viewagent = a;
		this.setVisible(true);
		this.setSize(1200, 655);
		this.setResizable(false);
		this.setLayout(new BorderLayout());
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (hasBeenModified) {
					int response = JOptionPane
							.showConfirmDialog(
									null,
									"The workflow has been modified, do you want to save it before closing?",
									"Closing",
									JOptionPane.YES_NO_CANCEL_OPTION,
									JOptionPane.WARNING_MESSAGE);
					switch (response) {
					case JOptionPane.YES_OPTION:
						save.doClick();
						break;
					case JOptionPane.NO_OPTION:
						break;
					case JOptionPane.CANCEL_OPTION:

						return;
					}
				}
				System.exit(0);
			}
		});

		Style style = context.getStyle(StyleContext.DEFAULT_STYLE);
		final Style center = document.addStyle("center", style);
		center.addAttribute(StyleConstants.Alignment,
				StyleConstants.ALIGN_CENTER);
		center.addAttribute(StyleConstants.Foreground, Color.black);
		right = document.addStyle("right", style);
		right.addAttribute(StyleConstants.Alignment, StyleConstants.ALIGN_RIGHT);
		right.addAttribute(StyleConstants.Foreground, new Color(0x2F, 0x8F,
				0x00));

		final Style left = document.addStyle("right", style);
		left.addAttribute(StyleConstants.Alignment, StyleConstants.ALIGN_LEFT);
		left.addAttribute(StyleConstants.Foreground,
				new Color(0x08, 0x10, 0x73));

		Border blackline = BorderFactory.createLineBorder(Color.gray);
		TitledBorder title;
		title = BorderFactory.createTitledBorder(blackline, "Assitant");
		title.setTitleJustification(TitledBorder.CENTER);

		graphContent.setBackground(new Color(238, 238, 238));
		// graphContent.setBorder(BorderFactory.createLineBorder(new Color(238,
		// 238, 238)));
		Dimension dimension = new Dimension(800, 540);
		graphContent.setPreferredSize(dimension);
		jsp = new JScrollPane();
		jsp.setPreferredSize(new Dimension(800, 540));
		jsp.setViewportView(graphContent);
		graph.add(jsp);
		pane.setLayout(new GridBagLayout());

		actions.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		text.setWrapStyleWord(true);
		text.setLineWrap(true);
		micro.setBackground(new Color(57, 74, 54));
		micro.setFocusable(false);
		final JToolTip microTip = new JToolTip();
		microTip.add(micro);
		micro.setToolTipText("Micro off");
		text.setMargin(new Insets(5, 5, 0, 5));
		// text.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		fileMenu.add(newFile);
		fileMenu.add(oppenFile);
		fileMenu.add(save);
		fileMenu.add(saveAs);
		fileMenu.add(exportMenu);
		fileMenu.add(quit);
		bar.add(fileMenu);
		bar.add(help);
		// help.setMenuLocation(30, 30);

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 0;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.ipady = 20;
		constraints.gridwidth = 2;
		pane.add(bar, constraints);

		conversation.setLayout(new GridBagLayout());
		GridBagConstraints conversationConstraints = new GridBagConstraints();
		conversationConstraints.fill = GridBagConstraints.HORIZONTAL;
		conversationConstraints.weightx = 0;
		conversationConstraints.gridx = 0;
		conversationConstraints.gridy = 0;
		conversationConstraints.gridwidth = 3;
		conversationConstraints.ipady = 437;
		historic.setEditable(false);
		// historic.setPreferredSize(new Dimension(32, 30));
		conversationContent.setSize(500, 30);
		conversation.add(conversationContent, conversationConstraints);

		conversationConstraints.fill = GridBagConstraints.HORIZONTAL;
		conversationConstraints.weightx = 0.9;
		conversationConstraints.gridx = 1;
		conversationConstraints.gridy = 1;
		conversationConstraints.gridwidth = 1;
		conversationConstraints.gridheight = 1;
		conversationConstraints.ipady = 9;
		conversation.add(actions, conversationConstraints);

		conversationConstraints.fill = GridBagConstraints.HORIZONTAL;
		conversationConstraints.weightx = 0;
		conversationConstraints.gridx = 0;
		conversationConstraints.gridy = 1;
		conversationConstraints.gridwidth = 1;
		conversationConstraints.ipady = 9;
		conversation.add(micro, conversationConstraints);

		conversationConstraints.fill = GridBagConstraints.HORIZONTAL;
		conversationConstraints.weightx = 0;
		conversationConstraints.gridx = 0;
		conversationConstraints.gridy = 2;
		conversationConstraints.gridwidth = 1;
		conversationConstraints.ipady = 1;
		conversation.add(status, conversationConstraints);

		conversationConstraints.fill = GridBagConstraints.HORIZONTAL;
		conversationConstraints.weightx = 0;
		conversationConstraints.gridx = 1;
		conversationConstraints.gridy = 2;
		conversationConstraints.gridwidth = 1;
		conversationConstraints.ipady = 1;
		verifLabel.setForeground(Color.gray);
		JScrollPane verifLabelScroll = new JScrollPane(verifLabel);
		verifLabelScroll
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		verifLabelScroll
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		verifLabelScroll.setBorder(BorderFactory.createEmptyBorder());
		conversation.add(verifLabelScroll, conversationConstraints);

		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 0.25;
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		// constraints.ipady = 500;
		constraints.insets = new Insets(20, 0, 0, 0);
		conversation.setBorder(title);
		pane.add(conversation, constraints);

		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 0.75;
		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.ipady = 528;
		constraints.insets = new Insets(20, 0, 0, 0);
		title = BorderFactory.createTitledBorder(blackline, "BPMN Graph");
		title.setTitleJustification(TitledBorder.CENTER);
		graph.setBorder(title);
		pane.add(graph, constraints);
		// graph.setBorder(new EmptyBorder(10, 10, 10, 10));

		this.add(pane);

		// listener sur le bouton du menu pour lancer le choix du fichier
		newFile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				wf = new WorkFlow();
				wf.starting();

				choose.setAcceptAllFileFilterUsed(false);
				choose.resetChoosableFileFilters();
				choose.addChoosableFileFilter(new FileNameExtensionFilter(
						"bpmnt file", "bpmnt"));
				try {
					GuiEvent ev = new GuiEvent(this, ViewAgent.WORKFLOW_SEND);
					ev.addParameter(wf);
					viewagent.postGuiEvent(ev);
					refresh(wf);
					historic.setText("");
					jsp.repaint();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		oppenFile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				choose.setAcceptAllFileFilterUsed(false);
				choose.resetChoosableFileFilters();
				choose.addChoosableFileFilter(new FileNameExtensionFilter(
						"bpmnt file", "bpmnt"));

				int returnVal = choose.showOpenDialog(choose);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = choose.getSelectedFile();
					try {
						wf = export.jsonImport(file);
						GuiEvent ev = new GuiEvent(this,
								ViewAgent.WORKFLOW_SEND);
						ev.addParameter(wf);
						viewagent.postGuiEvent(ev);
						refresh(wf);
						jsp.repaint();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		// listener sur le bouton de sauvegarde
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (fileName == null) {
					saveAs.doClick();
				} else {
					File file = new File(fileName);
					try {
						export.jsonExport(wf, file);
						hasBeenModified = false;
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		// listener sur le bouton du menu pour lancer le choix du fichier
		saveAs.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				saver.setAcceptAllFileFilterUsed(false);
				saver.setDialogTitle("Save as");
				saver.resetChoosableFileFilters();
				saver.addChoosableFileFilter(new FileNameExtensionFilter(
						"bpmnt file", "bpmnt"));

				int returnVal = saver.showSaveDialog(saver);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = saver.getSelectedFile();
					try {
						export.jsonExport(wf, file);
						fileName = file.getAbsolutePath();
						hasBeenModified = false;
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		// BPMN export
		exportMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				saver.setAcceptAllFileFilterUsed(false);
				saver.setDialogTitle("Save as a .bpmn file");
				saver.resetChoosableFileFilters();
				saver.addChoosableFileFilter(new FileNameExtensionFilter(
						"bpmn or xml file", "bpmn", "xml"));

				int returnVal = saver.showSaveDialog(saver);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = saver.getSelectedFile();
					try {
						export.bpmnExport(wf, file);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		// listener sur le bouton pour quitter
		quit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		help.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				d2.setTitle("Help");
				d2.setVisible(true);
				JTextArea helpText = new JTextArea();
				helpText.setText("** Orders accepted:\n- create/draw/add + activity/task,"
						+ "start, end, pool + called/named + the name that you decide\n"
						+ "- create/draw/add + join/split parallel gateway\n- connect/link"
						+ " + object + outcoming object's number + and/to/with + incoming object's"
						+ " number\n- put: It needs to be connect to an other object before\n- put/place/insert"
						+ " + object + object's number + in/inside pool + pool's number\n- remove/delete"
						+ " + object + object's number\n- remove/delete + pool + pool's number\n- rename/name"
						+ " + object + object's number\n- rename/name + pool + pool's number\n\n**"
						+ " Objects:\nSplit: 1 incoming link, 2 outcoming link\nJoin: 2 incoming link,"
						+ " 1 outcoming link\nStart: 1 outcoming link\nEnd: 1 incoming link\nTask: 1 "
						+ "incoming link, outcoming link");
				helpText.setEditable(false);
				Dimension dimensionHelp = new Dimension(550, 400);
				Insets inset = new Insets(50, 50, 50, 50);

				helpText.setBackground(new Color(238, 238, 238));
				Border borderHelp = BorderFactory.createEmptyBorder();
				TitledBorder titleHelp;
				titleHelp = BorderFactory.createTitledBorder(borderHelp,
						"What you can say to the Assistant");
				titleHelp.setTitleJustification(TitledBorder.CENTER);
				Border empty = new EmptyBorder(20, 20, 20, 20);
				CompoundBorder border = new CompoundBorder(titleHelp, empty);
				helpText.setBorder(border);
				helpText.setMargin(inset);
				d2.add(helpText);
				d2.setSize(dimensionHelp);
				d2.setResizable(false);

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

		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {

					String actionsText = text.getText();
					actionsText = actionsText.replace("\n", "");
					GuiEvent ev = new GuiEvent(this, ViewAgent.TEXT_SEND);
					ev.addParameter(actionsText);
					viewagent.postGuiEvent(ev);

					text.setText("");
					/* historic.getSize().height */
					final int pos = document.getLength();
					try {
						document.insertString(
								pos,
								actionsText
										+ System.getProperty("line.separator"),
								right);
						document.setParagraphAttributes(pos,
								actionsText.length(), right, true);
					} catch (BadLocationException e1) {
						e1.printStackTrace();
					}

				}
			}
		});

		micro.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				isMicroOn = !isMicroOn;
				if (isMicroOn) {
					micro.setFocusable(false);
					System.out.println("micro on");
					micro.setBackground(new Color(130, 255, 130));
					micro.setFocusable(false);
					micro.setToolTipText("Micro on");

					verifLabel.setText("Micro on");
					verifLabel.setForeground(Color.green);

					GuiEvent ev = new GuiEvent(this, ViewAgent.VOICE_SEND);
					ev.addParameter(isMicroOn);
					viewagent.postGuiEvent(ev);

				} else {
					// JToolTip microOn = new JToolTip();
					// microOn.add(micro);
					micro.setFocusable(true);
					System.out.println("micro off");
					micro.setBackground(new Color(57, 74, 54));
					micro.setFocusable(false);
					microTip.setVisible(false);
					micro.setToolTipText("Micro off");

					verifLabel.setText("Micro off");
					verifLabel.setForeground(Color.gray);

					GuiEvent ev = new GuiEvent(this, ViewAgent.VOICE_SEND);
					ev.addParameter(isMicroOn);
					viewagent.postGuiEvent(ev);
				}

			}

		});

		setVisible(true);

	}

	public void refresh(WorkFlow g) {
		wf = g;
		graphContent.refresh(g);
		jsp.updateUI();
		repaint();
		hasBeenModified = true;

	}

	public PropertyChangeSupport getPcs() {
		return pcs;
	}

	public void setPcs(PropertyChangeSupport pcs) {
		this.pcs = pcs;
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {

		if (arg0.getPropertyName().equals("message")) {
			if (arg0.getNewValue().equals("OK")) {
				verifLabel.setText("Message understood");
				verifLabel.setForeground(Color.green);
				// actions.updateUI();
			} else if (arg0.getNewValue().equals("ERROR")) {
				verifLabel
						.setText("The command is incorrect. Please try again.");
				verifLabel.setForeground(Color.red);
				// actions.updateUI();
			}
		} else if (arg0.getPropertyName().equals("voix")) {
			String text = (String) arg0.getNewValue();
			final int pos = document.getLength();
			try {
				document.insertString(pos,
						text + System.getProperty("line.separator"), right);
				document.setParagraphAttributes(pos, text.length(), right, true);
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}
		}
	}

}

