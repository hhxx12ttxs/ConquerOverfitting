package com.gmail.jafelds.ppedits.gui;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.MouseInputAdapter;

import com.gmail.jafelds.ppedits.Beat;
import com.gmail.jafelds.ppedits.Chart;
import com.gmail.jafelds.ppedits.Measure;
import com.gmail.jafelds.ppedits.SQLite;
import com.gmail.jafelds.ppedits.enums.Arrows;
import com.gmail.jafelds.ppedits.enums.Syncs;
import com.gmail.jafelds.ppedits.enums.Notes;
import com.gmail.jafelds.ppedits.enums.Styles;
import com.gmail.jafelds.ppedits.gui.components.BPMComponent;
import com.gmail.jafelds.ppedits.gui.components.MeasureComponent;
import com.gmail.jafelds.ppedits.gui.components.NoteComponent;
import com.gmail.jafelds.ppedits.gui.components.STPComponent;
import com.gmail.jafelds.ppedits.gui.components.ShadowComponent;

/**
 * @author wolfhome
 *
 */
public class GUI
{
	private JFrame frame; // The parent frame.
	
	private boolean isSaved; // Has this chart been saved?
	
	private Chart ch; // The chart of the song.
	private Syncs sync; // What's the present quantization?
	private Notes aNote; // Active note to place.
	private About about; // About window.
	
	private JButton bSN; // Single edit mode.
	private JButton bDB; // Double edit mode.
	private JButton bHD; // HalfDouble edit mode.
	
	private JComboBox eSongs; // The list of songs.
	private JComboBox eSync; // how much do you sync it?
	private JComboBox eNote; // present note to add.
	
	private JLabel sValid; // Could this edit be played on Pro as is?
	
	private JLabel sSong; // name of the song in the top section.
	
	private JLabel iSteps; // Number of steps
	private JLabel iJumps; // Number of jumps
	private JLabel iHolds; // Number of holds
	private JLabel iMines; // Number of mines
	private JLabel iTrips; // Number of trip(le)s
	private JLabel iRolls; // Number of rolls
	private JLabel iLifts; // Number of lifts
	private JLabel iFakes; // Number of fakes
	
	private JLabel iYPos; // Y position of the ShadowComponent.
	
	private JMenuItem mNeww;
	private JMenuItem mLoad;
	private JMenuItem mSave;
	private JMenuItem mAbot;
	private JMenuItem mExit;
	
	private JPanel cardTop; // top panel will use the card layout.
	private JPanel chooseTop; // Panel for choosing the song.
	private JPanel editTop; // Panel for controlling edit stuff.

	private JLayeredPane stepChart; // The panel that will contain the step chart.
	
	private JMenuBar bar; // The menu bar.
	
	private JFormattedTextField iDiff; // Difficulty of the edit.
	private JTextField sEdit; // Name of the edit.
	
	private JScrollPane scr; // the scrolling pane for the chart.
	
	private ShadowComponent shadow; // This highlights what will be changed.
	private MeasureComponent mcp; // the chart itself.
	private Map<Double, BPMComponent> bpms; // Collection of BPMs.
	private Map<Double, STPComponent> stps; // Collection of stops.
	private Map<Point, NoteComponent> notes; // Collection of arrows.
	private Map<Point, ShadowComponent> bads; // Badly placed arrows.
	
	private final String SONG_TOP = "Choose Song";
	private final String EDIT_TOP = "Edit Controls";
	private final String DEFAULT_DIFF = "10";
	private final String DEFAULT_EDIT = "Replace This";
	
	private final int MAX_BEATS = 192;
	private final int ARROW_SIZE = 48;
	private final int MAX_WIDTH = (Styles.DOUBLE.getColumns() + 2)
			* ARROW_SIZE + ARROW_SIZE / 2;
	private final int MAX_HEIGHT = 800;
	
	private final Dimension ARR = new Dimension(ARROW_SIZE, ARROW_SIZE);
	private final Dimension C_TOP = new Dimension(MAX_WIDTH, ARROW_SIZE * 2);
	
