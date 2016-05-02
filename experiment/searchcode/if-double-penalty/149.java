//<APPLET CODE = "SwingApplet.class" WIDTH = 700 HEIGHT = 400 ></applet>

// load for early releases
//import com.sun.java.swing.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SwingApplet extends JApplet implements ActionListener,Runnable{
	static final int BW=300, BH=300, BX=10, BY=10, NUM_WALLS=20, NC=2, NK=4,
		SAMP_W = 100, SAMP_H = 100;
	static final int DEF_EPOCHS = 50000;
	static final long DELAY=500;
	static int MAXX=400, MAXY=400;
	
	CatAndMouseGame game;
	CatAndMouseWorld trainWorld, playWorld; // seperate world from playing world
	RLController rlc;
	RLearner rl;
		
	JTabbedPane tabbedPane;
	Container instructions, playPanel, trainPanel, worldPanel;
	
	// world setting components
	JTextField rows, cols, obst, cheeses, cats;
	boolean[][] selectedWalls;
	ButtonGroup worldSelGroup;
	boolean sampleWorld=true, designWorld=false;
	int posisiMouse;
	
	// instructions components
	JLabel instructLabel, usageLabel;
	final String INSTRUCT_MESSAGE = "<html><p>This applet demonstrates how reinforcement <p>learning can be used to train an agent to play <p>a simple game.  In this case the game is Cat and <p>Mouse- the mouse tries to get to the cheese <p>and back to it's hole, the cat tries to catch the mouse.",
		USAGE_MESSAGE = "<html><p>You can train the agent by selecting the Train tab.  At <p>any time you can select the Play tab to see how <p>well the agent is performing!  Of course, the more <p>training, the better the chance the mouse <p>has of surviving :)";

	// train panel components
	public static final String START="S", CONT_CHECK="C";
	final String SETTINGS_TEXT = "These settings adjust some of the internal workings of the reinforcement learning algorithm.",
		SETTINGS_TEXT2 = "Please see the web pages for more details on what the parameters do.";
	JTextField alpha, gamma, epsilon, epochs, penalty, reward;
	JButton startTraining, stopTraining;
	JRadioButton softmax, greedy, sarsa, qlearn;
	JProgressBar progress;
	JLabel learnEpochsDone;	
	
	// play panel components
	JButton startbutt, stopbutt, pausebutt;
	boardPanel bp;
	public int mousescore=0, catscore =0, episode=1;
	JLabel mousescorelabel, episodelabel;
	final String MS_TEXT = "Mouse Score:", CS_TEXT = "Cat Score:", EP_TEXT = "Episode : " ;
	JSlider speed, smoothSlider;
	Image catImg, mouseImg[], mos;
	Image backgroundImg;
	chartPanel graphPanel;
	JLabel winPerc;
			
	boardObject cat, mouse, cheese, back, hole, wall;
	
	//Upload File
	public String namafile1 = "testing.txt";
	public String namafile2 = "testing2.txt";
					
	public SwingApplet() {
		getRootPane().putClientProperty("defeatSystemEventQueueCheck",Boolean.TRUE);
		
	}
	
	public void PlayMusic(){
		 try {
	         // Open an audio input stream.
			 File soundFile = new File("raisa.wav");
	         AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
	         // Get a sound clip resource.
	         Clip clip = AudioSystem.getClip();
	         // Open audio clip and load samples from the audio input stream.
	         clip.open(audioIn);
	         clip.start();
	         clip.loop(clip.LOOP_CONTINUOUSLY);
	      } catch (UnsupportedAudioFileException e) {
	         e.printStackTrace();
	      } catch (IOException e) {
	         e.printStackTrace();
	      } catch (LineUnavailableException e) {
	         e.printStackTrace();
	      }
	}
	
	
	public void init() {
		PlayMusic();
		
		// load images
		catImg = getImage(getCodeBase(), "cat.gif");
		mos = getImage(getCodeBase(), "mouse.gif");
		backgroundImg = getImage(getCodeBase(), "raisa.jpg");
		mouseImg = new Image[8];
			for (int i = 0; i < 8; i++) mouseImg[i] = getImage(getCodeBase(), (i+1) + "a.gif");
		Image wallImg = getImage(getCodeBase(), "wall.gif");
		Image cheeseImg = getImage(getCodeBase(), "cheese.gif");
		Image floorImg = getImage(getCodeBase(), "floor.gif");
		
		// set up board objects
		cat = new boardObject(catImg);
		mouse = new mouseObject(mouseImg);
		cheese = new boardObject(cheeseImg);
		back = new boardObject(floorImg);
		hole = new boardObject(Color.orange);
		wall = new boardObject(wallImg);
		
		// setup content panes
		tabbedPane = new JTabbedPane();
		
		//instructions = makeInstructions();
		worldPanel = makeWorldPanel();
		playPanel = makePlayPanel();
		trainPanel = makeTrainPanel();
		
		tabbedPane.addTab("World", worldPanel);
		tabbedPane.addTab("Play", playPanel);
		//tabbedPane.addTab("Instructions", instructions);
		tabbedPane.addTab("Train", trainPanel);
		tabbedPane.setSelectedIndex(0);

		// disable panes until world created
		tabbedPane.setEnabledAt(1,false);
		tabbedPane.setEnabledAt(2,false);
		
		// set up controls
		//setContentPane(new JPanel());
		//getContentPane().add(tabbedPane);
		
		getContentPane().add(tabbedPane);
	}

	public void worldInit(int xdim, int ydim, int numwalls, int numcats, int numcheeses) { 
		playWorld = new CatAndMouseWorld(xdim, ydim, numwalls, numcats, numcheeses, namafile1);
//		trainWorld = playWorld;
		trainWorld = new CatAndMouseWorld(namafile1, namafile2);
		gameInit(xdim,ydim);
	}

	private void gameInit(int xdim, int ydim) {
		// disable this pane
		tabbedPane.setEnabledAt(0,false);
				
		bp.setDimensions(xdim, ydim);
		
		rlc = new RLController(this, trainWorld, DELAY);
		rl = rlc.learner;
		rlc.start();
		
		game = new CatAndMouseGame(this, DELAY, playWorld, rl.getPolicy());
		game.start();

		// set text fields on panels
		penalty.setText(Integer.toString(trainWorld.deathPenalty));
		reward.setText(Integer.toString(trainWorld.cheeseReward));
		alpha.setText(Double.toString(rl.getAlpha()));
		gamma.setText(Double.toString(rl.getGamma()));
		epsilon.setText(Double.toString(rl.getEpsilon()));

		// enable other panes
		tabbedPane.setEnabledAt(1,true);
		tabbedPane.setEnabledAt(2,true);
		
		// switch active pane
		tabbedPane.setSelectedIndex(1);

		// set first position on board
		updateBoard();
	}
	
	// this method is triggered by SwingUtilities.invokeLater in other threads
	public void run() { updateBoard(); }
	
	/************ general functions ****************/
	public void updateBoard() {
		// update score panels
		mousescorelabel.setText(MS_TEXT+" "+Integer.toString(mousescore));
		episodelabel.setText(EP_TEXT+ " "+Integer.toString(episode));
		//catscorelabel.setText(CS_TEXT+" "+Integer.toString(catscore));
		updateScore();
		if (game.newInfo) {
			updateScore();
			game.newInfo = false;
		}
		
		// update progress info
		progress.setValue(rlc.epochsdone);
		learnEpochsDone.setText(Integer.toString(rlc.totaldone));
		if (rlc.newInfo) endTraining();
		
		// update game board
		bp.clearBoard();

		// draw walls
		boolean[][] w = game.getWalls(); 
		for (int i=0; i<w.length; i++) {
			for (int j=0; j<w[0].length; j++) {
				if (w[i][j]) bp.setSquare(wall, i, j);
			}
		}

		// draw cheeses
		boolean[][] ch = game.getCheeses(); 
		for (int i=0; i<ch.length; i++) {
			for (int j=0; j<ch[0].length; j++) {
				if (ch[i][j]) bp.setSquare(cheese, i, j);
			}
		}

		// draw cats
		boolean[][] c = game.getCats(); 
		for (int i=0; i<c.length; i++) {
			for (int j=0; j<c[0].length; j++) {
				if (c[i][j]) bp.setSquare(cat, i, j);
			}
		}

		// draw objects (cat over mouse over cheese)
		mouse.setPosisiMouse(game.getPosisiMouse());
		bp.setSquare(mouse, game.getMouse());
					
		// display text representation
		//System.out.println(bp);
		bp.repaint();
	}

	void doTraining() {
		// begin training
		int episodes = Integer.parseInt(epochs.getText());
		double aval = Double.parseDouble(alpha.getText());
		double gval = Double.parseDouble(gamma.getText());
		double eval = Double.parseDouble(epsilon.getText());
		int cval = Integer.parseInt(reward.getText());
		int dval = Integer.parseInt(penalty.getText());
				
		rl.setAlpha(aval);
		rl.setGamma(gval);
		rl.setEpsilon(eval);
				
		// disable controls
		startTraining.setEnabled(false);
		epochs.setEnabled(false);
		reward.setEnabled(false);
		penalty.setEnabled(false);
		alpha.setEnabled(false);
		gamma.setEnabled(false);
		epsilon.setEnabled(false);
		softmax.setEnabled(false);
		greedy.setEnabled(false);
		sarsa.setEnabled(false);
		qlearn.setEnabled(false);
				
		// fix progress bar
		progress.setMinimum(0);
		progress.setMaximum(episodes);
		progress.setValue(0);
		
		//ngetes progress bar, ngga penting
		/*progress.setMaximum(10);
		for(int pro=0;pro<=10;pro++){
			progress.setValue(pro);
			try {
			    Thread.sleep(1000);
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
		}*/
		
				
		// enable stop button
		stopTraining.setEnabled(true);
				
		// start training
		trainWorld.cheeseReward = cval;
		trainWorld.deathPenalty = dval;
		rlc.setEpisodes(episodes);
	}

	void endTraining() {
		// stop training
		rlc.stopLearner();
		
		// enable buttons
		startTraining.setEnabled(true);
		epochs.setEnabled(true);
		reward.setEnabled(true);
		penalty.setEnabled(true);
		alpha.setEnabled(true);
		gamma.setEnabled(true);
		epsilon.setEnabled(true);
		softmax.setEnabled(true);
		greedy.setEnabled(true);
		sarsa.setEnabled(true);
		qlearn.setEnabled(true);

		// disable stop button
		stopTraining.setEnabled(false);
	}

	void updateScore() {
		double newScore = mousescore;
		//System.out.println("haha " + mousescore);
		//winPerc.setText(Double.toString(newScore)+"%");
		graphPanel.updateScores();
		graphPanel.repaint();
		game.gameActive = true;		
	}	
	/************ general functions ****************/

	/********** Methods to construct panels *************/
	Container makeWorldPanel() {
		JPanel worldPane = new JPanel();
		worldPane.setLayout(new BorderLayout());

		//worldSelGroup = new ButtonGroup();
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		
		worldPane.add(chooseWorld(), BorderLayout.CENTER);
		//worldPane.add(customWorld(), BorderLayout.EAST);
		JButton upbutt = new JButton("Click here to Upload File Type 1");
		JButton loadbutt = new JButton("Click here to Upload File Type 2");
		JButton startbutt = new JButton("Click here to start!");
		startbutt.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				// selected world type, choose action
					worldInit(Integer.parseInt(cols.getText()),
						Integer.parseInt(rows.getText()),
						Integer.parseInt(obst.getText()),
						Integer.parseInt(cats.getText()),
						Integer.parseInt(cheeses.getText()));
			}
		});
		upbutt.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				// Open File Chooser and Upload the txt file
					JFileChooser fc = new JFileChooser();
					int returnVal = fc.showOpenDialog(SwingApplet.this);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
			            //Opening the File Chosen
						File file = fc.getSelectedFile();
			            System.out.println("Opening: " + file.getName() + ".");
			            namafile1= file.getName();
			        } else {
			        	System.out.println("Open command cancelled by user.");
			        }
			}
		});
		loadbutt.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				// Open File Chooser and Upload the txt file
					JFileChooser fc = new JFileChooser();
					int returnVal = fc.showOpenDialog(SwingApplet.this);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
			            //Opening the File Chosen
						File file = fc.getSelectedFile();
			            System.out.println("Opening: " + file.getName() + ".");
			            namafile2 = file.getName();
			        } else {
			        	System.out.println("Open command cancelled by user.");
			        }
			}
		});
		buttonPanel.add(upbutt,BorderLayout.NORTH);
		buttonPanel.add(loadbutt,BorderLayout.SOUTH);
		worldPane.add(startbutt, BorderLayout.SOUTH);
		worldPane.add(buttonPanel,BorderLayout.EAST);
		return worldPane;
	}
	
	Container customWorld() {
		JPanel pane = new JPanel();
		pane.setLayout(new BorderLayout());
				
		// add controls to set dimensions
		JPanel labelpane = new JPanel();
		labelpane.setLayout(new GridLayout(0,2));

		//worldSelGroup.add(custom);
		
		//JPanel controls = new JPanel();
		//controls.setLayout(new GridLayout(0,1));
		rows = new JTextField(Integer.toString(BY), 20);
		cols = new JTextField(Integer.toString(BX), 20);
		obst = new JTextField(Integer.toString(NUM_WALLS), 20);
		cheeses = new JTextField(Integer.toString(NC), 20);
		cats = new JTextField(Integer.toString(NK), 20);
		
		labelpane.add(new JLabel("Rows:",JLabel.RIGHT));
		labelpane.add(rows);
		labelpane.add(new JLabel("Columns:",JLabel.RIGHT));
		labelpane.add(cols);
		labelpane.add(new JLabel("Obstacles:",JLabel.RIGHT));
		labelpane.add(obst);
		labelpane.add(new JLabel("Cheese:",JLabel.RIGHT));
		labelpane.add(cheeses);
		labelpane.add(new JLabel("Cats:",JLabel.RIGHT));
		labelpane.add(cats);
		
		//labelpane.setBorder(BorderFactory.createTitledBorder("Custom World"));
		//labelpane.add(random);
		//labelpane.add(custom);

		pane.add(labelpane, BorderLayout.CENTER);
		//pane.add(controls, BorderLayout.EAST);
		//pane.add(labelpane);
		//pane.add(controls);
		
		return pane;	
	}
	
	Container chooseWorld() {
		JPanel pane = new JPanel();
		pane.setLayout(new GridLayout(0,3));

		// add random world option
		pane.add(customWorld());
		pane.setBorder(BorderFactory.createTitledBorder("Color Your Worlds"));
		
		return pane;
	}
	
	Container makeInstructions() {
		JPanel pane = new JPanel();
		pane.setLayout(new GridLayout(2,1));
		
		instructLabel = new JLabel(INSTRUCT_MESSAGE);
		usageLabel = new JLabel(USAGE_MESSAGE);
		
		pane.add(instructLabel);
		pane.add(usageLabel);
		return pane;
	}
	
	// makes the board panel and the controls to start and stop the game etc
	Container makePlayPanel() {
		JPanel pane = new JPanel();		
		//pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));

		// make drawable area
		pane.add(makeBoardPanel());
		
		// add buttons
		pane.add(makeButtonPane());
		

		//pane.setBackground(new Color(255,255,204));
		pane.setBorder(BorderFactory.createMatteBorder(1,1,2,2,Color.black));
				
		return pane;
	}

	Container makeTrainPanel() {
		JPanel trainPane = new JPanel();
		//trainPane.setLayout(new BoxLayout(trainPane, BoxLayout.X_AXIS));
		
		trainPane.add(makeSettingPanel());
		trainPane.add(makeParamPanel());

		return trainPane;
	}
	
	// a,g,e parameters for reinforcement learner
	Container makeParamPanel() {
		JPanel parampane = new JPanel();
		parampane.setLayout(new BorderLayout(1,2));
		
		
		JPanel labelpane = new JPanel();
		labelpane.setLayout(new GridLayout(0,1));
		labelpane.add(new JLabel("Death Penalty:", JLabel.RIGHT));
		labelpane.add(new JLabel("Cheese Reward:", JLabel.RIGHT));
		labelpane.add(new JLabel("Alpha:", JLabel.RIGHT));
		labelpane.add(new JLabel("Gamma:", JLabel.RIGHT));
		labelpane.add(new JLabel("Epsilon:", JLabel.RIGHT));
		labelpane.add(new JLabel("Action Selection Method:", JLabel.RIGHT));
		labelpane.add(new JLabel("Learning Method:", JLabel.RIGHT));
		labelpane.add(new JLabel("Epochs to train for:",JLabel.RIGHT));
		labelpane.add(new JLabel("Progress:",JLabel.RIGHT));
		labelpane.add(new JLabel("Epochs done:",JLabel.RIGHT));

		JPanel controlspane = new JPanel();
		controlspane.setLayout(new GridLayout(0,1));
		penalty = new JTextField(20);
		reward = new JTextField(20);
		alpha = new JTextField(20);
		gamma = new JTextField(20);
		epsilon = new JTextField(20);
		controlspane.add(penalty);
		controlspane.add(reward);
		controlspane.add(alpha);
		controlspane.add(gamma);
		controlspane.add(epsilon);

		JPanel actionButtons = new JPanel();
		actionButtons.setLayout(new GridLayout(1,0));
		softmax = new JRadioButton("Softmax");
		greedy = new JRadioButton("Greedy",true);
		ButtonGroup actionButts = new ButtonGroup();
		actionButts.add(softmax);
		actionButts.add(greedy);
		actionButtons.add(softmax);
		actionButtons.add(greedy);
		
		JPanel learnButtons = new JPanel();
		learnButtons.setLayout(new GridLayout(1,0));
		sarsa = new JRadioButton("SARSA");
		qlearn = new JRadioButton("Q-Learning",true);
		ButtonGroup learnButts = new ButtonGroup();
		learnButts.add(sarsa);
		learnButts.add(qlearn);
		learnButtons.add(sarsa);
		learnButtons.add(qlearn);
		epochs = new JTextField(Integer.toString(DEF_EPOCHS));
		progress = new JProgressBar();
		learnEpochsDone = new JLabel("0",JLabel.LEFT);
		 
		controlspane.add(actionButtons);
		controlspane.add(learnButtons);
		controlspane.add(epochs);
		controlspane.add(progress);
		controlspane.add(learnEpochsDone);

		parampane.add(labelpane, BorderLayout.CENTER);
		parampane.add(controlspane, BorderLayout.EAST);
		
		parampane.setBorder(BorderFactory.createTitledBorder("Parameters"));
		
		
		return parampane;
	}
	
	// number of epochs, other settings?, instructions?
	Container makeSettingPanel() {
		JPanel setPane = new JPanel();
		setPane.setLayout(new GridLayout(0,1));
		setPane.add(new JLabel(SETTINGS_TEXT));
		setPane.add(new JLabel(SETTINGS_TEXT2));

		JPanel controls = new JPanel();
		//controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
		
		startTraining = new JButton("Begin Training");
		startTraining.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doTraining();
			}
		});
		stopTraining = new JButton("Stop");
		stopTraining.setEnabled(false);
		stopTraining.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				endTraining();
			}
		});
		JButton clearPolicy = new JButton("Undo Training");
		clearPolicy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				game.setPolicy(rlc.resetLearner());
			}
		});
		controls.add(startTraining);
		controls.add(stopTraining);
		controls.add(clearPolicy);
		
		setPane.add(controls);
		return setPane;
	}

	Container makeBoardPanel() {
		JPanel boardPane = new JPanel();
		boardPane.setLayout(new BoxLayout(boardPane, BoxLayout.Y_AXIS));
		
		bp = new boardPanel(back, BW, BH);
		
		boardPane.add(bp);
		
		// speed control
		speed = new JSlider(0,10,5);
		speed.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				double ratio = 1 - ((double)speed.getValue())/10;
				game.delay = (long)(ratio*DELAY*2);
			}
		});
		speed.setMajorTickSpacing(2);
        speed.setPaintTicks(true);

        //Create the label table.
        Hashtable labelTable = new Hashtable();
        labelTable.put(new Integer( 0 ), new JLabel("Slow") );
        labelTable.put(new Integer( 30 ), new JLabel("Fast") );
        speed.setLabelTable(labelTable);
        speed.setPaintLabels(true);
		boardPane.add(speed);

        //boardPane.setBorder(BorderFactory.createTitledBorder("Game"));

		return boardPane;
	}
	
	Container makeButtonPane() {
		JPanel buttpane = new JPanel();
		buttpane.setLayout(new BoxLayout(buttpane, BoxLayout.Y_AXIS));
		
		// graph of scores
		buttpane.add(chartPane());
		
		// scores
		buttpane.add(scorePanel());
		
		// buttons
		buttpane.add(playControlPanel());
		
		return buttpane;
	}

	Container playControlPanel() {
		JPanel playPanel = new JPanel();
		playPanel.setLayout(new GridLayout(0,2));
		
		startbutt = new JButton("Start");
		startbutt.setActionCommand(START);
		startbutt.addActionListener(this);
		
		JCheckBox continuous = new JCheckBox("Continuous", true);
		continuous.setActionCommand(CONT_CHECK);
		continuous.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.DESELECTED)
					game.single=true;
				else game.single = false;
			}
		});

		// add controls to select greedy or rl mouse
		//JPanel learnButtons = new JPanel();
		//learnButtons.setLayout(new GridLayout(1,0));
		JRadioButton greedy = new JRadioButton("Greedy Mouse");
		JRadioButton smart = new JRadioButton("Smart Mouse",true);
		greedy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// set game to use greedy mouse
				game.mousetype = game.GREEDY;
			}
		});
		smart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// set game to use smart mouse
				game.mousetype = game.SMART;
			}
		});
		ButtonGroup mouseButts = new ButtonGroup();
		mouseButts.add(greedy);
		mouseButts.add(smart);

		// add to grid (l-r t-b)
		playPanel.add(startbutt);
		playPanel.add(smart);
		playPanel.add(continuous);
		playPanel.add(greedy);

		playPanel.setBorder(BorderFactory.createTitledBorder("Game Controls"));
		return playPanel;
	}
	
	Container scorePanel() {
		JPanel scorePane = new JPanel();
		//scorePane.setLayout(new BoxLayout(scorePane, BoxLayout.Y_AXIS));
		scorePane.setLayout(new GridLayout(0,2));
		
		// score labels
		ImageIcon cat = new ImageIcon(catImg);
		ImageIcon mouse = new ImageIcon(mos);
		mousescorelabel = new JLabel(MS_TEXT, mouse, JLabel.RIGHT);
		episodelabel = new JLabel(EP_TEXT, JLabel.RIGHT);
		//catscorelabel = new JLabel(CS_TEXT, cat, JLabel.RIGHT);

		// reset scores
		//JPanel hbox = new JPanel();
		//hbox.setLayout(new BoxLayout(hbox, BoxLayout.X_AXIS));
		JButton reset = new JButton("Reset Scores");
		reset.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				mousescore = 0;
				episode = 1;
				//catscore = 0;
				updateBoard();
			}			
		});
		winPerc = new JLabel("", JLabel.RIGHT); // winning percentage label
		//hbox.add(reset);
		//hbox.add(winPerc);
		
		//scorePane.add(hbox);

		scorePane.add(mousescorelabel);
		scorePane.add(episodelabel);
		scorePane.add(winPerc);
		//scorePane.add(catscorelabel);
		scorePane.add(reset);
		
		scorePane.setBorder(BorderFactory.createTitledBorder("Scores"));
		return scorePane;
	}
	
	Container chartPane() {
		JPanel ch=new JPanel();
		ch.setLayout(new BorderLayout());
		
		graphPanel = new chartPanel(this);
		graphPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		
		// smoothing control
		smoothSlider = new JSlider(JSlider.HORIZONTAL, 0,99,50);
		smoothSlider.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				double ratio = ((double)smoothSlider.getValue())/100;
				graphPanel.setSmoothing(ratio);
				graphPanel.repaint();
			}
		});
		smoothSlider.setMajorTickSpacing(20);
        smoothSlider.setPaintTicks(true);

        //Create the label table.
        Hashtable labelTable = new Hashtable();
        labelTable.put(new Integer( 0 ), new JLabel("Coarse") );
        labelTable.put(new Integer( 99 ), new JLabel("Smooth") );
        smoothSlider.setLabelTable(labelTable);
        smoothSlider.setPaintLabels(true);

		ch.add(graphPanel, BorderLayout.CENTER);
		ch.add(smoothSlider, BorderLayout.SOUTH);
		
		//ch.add(scorePanel(), BorderLayout.SOUTH);
		
		ch.setBorder(BorderFactory.createTitledBorder("Performance"));
		
		return ch;
	}
	
	/********** Methods to construct panels *************/

	/********** Action handling methods ****************/
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(START)) {
			game.gameOn = true;
		} else if (e.getActionCommand().equals("Draw")) {
			System.out.println("draw test");
			updateBoard();
		}
	}
	/********** Action handling methods ****************/
}

