import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.swing.*;

public class TetrisPanel extends JPanel implements KeyListener, WindowListener,
		MetaEventListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int ROWS = 18;

	public static final int COLUMNS = 10;

	public static final int FIELD_WIDTH = COLUMNS * Space.getWIDTH();

	public static final int FIELD_HEIGHT = ROWS * Space.getHEIGHT();

	public static final int STATUS_WIDTH = 110;

	public static final int STATUS_HEIGHT = FIELD_HEIGHT;

	public static final int PANEL_WIDTH = FIELD_WIDTH + STATUS_WIDTH;

	public static final int PANEL_HEIGHT = FIELD_HEIGHT;

	public static final int TEXT_MARGIN = 2;

	private static final int BLOCK_STEP = 1; // do not edit!

	private static final Color DEFAULT_COLOR = Color.BLACK;

	private static final int DEFAULT_FONTSIZE = 12;

	public static final int LEFT_ARROW = KeyEvent.VK_LEFT;

	public static final int RIGHT_ARROW = KeyEvent.VK_RIGHT;

	public static final int UP_ARROW = KeyEvent.VK_UP;

	public static final int DOWN_ARROW = KeyEvent.VK_DOWN;

	public static final int SHIFT_BUTTON = KeyEvent.VK_SHIFT;

	public static final int Z_BUTTON = KeyEvent.VK_Z;

	public static final int OPTIONS_BUTTON = KeyEvent.VK_O;

	public static final int PAUSE_BUTTON = KeyEvent.VK_P;

	public static final int NEWGAME_BUTTON = KeyEvent.VK_N;

	public static final int HELP_BUTTON = KeyEvent.VK_H;

	public static final int RECORD_BUTTON = KeyEvent.VK_R;

	public static final String DEFAULT_MUSICFILE = "src/main/resources/music/Human1gm.mid";

	private final String DEFAULT_BLOCKCELL = "src/main/resources/images/block2.jpg";

	private final String DEFAULT_EMPTYCELL = "src/main/resources/images/bg2.jpg";

	private final int ENDGAME_SONGS = 2;

	private float TIMESTEP_REDUCTION_FACTOR = 10f / 11;

	private int LINES_PER_LEVEL = 10;

	private int BONUS_POINTS_PER_LEVEL = 1;

	// private int POINTS_PER_LEVEL = 100;

	private int START_TIME = 0;

	private int START_TIMESTEP = 1000;

	private Timer timer;

	private int time;

	private int timeStep;

	protected Field theField;

	private Block activeBlock;

	private int nextBlockID;

	private Block nextBlock;

	protected int completedLines;

	private int singles;

	private int doubles;

	private int triples;

	private int tetrises;

	private int score;

	protected int linesInPurge;

	private boolean gameIsPaused;

	private boolean gameIsOver;

	private int level;

	private HelpPanel help = new HelpPanel(this);

	private OptionsPanel options = new OptionsPanel(this);

	private RecordPanel records = new RecordPanel();

	private JFrame helpFrame = new JFrame("Tetris Help");

	private JFrame optionsFrame = new JFrame("Tetris Options");

	private JFrame recordsFrame = new JFrame("Tetris Records");

	private Image blockCell = Toolkit.getDefaultToolkit().getImage(
			DEFAULT_BLOCKCELL);

	private Image emptyCell = Toolkit.getDefaultToolkit().getImage(
			DEFAULT_EMPTYCELL);

	private String musicFile = DEFAULT_MUSICFILE;

	public String[] endGameMusic = { "src/main/resources/music/Humdthgm.mid", "src/main/resources/music/Orcdthgm.mid" };

	private Sequencer sequencer;

	private Sequence sequence;

	private boolean musicIsOn = false;

	private boolean musicIsPlaying = false;

	private boolean soundIsOn = false;

	private boolean alternateKeys = false;

	private SoundManager soundManager = new SoundManager();

	public TetrisPanel() {
		super();
		setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
		setLayout(null);
		addKeyListener(this);
		setFocusable(true);
		init();
	}

	public void init() {
		theField = new Field(ROWS, COLUMNS);
		time = START_TIME;
		timeStep = START_TIMESTEP;
		nextBlockID = createBlockID();
		completedLines = 0;
		singles = 0;
		doubles = 0;
		triples = 0;
		tetrises = 0;
		score = 0;
		linesInPurge = 0;
		gameIsPaused = false;
		gameIsOver = false;
		level = 0;
		createBlock();
		setTimeStep();
		setTimer(timeStep);
		timer.start();
		if (musicIsOn && musicIsPlaying) {
			restartMusic();
		} else if (musicIsOn && !musicIsPlaying) {
			startMusic();
		}
		repaint();
	}

	public void setTimer(int timeStep) {
		timer = new Timer(timeStep, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// necessary?
				if (gameIsOver) {
					return;
				}
				++time;
				moveBlock(0, BLOCK_STEP);
				repaint();
			}
		});
	}

	public void setTimeStep() {
		timeStep = (int) (START_TIMESTEP * Math.pow(TIMESTEP_REDUCTION_FACTOR,
				level));
		if (timeStep <= 0) {
			timeStep = 1;
		}
	}

	public void paint(Graphics g) {
		g.setColor(DEFAULT_COLOR);

		if (gameIsPaused && !gameIsOver) {
			g.setFont(new Font(null, Font.ITALIC, DEFAULT_FONTSIZE));
			g.clearRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
			g.drawString("Press 'P' to resume game",
					(int) PANEL_WIDTH / 2 - 80, (int) PANEL_HEIGHT / 2);
			return;
		}

		// TODO double buffering
		g.clearRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
		drawStatus(g);
		drawField(g);
		drawBlock(g);

		if (gameIsOver) {
			g.setFont(new Font(null, Font.ITALIC, DEFAULT_FONTSIZE));
			g.drawString("GAME OVER", FIELD_WIDTH + TEXT_MARGIN,
					FIELD_HEIGHT - 50);
			g.drawString("Press 'N' to", FIELD_WIDTH + TEXT_MARGIN,
					FIELD_HEIGHT - 35);
			g.drawString("play again.", FIELD_WIDTH + TEXT_MARGIN,
					FIELD_HEIGHT - 20);
			return;
		}
	}

	public void meta(MetaMessage meta) {
		// end of song
		if (meta.getType() == 47) {
			if (options.getMusicMenu().getSelectedItem() == options
					.getRandomLabel()) {
				sequencer.stop();
				sequencer.close();
				musicFile = options.getRandomSong();
				startMusic();
			} else {
				sequencer.setTickPosition(0);
				sequencer.start();
			}
		}
	}

	public void startMusic() {
		if (gameIsOver) {
			return;
		}

		try {
			sequence = MidiSystem.getSequence(new File(musicFile));
			// sequence = MidiSystem.getSequence(new URL(
			// "http://hostname/midiaudiofile"));

			sequencer = MidiSystem.getSequencer();
			sequencer.open();
			sequencer.setSequence(sequence);
			// sequence.setLoop
			sequencer.addMetaEventListener(this);

			sequencer.start();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}

		musicIsPlaying = true;
	}

	public void stopMusic() {
		if (musicIsPlaying) {
			sequencer.stop();
			sequencer.close();
		}
		musicIsPlaying = false;
	}

	public void restartMusic() {
		stopMusic();
		startMusic();
	}

	public int createBlockID() {
		return (int) (Math.floor(Block.getUNIQUE_IDS()) * Math.random());
	}

	public void createBlock() {
		activeBlock = new Block(nextBlockID);
		nextBlockID = createBlockID();
		nextBlock = new Block(nextBlockID);
		checkEndGame();
	}

	public void drawStatus(Graphics g) {
		// TODO
		g.setColor(new Color(214, 243, 255, 100));
		g.fillRect(FIELD_WIDTH, 0, STATUS_WIDTH, STATUS_HEIGHT);

		g.setColor(Color.BLACK);
		g.setFont(new Font(null, Font.PLAIN, DEFAULT_FONTSIZE));
		g.drawString("Score: " + score, FIELD_WIDTH + TEXT_MARGIN, 15);
		g.drawString("Time: " + time, FIELD_WIDTH + TEXT_MARGIN, 30);
		g.drawString("Level: " + level, FIELD_WIDTH + TEXT_MARGIN, 45);
		g.drawString("Lines: " + completedLines, FIELD_WIDTH + TEXT_MARGIN, 60);
		g.drawString("Single: " + singles, FIELD_WIDTH + TEXT_MARGIN, 75);
		g.drawString("Double: " + doubles, FIELD_WIDTH + TEXT_MARGIN, 90);
		g.drawString("Triple: " + triples, FIELD_WIDTH + TEXT_MARGIN, 105);
		g.drawString("Tetris: " + tetrises, FIELD_WIDTH + TEXT_MARGIN, 120);
		g.setFont(new Font(null, Font.ITALIC, DEFAULT_FONTSIZE));
		g.drawString("Press 'H' for help", FIELD_WIDTH + TEXT_MARGIN, 150);

		g.setColor(new Color(23, 63, 255, 100));
		// TODO drawBlock(x, y)
		for (int i = 0; i < Block.getSPACES_PER_BLOCK(); ++i) {
			// TODO remove correction constants
			int x = Space.getWIDTH()
					* (nextBlock.getOccupied()[i].getColnum() - 1) + 157;
			int y = Space.getHEIGHT()
					* (nextBlock.getOccupied()[i].getRownum() - 1 + 9);
			// g.fillRect(x, y, Space.getWIDTH(), Space.getHEIGHT());
			g.drawImage(blockCell, x, y, this);
		}

		g.setColor(DEFAULT_COLOR);
	}

	public void drawField(Graphics g) {
		for (int i = 0; i < theField.getRows(); ++i) {
			for (int j = 0; j < theField.getCols(); ++j) {
				int x = Space.getWIDTH() * j;
				int y = Space.getHEIGHT() * i;
				if (theField.getSpaceArr()[i][j].isOccupied()) {
					// g.fillRect(x, y, Space.getWIDTH(), Space.getHEIGHT());
					g.drawImage(blockCell, x, y, this);
				} else {
					g.drawImage(emptyCell, x, y, this);
				}
			}
		}
	}

	public void drawBlock(Graphics g) {
		for (int i = 0; i < Block.getSPACES_PER_BLOCK(); ++i) {
			int x = Space.getWIDTH()
					* (activeBlock.getOccupied()[i].getColnum() - 1);
			int y = Space.getHEIGHT()
					* (activeBlock.getOccupied()[i].getRownum() - 1);
			// g.fillRect(x, y, Space.getWIDTH(), Space.getHEIGHT());
			g.drawImage(blockCell, x, y, this);
		}
	}

	public void checkEndGame() {
		for (int i = 0; i < Block.getSPACES_PER_BLOCK(); ++i) {
			int rownum = activeBlock.getOccupied()[i].getRownum();
			int colnum = activeBlock.getOccupied()[i].getColnum();
			if (theField.getSpaceAt(rownum, colnum).isOccupied()) {
				endGame();
			}
		}
	}

	/**
	 * @return Success or failure
	 */
	public boolean moveBlock(int x, int y) {
		if (gameIsPaused) {
			return false;
		}
		// check
		for (int i = 0; i < Block.getSPACES_PER_BLOCK(); ++i) {
			int rownum = activeBlock.getOccupied()[i].getRownum();
			int colnum = activeBlock.getOccupied()[i].getColnum();
			if (x == 0 && y > 0) {
				if (((rownum + y >= 1) && (rownum + y <= ROWS) && theField
						.getSpaceAt(rownum + y, colnum).isOccupied())
						|| rownum >= ROWS) {
					stopBlock();
					return false;
				}
			} else if (x == 0 && y < 0) {
				if (((rownum + y >= 1) && (rownum + y < ROWS) && theField
						.getSpaceAt(rownum + y, colnum).isOccupied())
						|| rownum <= 1) {
					return false;
				}
			} else if (y == 0 && x > 0) {
				if (((colnum + x >= 1) && (colnum + x <= COLUMNS) && theField
						.getSpaceAt(rownum, colnum + x).isOccupied())
						|| colnum >= COLUMNS) {
					return false;
				}
			} else if (y == 0 && x < 0) {
				if (((colnum + x >= 1) && (colnum + x <= COLUMNS) && theField
						.getSpaceAt(rownum, colnum + x).isOccupied())
						|| colnum <= 1) {
					return false;
				}
			} else {
				// undefined movement
				return false;
			}
		}
		// set
		/*
		 * for (int i = 0; i < Block.getSPACES_PER_BLOCK(); ++i) { int rownum =
		 * activeBlock.getOccupied()[i].getRownum(); int colnum =
		 * activeBlock.getOccupied()[i].getColnum();
		 * activeBlock.getRefSpace().setRownum(rownum + y);
		 * activeBlock.getRefSpace().setColnum(colnum + x); }
		 */
		int rownum = activeBlock.getRefSpace().getRownum();
		int colnum = activeBlock.getRefSpace().getColnum();
		activeBlock.getRefSpace().setRownum(rownum + y);
		activeBlock.getRefSpace().setColnum(colnum + x);
		activeBlock.buildFromRef();

		repaint();
		return true;
	}

	public void stopBlock() {
		for (int i = 0; i < Block.getSPACES_PER_BLOCK(); ++i) {
			int rownum = activeBlock.getOccupied()[i].getRownum();
			int colnum = activeBlock.getOccupied()[i].getColnum();
			theField.getSpaceAt(rownum, colnum).setOccupied(true);
		}
		purgeLine();
		createBlock();
	}

	public void purgeLine() {
		linesInPurge = 0; // TODO careful
		
		for (int i = 0; i < theField.getRows(); ++i) {
			boolean lineComplete = false;
			// check
			for (int j = 0; j < theField.getCols(); ++j) {
				if (!theField.getSpaceArr()[i][j].isOccupied()) {
					break;
				}
				if (j == theField.getCols() - 1) {
					lineComplete = true;
				}
			}
			// set
			if (lineComplete) {
				// play sound
				/*
				 * if (soundIsOn) {
				 * soundManager.playClip(soundManager.getLineComplete()); }
				 */
				// remove line
				for (int j = 0; j < theField.getCols(); ++j) {
					theField.getSpaceArr()[i][j].setOccupied(false);
				}
				// rows above i shift down
				if (i <= 1) {
					return;
				}
				for (int row = i - 1; row >= 1; --row) {
					for (int j = 0; j < theField.getCols(); ++j) {
						if (theField.getSpaceArr()[row][j].isOccupied()) {
							theField.getSpaceArr()[row][j].setOccupied(false);
							theField.getSpaceArr()[row + 1][j]
									.setOccupied(true);
						}
					}
				}
				++linesInPurge;
				++completedLines;
			}
		}
		countScore();
	}

	// TODO
	public void countScore() {
		switch (linesInPurge) {
		case 1:
			++singles;
			if (soundIsOn) {
				soundManager.playClip(soundManager.getLineComplete());
			}
			break;
		case 2:
			++doubles;
			if (soundIsOn) {
				soundManager.playClip(soundManager.getLineComplete());
			}
			break;
		case 3:
			++triples;
			if (soundIsOn) {
				soundManager.playClip(soundManager.getTriple());
			}
			break;
		case 4:
			++tetrises;
			if (soundIsOn) {
				soundManager.playClip(soundManager.getTetris());
			}
			break;
		default:
			break;
		}

		for (int i = linesInPurge; i >= 1; --i) {
			score += COLUMNS * i + 0 * BONUS_POINTS_PER_LEVEL * level;
		}

		// if (score >= POINTS_PER_LEVEL * (level + 1)) {
		if (completedLines >= LINES_PER_LEVEL * (level + 1)) {
			++level;
			setTimeStep();
			timer.stop();
			setTimer(timeStep);
			timer.start();
		}
	}

	public void endGame() {
		if (gameIsOver) {
			return;
		}
		gameIsOver = true;

		repaint();
		timer.stop();

		if (soundIsOn) {
			soundManager.playClip(soundManager.getGameOver());
		}

		// TODO work properly
		if (musicIsOn) {
			stopMusic();
			/*
			 * String prevMusicFile = new String(musicFile); musicFile =
			 * endGameMusic[(int) Math.floor((ENDGAME_SONGS) Math.random())];
			 * startMusic(); musicFile = prevMusicFile;
			 */
		}

		if (score > records.getHighScore().getScore()) {
			records.setHighScore(new Record(score, time, level, completedLines,
					singles, doubles, triples, tetrises, getInitials("Score")));
		}
		if (time > records.getHighTime().getTime()) {
			records.setHighTime(new Record(score, time, level, completedLines,
					singles, doubles, triples, tetrises, getInitials("Time")));
		}
		if (level > records.getHighLevel().getLevel()) {
			records.setHighLevel(new Record(score, time, level, completedLines,
					singles, doubles, triples, tetrises, getInitials("Level")));
		}
		if (completedLines > records.getHighLines().getLines()) {
			records.setHighLines(new Record(score, time, level, completedLines,
					singles, doubles, triples, tetrises, getInitials("Lines")));
		}
		if (singles > records.getHighSingles().getSingles()) {
			records.setHighSingles(new Record(score, time, level,
					completedLines, singles, doubles, triples, tetrises,
					getInitials("Single")));
		}
		if (doubles > records.getHighDoubles().getDoubles()) {
			records.setHighDoubles(new Record(score, time, level,
					completedLines, singles, doubles, triples, tetrises,
					getInitials("Double")));
		}
		if (triples > records.getHighTriples().getTriples()) {
			records.setHighTriples(new Record(score, time, level,
					completedLines, singles, doubles, triples, tetrises,
					getInitials("Triple")));
		}
		if (tetrises > records.getHighTetrises().getTetrises()) {
			records.setHighTetrises(new Record(score, time, level,
					completedLines, singles, doubles, triples, tetrises,
					getInitials("Tetris")));
		}
		records.save();
	}

	public String getInitials(String s) {
		String initials = JOptionPane.showInputDialog(null,
				"You have achieved a new record in " + s + "!\n"
						+ "Enter your initials:", "New High " + s,
				JOptionPane.QUESTION_MESSAGE);
		if (initials == null || initials.length() < 1) {
			initials = "DEFAULT";
		}
		return initials;
	}

	/**
	 * 
	 * @param b
	 * @return True if b hit a wall or another block
	 */
	public boolean hitTest(Block b) {
		for (int i = 0; i < Block.getSPACES_PER_BLOCK(); ++i) {
			int rownum = b.getOccupied()[i].getRownum();
			int colnum = b.getOccupied()[i].getColnum();
			if ((rownum < 1) || rownum > theField.getRows() || (colnum < 1)
					|| colnum > theField.getCols()) {
				return true;
			}
			if (theField.getSpaceAt(rownum, colnum).isOccupied()) {
				return true;
			}
		}
		return false;
	}

	public void dropBlock() {
		boolean playSound = false;
		while (moveBlock(0, BLOCK_STEP)) {
			playSound = true;
		}
		if (soundIsOn && playSound) {
			soundManager.playClip(soundManager.getMove());
		}
	}

	public void rotateBlock() {
		Block tmp = new Block(activeBlock);
		tmp.rotate();
		if (!hitTest(tmp)) {
			activeBlock = new Block(tmp);
		}
		if (soundIsOn) {
			soundManager.playClip(soundManager.getRotate());
		}
	}

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if (gameIsOver && key != NEWGAME_BUTTON && key != HELP_BUTTON
				&& key != RECORD_BUTTON && key != OPTIONS_BUTTON) {
			return;
		} else if (gameIsPaused && key != PAUSE_BUTTON && key != NEWGAME_BUTTON
				&& key != HELP_BUTTON && key != RECORD_BUTTON
				&& key != OPTIONS_BUTTON) {
			return;
		}
		// uses Java coordinate system
		if (key == UP_ARROW) {
			rotateBlock();
			repaint();
		} else if (key == DOWN_ARROW) {
			if (moveBlock(0, BLOCK_STEP) && soundIsOn) {
				soundManager.playClip(soundManager.getMove());
			}
			repaint();
		} else if (key == LEFT_ARROW) {
			if (moveBlock(-BLOCK_STEP, 0) && soundIsOn) {
				soundManager.playClip(soundManager.getMove());
			}
			repaint();
		} else if (key == RIGHT_ARROW) {
			if (moveBlock(BLOCK_STEP, 0) && soundIsOn) {
				soundManager.playClip(soundManager.getMove());
			}
			repaint();
		} else if (key == SHIFT_BUTTON && !alternateKeys) {
			dropBlock();
			repaint();
		} else if (key == SHIFT_BUTTON && alternateKeys) {
			rotateBlock();
			repaint();
		} else if (key == Z_BUTTON) {
			dropBlock();
			repaint();
		} else if (key == OPTIONS_BUTTON) {
			showOptions();
			repaint();
		} else if (key == PAUSE_BUTTON) {
			togglePause();
			repaint();
		} else if (key == NEWGAME_BUTTON) {
			timer.stop();
			init();
		} else if (key == HELP_BUTTON) {
			showHelp();
			repaint();
		} else if (key == RECORD_BUTTON) {
			showRecords();
			repaint();
		}
	}

	public void pause() {
		timer.stop();
		gameIsPaused = true;
		repaint();
	}

	public void unpause() {
		gameIsPaused = false;
		timer.start();
	}

	public void togglePause() {
		if (!gameIsPaused) {
			pause();
		} else {
			unpause();
		}
	}

	public void showHelp() {
		helpFrame.setContentPane(help);
		helpFrame.pack();
		helpFrame.setResizable(false);
		helpFrame.show();
		// repaint();
		// unpause();
	}

	public void showOptions() {
		optionsFrame.setContentPane(options);
		optionsFrame.pack();
		optionsFrame.setResizable(false);
		optionsFrame.show();
	}

	public void showRecords() {
		records.load();
		records.setTable();
		recordsFrame.setContentPane(new RecordPanel());
		recordsFrame.pack();
		recordsFrame.setResizable(false);
		recordsFrame.show();
	}

	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void windowClosing(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void windowActivated(WindowEvent arg0) {
		unpause();
	}

	public void windowDeactivated(WindowEvent arg0) {
		pause();
	}

	/**
	 * @return Returns the nCOLS.
	 */
	public static int getCOLUMNS() {
		return COLUMNS;
	}

	/**
	 * @return Returns the nROWS.
	 */
	public static int getROWS() {
		return ROWS;
	}

	/**
	 * @return Returns the timeStep.
	 */
	public int getTimeStep() {
		return timeStep;
	}

	/**
	 * @return Returns the blockCell.
	 */
	public Image getBlockCell() {
		return blockCell;
	}

	/**
	 * @param blockCell
	 *            The blockCell to set.
	 */
	public void setBlockCell(Image blockCell) {
		this.blockCell = blockCell;
	}

	/**
	 * @return Returns the musicFile.
	 */
	public String getMusicFile() {
		return musicFile;
	}

	/**
	 * @param musicFile
	 *            The musicFile to set.
	 */
	public void setMusicFile(String musicFile) {
		this.musicFile = musicFile;
	}

	/**
	 * @return Returns the musicIsOn.
	 */
	public boolean getMusicIsOn() {
		return musicIsOn;
	}

	/**
	 * @param musicIsOn
	 *            The musicIsOn to set.
	 */
	public void setMusicIsOn(boolean musicIsOn) {
		this.musicIsOn = musicIsOn;
	}

	/**
	 * @return Returns the soundIsOn.
	 */
	public boolean getSoundIsOn() {
		return soundIsOn;
	}

	/**
	 * @param soundIsOn
	 *            The soundIsOn to set.
	 */
	public void setSoundIsOn(boolean soundIsOn) {
		this.soundIsOn = soundIsOn;
	}

	public void toggleSoundIsOn() {
		if (soundIsOn) {
			soundIsOn = false;
		} else {
			soundIsOn = true;
		}
	}

	/**
	 * @return Returns the alternateKeys.
	 */
	public boolean getAlternateKeys() {
		return alternateKeys;
	}

	/**
	 * @param alternateKeys
	 *            The alternateKeys to set.
	 */
	public void setAlternateKeys(boolean alternateKeys) {
		this.alternateKeys = alternateKeys;
	}

	/**
	 * @return Returns the soundManager.
	 */
	public SoundManager getSoundManager() {
		return soundManager;
	}

	/**
	 * @param soundManager
	 *            The soundManager to set.
	 */
	public void setSoundManager(SoundManager soundManager) {
		this.soundManager = soundManager;
	}
}