	/**
	 * Set up the GUI, then initialize the components.
	 * TODO: Fix the order of some of these.
	 */
	public GUI()
	{
		frame = new JFrame("Pump Pro Edits Measure Editor");
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.setBackground(Color.WHITE);
		
		class QuitAdapter extends WindowAdapter
		{
			public void windowClosing(WindowEvent w)
			{
				isQuitting();
			}
		}
		
		QuitAdapter q = new QuitAdapter();
		frame.addWindowListener(q);
		
		shadow = new ShadowComponent();
		shadow.setMinimumSize(ARR);
		shadow.setMaximumSize(ARR);
		shadow.setPreferredSize(ARR);
		shadow.setSize(ARR);
		bpms = new HashMap<Double, BPMComponent>();
		stps = new HashMap<Double, STPComponent>();
		notes = new HashMap<Point, NoteComponent>();
		bads = new HashMap<Point, ShadowComponent>();
		
		isSaved = true;
		
		
		
		about = new About(frame);
		about.setLocationRelativeTo(frame);
		
		Dimension window = new Dimension(MAX_WIDTH, MAX_HEIGHT);
		frame.setPreferredSize(window);
		frame.setMaximumSize(window);
		frame.setLocation(25, 25);
		frame.setResizable(false);
		
		loadMenu();
		frame.setJMenuBar(bar);
		
		initComponents();
	}
	
	private void updateStats()
	{
		for (Map.Entry<Point, ShadowComponent> b : bads.entrySet())
		{
			stepChart.remove(b.getValue());
		}
		bads.clear();
		
		ArrayList<Point> invalids = ch.findInvalids();
		
		sValid.setText(invalids.size() > 0 ? "No" : "Yes");
		
		for (Point p : invalids)
		{
			ShadowComponent sc = new ShadowComponent();
			sc.setSize(ARR);
			sc.setPreferredSize(ARR);
			sc.setMinimumSize(ARR);
			sc.setMaximumSize(ARR);
			sc.setBounds((int)p.getX(), (int)p.getY() + ARROW_SIZE,
					ARROW_SIZE, ARROW_SIZE);
			bads.put(p, sc);
			sc.setColor(new Color(255, 255, 255, 128));
			sc.setLocation((int)p.getX(), (int)p.getY() + ARROW_SIZE);
			stepChart.add(sc);
			sc.repaint(sc.getBounds());
			stepChart.setLayer(sc, Integer.MAX_VALUE);
		}
		
		chartRepaint();
		ch.getStats();
		iSteps.setText(ch.getSteps() + "");
		iJumps.setText(ch.getJumps() + "");
		iHolds.setText(ch.getHolds() + "");
		iMines.setText(ch.getMines() + "");
		iTrips.setText(ch.getTrips() + "");
		iRolls.setText(ch.getRolls() + "");
		iLifts.setText(ch.getLifts() + "");
		iFakes.setText(ch.getFakes() + "");
	}
	