class chartPanel extends JPanel {
	Vector history;
	SwingApplet a;
	final int POINTW=1, POINTH=1, PREFX = 200, PREFY = 100;
	double smoothing = 0.5;
	
	final Color bg=Color.white, fg=Color.blue;
	int MAXSIZE;
	int lastm=0, lastc=0;
	
	
	public chartPanel(SwingApplet a) {
		this.a = a;
		history = new Vector();
	}
	
	public void updateScores() {
		int m = a.mousescore, c = a.episode;
		int dm = m-lastm, dc = c-lastc;
		lastm=m; lastc=c;
		double score, score2;
		if ((m+c)==0) {
			score = 0;
			score2 = 0;
		}
		else {
			score = ((double)dm);
			score2 = ((double)dc);
		}
		addScore(score);
		addScore(score2);
	}
	
	public void paintComponent(Graphics g) {
		MAXSIZE=getWidth()*2;
		
		// draw panel
		g.setColor(bg);
		g.fillRect(0,0,getWidth(),getHeight());
		g.setColor(fg);
		
		double previous=0, thisval, newval;
		for (int x=0; x<history.size(); x++) {
			// draw this point
			
			// smooth with previous values
			thisval = 1 - ((Double)history.elementAt(x)).doubleValue();
			//if (x != startpoint)
			newval = smoothing * previous + (1 - smoothing) * thisval;
			if ((newval >= 0) && (newval <= 1)) previous = newval;
			else System.err.println("Invalid new value: "+newval);
			int yval = (int) (newval * getHeight());
			int xval = x-(history.size() - getWidth());
			//System.out.println("index="+x+" thisval="+thisval+"newval="+newval+" xval="+xval+" yval="+yval+" previous="+previous);
			g.drawOval(xval,yval,POINTW,POINTH);
		}
		
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(PREFX, PREFY);
	}
	
	public void setSmoothing(double s) { smoothing = s; }
	
	void addScore(double s) {
		if (!((s >= 0) && (s<=1))) {
			System.err.println("Graph: rejecting value"+s);
			return;
		}
		
		history.addElement(new Double(s));

		// prune if list too big
		if (history.size() >= MAXSIZE) history.remove(0);

		//System.out.println("Size:"+history.size()+" maxsize:"+MAXSIZE);

		/*
			System.out.println("History being pruned."+Thread.currentThread().getName());
			Vector nVec = new Vector();
			for (int i=(MAXSIZE/3); i<history.size(); i++) {
				nVec.addElement(history.elementAt(i));
			}
			history = nVec;
			System.out.println("Pruning Finished.");
		}*/
	}
}