	/**
	 * These components always need to come first.
	 */
	private void resetBaseComponents()
	{
		stepChart.removeAll();
		mcp = new MeasureComponent();
		
		/**
		 * The NoteAdapter deals with the placing of notes within
		 * the chart.
		 * @author Jason "Wolfman2000" Felds
		 *
		 */
		class NoteAdapter extends MouseInputAdapter
		{
			/**
			 * On the click of the mouse, add the proper NoteComponent
			 * to where the ShadowComponent is set up.
			 * If the very same NoteComponent is there, then remove it
			 * instead.
			 */
			public void mouseClicked(MouseEvent m)
			{
				int sX = shadow.getX();
				int sY = shadow.getY();
				int tX = mcp.getX();
				int col = 0;
				int measure = (sY - ARROW_SIZE) / 192;
				String s = iYPos.getText();
				int beat = Integer.parseInt(s.substring(0, s.indexOf(' ')));
				
				while (tX + ARROW_SIZE <= sX)
				{
					tX += ARROW_SIZE;
					col += 1;
				}
				Arrows a = Arrows.getArrowByColumn(ch.getStyle(), col);
				
				Point p = new Point(sX, sY);
				Rectangle sB = shadow.getBounds();
				
				NoteComponent n = notes.get(p);
				Syncs z = Syncs.getHighestEnum(beat);
				
				isSaved = false;
				mSave.setEnabled(true);
				
				if (n != null)
				{
					/*
					 * No matter what, the old NoteComponent in use has to
					 * go away. Remove it. At this point, determine if
					 * the type to see if it's the same. If it is
					 * the same, remove it from the collection.
					 * Otherwise, replace it.
					 */
					stepChart.remove(n);
					ch.getMeasure(measure).getBeat(beat).resetNote(col);
					if (n.getNote().equals(aNote))
					{
						notes.remove(p);
						updateStats();
						return;
					}
				}
				n = new NoteComponent(aNote, z, a);
				n.setPreferredSize(ARR);
				n.setSize(ARR);
				n.setMinimumSize(ARR);
				n.setMaximumSize(ARR);
				n.setBounds(sB);
				
				notes.put(p, n);
				stepChart.add(n);
				stepChart.setLayer(n, 2000 + sY);
				
				ch.getMeasure(measure).getBeat(beat).setNote(col, n.getNote());
				updateStats();
			}
			
			
			
			/**
			 * This is called when dealing with the ShadowComponent.
			 * This moves it to its proper position so the user will
			 * know where a note will be placed.
			 * @param m the MouseEvent showing where the mouse is.
			 */
			private void setPosition(MouseEvent m)
			{
				int tX = mcp.getX();
				int tY = mcp.getY();
				
				int aX = tX + m.getX();
				int aY = tY + m.getY();
				
				int span = MAX_BEATS / sync.getSync();
				while (tY + span < aY)
				{
					tY += span;
				}
				
				span = ARROW_SIZE;
				while (tX + span < aX)
				{
					tX += span;
				}
				
				shadow.setLocation(tX, tY);
				iYPos.setText(((shadow.getY() - ARROW_SIZE) % 192) + " / 192");
				shadow.repaint(mcp.getBounds());
			}
			
			/**
			 * When the grid is entered, draw the ShadowComponent
			 * in its position.
			 */
			public void mouseEntered(MouseEvent m)
			{
				shadow.setColor(new Color(0, 191, 255, 128));
				setPosition(m);
			}
			
			/**
			 * When the grid is exited, place the ShadowComponent
			 * outside and invisible.
			 */
			public void mouseExited(MouseEvent m)
			{
				shadow.setLocation(0, 0);
				shadow.setColor(new Color(255, 255, 255, 0));
				shadow.repaint(mcp.getBounds());
				iYPos.setText("??? / 192");
			}
			
			/**
			 * When staying inside the gird, merely update
			 * the ShadowComponent's location.
			 */
			public void mouseMoved(MouseEvent m)
			{
				setPosition(m);
			}
		}
		NoteAdapter n = new NoteAdapter();
		mcp.addMouseMotionListener(n);
		mcp.addMouseListener(n);
		
		stepChart.add(mcp);
		stepChart.setLayer(mcp, 1000);
		shadow.setBounds(96, 96, shadow.getWidth(), shadow.getHeight());
		stepChart.add(shadow);
		stepChart.setLayer(shadow, Integer.MAX_VALUE - 10000);
		
		ch = new Chart(); // reasonable default
		ch.setStyle(Styles.DUMMY);
		ch.setDifficulty(Integer.parseInt(DEFAULT_DIFF));
		ch.setEditName(DEFAULT_EDIT);
		
		ArrayList<String> sList = ch.getSQLite().getSongList();
		sList.add(0, null);
		for (String s : sList)
		{
			eSongs.addItem(s);
		}
		eSongs.setSelectedIndex(0);
		eSync.setSelectedIndex(0);
		eNote.setSelectedIndex(0);
		
		bpms.clear();
		stps.clear();
		notes.clear();
		bads.clear();
		isSaved = true;
		
		sSong.setText("");
		iDiff.setText(DEFAULT_DIFF);
		sEdit.setText(DEFAULT_EDIT);
		
		sync = Syncs.FOURTH; // reasonable default.
		aNote = Notes.TAP; // reasonable default.
		
		mSave.setEnabled(false);
		mNeww.setEnabled(false);
		bSN.setEnabled(false);
		bDB.setEnabled(false);
		bHD.setEnabled(false);
		
	}
	
	/**
	 * This is called when it's time to repaint what's on the chart.
	 */
	private void chartRepaint()
	{
		stepChart.revalidate();
		stepChart.repaint();
	}
	
	/**
	 * When a new song is chosen, Ensure the stepChart panel
	 * is set up properly.
	 */
	private void resizeChartPanel()
	{
		int w = ARROW_SIZE * ch.getStyle().getColumns();
		int max_w = ARROW_SIZE * Styles.DOUBLE.getColumns();
		int gap = (max_w - w) / 2; // How much do I offset?
		int measures = ch.getMeasures();
		Dimension size = new Dimension(MAX_WIDTH,
				measures * MAX_BEATS + ARROW_SIZE * 2);
		stepChart.setSize(size);
		stepChart.setPreferredSize(size);
		mcp.setBounds(ARROW_SIZE + gap, ARROW_SIZE,
				w + 1, MAX_BEATS * measures + 1);
		updateStats(); // shouldn't take too long at this point.
		//chartRepaint();

		
	}
	
	/**
	 * Set up the chart with the specified song.
	 * If there is no song, do not bother with the other steps.
	 */
	private void loadSong()
	{
		String sName = ch.getSongName();
		SQLite sqlite = ch.getSQLite();
		int measures = 0;
		
		if (sName == null)
		{
			mNeww.setEnabled(false);
			
			ch.setMeasures(measures);
		}
		else
		{
			isSaved = true;
			mSave.setEnabled(false);
			mNeww.setEnabled(true);
			measures = sqlite.getMeasuresByName(sName);
			ch.setMeasures(measures);
			
			int col = ch.getStyle().getColumns();
			mcp.setColumns(col);
			mcp.setMeasures(measures);

			int nX = ARROW_SIZE * (col + 2);
			int nY = ARROW_SIZE / 2;
			final Dimension d = new Dimension(nX, nY);
			
			
			Map<Double, Double> b = sqlite.getBPMsBySong(sName);
			for (Map.Entry<Double, Double> c : b.entrySet())
			{
				double be = c.getKey();
				BPMComponent bc = new BPMComponent(col, c.getValue());
				int sY = (int)(ARROW_SIZE - 7 + be * ARROW_SIZE);
				
				bc.setBounds(ARROW_SIZE * 6, sY, nX, nY);
				bc.setPreferredSize(d);
				bc.setSize(d);
				bc.setMaximumSize(d);
				bc.setMinimumSize(d);
				
				bpms.put(be, bc);
				stepChart.add(bc);
				stepChart.setLayer(bc, 2000);
			}
			
			b = sqlite.getStopsBySong(sName);
			for (Map.Entry<Double, Double> c : b.entrySet())
			{
				double be = c.getKey();
				STPComponent bc = new STPComponent(col, c.getValue());
				int sY = (int)(ARROW_SIZE - 7 + be * ARROW_SIZE);
				
				int sX = 0;
				if (col == 5)
				{
					sX = (int)(ARROW_SIZE * 2.5);
				}
				else if (col == 6)
				{
					sX = ARROW_SIZE * 2;
				}
				
				bc.setBounds(sX, sY, nX, nY);
				bc.setPreferredSize(d);
				bc.setSize(d);
				bc.setMaximumSize(d);
				bc.setMinimumSize(d);
				
				stps.put(be, bc);
				stepChart.add(bc);
				stepChart.setLayer(bc, 2000);
			}
			
		}
		resizeChartPanel();
	}
	
	/**
	 * Set up the stepChart JLayeredPane to accept the chart,
	 * the shadow rectangle, and other attributes.
	 */
	private void loadChartPanel()
	{
		stepChart = new JLayeredPane();
		resetBaseComponents();
		
		
		
		//resizeChartPanel();
		
		scr = new JScrollPane(stepChart);
		scr.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scr.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
	}
	
	/**
	 * The editTop is the crowded toolbar for controlling your edit.
	 * The left side allows for setting up options such as what
	 * note type to place and how specific you want it.
	 * The right side contains stats of the edit at its present time.
	 */
	private void loadEditTop()
	{
		editTop = new JPanel();
		GroupLayout layout = new GroupLayout(editTop);
		editTop.setLayout(layout);
		editTop.setBackground(new Color(200, 200, 200));
		
		Syncs[] tmp = Syncs.values();
		String[] choices = new String[tmp.length];
		for (int b = 0; b < tmp.length; b++)
		{
			choices[b] = tmp[b].getEnglish();
		}
		
		eSync = new JComboBox(choices);
		eSync.setSelectedIndex(0);
		JLabel lSync = new JLabel("Note division:");
		
		Notes[] tmp2 = Notes.values();
		choices = new String[tmp2.length - 1];
		/*
		 * Why does b = 1 first? There is no need to display the empty
		 * note as a selection. Clicking on a square with a fully occupied
		 * arrow will remove it.
		 */
		for (int b = 1; b < tmp2.length; b++)
		{
			choices[b - 1] = tmp2[b].getKind();
		}
		eNote = new JComboBox(choices);
		eNote.setSelectedIndex(0);
		JLabel lNote = new JLabel("Note kind:");
		
		JLabel lDiff = new JLabel("Diff Rating:");
		
		NumberFormat fmt = NumberFormat.getIntegerInstance();
		fmt.setMinimumIntegerDigits(1);
		fmt.setMaximumIntegerDigits(2);
		fmt.setMaximumFractionDigits(0); // Are these two required?
		fmt.setMinimumFractionDigits(0);
		iDiff = new JFormattedTextField(fmt);
		iDiff.setColumns(2);
		iDiff.setText(DEFAULT_DIFF);
		
		JLabel lEdit = new JLabel("Edit Name:");
		sEdit = new JTextField(12);
		sEdit.setText(DEFAULT_EDIT);
		
		
		JLabel lClean = new JLabel("Valid Edit:");
		
		sValid = new JLabel("Yes");
		
		JLabel lSong = new JLabel("Song Name:");
		sSong = new JLabel("");
		
		JLabel lYPos = new JLabel("Y Pos:");
		iYPos = new JLabel("??? / 192");
		
		JLabel lSteps = new JLabel("Steps:");
		iSteps = new JLabel("0");
		iSteps.setHorizontalAlignment(JLabel.TRAILING);
		JLabel lJumps = new JLabel("Jumps:");
		iJumps = new JLabel("0");
		iJumps.setHorizontalAlignment(JLabel.TRAILING);
		JLabel lHolds = new JLabel("Holds:");
		iHolds = new JLabel("0");
		iHolds.setHorizontalAlignment(JLabel.TRAILING);
		JLabel lMines = new JLabel("Mines:");
		iMines = new JLabel("0");
		iMines.setHorizontalAlignment(JLabel.TRAILING);
		JLabel lTrips = new JLabel("Trips:");
		iTrips = new JLabel("0");
		iTrips.setHorizontalAlignment(JLabel.TRAILING);
		JLabel lRolls = new JLabel("Rolls:");
		iRolls = new JLabel("0");
		iRolls.setHorizontalAlignment(JLabel.TRAILING);
		JLabel lLifts = new JLabel("Lifts:");
		iLifts = new JLabel("0");
		iLifts.setHorizontalAlignment(JLabel.TRAILING);
		JLabel lFakes = new JLabel("Fakes:");
		iFakes = new JLabel("0");
		iFakes.setHorizontalAlignment(JLabel.TRAILING);
		
		/*
		 * One of the disadvantages of the GroupLayout is that keeping
		 * everything aligned is tricky. Each element has to be defined
		 * twice.
		 */
		
		GroupLayout.Alignment lead = GroupLayout.Alignment.LEADING;
		LayoutStyle.ComponentPlacement rel = LayoutStyle.ComponentPlacement.RELATED;
		GroupLayout.Alignment back = GroupLayout.Alignment.TRAILING;
		GroupLayout.Alignment base = GroupLayout.Alignment.BASELINE;
		
		layout.setHorizontalGroup(
			layout.createSequentialGroup()
			.addPreferredGap(rel, 10, 10)
			.addGroup(layout.createParallelGroup(lead)
				.addComponent(lSync)
				.addComponent(lDiff)
				.addComponent(lClean)
				.addComponent(lSong)
			)
			.addGroup(layout.createParallelGroup(lead)
				.addGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(lead)
						.addComponent(eSync, 90, 90, 90)
						.addComponent(iDiff, 40, 40, 40)
						.addComponent(sValid)
					)
					.addGroup(layout.createParallelGroup(lead)
						.addComponent(lNote)
						.addComponent(lEdit)
						.addComponent(lYPos)
					)
					.addGroup(layout.createParallelGroup(lead)
						.addComponent(eNote, 140, 140, 140)
						.addComponent(sEdit, 100, 100, 100)
						.addComponent(iYPos)
					)
				)
				.addComponent(sSong)
			)
			.addPreferredGap(rel, 10, 10)
			.addGroup(layout.createParallelGroup(lead)
				.addComponent(lSteps)
				.addComponent(lHolds)
				.addComponent(lTrips)
				.addComponent(lLifts)
			)
			.addGap(20)
			.addGroup(layout.createParallelGroup(back)
				.addComponent(iSteps)
				.addComponent(iHolds)
				.addComponent(iTrips)
				.addComponent(iLifts)
			)
			.addGap(10)
			.addGroup(layout.createParallelGroup(lead)
				.addComponent(lJumps)
				.addComponent(lMines)
				.addComponent(lRolls)
				.addComponent(lFakes)
			)
			.addGap(20)
			.addGroup(layout.createParallelGroup(back)
				.addComponent(iJumps)
				.addComponent(iMines)
				.addComponent(iRolls)
				.addComponent(iFakes)
			)
		);
		layout.setVerticalGroup(
			layout.createSequentialGroup() // both were baseline
			.addGroup(layout.createParallelGroup(base)
				.addComponent(lSync)
				.addComponent(eSync)
				.addComponent(lNote)
				.addComponent(eNote)
				.addComponent(lSteps)
				.addComponent(iSteps)
				.addComponent(lJumps)
				.addComponent(iJumps)
			)
			.addGroup(layout.createParallelGroup(base)
				
				.addComponent(lDiff)
				.addComponent(iDiff)
				.addComponent(lEdit)
				.addComponent(sEdit)
				.addComponent(lHolds)
				.addComponent(iHolds)
				.addComponent(lMines)
				.addComponent(iMines)
			)
			.addGroup(layout.createParallelGroup(base)
				.addComponent(lClean)
				.addComponent(sValid)
				.addComponent(lYPos)
				.addComponent(iYPos)
				.addComponent(lTrips)
				.addComponent(iTrips)
				.addComponent(lRolls)
				.addComponent(iRolls)
			)
			.addPreferredGap(rel, 10, 10)
			.addGroup(layout.createParallelGroup(base)
				.addComponent(lSong)
				.addComponent(sSong)
				.addComponent(lLifts)
				.addComponent(iLifts)
				.addComponent(lFakes)
				.addComponent(iFakes)
			)
		);
		
		/**
		 * The SyncListener adjusts the present sync rate for placing
		 * notes. The higher the rate, the more specific notes you
		 * can place.
		 * @author Jason "Wolfman2000" Felds
		 */
		class SyncListener implements ActionListener
		{
			public void actionPerformed(ActionEvent e)
			{
				sync = Syncs.getEnum(eSync.getSelectedItem().toString());
			}
		}
		eSync.addActionListener(new SyncListener());
		
		/**
		 * The NoteListener determines what notes will be placed at
		 * present. Use the drop down menu to change what is placed.
		 * @author Jason "Wolfman2000" Felds
		 */
		class NoteListener implements ActionListener
		{
			public void actionPerformed(ActionEvent e)
			{
				aNote = Notes.getEnum(eNote.getSelectedItem().toString());
			}
		}
		eNote.addActionListener(new NoteListener());
		
		/**
		 * The EditListener is used to update the name of the edit
		 * internally. This is technically not required, but
		 * perhaps I should make it on saving... :)
		 * @author Jason "Wolfman2000" Felds
		 */
		class EditListener implements ActionListener
		{
			public void actionPerformed(ActionEvent e)
			{
				ch.setEditName(sEdit.getText());
			}
		}
		sEdit.addActionListener(new EditListener());
		
		/**
		 * The DiffListener is used to adjust the difficulty
		 * internally. This is required.
		 * @author Jason "Wolfman2000" Felds
		 */
		class DiffListener implements ActionListener
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					ch.setDifficulty(Integer.parseInt(iDiff.getText()));
				}
				catch (NumberFormatException n) {}
			}
		}
		iDiff.addActionListener(new DiffListener());
	}
	
	/**
	 * This sets up the cardTop panel. The CardLayout is used to
	 * switch between Song mode and Edit mode. Song mode allows you
	 * to choose your song. Edit mode allows you to edit the chart
	 * with the chosen song and style.
	 */
	private void loadCardTop()
	{
		cardTop = new JPanel(new CardLayout());
		loadEditTop();
		loadChooseTop();
		cardTop.add(chooseTop, SONG_TOP);
		cardTop.add(editTop, EDIT_TOP);
		cardTop.setPreferredSize(C_TOP);
		cardTop.setMaximumSize(C_TOP);
		cardTop.setMinimumSize(C_TOP);
		
	}
	
	/**
	 * The chooseTop is wehre the user picks a song and style for
	 * editing.
	 */
	private void loadChooseTop()
	{
		chooseTop = new JPanel();
		GroupLayout layout = new GroupLayout(chooseTop);
		chooseTop.setLayout(layout);
		chooseTop.setBackground(new Color(200, 200, 200));
		
		JLabel choose = new JLabel("Choose your song:");
		
		//ArrayList<String> sList = ch.getSQLite().getSongList();
		//sList.add(0, null);
		eSongs = new JComboBox();
		
		class SongListener implements ActionListener
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					ch.setSongName(eSongs.getSelectedItem().toString());
					bSN.setEnabled(true);
					bDB.setEnabled(true);
					bHD.setEnabled(true);
				}
				catch (NullPointerException n) {}
			}
		}
		
		eSongs.addActionListener(new SongListener());
		
		class StyleListener implements ActionListener
		{
			private Styles iStyle;
			
			public StyleListener(Styles s)
			{
				iStyle = s;
			}
			public void actionPerformed(ActionEvent e)
			{
				CardLayout c1 = (CardLayout)cardTop.getLayout();
				c1.show(cardTop, EDIT_TOP);
				
				ch.setStyle(iStyle);
				String sName = ch.getSongName();
				sSong.setText(sName);
				sValid.setText("Yes");
				iYPos.setText("??? / 192");
				loadSong();
				isSaved = true;
			}
		}
		
		bSN = new JButton("single");
		bSN.addActionListener(new StyleListener(Styles.SINGLE));
		bDB = new JButton("double");
		bDB.addActionListener(new StyleListener(Styles.DOUBLE));
		bHD = new JButton("halfdouble");
		bHD.addActionListener(new StyleListener(Styles.HALFDOUBLE));
		
		final int SPACING = 100;
		
		layout.setHorizontalGroup(
			layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(choose)
					.addComponent(bSN)
				)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
					.addGroup(layout.createSequentialGroup()
						.addComponent(bDB)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
								SPACING, SPACING)
						.addComponent(bHD)
					)
					.addComponent(eSongs, GroupLayout.Alignment.LEADING,
							SPACING * 3, SPACING * 3, SPACING * 3)
				)
				.addContainerGap(72, GroupLayout.PREFERRED_SIZE)
			)
		);
		layout.setVerticalGroup(
			layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(23, 23, 23)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(choose)
					.addComponent(eSongs, GroupLayout.PREFERRED_SIZE,
							GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				)
				.addGap(18, 18, 18)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(bSN)
					.addComponent(bHD)
					.addComponent(bDB)
				)
				.addContainerGap(144, Short.MAX_VALUE)
			)
		);
	}
	
	private void loadMenu()
	{
		bar = new JMenuBar();
		JMenu menu = new JMenu("Options");
		bar.add(menu);
		
		/**
		 * The NewListener effectively resets everything to its original
		 * setup. This is useful if one wants to make a new edit.
		 * @author Jason "Wolfman2000" Felds
		 */
		class NewListener implements ActionListener
		{
			public void actionPerformed(ActionEvent e)
			{
				int n = isSavingRecent();
				if (n == JOptionPane.CANCEL_OPTION)
				{
					return;
				}
				else if (n == JOptionPane.YES_OPTION)
				{
					saveEdit();
				}
				
				resetBaseComponents();
				loadSong();
				CardLayout c = (CardLayout)(cardTop.getLayout());
				c.show(cardTop, SONG_TOP);
			}
		}
		mNeww = new JMenuItem("New");
		mNeww.addActionListener(new NewListener());
		mNeww.setEnabled(false);
		menu.add(mNeww);
		
		class LoadListener implements ActionListener
		{
			/**
			 * This private function is kept separate in order to attempt to
			 * keep everything neater. This function actually loads the
			 * arrows and graphics into the GUI.
			 */
			private void loadChart()
			{
				notes.clear(); // Be sure everything got wiped out.
				Styles s = ch.getStyle();
				int cols = s.getColumns();
				sEdit.setText(ch.getEditName());
				iDiff.setText(ch.getDifficulty() + "");
				
				for (int i = 0; i < ch.getMeasures(); i++)
				{
					Measure m = ch.getMeasure(i);
					
					for (Map.Entry<Integer,Beat> b : m.getBeats().entrySet())
					{
						Beat c = b.getValue();
						int k = b.getKey();
						
						ROW_LOOP:
						for (int j = 0; j < cols; j++)
						{
							Notes z = c.getNote(j);
							if (z.getType() == '0')
							{
								continue ROW_LOOP;
							}
							Point p = new Point(j * ARROW_SIZE + ARROW_SIZE,
									i * MAX_BEATS + k + ARROW_SIZE);
							NoteComponent n = new NoteComponent(c.getNote(j),
									Syncs.getHighestEnum(k),
									Arrows.getArrowByColumn(j, s));
							n.setSize(ARR);
							n.setPreferredSize(ARR);
							n.setMaximumSize(ARR);
							n.setMinimumSize(ARR);
							n.setBounds((int)p.getX(), (int)p.getY(),
									ARROW_SIZE, ARROW_SIZE);
							notes.put(p, n);
							stepChart.add(n);
							stepChart.setLayer(n, (int) (2000 + p.getY()));
						}
					}
				}
				CardLayout c1 = (CardLayout)cardTop.getLayout();
				c1.show(cardTop, EDIT_TOP);
				loadSong();
			}
			
			public void actionPerformed(ActionEvent e)
			{
				int n = isSavingRecent();
				if (n == JOptionPane.CANCEL_OPTION)
				{
					return;
				}
				else if (n == JOptionPane.YES_OPTION)
				{
					saveEdit();
				}
				
				JFileChooser choose = new JFileChooser();
				choose.setDialogTitle("Select the .edit file you wish to "+
						"work on.");
				choose.setDragEnabled(false);
				
				int rVal = choose.showOpenDialog(stepChart);
				if (rVal == JFileChooser.APPROVE_OPTION)
				{
					String file = choose.getSelectedFile().getAbsolutePath();
					ch.loadChart(file);
					
					this.loadChart();
				}
			}
		}
		mLoad = new JMenuItem("Load");
		mLoad.addActionListener(new LoadListener());
		menu.add(mLoad);
		
		class SaveListener implements ActionListener
		{
			public void actionPerformed(ActionEvent e)
			{
				if (isSavingBadEdit())
				{
					saveEdit();
				}
			}
		}
		mSave = new JMenuItem("Save");
		mSave.addActionListener(new SaveListener());
		mSave.setEnabled(false);
		menu.add(mSave);
		
		class AboutListener implements ActionListener
		{
			public void actionPerformed(ActionEvent e)
			{
				about.setVisible(true);
			}
		}
		mAbot = new JMenuItem("About");
		mAbot.addActionListener(new AboutListener());
		menu.add(mAbot);
		
		class QuitListener implements ActionListener
		{
			public void actionPerformed(ActionEvent e)
			{
				isQuitting();
			}
		}
		mExit = new JMenuItem("Quit");
		mExit.addActionListener(new QuitListener());
		menu.add(mExit);
	}
	private void initComponents()
	{
		Container cp = frame.getContentPane();
		cp.setLayout(new BoxLayout(cp, BoxLayout.PAGE_AXIS));
		
		loadCardTop();
		cp.add(cardTop);
		
		loadChartPanel();
		cp.add(scr);
		
		frame.pack();
		frame.setVisible(true);
	}
	
	private int isSavingRecent()
	{
		if (isSaved)
		{
			return JOptionPane.NO_OPTION;
		}
		String options[] = {"Save my work please.",
				"No, but thank you anyway.",
				"Resume work on this edit."};
		int n = JOptionPane.showOptionDialog(frame,
				"Your edit has recent changes. Do you "
				+ "wish to save your work first?",
				"Save before working on a new edit?",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null, options,
				options[0]);
		
		if (n == JOptionPane.CANCEL_OPTION)
		{
			return n;
		}
		else if (n == JOptionPane.YES_OPTION)
		{
			if (bads.size() > 0)
			{
				options[0] = "I wish to save anyway.";
				options[1] = "On second thought,\ndon't bother saving.";
				options[2] = "I'll fix up the edit first.";
				return JOptionPane.showOptionDialog(frame,
						"Your edit looks to have errors in it. "
						+ "Do you still wish to save your work?",
						"Problems found in your edit!",
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null, options,
						options[1]);
			}
			return JOptionPane.YES_OPTION;
		}
		return n;
	}
	
	private void isQuitting()
	{
		int n = isSavingRecent();
		if (n == JOptionPane.CANCEL_OPTION)
		{
			return;
		}
		else if (n == JOptionPane.YES_OPTION)
		{
			saveEdit();
		}
		System.exit(0);
	}
	
	private boolean isSavingBadEdit()
	{
		if (bads.size() > 0)
		{
			String options[] = {"Save anyway.", "I'll fix it up."};
			int n = JOptionPane.showOptionDialog(frame,
					"Your edit looks to have errors in it. "
					+ "Do you still wish to save your work?",
					"Problems found in your edit!",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null, options,
					options[1]);
			
			if (n == JOptionPane.NO_OPTION)
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * This function is called when the user is able to save their chart.
	 */
	private void saveEdit()
	{
		JFileChooser choose = new JFileChooser();
		choose.setAcceptAllFileFilterUsed(false);
		choose.setDialogTitle("Save your .edit file");
		choose.setDragEnabled(false);
		
		if (choose.showSaveDialog(stepChart) == JFileChooser.APPROVE_OPTION)
		{
			ch.saveChart(choose.getSelectedFile().getAbsolutePath());
			mSave.setEnabled(false);
			isSaved = true;
		}
	}
	
	public static void main(String[] args) throws ClassNotFoundException
	{
		Class.forName("org.sqlite.JDBC");
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				new GUI();
			}
		});
	}
}

